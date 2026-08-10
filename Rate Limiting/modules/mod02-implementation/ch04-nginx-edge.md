# Chapter 4 — Edge Rate Limiting with NGINX

NGINX can reject excess traffic before it consumes application connections. Its request limiter uses a leaky-bucket-style mechanism with optional bursting.

## Basic per-IP limit

Put the shared-memory zone in the `http` context:

```nginx
http {
    limit_req_zone $binary_remote_addr
                   zone=per_ip:10m
                   rate=10r/s;

    server {
        listen 80;

        location /api/ {
            limit_req zone=per_ip burst=20 nodelay;
            limit_req_status 429;
            add_header Retry-After 1 always;

            proxy_pass http://application;
        }
    }
}
```

- `rate=10r/s` sets the sustained rate.
- `burst=20` tolerates a backlog above that rate.
- `nodelay` admits allowed burst requests immediately; without it, excess-within-burst requests are delayed.
- `10m` is shared state size, not a traffic allowance.

## Tenant-aware key

Never trust a tenant header directly from the internet. Have a trusted authentication layer set or overwrite it, then:

```nginx
map $http_x_verified_tenant $limit_key {
    default $http_x_verified_tenant;
    ""      $binary_remote_addr;
}

limit_req_zone $limit_key zone=per_subject:20m rate=50r/s;
```

This falls back to IP for unauthenticated traffic. Ensure external callers cannot bypass the authentication hop or inject the trusted header.

## Different endpoint policies

```nginx
limit_req_zone $limit_key zone=login:10m rate=5r/m;
limit_req_zone $limit_key zone=search:20m rate=20r/s;

server {
    location = /login {
        limit_req zone=login burst=3 nodelay;
        limit_req_status 429;
        proxy_pass http://application;
    }

    location /search {
        limit_req zone=search burst=40 nodelay;
        limit_conn per_ip_connections 10;
        proxy_pass http://application;
    }
}
```

Request-rate and connection limits can be combined, but define the `limit_conn_zone` separately in `http`.

## Placement tradeoffs

NGINX is strong at:

- cheap early rejection;
- per-IP and simple trusted-header policies;
- smoothing traffic toward upstreams;
- protecting connections.

Application or dedicated limiters are stronger at:

- plan-specific dynamic limits;
- cost known only after parsing/authentication;
- hierarchical tenant/user/resource policies;
- globally consistent usage across regions.

## Proxy/IP warning

If NGINX sits behind a load balancer and `$remote_addr` is always the load balancer, every user shares one bucket. Configure the real-IP module only for known proxy ranges; trusting forwarded IP headers from arbitrary clients enables spoofing.

## Lab

Configure `rate=2r/s burst=4` once with `nodelay` and once without it. Send six simultaneous requests and compare latency and rejection behavior.

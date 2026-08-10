# Chapter 5 — Keys, Policies, Headers, and Client Behavior

## Choose a key hierarchy

One key is rarely enough:

```text
global
└── tenant
    ├── user
    └── route group
```

Example checks for a write request:

1. Global emergency capacity.
2. Tenant plan allowance.
3. User anti-abuse allowance.
4. Expensive-write route allowance.

The request passes only if every required policy permits it. Decide whether checking consumes all buckets atomically; sequential consumption can charge an earlier bucket even if a later one rejects.

## Policy model

```yaml
policy_id: pro-v3
subject: tenant
rules:
  - resource: api.read
    algorithm: token_bucket
    refill_per_second: 100
    burst: 300
    cost: 1
  - resource: api.export
    algorithm: token_bucket
    refill_per_minute: 5
    burst: 2
    cost: 1
```

Version policies. Persist the policy ID used in decision logs so later investigations can reconstruct behavior.

## HTTP response contract

A useful rejection:

```http
HTTP/1.1 429 Too Many Requests
Content-Type: application/problem+json
Retry-After: 7
RateLimit-Limit: 100
RateLimit-Remaining: 0

{
  "type": "https://api.example.com/problems/rate-limit",
  "title": "Rate limit exceeded",
  "status": 429,
  "policy": "tenant-search",
  "retry_after_seconds": 7,
  "request_id": "req_123"
}
```

Header standards evolve; verify the current standard before promising exact semantics publicly. `Retry-After` may be delta seconds or an HTTP date. Delta seconds avoid client clock disagreement.

Do not expose sensitive internal capacity or other tenants’ usage.

## Client retry

Clients should honor server guidance and add jitter:

```js
function fullJitter(attempt, retryAfterMs = 0) {
  const cap = Math.min(30_000, 500 * 2 ** attempt);
  return Math.max(retryAfterMs, Math.random() * cap);
}
```

Retry only idempotent operations automatically, or use idempotency keys. A retry must not accidentally duplicate a payment or job.

## Weighted requests

Flat request counts fail when costs differ:

```text
GET /items/123       cost 1
POST /search         cost 2 + requested_page_size / 25
POST /bulk-export    cost 100
```

Estimate cost before expensive work. If actual cost is known later, reserve an estimate and reconcile the difference. Set a hard maximum so one request cannot create unbounded work.

## Fairness choices

| Key | Benefit | Failure mode |
|---|---|---|
| IP | Available early | NAT unfairness, rotation |
| User | Human-level fairness | One user may have many integrations |
| API key | Integration control | Key sharing |
| Tenant | Plan enforcement | Noisy user hurts tenant peers |
| Route | Protects resource class | No caller fairness |
| Composite | Precise | More state and policy complexity |

## Exercise

Design a composite policy for a multi-tenant search API. Include anonymous IP, authenticated tenant, per-user, global, and expensive-query cost rules. State which rejection wins when multiple rules fail.

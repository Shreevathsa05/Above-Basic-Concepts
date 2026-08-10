# Chapter 3 — Application Limiters in Spring Boot and Node.js

This chapter demonstrates local token buckets. Local state is useful for learning and for single-instance services. It does **not** enforce one global limit across replicas.

## Spring Boot with Bucket4j

Dependency coordinates change over time, so choose the current Bucket4j artifact compatible with your Java and Spring versions. The core pattern is:

```java
@Component
public class ApiRateLimitFilter extends OncePerRequestFilter {
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket newBucket() {
        Bandwidth policy = Bandwidth.classic(
            100,
            Refill.greedy(100, Duration.ofMinutes(1))
        );
        return Bucket.builder().addLimit(policy).build();
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain chain
    ) throws ServletException, IOException {
        // In production, derive this from authenticated principal/tenant.
        String key = request.getRemoteAddr();
        Bucket bucket = buckets.computeIfAbsent(key, ignored -> newBucket());
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        response.setHeader("RateLimit-Limit", "100");
        response.setHeader("RateLimit-Remaining",
            String.valueOf(probe.getRemainingTokens()));

        if (probe.isConsumed()) {
            chain.doFilter(request, response);
            return;
        }

        long retrySeconds = Math.max(
            1, TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill())
        );
        response.setStatus(429);
        response.setHeader("Retry-After", String.valueOf(retrySeconds));
        response.setContentType("application/json");
        response.getWriter().write(
            "{\"error\":\"rate_limit_exceeded\",\"retry_after\":" +
            retrySeconds + "}"
        );
    }
}
```

Production improvements:

- Bound or expire the `buckets` map; otherwise arbitrary keys cause a memory leak.
- Key on an authenticated tenant or API key before falling back to IP.
- Exclude health checks and internal trusted traffic deliberately, not accidentally.
- Resolve policy once and cache it, but support controlled configuration updates.

## Node.js/Express token bucket

```js
import express from "express";

const app = express();
const buckets = new Map();
const RATE_PER_MS = 10 / 1000; // 10 tokens/second
const CAPACITY = 30;

function consume(key, cost = 1, now = Date.now()) {
  const old = buckets.get(key) ?? { tokens: CAPACITY, updatedAt: now };
  const elapsed = Math.max(0, now - old.updatedAt);
  const tokens = Math.min(CAPACITY, old.tokens + elapsed * RATE_PER_MS);

  if (tokens < cost) {
    buckets.set(key, { tokens, updatedAt: now });
    return {
      allowed: false,
      remaining: Math.floor(tokens),
      retryMs: Math.ceil((cost - tokens) / RATE_PER_MS),
    };
  }

  const remaining = tokens - cost;
  buckets.set(key, { tokens: remaining, updatedAt: now });
  return { allowed: true, remaining: Math.floor(remaining), retryMs: 0 };
}

function rateLimit(req, res, next) {
  // Prefer req.auth.tenantId set by verified authentication middleware.
  const key = req.auth?.tenantId ?? req.ip;
  const result = consume(key);

  res.set("RateLimit-Limit", String(CAPACITY));
  res.set("RateLimit-Remaining", String(result.remaining));

  if (result.allowed) return next();

  res.set("Retry-After", String(Math.max(1, Math.ceil(result.retryMs / 1000))));
  return res.status(429).json({ error: "rate_limit_exceeded" });
}

app.use(rateLimit);
app.get("/api/search", (_req, res) => res.json({ ok: true }));
app.listen(3000);
```

Add idle-key eviction:

```js
const IDLE_MS = 10 * 60 * 1000;
setInterval(() => {
  const cutoff = Date.now() - IDLE_MS;
  for (const [key, bucket] of buckets) {
    if (bucket.updatedAt < cutoff) buckets.delete(key);
  }
}, 60_000).unref();
```

## Concurrency limiter companion

Rate and concurrency limits solve different problems. A small Node.js semaphore can cap in-flight expensive work:

```js
let inFlight = 0;
const MAX_IN_FLIGHT = 50;

function concurrencyLimit(req, res, next) {
  if (inFlight >= MAX_IN_FLIGHT) {
    return res.status(503).set("Retry-After", "1")
      .json({ error: "service_busy" });
  }
  inFlight++;
  let released = false;
  const release = () => {
    if (!released) { released = true; inFlight--; }
  };
  res.on("finish", release);
  res.on("close", release);
  next();
}
```

## Lab

Run two application replicas. Send alternating requests to each. Observe that each local bucket admits its own burst, making the effective aggregate capacity roughly twice the configured value. Chapter 6 fixes that.

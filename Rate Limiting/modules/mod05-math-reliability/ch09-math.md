# Chapter 9 — Why the Math Behind Rate Limits Feels Hard

The arithmetic is usually easy. The hard part is that traffic, service time, distributed clocks, retries, and human notions of fairness interact.

## 1. Units and burst envelopes

For token bucket refill `r`, capacity `B`, and interval `t`:

```text
accepted work <= B + r × t
```

If a dependency can safely absorb at most `S` requests during any interval `t`, choose parameters satisfying:

```text
B + r × t <= S
```

This must hold for the dangerous interval, not only one minute averages.

Example: a database tolerates 200 immediate queries and 50 queries/s afterward. A reasonable starting envelope is `B=200, r=50/s`, then reduce it for headroom and other traffic.

## 2. Rate becomes concurrency

Little’s Law:

```text
average concurrency = arrival rate × average time in system
L = λW
```

At 100 requests/s and 200 ms average duration:

```text
L = 100 × 0.2 = 20 in-flight requests
```

At the same rate with a 5-second slowdown, concurrency becomes 500. Therefore a rate limiter derived only from normal latency fails during degradation. Pair it with concurrency limits and backpressure.

## 3. Averages hide tails

Capacity planning using average latency ignores p95/p99 and correlated bursts. If all requests slow together, independence assumptions fail. Load tests should model burst shape, request mix, and downstream slowdown—not just uniform traffic.

## 4. Retry feedback

If rejected clients retry simultaneously, offered load grows:

```text
new traffic + retries = offered traffic
```

Suppose 1,000 requests arrive, 500 fail, and every failed request retries each second. The next second includes ordinary new work plus 500 retries. Exponential backoff, jitter, bounded attempts, and server retry guidance break synchronization.

## 5. False precision

A “100 per minute” fixed window may allow 200 near a boundary. A distributed local limiter across 10 instances may allow 10 times the intended rate. A leased system may overshoot by outstanding leases. Write these error bounds alongside the limit.

## 6. Probabilistic arrivals

Real arrival counts vary even at a steady average. A simplified Poisson model has mean and variance `λt`; standard deviation is `sqrt(λt)`. It can provide intuition, but bots, batch jobs, launches, and retries are often burstier and correlated. Measurements beat a convenient distribution.

## Why people say the math is hard

They usually mean one of five things:

1. Translating business fairness into measurable units.
2. Bounding burst and distributed overshoot.
3. Connecting request rate to variable service time and concurrency.
4. Predicting feedback from retries and queues.
5. Allocating capacity among dimensions without wasting it.

The solution is not advanced equations first. Begin with units, invariants, traffic envelopes, and explicit acceptable error.

## Worked sizing exercise

Given:

- dependency capacity: 1,000 operations/s;
- keep 30% safety headroom;
- ordinary call cost: 1 operation;
- premium call cost: 5 operations;
- dangerous immediate burst: 300 operations.

Usable sustained budget:

```text
r = 1000 × (1 - 0.30) = 700 cost units/s
B <= 300 cost units
```

If traffic is 80% ordinary and 20% premium, mean cost/request is:

```text
0.8 × 1 + 0.2 × 5 = 1.8 units/request
```

An estimated average request rate is `700 / 1.8 ≈ 389 requests/s`, but enforcement should charge actual request class cost, not rely on that average.

## Exercise

For `r=40/s`, `B=100`, four regions, and independent full buckets, calculate worst initial burst and 10-second allowance. Then allocate a single global envelope among regions and discuss stranded capacity.

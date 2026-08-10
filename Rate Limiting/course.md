# Rate Limiting Mastery

A practical, architecture-first course for learning rate limiting from first principles to large-scale and AI workloads.

## Who this is for

- Backend and platform engineers who know basic HTTP.
- Architects choosing between gateway, application, and distributed enforcement.
- Engineers operating Spring Boot, Node.js, NGINX, Redis, or AI APIs.

## Outcomes

By the end, you will be able to:

1. Explain what a rate limiter protects and what it cannot protect.
2. Implement fixed window, sliding window, token bucket, and leaky bucket limiters.
3. Choose keys, limits, response headers, and failure behavior.
4. Design local and distributed limiters and reason about correctness at scale.
5. Model bursts, memory, clock error, concurrency, and queueing with approachable math.
6. Build hierarchical, adaptive, and cost-aware limits.
7. Design limits for LLM tokens, GPUs, tenants, and streaming requests.

## How to use this course

Read chapters in order on the first pass. Every chapter combines theory and examples so the code appears beside the decision it illustrates. Then use the decision guides and labs as references.

Examples are intentionally small. Production code must also include authentication, observability, configuration validation, safe defaults, and load tests.

## Curriculum

| Level | Module | Chapter | Main deliverable |
|---|---|---|---|
| Beginner | 1. Foundations | [1. What rate limiting really does](modules/mod01-foundations/ch01-what-and-why.md) | Threat and requirement model |
| Beginner | 1. Foundations | [2. Algorithms and burst behavior](modules/mod01-foundations/ch02-algorithms.md) | Algorithm comparison and simulations |
| Beginner | 2. First implementations | [3. Application limiters](modules/mod02-implementation/ch03-application-limiters.md) | Spring Boot and Node.js examples |
| Beginner | 2. First implementations | [4. Edge limiting with NGINX](modules/mod02-implementation/ch04-nginx-edge.md) | NGINX configuration |
| Intermediate | 3. Production API design | [5. Keys, policies, headers, and clients](modules/mod03-production/ch05-api-contract.md) | Policy and HTTP contract |
| Intermediate | 3. Production API design | [6. Distributed rate limiting](modules/mod03-production/ch06-distributed.md) | Redis design and atomic script |
| Intermediate | 4. Architecture | [7. Where to enforce and how to compose](modules/mod04-architecture/ch07-placement-and-layers.md) | Layered architecture |
| Advanced | 4. Architecture | [8. Scale, consistency, and failure choices](modules/mod04-architecture/ch08-scale-and-tradeoffs.md) | Design decision matrix |
| Advanced | 5. Math and reliability | [9. The math people call hard](modules/mod05-math-reliability/ch09-math.md) | Capacity and error calculations |
| Advanced | 5. Math and reliability | [10. Testing, observability, and operations](modules/mod05-math-reliability/ch10-operations.md) | Test and rollout plan |
| Advanced | 6. Modern workloads | [11. AI and LLM rate limiting](modules/mod06-ai/ch11-ai-rate-limiting.md) | Multi-dimensional AI limiter |
| Mastery | 6. Modern workloads | [12. Capstone architecture](modules/mod06-ai/ch12-capstone.md) | End-to-end design exercise |

## Suggested schedule

- Week 1: Chapters 1–4; implement local limiters.
- Week 2: Chapters 5–7; add Redis and layered enforcement.
- Week 3: Chapters 8–10; load-test and operate the design.
- Week 4: Chapters 11–12; build the AI capstone.

## Course-wide conventions

- `r` = sustained refill rate in requests/second.
- `B` = token-bucket capacity, or maximum burst.
- `L` = configured request limit per window.
- `W` = window duration.
- `429 Too Many Requests` means the caller exceeded a policy.
- `503 Service Unavailable` is usually more honest when infrastructure capacity, not caller policy, is the constraint.

## Completion standard

Do not call a limiter “done” because it returns 429. You should be able to state its fairness key, burst semantics, maximum overshoot, consistency model, dependency failure behavior, memory bound, observability signals, and client retry contract.

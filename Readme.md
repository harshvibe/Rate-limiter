Redis Rate Limiter (Spring Boot)

A distributed rate limiter for Spring Boot APIs, backed by Redis. It uses an atomic sliding window algorithm implemented as a Lua script, and supports per-endpoint limits through a custom @RateLimit annotation.

Features:
Sliding window rate limiting using Redis sorted sets (no 2x burst at window boundaries)
Atomic check-and-record in a single Lua script, so it is safe under concurrent requests and multiple app instances
Custom @RateLimit(limit, window) annotation for per-endpoint limits, with global defaults as fallback
Standard response headers: X-RateLimit-Limit, X-RateLimit-Remaining, Retry-After
Returns 429 Too Many Requests with a JSON body when the limit is exceeded
Fail-open behavior: if Redis is unavailable, requests are allowed and the error is logged
Dockerized with a multi-stage build and Docker Compose

Tech stack:
Java 17, Spring Boot, Spring Data Redis, Redis 7, Lua, Docker, Docker Compose

how it works:
Client -> RateInterceptor -> RateLimiterService -> Redis (Lua script)
|                                         |
|  allowed?  <----------------------------+
+-- yes: continue to controller
+-- no:  429 + Retry-After

1. RateInterceptor runs before every /api/** request and identifies the client by IP.
2. RateLimiterService runs a Lua script on Redis that:
   removes entries older than the window (ZREMRANGEBYSCORE)
   counts the remaining entries (ZCARD)
   if under the limit, records the request (ZADD) and allows it
   otherwise rejects it and calculates Retry-After from the oldest entry in the window
3. Because all steps run inside one Lua script, Redis executes them atomically with no race conditions.

Why sliding window instead of fixed window?

A fixed window counter (INCR + EXPIRE) allows a burst of up to 2x the limit across a window boundary: for example, N requests at second 9 and N more at second 11. The sliding window counts requests in the last window seconds from the current moment, so the limit holds at any point in time.

Trade-off: the sliding window log stores one entry per request, 
so memory grows with the limit. This is fine for limits like 100 
requests per minute. For very high limits, a token bucket or sliding window
counter would use less memory.

Running:
With Docker Compose.
docker compose up --build
The API is available at http://localhost:8080.
Locally

Start Redis, then run the app:
docker run -d --name redis -p 6379:6379 redis:7
./mvnw spring-boot:run
Try it:
for i in {1..8}; do curl -i http://localhost:8080/api/tests; done

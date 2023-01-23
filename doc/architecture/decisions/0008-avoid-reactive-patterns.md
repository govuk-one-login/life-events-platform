# 8. Avoid reactive patterns

Date: 2023-01-23

## Status

Proposed

## Context

The Spring ecosystem has split into Web MVC (blocking) and Webflux (reactive). Webflux is based on Project Reactor, which
is still fairly immature compared to the blocking ecosystem. The [Webflux documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html#webflux-framework-choice)
states that MVC is the best choice for projects with blocking dependencies.

Webflux is valuable in applications under heavy load by making efficient use of threads and reducing memory consumption
([docs](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html#webflux-performance)).
While this may become an important in the future of this project this is not guaranteed, and some of the issues described
can be mitigated with autoscaling.

## Decision

Use more mature blocking APIs, except for WebClient which is the only supported option.

## Consequences

- Potential future performance impacts
- Easier to write and debug code
- Greater choice of dependencies
- Increased overall speed of development

[Next >>](9999-end.md)

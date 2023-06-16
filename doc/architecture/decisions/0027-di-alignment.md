# 27. DI Alignment

[Next >>](9999-end.md)

Date: 2023-05-10

## Status

Proposed

## Context

When a client no longer wants to receive events from our system, we should have some way of off-boarding them. This
should include stopping sending them events and marking them as deleted, we do not want to fully delete them from our DB
until it is safe to do so with respect to data security.

## Approach



![Image](di-alignment.svg)

[Next >>](9999-end.md)

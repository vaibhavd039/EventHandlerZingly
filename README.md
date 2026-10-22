# Event-Driven Communication

This project implements an engagement-event processor as a modern modular monolith using Java's own concurrency toolkit. It is one Spring Boot application in one container, with clear internal modules for ingestion, orchestration, reliability, persistence, preferences, and notification handlers. Chat, email, and voice handlers live under `notification.handler`; `NotificationHandlerFactory` selects one by channel without exposing concrete handlers to event processing. They are not separate microservices. It does not embed Kafka. The local version uses a shared `BlockingQueue`/scheduler, an `ExecutorService` worker pool, per-customer serialization, and H2 or in-memory persistence. Liquibase owns all database schema migrations. User preference updates use an in-process Observer pattern.

The full design is in [DESIGN.md](DESIGN.md).

## Identifier policy

All domain identifiers use `java.util.UUID`: `tenantId`, `customerId`, `eventId`, `preferenceId`, `changeLogId`, and `dlqId`. REST requests therefore accept canonical UUID values, and generated internal IDs use `UUID.randomUUID()`.

The current H2 schema stores UUIDs as canonical 36-character `VARCHAR` values. Entity fields explicitly map UUIDs to `VARCHAR`, which keeps the SQL migrations simple and portable while preserving UUID validation and type safety in Java. Business values such as `channel`, `notificationType`, `updatedBy`, and status are not identifiers and keep their natural types.

## API documentation and testing

Start the Spring Boot application from the `events` directory. Swagger UI is available at `http://localhost:8080/swagger-ui.html`, and the generated OpenAPI JSON is available at `http://localhost:8080/v3/api-docs`.

Import [`events/postman/Engagement-Events.postman_collection.json`](events/postman/Engagement-Events.postman_collection.json) into Postman. The collection already contains variables, request bodies, response checks, duplicate-event tests, preference tests, a forced DLQ scenario, replay, and statistics. Run its folders in order against the default `http://localhost:8080` base URL.

## Kafka mapping

| Local implementation | Real Kafka concept |
| --- | --- |
| Customer logical lane | Partition selected by the event ordering key |
| `BlockingQueue` and ready-customer tokens | Consumer poll loop plus local scheduling |
| `ExecutorService` workers | Consumers/tasks in one consumer group |
| H2 inbox and result tables | Application inbox/idempotency store and result database |
| Customer sequence | Partition offset plus optional application sequence |
| Successful processing transition | Database result commit followed by offset commit |
| Retry scheduler | Retry topic or delayed-message mechanism |
| DLQ table | Dead-letter topic plus inspection metadata |

The in-memory version differs from Kafka because it has one process, no replicated durable log, no consumer-group rebalancing, and no broker-managed offsets. H2/database state is the source of truth for idempotency and results. In Kafka, database commit and offset commit are separate operations, so an inbox/outbox or equivalent recovery strategy is still required.

The ordering rule remains the same in both versions: all events for one ordering key use one lane/partition, and a later event cannot be committed before an earlier retrying or dead-lettered event.

## User preferences

`user_preference` stores tenant-scoped subscriptions by user/customer, notification type, and channel. A user can disable one notification without disabling all notifications. The engagement event is still persisted and processed; only the outbound notification is marked `SUPPRESSED_BY_PREFERENCE` when disabled.

`UserPreferenceService` is the Observer subject. After a preference transaction commits, it publishes `PreferenceChangedEvent` to observers such as a preference-cache observer, notification-routing observer, and `PreferenceChangeLogObserver`. The change-log observer stores `{"prevValue": ..., "currValue": ...}` in a `VARCHAR` column. The response DTO parses this string and exposes `changeLog` as a JSON object. The database remains authoritative; observers maintain derived state and must not change event ordering.

## Database migrations

Liquibase is the single source of truth for the schema, while table definitions are written as Liquibase formatted SQL. The master changelog is under `src/main/resources/db/changelog/`, and it includes files such as `001-create-event-inbox.sql`. Each SQL file declares `--liquibase formatted sql` and a stable `--changeset author:id`. Every table, constraint, and index must be introduced through a versioned SQL changeset. Hibernate schema auto-generation and ad-hoc SQL migrations are disabled. Application startup runs pending Liquibase changesets before event ingestion begins, and tests run the same SQL changesets against an isolated database.

Tenant channel configuration is normalized: `tenant_account` stores tenant identity, while `tenant_config` stores one `(tenant_id, channel_type, is_enabled)` row per channel. Existing CHAT, EMAIL, and VOICE flags are migrated by changeset `009`; `default_preference_enabled` and the unused `config_json` column are removed. Hard-coded database channel checks are also removed so a newly supported channel can be inserted without another schema migration.

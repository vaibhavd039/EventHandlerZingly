# Event-driven communication diagrams

| Area | Mermaid source | Rendered image |
|---|---|---|
| High-level design | `01-hld.mmd` | `01-hld.png` |
| Event creation sequence | `02-event-creation-sequence.mmd` | `02-event-creation-sequence.png` |
| Event creation activity | `03-event-creation-activity.mmd` | `03-event-creation-activity.png` |
| Worker processing sequence | `04-worker-processing-sequence.mmd` | `04-worker-processing-sequence.png` |
| Worker processing activity | `05-worker-processing-activity.mmd` | `05-worker-processing-activity.png` |
| DLQ sequence | `06-dlq-sequence.mmd` | `06-dlq-sequence.png` |
| DLQ activity | `07-dlq-activity.mmd` | `07-dlq-activity.png` |

The diagrams describe the current Spring Boot implementation. The database-backed event inbox is the durable queue, while `CustomerQueueCoordinator` provides tenant-and-customer ordering and parallel processing across different customers.

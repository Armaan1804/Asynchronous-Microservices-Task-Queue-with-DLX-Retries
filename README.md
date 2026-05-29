# Resilient Distributed Task Processing Queue

A fault-tolerant, asynchronous task processing system built using a Spring Boot microservice architecture. It decouples high-throughput API requests from resource-intensive background jobs using a RabbitMQ message broker, utilizing a PostgreSQL database for real-time task status tracking and Dead Letter Exchanges (DLX) for automatic retries and failure isolation.

---

## 🏗️ Architecture Overview

```
[ Client ] ---> ( POST /api/tasks ) ---> [ Producer Service ]
                                             |
                                      ( Logs to Postgres )
                                      ( Publishes to Queue )
                                             v
                                      [ RabbitMQ Queue ] (task_queue)
                                       /     |     \
                             ( Prefetch / Fair Dispatch )
                                     /       |       \
                                    v        v        v
                               [Worker 1] [Worker 2] [Worker 3]
                                    |        |        |
                                    +--------+--------+ (Updates state in Postgres)
                                             |
                                 (If fails 3x, routes to)
                                             v
                                   [ Dead Letter Queue ] (task_queue_dlq)
```

---

## ✨ Features

- **Microservice Design**: Independent Producer API Gateway and Consumer Worker nodes.
- **Asynchronous Decoupling**: API accepts tasks instantly and delegates processing to RabbitMQ.
- **Real-Time Tracking**: Task states (`QUEUED`, `PROCESSING`, `COMPLETED`, `FAILED`) are tracked in a PostgreSQL database with timestamps and failure reasons.
- **Resiliency & Fault Tolerance**: Spring AMQP container retries (up to 3 times) with exponential back-off.
- **Dead Letter Queue (DLQ)**: Mismatched or permanently failed tasks are cleanly routed to a Dead Letter Exchange (DLX) and isolated in `task_queue_dlq`.
- **Fair Dispatch Load Balancing**: Configured listener prefetch limit (`prefetch=1`) to balance tasks across multiple worker instances.

---

## 🛠️ Tech Stack

- **Core**: Java 17+, Spring Boot 3.3.0
- **Message Broker**: RabbitMQ
- **Database**: PostgreSQL (JPA / Hibernate)
- **Containerization**: Docker & Docker Compose

---

## 🚀 How to Run & Test

### Prerequisites
- Docker & Docker Compose installed OR Erlang, RabbitMQ, and PostgreSQL installed natively.

### Run with Docker Compose
```bash
docker compose up --build --scale worker-node=3
```

### Test API Endpoints
1. **Submit a successful task (e.g. takes 3 seconds)**:
   ```bash
   curl -X POST "http://localhost:8080/api/tasks?type=ImageProcessing&duration=3000"
   ```
2. **Submit a failing task (triggers 3 retries and routes to DLQ)**:
   ```bash
   curl -X POST "http://localhost:8080/api/tasks?type=fail&duration=1000"
   ```
3. **Query Task Status (in PostgreSQL)**:
   ```sql
   SELECT * FROM tasks;
   ```
4. **Access RabbitMQ Dashboard**:
   Go to [http://localhost:15672](http://localhost:15672) (Login: `guest` / `guest`).

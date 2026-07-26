# Running the whole platform with Docker

Everything (databases, Kafka, Redis, service discovery, config server, gateway
and the five business services) runs from a single `docker compose` command.
Containers talk to each other by service name on the compose network; the
localhost values in `config-repo` are overridden per container in
`docker-compose.yml`.

## Prerequisites

- Docker Desktop (or Docker Engine + compose plugin).
- Nothing else. You do **not** need a local JDK, Postgres, Kafka or Redis to run
  the stack in Docker.

## Build and start

```bash
# build all images and start everything
docker compose up --build

# or, once images exist, just start
docker compose up
```

Startup order is handled with healthchecks: Postgres, Kafka and the config
server must be healthy before the apps start, so the first boot takes a little
longer.

## Databases

The `postgres` container creates the five databases on first start via
`scripts/postgres-init/01-create-databases.sql`:

- `ticketing_event`, `ticketing_inventory`, `ticketing_booking`,
  `ticketing_payment`, `ticketing_notification`

The schema itself is created by Flyway inside each service on boot.

### Will it clash with the Postgres already installed on my laptop?

No. The container runs its own isolated Postgres 16 and never touches whatever
you have installed on the host. The only thing that can collide is a **host
port**, so the container's 5432 is published on host port **5433**
(`5433:5432`). Your local Postgres keeps 5432; connect a client to
`localhost:5433` if you want to inspect the container's databases. Docker does
not detect or reuse host services - each container is self-contained.

### Image versions

Images are pinned to a fixed tag (`postgres:16`, `redis:7-alpine`,
`apache/kafka:3.9.0`, `eclipse-temurin:21-jre-jammy`). Docker tags are not
semver ranges - you pin an exact tag, not a range like `^16`. Use a broader tag
(`postgres:16`) to get the latest 16.x on pull, or a narrower one
(`postgres:16.4`) to lock it down completely.

## Ports

| Service            | URL                      |
| ------------------ | ------------------------ |
| API gateway        | http://localhost:8080    |
| Eureka dashboard   | http://localhost:8761    |
| Config server      | http://localhost:8888    |
| Kafka UI           | http://localhost:8090    |
| Postgres           | localhost:5433           |
| Event / Inventory / Booking / Payment / Notification | 8081 / 8082 / 8083 / 8084 / 8085 |

All traffic should go through the gateway on 8080; the direct service ports are
exposed only for debugging.

## Run the end-to-end test

With the stack up:

```bash
node scripts/e2e.mjs
```

## Publishing images to Docker Hub

Image names are parameterised. Log in and push:

```bash
docker login
REGISTRY=<your-dockerhub-user> docker compose build
REGISTRY=<your-dockerhub-user> docker compose push
```

On another machine, put `docker-compose.yml` next to a copy of
`scripts/postgres-init/`, set the same `REGISTRY`, and run `docker compose up` -
compose pulls the published images and starts the whole platform.

In CI this is automated by `.github/workflows/docker-publish.yml`, which builds
and pushes every image on each push to `main` (needs the `DOCKERHUB_USERNAME`
and `DOCKERHUB_TOKEN` repository secrets).

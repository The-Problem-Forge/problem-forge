# Startup instuctions

* Project consists of 2 docker-compose packages - backend + frontend.

* If not using `deploy.sh` create docker network before start.

```bash
docker network create -d bridge problem-forge-network
```

* Database connection

```bash
docker exec -it <container_id> psql -U postgres -d problem_forge_db
```
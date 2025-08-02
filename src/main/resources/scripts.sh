docker run --name influxdb -p 8086:8086
-v influxdb-data:/var/lib/influxdb2
-e DOCKER_INFLUXDB_INIT_MODE=setup
-e DOCKER_INFLUXDB_INIT_USERNAME=yusuf_influx
-e DOCKER_INFLUXDB_INIT_PASSWORD=localpassword
-e DOCKER_INFLUXDB_INIT_ORG=yusufu
-e DOCKER_INFLUXDB_INIT_BUCKET=javaspringfeatures
-e DOCKER_INFLUXDB_INIT_ADMIN_TOKEN=yusuf_token
influxdb:2.7

docker exec influxdb influx bucket create --name javaspringfeatures2 --org yusufu

docker exec influxdb influx bucket list --org yusufu
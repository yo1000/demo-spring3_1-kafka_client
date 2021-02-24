# Demo Spring 3.1 Kafka Client

Examples
```
./mvnw clean package && docker-compose up

curl -i -XPOST -H'Content-Type: application/json; charset=utf-8' -d"TEST $(date '+%Y-%m-%d %H:%M:%S')" localhost:8080/message/
```

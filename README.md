## gRPC Java - _social_

## _Setup mongodb_

* docker-compose file
```yaml
    version: "3.5"
    
    services:
      mongo:
        container_name: mongo
        image: mongo:4.0.6
        ports:
          - 27017:27017
        command: mongod
```
* in _terminal_
* Running docker compose
```text
docker-compose -f docker-compose.yml up -d
```

* cek _image_ running
```text
docker container ls
```

* go to mongo shell
```text
docker exec -it mongo /bin/sh   
```

* in _mongo-shell_
* running mongo server
```text
mongo

 - or -
 
mongo --host localhost --port 27017

```
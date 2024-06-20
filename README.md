# Experimental Kotlin Rewrite of apgdiff

[![CircleCI](https://dl.circleci.com/status-badge/img/gh/lovelysystems/apgdiff/tree/master.svg?style=svg)](https://dl.circleci.com/status-badge/redirect/gh/lovelysystems/apgdiff/tree/master)

Another PostgreSQL Diff Tool is free PostgreSQL diff tool that is useful for
comparison/diffing of database schemas dumped via `pg_dump`.

## Install and run locally

```shell script
./gradlew installDist
./build/install/apgdiff/bin/apgdiff --help 
```

## Build and run Docker Image

The executable in the docker image is a native binary created with kotlin multiplatform, so there is no need for a JVM.
Currently only an `linux/amd64` image is built.

```shell script
./gradlew buildDockerImage
docker run --rm lovelysystems/apgdiff:dev --help
```

# Credits

This project is a kotlin rewrite of https://github.com/netwo-io/apgdiff . The
original project can be found ot https://www.apgdiff.com/.

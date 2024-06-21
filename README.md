# Experimental Kotlin Rewrite of apgdiff

[![CircleCI](https://dl.circleci.com/status-badge/img/gh/lovelysystems/apgdiff/tree/master.svg?style=svg)](https://dl.circleci.com/status-badge/redirect/gh/lovelysystems/apgdiff/tree/master)
[![Docker Image Version](https://img.shields.io/docker/v/lovelysystems/apgdiff?sort=semver&label=Docker)](https://hub.docker.com/repository/docker/lovelysystems/apgdiff)


Another PostgreSQL Diff Tool is useful for comparison/diffing of database schemas.

The schemas are compared based on database dumps created by using `pg_dump`. So 
for the actual comparison, there is no direct database access required.

## Install and run locally (JVM)

```shell script
./gradlew installDist
./build/install/apgdiff/bin/apgdiff --help 
```

## Build and run Docker Image

The executable in the docker image is a native binary created with [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html),
so there is no need for a JVM. The docker image is built for amd64 and arm64 platforms.

```shell script
./gradlew buildDockerImage
docker run --rm lovelysystems/apgdiff:dev --help
```

# Credits

This project is a kotlin rewrite of https://github.com/netwo-io/apgdiff . The
original project can be found ot https://www.apgdiff.com/.

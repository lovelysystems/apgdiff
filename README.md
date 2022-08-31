# Experimental Kotlin Rewrite of apgdiff

[![Build Status](https://travis-ci.com/lovelysystems/apgdiff.svg?branch=master)](https://travis-ci.com/lovelysystems/apgdiff)

Another PostgreSQL Diff Tool is free PostgreSQL diff tool that is useful for
comparison/diffing of database schemas dumped via `pg_dump`.

## Install and run locally

```shell script
./gradlew installDist
./build/install/apgdiff/bin/apgdiff --help 
```

## Build and run Docker Image

The executable in the docker image is built using [GraalVM](https://www.graalvm.org/) so there is no need for a JRE at
runtime only libc.

```shell script
./gradlew buildDockerImage
docker run --rm lovelysystems/apgdiff:dev --help
```

Note that this native image is only compiled towards the platform the docker build is running on. So for example if you
build on an M1 Mac it will generate an arm64 executable.

There is currently no cross compilation possible directly by `native-image`
see <https://github.com/oracle/graal/issues/407>

# Credits

This project is a kotlin rewrite of https://github.com/netwo-io/apgdiff . The
original project can be found ot https://www.apgdiff.com/.

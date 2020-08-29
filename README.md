# Experimental Kotlin Rewrite of apgdiff

[![Build Status](https://travis-ci.com/lovelysystems/apgdiff.svg?branch=master)](https://travis-ci.com/lovelysystems/apgdiff)

Another PostgreSQL Diff Tool is free PostgreSQL diff tool that is useful for
comparison/diffing of database schemas.

## Install and run locally

```shell script
./gradlew installDist
./build/install/apgdiff/bin/apgdiff --help 
```

## Build and run Docker Image

```shell script
./gradlew buildDockerImage
docker run --rm lovelysystems/apgdiff:dev apgdiff --help
```

# Credits

This project is a kotlin rewrite of https://github.com/netwo-io/apgdiff . The
original project can be found ot https://www.apgdiff.com/.

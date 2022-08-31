# use the graalvm image to build the static executable
FROM ghcr.io/graalvm/graalvm-ce:22.2.0 AS builder
RUN gu install native-image

COPY *-fat.jar /tmp/fat.jar

# currently we require libc, however it should be possible to include musl instead of libc
# see https://www.graalvm.org/reference-manual/native-image/StaticImages/
RUN native-image \
    -H:+StaticExecutableWithDynamicLibC \
    -H:Class=cz.startnet.utils.pgdiff.CLIKt \
    --no-fallback \
    -jar /tmp/fat.jar \
    /tmp/apgdiff

# the actual docker image using the native image with libc dependencies
# see https://github.com/GoogleContainerTools/distroless/blob/main/cc/README.md
FROM gcr.io/distroless/cc
COPY --from=builder /tmp/apgdiff /usr/local/bin/apgdiff

ENTRYPOINT ["/usr/local/bin/apgdiff"]

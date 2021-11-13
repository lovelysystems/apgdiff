# use the graalvm image to build the static executable
FROM ghcr.io/graalvm/graalvm-ce:java11-21.3.0 AS graalvm
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
# see https://console.cloud.google.com/gcr/images/distroless/GLOBAL/cc-debian10@sha256:c33fbcd3f924892f2177792bebc11f7a7e88ccbc247f0d0a01a812692259503a/details?tab=info
FROM gcr.io/distroless/cc-debian10@sha256:c33fbcd3f924892f2177792bebc11f7a7e88ccbc247f0d0a01a812692259503a
COPY --from=graalvm /tmp/apgdiff /usr/local/bin/
ENTRYPOINT ["/usr/local/bin/apgdiff"]

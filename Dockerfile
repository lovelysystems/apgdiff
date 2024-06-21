FROM --platform=$TARGETPLATFORM debian:12.5-slim

ARG TARGETARCH
COPY $TARGETARCH/apgdiff.kexe /usr/local/bin/apgdiff
ENTRYPOINT ["/usr/local/bin/apgdiff"]

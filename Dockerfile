FROM debian:12.5-slim
COPY apgdiff.kexe /usr/local/bin/apgdiff
ENTRYPOINT ["/usr/local/bin/apgdiff"]

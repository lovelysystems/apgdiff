FROM openjdk:14-jdk-alpine
COPY *.tar /install/
RUN cd /usr/local && tar --strip-components=1 -xf /install/*.tar && rm -rf /install
CMD ["apgdiff", "--help"]

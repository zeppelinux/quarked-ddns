FROM redhat/ubi8-minimal:latest

ARG JAVA_PACKAGE=java-11-openjdk-headless
ARG RUN_JAVA_VERSION=1.3.8
ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en'
# Install java and the run-java script
RUN microdnf install curl ca-certificates tar gzip ${JAVA_PACKAGE} \
    && microdnf update \
    && microdnf clean all \
    && mkdir /deployments \
    && chown 1001 /deployments \
    && chmod "g+rwX" /deployments \
    && chown 1001:root /deployments \
    && curl https://repo1.maven.org/maven2/io/fabric8/run-java-sh/${RUN_JAVA_VERSION}/run-java-sh-${RUN_JAVA_VERSION}-sh.sh -o /deployments/run-java.sh \
    && chown 1001 /deployments/run-java.sh \
    && chmod 540 /deployments/run-java.sh \
    && echo "securerandom.source=file:/dev/urandom" >> /etc/alternatives/jre/conf/security/java.security

# Configure the JAVA_OPTIONS, you can add -XshowSettings:vm to also display the heap size.
ENV JAVA_OPTIONS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"

ARG TAG
ADD https://github.com/zeppelinux/quarked-ddns/releases/download/${TAG}/quarkus-app.tar.gz /var/tmp/

# Also set up permissions for user `1001`
RUN mkdir -p /var/tmp/quarkus-app \
    && tar -xzf /var/tmp/quarkus-app.tar.gz -C /var/tmp/quarkus-app \
    && rm /var/tmp/quarkus-app.tar.gz \
    && mv /var/tmp/quarkus-app/* /deployments/ \
    && chown -R 1001 /deployments/


EXPOSE 8080
USER 1001

ENTRYPOINT [ "/deployments/run-java.sh" ]

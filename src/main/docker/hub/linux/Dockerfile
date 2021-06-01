FROM redhat/ubi8-minimal:latest
WORKDIR /work/
RUN chown 1001 /work \
    && chmod "g+rwX" /work \
    && chown 1001:root /work

ARG TAG
ADD https://github.com/zeppelinux/quarked-ddns/releases/download/${TAG}/quarked-ddns-linux /work/application

RUN chown 1001 /work/application && chmod "u+x" /work/application

EXPOSE 8080
USER 1001

CMD ["./application", "-Dquarkus.http.host=0.0.0.0"]
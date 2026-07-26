ARG VERSION=26.7.0
ARG JAVA_VERSION=25

# Stage 1: Build SPI with Java 25
FROM eclipse-temurin:${JAVA_VERSION}-jdk-alpine AS spi-builder

WORKDIR /build

COPY keycloak-jwe-spi/pom.xml keycloak-jwe-spi/pom.xml
COPY keycloak-jwe-spi/src keycloak-jwe-spi/src

RUN apk add --no-cache maven && \
    cd keycloak-jwe-spi && \
    mvn clean package -DskipTests -q

# Stage 2: Java 25 source image
FROM eclipse-temurin:${JAVA_VERSION}-jdk-jammy AS java25

# Stage 3: Build Keycloak with the SPI using Java 25
FROM keycloak/keycloak:${VERSION} AS builder

USER root

COPY --from=java25 /opt/java/openjdk/ /opt/java/

ENV JAVA_HOME=/opt/java
ENV PATH="${JAVA_HOME}/bin:${PATH}"

RUN java -version

USER keycloak

ENV KC_HEALTH_ENABLED=true
ENV KC_DB=postgres

COPY --from=spi-builder --chown=keycloak:keycloak /build/keycloak-jwe-spi/target/keycloak-jwe-spi-0.0.1-SNAPSHOT.jar /opt/keycloak/providers/keycloak-jwe-spi.jar

WORKDIR /opt/keycloak

RUN /opt/keycloak/bin/kc.sh build

# Stage 4: Final image - Java 25 runtime with Keycloak
FROM eclipse-temurin:${JAVA_VERSION}-jdk-jammy

ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="${JAVA_HOME}/bin:${PATH}"

RUN groupadd -r keycloak && useradd -r -g keycloak -d /opt/keycloak keycloak

COPY --from=builder --chown=keycloak:keycloak /opt/keycloak/ /opt/keycloak/

EXPOSE 8080

USER keycloak

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
CMD ["start-dev", "--import-realm"]

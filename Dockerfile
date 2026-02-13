# This is the Dockerfile responsible for creating an image compatible with Ubuntu Server (amd64)

# Frontend Build Stage
FROM node:18-bullseye-slim AS frontend-builder
LABEL maintainer="bgls"

WORKDIR /app

COPY package.json package-lock.json angular.json ngsw-config.json tsconfig.json tsconfig.app.json ./
COPY webpack/ webpack/
COPY src/main/webapp src/main/webapp/

RUN npm ci --prefer-offline --no-audit --loglevel=error
RUN npm run webapp:prod

# Backend Build Stage
FROM eclipse-temurin:17-jdk-focal AS backend-builder
LABEL maintainer="bgls"

ARG MAVEN_VERSION=3.8.7
ARG MAVEN_HOME=/usr/share/maven
ARG CONTAINER_USER=appuser
ARG CONTAINER_GROUP=appuser

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        curl unzip procps \
        git gnupg2 libstdc++6 zlib1g locales \
    && rm -rf /var/lib/apt/lists/* \
    && curl -fsSL https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.zip -o /tmp/maven.zip \
    && unzip -q /tmp/maven.zip -d /usr/share \
    && mv /usr/share/apache-maven-${MAVEN_VERSION} ${MAVEN_HOME} \
    && ln -s ${MAVEN_HOME}/bin/mvn /usr/bin/mvn \
    && rm /tmp/maven.zip \
    && groupadd ${CONTAINER_GROUP} \
    && useradd -ms /bin/bash -g ${CONTAINER_GROUP} ${CONTAINER_USER} \
    && mkdir -p /home/${CONTAINER_USER}/.m2 \
    && chown -R ${CONTAINER_USER}:${CONTAINER_GROUP} /home/${CONTAINER_USER}/.m2

ENV MAVEN_HOME=${MAVEN_HOME}
ENV PATH=${MAVEN_HOME}/bin:${PATH}

WORKDIR /app
USER ${CONTAINER_USER}

COPY --chown=${CONTAINER_USER}:${CONTAINER_GROUP} mvnw .
COPY --chown=${CONTAINER_USER}:${CONTAINER_GROUP} .mvn .mvn
COPY --chown=${CONTAINER_USER}:${CONTAINER_GROUP} pom.xml .
COPY --chown=${CONTAINER_USER}:${CONTAINER_GROUP} sonar-project.properties .

COPY --chown=${CONTAINER_USER}:${CONTAINER_GROUP} src src

COPY --from=frontend-builder --chown=${CONTAINER_USER}:${CONTAINER_GROUP} /app/target/classes/static target/classes/static

ENV MAVEN_OPTS="-Xmx1G"
RUN ./mvnw package -Pprod -DskipTests -Dskip.frontend.build=true

# Final Image Stage
FROM eclipse-temurin:17-jre-focal AS final
LABEL maintainer="bgls"

ARG CONTAINER_USER=appuser
ARG CONTAINER_GROUP=appuser
RUN groupadd ${CONTAINER_GROUP} && useradd -ms /bin/bash -g ${CONTAINER_GROUP} ${CONTAINER_USER}

ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS
ENV JHIPSTER_SLEEP=0

WORKDIR /app
USER ${CONTAINER_USER}

COPY --from=backend-builder /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]

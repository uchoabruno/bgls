# Stage 0: Build the frontend (Node.js and npm are needed here for arm64)
# Usamos uma imagem Node.js específica para arm64
FROM --platform=linux/arm64 node:18-bullseye-slim AS frontend-builder
LABEL maintainer="bgls"

WORKDIR /app

# Copia os arquivos necessários para o build do frontend para aproveitar o cache do Docker
# (package.json, package-lock.json, angular.json, ngsw-config.json, tsconfig.json, webpack/ configs, e a pasta src/main/webapp)
# ASSUMINDO QUE ngsw-config.json ESTÁ NA RAIZ DO PROJETO JHipster.
COPY package.json package-lock.json angular.json ngsw-config.json tsconfig.json tsconfig.app.json ./
COPY webpack/ webpack/
COPY src/main/webapp src/main/webapp/

# Instala as dependências do Node.js e constrói o frontend para produção
# `--prefer-offline` usa o cache local do npm se disponível
# `--no-audit` desabilita a auditoria de segurança (pode ser habilitado em produção final)
# `--loglevel=error` minimiza o output do npm
RUN npm ci --prefer-offline --no-audit --loglevel=error
RUN npm run webapp:prod

# A saída do `npm run webapp:prod` para um projeto JHipster geralmente vai para `target/classes/static`
# dentro do diretório de trabalho do Maven. Aqui, como estamos em um ambiente separado,
# a saída estará em /app/target/classes/static.

# Stage 1: Build the Java application (without frontend build)
# Usamos uma imagem temurin Java 17 específica para arm64
FROM --platform=linux/arm64 eclipse-temurin:17-jre-focal AS backend-builder
LABEL maintainer="bgls"

# Argumentos para instalação do Maven e configuração de usuário
ARG MAVEN_VERSION=3.8.7
ARG MAVEN_HOME=/usr/share/maven
ARG CONTAINER_USER=appuser
ARG CONTAINER_GROUP=appuser

# Instala o Maven e outras ferramentas de build necessárias
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

# Corrigido: Usar sintaxe ENV key=value
ENV MAVEN_HOME=${MAVEN_HOME}
ENV PATH=${MAVEN_HOME}/bin:${PATH}

# Cria o diretório da aplicação e define o usuário
WORKDIR /app
USER ${CONTAINER_USER}

# Copia o wrapper Maven e o pom.xml
COPY --chown=${CONTAINER_USER}:${CONTAINER_GROUP} mvnw .
COPY --chown=${CONTAINER_USER}:${CONTAINER_GROUP} .mvn .mvn
COPY --chown=${CONTAINER_USER}:${CONTAINER_GROUP} pom.xml .

# Copia o código fonte Java e recursos
COPY --chown=${CONTAINER_USER}:${CONTAINER_GROUP} src src

# Copia os recursos estáticos do frontend construídos na etapa anterior para o local esperado pelo Maven
# Isso garante que o JAR final inclua o frontend.
COPY --from=frontend-builder --chown=${CONTAINER_USER}:${CONTAINER_GROUP} /app/target/classes/static target/classes/static

# Constrói a aplicação Java, pulando explicitamente a execução do frontend-maven-plugin
# Corrigido: Usar sintaxe ENV key=value
ENV MAVEN_OPTS="-Xmx1G"
# ADICIONADO: -Dskip.frontend.build=true para pular o frontend-maven-plugin
RUN ./mvnw package -Pprod -DskipTests -Dskip.frontend.build=true

# Stage 2: Cria a imagem final de produção (runtime)
FROM --platform=linux/arm64 eclipse-temurin:17-jre-focal AS final
LABEL maintainer="bgls"

# Cria um usuário não-root para segurança
ARG CONTAINER_USER=appuser
ARG CONTAINER_GROUP=appuser
RUN groupadd ${CONTAINER_GROUP} && useradd -ms /bin/bash -g ${CONTAINER_GROUP} ${CONTAINER_USER}

# Define variáveis de ambiente para o Spring Boot
ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS
ENV JHIPSTER_SLEEP=0

WORKDIR /app
USER ${CONTAINER_USER}

# Copia o JAR construído da etapa de backend-builder
COPY --from=backend-builder /app/target/*.jar app.jar

# Define o ponto de entrada da aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]

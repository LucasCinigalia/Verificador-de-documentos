# Estágio 1: compila com Maven. As dependências ficam numa camada própria para o cache do
# Docker reaproveitá-las enquanto o pom.xml não mudar.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

# Estágio 2: só a JRE e o jar, rodando sem root.
FROM eclipse-temurin:21-jre
RUN useradd --system --uid 10001 veridoc \
    && mkdir -p /var/lib/veridoc/arquivos \
    && chown -R veridoc /var/lib/veridoc
USER veridoc
WORKDIR /app
COPY --from=build /app/target/veridoc-api-*.jar app.jar
ENV ARMAZENAMENTO_DIRETORIO=/var/lib/veridoc/arquivos \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

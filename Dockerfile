# Builds any one module: docker build --build-arg MODULE=api-gateway .
FROM eclipse-temurin:21-jdk AS build
WORKDIR /src
RUN apt-get update && apt-get install -y --no-install-recommends maven && rm -rf /var/lib/apt/lists/*
COPY . .
ARG MODULE
RUN mvn -B -q -pl ${MODULE} -am package -DskipTests && cp ${MODULE}/target/${MODULE}-*.jar /app.jar

FROM eclipse-temurin:21-jre
RUN useradd --system --uid 10001 app
COPY --from=build /app.jar /app/app.jar
USER app
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]

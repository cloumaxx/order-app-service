# BUILD
FROM eclipse-temurin:21-jdk as build
WORKDIR /app

# Copy wrapper and config
COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./

RUN ./gradlew --no-daemon dependencies || true

COPY src src

# build JAR
RUN ./gradlew --no-daemon clean bootJar -x test

# RUN TIME
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy JAR
COPY --from=build /app/build/libs/*SNAPSHOT.jar app.jar

ENV JAVA_OPTS=""
EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
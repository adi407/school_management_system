FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY sms-core/pom.xml sms-core/
COPY sms-auth/pom.xml sms-auth/
COPY sms-api/pom.xml sms-api/
COPY sms-batch/pom.xml sms-batch/

# Download dependencies
RUN --mount=type=cache,target=/root/.m2 \
    mvn -f pom.xml dependency:go-offline -q

COPY . .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -f pom.xml package -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S sms && adduser -S sms -G sms
USER sms

COPY --from=build /app/sms-api/target/sms-api-*.jar app.jar

# Use virtual threads (Project Loom) for Tomcat
ENV JAVA_OPTS="-Xms256m -Xmx1g -XX:+UseG1GC -Dspring.threads.virtual.enabled=true"

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

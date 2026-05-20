FROM eclipse-temurin:21-jre
WORKDIR /app
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} ./app.jar

RUN addgroup --system app && adduser --system --ingroup app app \
    && chown app:app /app
USER app
ENTRYPOINT ["java","-jar","/app.jar"]
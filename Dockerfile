FROM amazoncorretto:21-alpine
WORKDIR /app
COPY . .
RUN chmod +x gradlew
EXPOSE 9000
CMD ["./gradlew", "bootRun"]
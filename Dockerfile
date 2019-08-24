FROM clojure:openjdk-8-lein-alpine AS builder

COPY . /clj-googledrive-form
RUN cd /clj-googledrive-form && lein uberjar

FROM openjdk:8-alpine

COPY --from=builder /clj-googledrive-form/target/uberjar/clj-googledrive-form-*-standalone.jar \
                    /clj-googledrive-form/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/clj-googledrive-form/app.jar"]

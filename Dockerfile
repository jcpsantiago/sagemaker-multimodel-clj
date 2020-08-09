FROM clojure:openjdk-11-lein AS builder

COPY . /sagemaker-multimodel
RUN cd /sagemaker-multimodel && lein uberjar

FROM adoptopenjdk/openjdk11-openj9:alpine-slim

RUN apk add libgomp

COPY --from=builder /sagemaker-multimodel/target/uberjar/ubersage.jar \
                    /sagemaker-multimodel/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/sagemaker-multimodel/app.jar"]


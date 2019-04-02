FROM openjdk:alpine
COPY key-value-example/build/libs/key-value-example-1.jar /usr/src/app/
WORKDIR /usr/src/app
CMD java -XX:+PrintFlagsFinal $JAVA_OPTIONS -jar key-value-example-1.jar
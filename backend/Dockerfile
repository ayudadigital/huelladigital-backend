FROM adoptopenjdk/openjdk11:alpine-slim
COPY target/*.jar /
ENV packageFile="/*.jar"
CMD java -jar -Dspring.profiles.active=${PROFILE} ${packageFile}

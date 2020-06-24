FROM adoptopenjdk/openjdk11:alpine-slim
COPY target/*.jar /
ENV packageFile="/*.jar"
CMD java -jar -Dfile.encoding="UTF-8" -Dspring.profiles.active=${PROFILE} ${packageFile}

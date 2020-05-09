FROM maven:3.6.3-jdk-11
COPY target/*.jar /
ENV packageFile="/*.jar"
CMD java -jar ${packageFile} -Dspring.profiles.active=${PROFILE}

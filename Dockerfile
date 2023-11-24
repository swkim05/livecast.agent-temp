##
## Image for thunderstream.rgate
##
FROM centos:7

## Update and package install
RUN yum -y update && yum clean all
RUN yum -y install tomcat-native bind-utils net-tools
RUN yum -y install java-1.8.0-openjdk

## Install application
ARG JAR_FILE
RUN mkdir -p /aetherit/project/base
RUN mkdir -p /aetherit/project/base/config
RUN mkdir -p /aetherit/project/base/logs
COPY ./config/logback-spring.xml    /aetherit/project/base/config/logback-spring.xml
COPY ./target/${JAR_FILE}           /aetherit/project/base/app.jar

EXPOSE 8000
EXPOSE 8080

WORKDIR /aetherit/project/base

CMD java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000 -Djava.library.path=/usr/lib/x86_64-linux-gnu -jar /aetherit/project/base/app.jar

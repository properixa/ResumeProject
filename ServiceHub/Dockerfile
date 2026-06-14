FROM tomcat:jdk21
COPY build/libs/service-hub-api.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
CMD ["catalina.sh", "run"]
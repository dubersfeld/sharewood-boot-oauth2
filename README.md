# sharewood-boot-oauth2
A Spring photo REST server with separate Spring OAuth2 authorization server and Spring client. 

All three applications are developed with Spring Boot support.

The authorization server AuthorizationServer is deployed with context path: localhost:8080/authorization

The resource server Sharewood is deployed with context path: localhost:8081/sharewood

The Spring client Fleetwood is deployed with context path: localhost:9090/fleetwood

A MySql database is used. The generating file sharewoodDB.sql contains all tables needed by Spring OAuth2 support.

Only authorization code grant is supported in this application. Only bearer tokens are used. No refresh token is provided in this basic version.

Only default Spring components are used for implementation. 

Here are the application properties used to set most deployment variables:

AuthorizationServer

spring.datasource.driver-class-name=com.mysql.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/transitSharewoodDB
spring.datasource.username=tomcatUser
spring.datasource.password=password1234

server.port=8080
server.context-path=/authorization
spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl

Sharewood

spring.datasource.driver-class-name=com.mysql.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/transitSharewoodDB
spring.datasource.username=tomcatUser
spring.datasource.password=password1234

server.port=8081
server.context-path=/sharewood
spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl

#override default upload file max size 1MB
spring.http.multipart.max-file-size=10MB
spring.http.multipart.max-request-size=10MB

security.oauth2.resource.id=SHAREWOOD

#server temporary directory
photos.baseDirPath=/home/dominique/workspace-resource-boot/photos/

Fleetwood

server.port=9090
server.context-path=/fleetwood

security.oauth2.client.client-id=Fleetwood
security.oauth2.client.client-secret=y471l12D2y55U5558rd2

#authorization server
accessTokenUri=http://localhost:8080/authorization/oauth/token
userAuthorizationUri=http://localhost:8080/authorization/oauth/authorize

#resource server
producerBaseURL=http://localhost:8081/sharewood/api/
sharewoodPhotoBaseURL=http://localhost:8081/sharewood/api/photos

#client temporary directory
tempDir=/home/dominique/Pictures/client/tmp/

#logging pattern for the console
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n


For a version upgraded to SpringBoot 2.0.3 follow this link:

https://github.com/dubersfeld/sharewood-oauth2-upgrade

For a microservice-oriented version follow this link:

https://github.com/dubersfeld/sharewood-reloaded


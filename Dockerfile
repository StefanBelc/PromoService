FROM maven:3.9-amazoncorretto-21 AS build

WORKDIR /build

RUN yum install -y git && yum clean all

COPY PromoBridgeSDK /build/promobridge-sdk


WORKDIR /build/promobridge-sdk
RUN git checkout -f 4.5 \
 && mvn clean install -DskipTests


WORKDIR /build/app
COPY PromoService/pom.xml .

RUN mvn dependency:go-offline

COPY PromoService/src ./src
RUN mvn clean package -DskipTests

FROM amazoncorretto:21-alpine
WORKDIR /app
COPY --from=build /build/app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
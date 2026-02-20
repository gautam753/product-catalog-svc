FROM public.ecr.aws/amazoncorretto/amazoncorretto:17
ARG JAR_FILE=target/product-catalog-svc-1.0.0.jar
COPY ${JAR_FILE} product-catalog-svc.jar
ENV JAVA_OPTS="-Xms512m -Xmx1024m"
ENTRYPOINT ["java","-jar","/product-catalog-svc.jar"]
RUN mkdir -p /logs && chmod 777 /logs
EXPOSE 8081

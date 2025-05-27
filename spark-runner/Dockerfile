FROM bitnami/spark:latest

# Copy your Spark JAR into the image
COPY spark-scala-project-assembly-0.1.0-SNAPSHOT.jar /opt/spark/app.jar

# Default command to run the Spark job
CMD ["/opt/bitnami/spark/bin/spark-submit", "--master", "local", "--class", "etlJob", "/opt/spark/app.jar"]
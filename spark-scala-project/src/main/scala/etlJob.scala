import org.apache.spark.sql.SparkSession
import java.time.LocalDateTime
import java.sql.{Connection, DriverManager, PreparedStatement, Timestamp}
import org.apache.spark.sql.functions._

object etlJob {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Simple Spark App")
      .master("local[*]")
      .getOrCreate()
    import spark.implicits._

    // JDBC connection properties metadata DB
    val jdbcUrl = "jdbc:postgresql://host.docker.internal:5434/metadata_db"
    val connectionProperties = new java.util.Properties()
    connectionProperties.setProperty("user", "metadata_user")
    connectionProperties.setProperty("password", "metadata_pass")
    connectionProperties.setProperty("driver", "org.postgresql.Driver")

    // JDBC connection properties prod DB
    val jdbcUrlProd = "jdbc:postgresql://host.docker.internal:5433/analytics_db"
    val connectionPropertiesProd = new java.util.Properties()
    connectionPropertiesProd.setProperty("user", "prod_user")
    connectionPropertiesProd.setProperty("password", "prod_pass")
    connectionPropertiesProd.setProperty("driver", "org.postgresql.Driver")

    val connection = DriverManager.getConnection(
      jdbcUrl,
      connectionProperties.getProperty("user"),
      connectionProperties.getProperty("password")
    )

    val incomingFilesDf = spark.read.jdbc(jdbcUrl, "incoming_files", connectionProperties)
      .filter("status = 'Pending'")

    val filePaths = incomingFilesDf.select("file_path").collect().map(_.getString(0))

    filePaths.foreach { path =>
      val startTime = Timestamp.valueOf(LocalDateTime.now())
      var status = "Completed"
      var statusMessage: String = null
      try {
        val df = spark.read.option("header", "true").csv(path)

        // 1. Data cleansing: Remove rows with invalid or missing TotalCharges
        // TotalCharges can sometimes be blank strings, convert to Double, filter invalids
        val cleanedDf = df.filter($"TotalCharges".isNotNull && length(trim($"TotalCharges")) > 0)
          .withColumn("TotalChargesDouble", $"TotalCharges".cast("double"))
          .filter($"TotalChargesDouble".isNotNull)

        // 2. Filter long-term customers (Tenure > 12 months)
        val longTermDf = cleanedDf.filter($"Tenure" > 12)

        // 3. Add ChurnFlag column: 1 if Churn = "Yes", else 0
        val flaggedDf = longTermDf.withColumn("ChurnFlag", when(lower($"Churn") === "yes", 1).otherwise(0))

        // 4. Aggregate churn rate by ContractType and InternetService
        val churnSummary = flaggedDf.groupBy("ContractType", "InternetService")
          .agg(
            count("CustomerID").as("NumCustomers"),
            avg("ChurnFlag").as("ChurnRate")
          )
          .orderBy("ContractType", "InternetService")

        // 5. Write long term customers to DB table 'long_term_customers'
        flaggedDf.drop("TotalCharges").withColumnRenamed("TotalChargesDouble", "TotalCharges").withColumn("source_file", lit(path))
          .write
          .mode("append")
          .jdbc(jdbcUrlProd, "long_term_customers", connectionPropertiesProd)

        // 6. Write churn summary to DB table 'churn_summary'
        churnSummary.withColumn("source_file", lit(path))
          .write
          .mode("append")
          .jdbc(jdbcUrlProd, "churn_summary", connectionPropertiesProd)

      } catch {
        case e: Exception =>
          status = "Failed"
          statusMessage = e.getMessage
      }

      val endTime = Timestamp.valueOf(LocalDateTime.now())
      val updatedAt = endTime

      val sql =
        """
          |UPDATE incoming_files
          |SET processing_start_time = ?, processing_end_time = ?, status = ?, status_message = ?, updated_at = ?
          |WHERE file_path = ?
          |""".stripMargin

      val statement: PreparedStatement = connection.prepareStatement(sql)
      statement.setTimestamp(1, startTime)
      statement.setTimestamp(2, endTime)
      statement.setString(3, status)
      statement.setString(4, statusMessage)
      statement.setTimestamp(5, updatedAt)
      statement.setString(6, path)

      statement.executeUpdate()
      statement.close()
    }

    spark.stop()
  }
}
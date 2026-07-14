import sbtassembly.AssemblyPlugin.autoImport._
import sbtassembly.{MergeStrategy, PathList}

ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.12.18"

lazy val root = (project in file("."))
  .settings(
    name := "spark-scala-project",

    libraryDependencies ++= Seq(

      // Spark (provided by the Spark runtime)
      "org.apache.spark" %% "spark-core" % "3.5.5" % "provided",
      "org.apache.spark" %% "spark-sql"  % "3.5.5" % "provided",

      // PostgreSQL
      "org.postgresql" % "postgresql" % "42.7.7",

      // Iceberg runtime
      "org.apache.iceberg" % "iceberg-spark-runtime-3.5_2.12" % "1.9.2",

      // AWS bundle for S3FileIO
      "org.apache.iceberg" % "iceberg-aws-bundle" % "1.9.2",

      // Hadoop S3A (optional but useful if you also use s3a:// elsewhere)
      "org.apache.hadoop" % "hadoop-aws" % "3.3.6"
    ),

    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", "MANIFEST.MF") =>
        MergeStrategy.discard

      case PathList("META-INF", "INDEX.LIST") =>
        MergeStrategy.discard

      case PathList("META-INF", "DEPENDENCIES") =>
        MergeStrategy.discard

      case PathList("META-INF", "LICENSE") =>
        MergeStrategy.discard

      case PathList("META-INF", "NOTICE") =>
        MergeStrategy.discard

      case PathList("META-INF", "services", _ @ _*) =>
        MergeStrategy.concat

      case "reference.conf" =>
        MergeStrategy.concat

      case "application.conf" =>
        MergeStrategy.concat

      case PathList("META-INF", xs @ _*) =>
        MergeStrategy.discard

      case _ =>
        MergeStrategy.first
    }
  )
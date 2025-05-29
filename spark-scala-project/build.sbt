ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.12.15"

lazy val root = (project in file("."))
  .settings(
    name := "spark-scala-project",
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-core" % "3.4.1" % "provided",
      "org.apache.spark" %% "spark-sql"  % "3.4.1" % "provided",
      "org.postgresql" % "postgresql" % "42.7.1"
    )
  )
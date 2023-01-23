import Dependencies._

ThisBuild / scalaVersion     := "2.13.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "uk.gov.nationalarchives"
ThisBuild / organizationName := "tdr-backend-check-utils"

lazy val root = (project in file("."))
  .settings(
    name := "tdr-backend-checks-utils",
    libraryDependencies ++= Seq(
      awsS3,
      circeCore,
      circeGeneric,
      circeParser,
      mockito % Test,
      scalaTest % Test,
    )
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.

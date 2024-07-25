import sbt._

object Dependencies {
  private val circeVersion = "0.14.9"

  lazy val awsS3 = "software.amazon.awssdk" % "s3" % "2.26.24"
  lazy val circeCore = "io.circe" %% "circe-core" % circeVersion
  lazy val circeParser = "io.circe" %% "circe-parser" % circeVersion
  lazy val circeGeneric = "io.circe" %% "circe-generic" % circeVersion
  lazy val mockito = "org.mockito" %% "mockito-scala" % "1.17.37"
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.19"
}

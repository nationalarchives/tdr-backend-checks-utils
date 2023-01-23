import Dependencies._

ThisBuild / version := (ThisBuild / version).value
ThisBuild / organization     := "uk.gov.nationalarchives"
ThisBuild / organizationName := "National Archives"

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/nationalarchives/tdr-backend-checks-utils"),
    "git@github.com:nationalarchives/tdr-backend-checks-utils.git"
  )
)
developers := List(
  Developer(
    id    = "tna-digital-archiving-jenkins",
    name  = "TNA Digital Archiving",
    email = "digitalpreservation@nationalarchives.gov.uk",
    url   = url("https://github.com/nationalarchives/tdr-backend-checks-utils")
  )
)

ThisBuild / description := "Utility classes and methods to support backend check processing for TDR"
ThisBuild / licenses := List("MIT" -> new URL("https://choosealicense.com/licenses/mit/"))
ThisBuild / homepage := Some(url("https://github.com/nationalarchives/tdr-backend-checks-utils"))

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

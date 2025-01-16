import Dependencies._
import sbtrelease.ReleaseStateTransformations._

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
    id    = "tna-da-bot",
    name  = "TNA Digital Archiving",
    email = "s-GitHubDABot@nationalarchives.gov.uk",
    url   = url("https://github.com/nationalarchives/tdr-backend-checks-utils")
  )
)

ThisBuild / description := "Utility classes and methods to support backend check processing for TDR"
ThisBuild / licenses := List("MIT" -> new URL("https://choosealicense.com/licenses/mit/"))
ThisBuild / homepage := Some(url("https://github.com/nationalarchives/tdr-backend-checks-utils"))

scalaVersion := "2.13.16"

useGpgPinentry := true
publishTo := sonatypePublishToBundle.value
publishMavenStyle := true

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommand("publishSigned"),
  releaseStepCommand("sonatypeBundleRelease"),
  setNextVersion,
  commitNextVersion,
  pushChanges
)

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

organization in ThisBuild := "org.example"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.13.0"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.3" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.1.1" % Test

lazy val `URLShortner` = (project in file("."))
  .aggregate(
    `URLShortner-api`,
    `URLShortner-impl`,
    `URLShortner-stream-api`,
    `URLShortner-stream-impl`
  )

lazy val `URLShortner-api` = (project in file("URLShortner-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `URLShortner-impl` = (project in file("URLShortner-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings)
  .dependsOn(`URLShortner-api`)

lazy val `URLShortner-stream-api` = (project in file("URLShortner-stream-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )
  .dependsOn(`URLShortner-api`)

lazy val `URLShortner-stream-impl` =
  (project in file("URLShortner-stream-impl"))
    .enablePlugins(LagomScala)
    .settings(
      libraryDependencies ++= Seq(
        lagomScaladslTestKit,
        macwire,
        scalaTest
      )
    )
    .dependsOn(`URLShortner-stream-api`, `URLShortner-api`)

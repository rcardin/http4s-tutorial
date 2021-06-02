val Http4sVersion = "1.0.0-M21"
val CirceVersion = "0.14.0-M5"

lazy val root = (project in file("."))
  .settings(
    organization := "in.rcard",
    name := "http4s-tutorial",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.13.4",

    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "io.circe" %% "circe-generic" % CirceVersion
    )
  )

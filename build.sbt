val scala3Version = "3.2.2"

lazy val root = project
  .in(file("."))
  .settings(
    name := "dbmng",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test
  )

lazy val akkaVersion = "2.7.0"
  
// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
// Run in a separate JVM, to make sure sbt waits until all threads have
// finished before returning.
// If you want to keep the application running while executing other
// sbt tasks, consider https://github.com/spray/sbt-revolver/
fork := true

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
  "org.scalatest" % "scalatest_2.11" % "3.0.1"
)

resolvers += ("Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/").withAllowInsecureProtocol(true)
//resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "com.typesafe.play" %% "play-json" % "2.10.0-RC5"

//libraryDependencies += "com.typesafe.akka" %% "akka-cluster" % "2.5.32"
//libraryDependencies += "com.typesafe.akka" %% "akka-cluster" % "2.8.0"
val AkkaVersion = "2.8.0"
libraryDependencies += "com.typesafe.akka" %% "akka-cluster-typed" % AkkaVersion

run  / connectInput in run := true

name := "youi"
organization in ThisBuild := "io.youi"
version in ThisBuild := "0.2.5-SNAPSHOT"
scalaVersion in ThisBuild := "2.12.1"
crossScalaVersions in ThisBuild := List("2.12.1", "2.11.8")
sbtVersion in ThisBuild := "0.13.13"
resolvers in ThisBuild += Resolver.sonatypeRepo("releases")
scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation")

lazy val root = project.in(file("."))
  .aggregate(
    coreJS, coreJVM, stream, communicationJS, communicationJVM, dom, client, server, serverUndertow, ui, appJS, appJVM,
    templateJS, templateJVM, exampleJS, exampleJVM
  )
  .settings(
    resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases",
    publish := {},
    publishLocal := {}
  )

lazy val core = crossProject.in(file("core"))
  .settings(
    name := "youi-core",
    resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases",
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "com.outr" %%% "scribe" % "1.4.1",
      "com.outr" %%% "reactify" % "1.4.7",
      "org.scalactic" %%% "scalactic" % "3.0.1",
      "org.scalatest" %%% "scalatest" % "3.0.1" % "test"
    )
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % "2.4.17"
    )
  )
  .jsSettings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.1"
    )
  )

lazy val coreJS = core.js
lazy val coreJVM = core.jvm

lazy val stream = project.in(file("stream"))
  .settings(
    name := "youi-stream",
    libraryDependencies ++= Seq(
      "org.powerscala" %% "powerscala-io" % "2.0.5"
    )
  )

lazy val dom = project.in(file("dom"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "youi-dom"
  )
  .dependsOn(coreJS)
  .dependsOn(stream % "compile")

lazy val client = project.in(file("client"))
  .settings(
    name := "youi-client",
    libraryDependencies ++= Seq(
      "org.apache.httpcomponents" % "httpasyncclient" % "4.1.3",
      "org.powerscala" %% "powerscala-io" % "2.0.5"
    ),
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % "0.7.0")
  )
  .dependsOn(coreJVM)

lazy val server = project.in(file("server"))
  .settings(
    name := "youi-server",
    libraryDependencies ++= Seq(
      "net.sf.uadetector" % "uadetector-resources" % "2014.10",
      "org.scalactic" %% "scalactic" % "3.0.1",
      "org.scalatest" %% "scalatest" % "3.0.1" % "test"
    )
  )
  .dependsOn(coreJVM, stream)

lazy val serverUndertow = project.in(file("serverUndertow"))
  .settings(
    name := "youi-server-undertow",
    libraryDependencies ++= Seq(
      "io.undertow" % "undertow-core" % "1.4.11.Final",
      "org.scalactic" %% "scalactic" % "3.0.1",
      "org.scalatest" %% "scalatest" % "3.0.1" % "test"
    )
  )
  .dependsOn(server)

lazy val communication = crossProject.in(file("communication"))
  .settings(
    name := "youi-communication",
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "upickle" % "0.4.4",
      "org.scalactic" %%% "scalactic" % "3.0.1",
      "org.scalatest" %%% "scalatest" % "3.0.1" % "test"
    )
  )
  .dependsOn(core)

lazy val communicationJS = communication.js
lazy val communicationJVM = communication.jvm.dependsOn(server)

lazy val ui = project.in(file("ui"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "youi-ui",
    libraryDependencies ++= Seq(
      "com.outr" %%% "scalajs-pixijs" % "4.4.3"
    )
  )
  .dependsOn(coreJS, dom)

lazy val app = crossProject.in(file("app"))
  .settings(
    name := "youi-app"
  )
  .dependsOn(core, communication)

lazy val appJS = app.js.dependsOn(ui)
lazy val appJVM = app.jvm

lazy val template = crossProject.in(file("template"))
  .settings(
    name := "youi-template"
  )
  .jsSettings(
    crossTarget in fastOptJS := baseDirectory.value / ".." / "jvm" / "src" / "main" / "resources" / "app",
    crossTarget in fullOptJS := baseDirectory.value / ".." / "jvm" / "src" / "main" / "resources" / "app"
  )
  .jvmSettings(
    fork := true,
    libraryDependencies ++= Seq(
      "org.powerscala" %% "powerscala-io" % "2.0.5"
    ),
    assemblyJarName in assembly := "youi-template.jar"
  )
  .dependsOn(app)

lazy val templateJS = template.js.dependsOn(ui)
lazy val templateJVM = template.jvm.dependsOn(serverUndertow)

lazy val example = crossProject.in(file("example"))
  .settings(
    name := "youi-server-example"
  )
  .jsSettings(
    crossTarget in fastOptJS := baseDirectory.value / ".." / "jvm" / "src" / "main" / "resources" / "app",
    crossTarget in fullOptJS := baseDirectory.value / ".." / "jvm" / "src" / "main" / "resources" / "app"
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "org.scala-lang.modules" %% "scala-xml" % "1.0.6"
    )
  )
  .dependsOn(app, template)

lazy val exampleJS = example.js.dependsOn(ui)
lazy val exampleJVM = example.jvm.dependsOn(serverUndertow)
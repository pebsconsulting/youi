import sbt._
import Keys._

import com.github.siasia.WebPlugin._
import com.github.siasia.PluginKeys._

object HyperScalaBuild extends Build {
  // ~;container:start; container:reload /

  val powerScalaConvert = "org.powerscala" %% "powerscala-convert" % "1.2-SNAPSHOT"
  val powerScalaReflect = "org.powerscala" %% "powerscala-reflect" % "1.2-SNAPSHOT"
  val powerScalaHierarchy = "org.powerscala" %% "powerscala-hierarchy" % "1.2-SNAPSHOT"
  val powerScalaProperty = "org.powerscala" %% "powerscala-property" % "1.2-SNAPSHOT"
  val jdom = "org.jdom" % "jdom" % "2.0.2"

  val htmlcleaner = "net.sourceforge.htmlcleaner" % "htmlcleaner" % "2.2"

  val specs2 = "org.specs2" %% "specs2" % "1.11" % "test"

  val jettyVersion = "8.1.7.v20120910"
  val jettyWebapp = "org.eclipse.jetty" % "jetty-webapp" % "8.1.7.v20120910" % "container,compile"
  val servlet = "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container,compile" artifacts Artifact("javax.servlet", "jar", "jar")

  val commonsFileUpload = "commons-fileupload" % "commons-fileupload" % "1.2.2"
  val commonsIO = "commons-io" % "commons-io" % "1.3.2"

  val atmosphereRuntime = "org.atmosphere" % "atmosphere-runtime" % "1.0.2"
  val atmosphereJQuery = "org.atmosphere" % "atmosphere-jquery" % "1.0.2"
  val annotationDetector = "eu.infomas" % "annotation-detector" % "3.0.0"

  val servletApi = "javax.servlet" % "javax.servlet-api" % "3.0.1" % "compile"

  val baseSettings = Defaults.defaultSettings ++ Seq(
    version := "0.3-SNAPSHOT",
    organization := "org.hyperscala",
    scalaVersion := "2.9.2",
    libraryDependencies ++= Seq(
      powerScalaConvert,
      powerScalaReflect,
      powerScalaHierarchy,
      powerScalaProperty,
      jdom,
      htmlcleaner,
      specs2
    ),
    scalacOptions ++= Seq("-unchecked", "-deprecation"),
    resolvers ++= Seq("Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"),
    publishTo <<= version {
      (v: String) =>
        val nexus = "https://oss.sonatype.org/"
        if (v.trim.endsWith("SNAPSHOT"))
          Some("snapshots" at nexus + "content/repositories/snapshots")
        else
          Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    publishArtifact in Test := false,
    pomExtra := (
      <url>http://hyperscala.org</url>
        <licenses>
          <license>
            <name>BSD-style</name>
            <url>http://www.opensource.org/licenses/bsd-license.php</url>
            <distribution>repo</distribution>
          </license>
        </licenses>
        <scm>
          <developerConnection>scm:https://github.com/darkfrog26/hyperscala.git</developerConnection>
          <connection>scm:https://github.com/darkfrog26/hyperscala.git</connection>
          <url>https://github.com/darkfrog26/hyperscala</url>
        </scm>
        <developers>
          <developer>
            <id>darkfrog</id>
            <name>Matt Hicks</name>
            <url>http://matthicks.com</url>
          </developer>
        </developers>)
  )

  private def createSettings(_name: String) = baseSettings ++ Seq(name := _name)

  lazy val root = Project("root", file("."), settings = createSettings("hyperscala-root"))
    .settings(publishArtifact in Compile := false, publishArtifact in Test := false)
    .aggregate(core, html, javascript, web, bean, ui, generator, examples, site)
  lazy val core = Project("core", file("core"), settings = createSettings("hyperscala-core"))
  lazy val html = Project("html", file("html"), settings = createSettings("hyperscala-html"))
    .dependsOn(core)
  lazy val javascript = Project("javascript", file("javascript"), settings = createSettings("hyperscala-javascript"))
    .dependsOn(html)
  lazy val web = Project("web", file("web"), settings = createSettings("hyperscala-web"))
    .dependsOn(html)
    .settings(libraryDependencies ++= Seq(servletApi,
                                          commonsFileUpload,
                                          commonsIO,
                                          atmosphereRuntime,
                                          atmosphereJQuery,
                                          annotationDetector))
  lazy val bean = Project("bean", file("bean"), settings = createSettings("hyperscala-bean"))
    .dependsOn(web)
  lazy val ui = Project("ui", file("ui"), settings = createSettings("hyperscala-ui"))
    .dependsOn(bean)
  lazy val generator = Project("generator", file("generator"), settings = createSettings("hyperscala-generator"))
    .settings(publishArtifact := false)

  // Examples and Site
  lazy val examples = Project("examples", file("examples"), settings = createSettings("hyperscala-examples"))
    .dependsOn(web, bean, ui)
  lazy val site = Project("site", file("site"), settings = createSettings("hyperscala-site"))
    .dependsOn(examples)
    .settings(libraryDependencies ++= Seq(jettyWebapp, servlet))
    .settings(webSettings: _*)
    .settings(port in container.Configuration := 8000)
}
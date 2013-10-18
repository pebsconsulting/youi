import sbt._

object Dependencies {
  val powerScalaVersion = "1.6.3-SNAPSHOT"
  val outrNetVersion = "1.0.0-SNAPSHOT"

  val powerScalaReflect = "org.powerscala" %% "powerscala-reflect" % powerScalaVersion
  val powerScalaHierarchy = "org.powerscala" %% "powerscala-hierarchy" % powerScalaVersion
  val powerScalaProperty = "org.powerscala" %% "powerscala-property" % powerScalaVersion

  val outrNetCore = "com.outr.net" %% "outrnet-core" % outrNetVersion
  val outrNetServlet = "com.outr.net" %% "outrnet-servlet" % outrNetVersion
  val outrNetCommunicatorClient = "com.outr.net" %% "outrnet-communicator-client" % outrNetVersion
  val outrNetCommunicatorServer = "com.outr.net" %% "outrnet-communicator-server" % outrNetVersion

  val jdom = "org.jdom" % "jdom2" % "2.0.5"
  val jaxen = "jaxen" % "jaxen" % "1.1.4"

  val htmlcleaner = "net.sourceforge.htmlcleaner" % "htmlcleaner" % "2.6.1"
  val akkaActors = "com.typesafe.akka" % "akka-actor_2.10" % "2.2.1"
  val uaDetector = "net.sf.uadetector" % "uadetector-resources" % "2013.09"

  val scalaTest = "org.scalatest" % "scalatest_2.10" % "2.0.RC2" % "test"
}
import sbt._

class QbertProjectDefinition(info: ProjectInfo) extends DefaultProject(info) {
  val jbossRepository = "JBoss Public Maven 2 Repository" at "http://repository.jboss.org/nexus/content/groups/public/"

  val netty = "org.jboss.netty" % "netty" % "3.2.2.Final"
  val slf4japi = "org.slf4j" % "slf4j-api" % "1.6.1"
  val slf4jsimple = "org.slf4j" % "slf4j-simple" % "1.6.1"

  val specs = "org.scala-tools.testing" % "specs_2.8.0" % "1.6.5"
  val mockito = "org.mockito" % "mockito-core" % "1.8.5"
}

import sbt._

class QbertProjectDefinition(info: ProjectInfo) extends DefaultProject(info) {
  val jbossRepository = "JBoss Public Maven 2 Repository" at "http://repository.jboss.org/nexus/content/groups/public/"
  val akkaRespository = "Akka Maven2 Repository" at "http://www.scalablesolutions.se/akka/repository/"
  val multiverseRepository = "Multiverse Maven2 Repository" at "http://multiverse.googlecode.com/svn/maven-repository/releases/"
  val guiceyRepository = "GuiceyFruit Maven2 Repository" at "http://guiceyfruit.googlecode.com/svn/repo/releases/"

  val amqpClient = "com.rabbitmq" % "amqp-client" % "2.2.0"
  val commonsIO = "commons-io" % "commons-io" % "2.0"
  val netty = "org.jboss.netty" % "netty" % "3.2.2.Final"
  val slf4japi = "org.slf4j" % "slf4j-api" % "1.6.1"
  val slf4jsimple = "org.slf4j" % "slf4j-simple" % "1.6.1"
  val akka = "se.scalablesolutions.akka" % "akka-actor" % "1.0-RC1"

  val specs = "org.scala-tools.testing" % "specs_2.8.0" % "1.6.5"
  val mockito = "org.mockito" % "mockito-core" % "1.8.5"
}

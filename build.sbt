name := "akkaHttpExample"

version := "1.0"

// ============== Assembly =============================================

assemblyJarName in assembly := "akkaHttpExample.jar"

mainClass in assembly := Some("net.composmin.akkahttp.Main")


// ============== Docker ===============================================

enablePlugins(DockerPlugin)
// http://www.scala-sbt.org/sbt-native-packager/formats/docker.html#busybox-ash-support
enablePlugins(AshScriptPlugin)

maintainer in Docker := "Compos Min"

packageSummary in Docker := "Akka HTTP stream example"

packageDescription := "Akka HTTP stream example"

packageName in Docker := "akka-http"

dockerBaseImage := "delitescere/java"

dockerExposedPorts := Seq(8080)

dockerRepository := Some("composmin")



Common.settings


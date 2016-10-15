name := "akkaHttpExample"

version := "1.1"

// ============== Assembly =============================================

assemblyJarName in assembly := "akkaHttpExample.jar"

mainClass in assembly := Some("net.composmin.akkahttp.Main")


// ============== Docker ===============================================
// NOTE: The produced docker image does not use the assembly produced above.
// they are indendependent build products with Docker packaging all runtime
// dependencies and adding them to the class path.

enablePlugins(DockerPlugin)
// http://www.scala-sbt.org/sbt-native-packager/formats/docker.html#busybox-ash-support
enablePlugins(AshScriptPlugin)

maintainer in Docker := "Compos Min"

packageSummary in Docker := "Akka HTTP stream example"

packageDescription := "Akka HTTP stream example"

packageName in Docker := "akka-http"

version in Docker := "latest"

dockerBaseImage := "delitescere/java"

dockerExposedPorts := Seq(8080)

dockerRepository := Some("composmin")



Common.settings


package net.composmin.akkahttp

object EnvDumper {

  def dumpEnv() : String = {
    sys.env.mkString("\n")
  }
}

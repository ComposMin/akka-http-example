package net.composmin.akkahttp

import org.scalatest.{BeforeAndAfterEach, FunSuite}

/**
  * Created by cfegan on 5/06/2016.
  */
class EnvDumperTest extends FunSuite with BeforeAndAfterEach {

  override def beforeEach() {

  }

  override def afterEach() {

  }

  test("testDumpEnv") {
    println(EnvDumper.dumpEnv())
  }

}

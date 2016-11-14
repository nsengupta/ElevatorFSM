package common

import akka.testkit.TestKit
import org.scalatest.{BeforeAndAfterAll, Suite}

/**
  * Created by nirmalya on 27/10/16.
  */
trait StopSystemAfterAll extends BeforeAndAfterAll {
  this: TestKit with Suite =>
  //  This trait can only be used if it’s mixedin with a test that uses the TestKit.
  override protected def afterAll() {
    super.afterAll()
    system.terminate() // Shuts down the system

  }
}

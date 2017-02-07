package com.markit

import org.scalatest.FunSuite

class MarkItTest extends FunSuite{

  test("test dailyPrices") {
    assert(MarkITApp.dailyPrices("GOOG").length > 0)
  }

  test("test returns") {
    assert(MarkITApp.returns("GOOG").length > 0)
  }

  test("test meanReturn") {
    assert(MarkITApp.meanReturn("GOOG") > 0)
  }

}

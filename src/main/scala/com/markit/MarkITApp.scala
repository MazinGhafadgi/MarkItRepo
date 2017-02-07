package com.markit

import java.time.LocalDate

import scalaj.http.Http
import scala.io.Source


object MarkITApp {
  val exp = """^\s*([\d\d\d\d-\d\d-\d\d]+)\s*\,\s*([^,]+)\s*\,\s*([^,]+)\s*\,\s*([^,]+)\s*\,\s*([^,]+)\s*\,\s*([^,]+)\s*\,\s*([^,]+)\s*""".r

  def main(array : Array[String]) : Unit = {
    val googleDailyPrices = dailyPrices("GOOG")
    val googleDailyReturns = returns("GOOG")
    println(pricesURL(LocalDate.of(2017, 2, 2), "GOOG"))
    println(googleDailyPrices.length)
    println(googleDailyReturns.length)
    println(meanReturn("GOOG"))
  }

  //The following URL provides one year historical stock price quotes from Yahoo finance
  def pricesURL(businessDate : java.time.LocalDate, ticker: String) : String = {
    val lastYear = businessDate.minusYears(1)
    val url =f"http://real-chart.finance.yahoo.com/table.csv?s=$ticker&a=${lastYear.getMonthValue}&b=${lastYear.getDayOfMonth}&c=${lastYear.getYear}&d=${businessDate.getMonthValue}&e=${businessDate.getDayOfMonth}&f=${businessDate.getYear}&g=d&ignore=.csv"
    Http(url).asString.body
  }

  /*
     Structure of the payload
    Date,Open,High,Low,Close,Volume,Adj Close
   */
  def dailyPrices(ticker: String) : List[Double] = {
    val result = pricesURL(LocalDate.of(2017, 2, 2), ticker)
    val prices =  for {
    //drop the first line of the payload (Date,Open,High,Low,Close,Volume,Adj Close)
      line <- Source.fromString(result).getLines().drop(1)
      values = dailyPrice(line).toDouble
    } yield values
    prices.toList
  }

  private def dailyPrice(line : String) : String = {
    line match {
      case exp(date,open,high,low,close,volume,adjClose) => adjClose
      case _ => throw new MatchError(s" line does not match the expected format (Date,Open,High,Low,Close,Volume,Adj Close) $line")
    }
  }

  /* 2- daily returns, where return = ( Price_Today – Price_Yesterday)/Price_Yesterday */
  def returns(ticker:String) : Seq[Double] = {
    val prices = dailyPrices(ticker).toArray
    var seqs = scala.collection.mutable.Set[Double]()
    for (i <- 0 to prices.length) {
      if(i + 1 < prices.length)
        seqs += (prices(i) - prices(i + 1))/prices(i + 1)
    }
    seqs.toSeq
  }

  def meanReturn(ticker:String): Double = {
    val prices = dailyPrices(ticker)
    prices.foldLeft(0.0){(a, b) => a + b} / prices.length
  }

}

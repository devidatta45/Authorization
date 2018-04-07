package com.sony.utils

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

case class FutureOnEither[L, R](futureEither: Future[Either[L, R]]) {

  def map[B](f: R => B): FutureOnEither[L, B] = FutureOnEither {
    futureEither.map {
      _.map(f)
    }
  }

  def flatMap[B](f: R => FutureOnEither[L, B]): FutureOnEither[L, B] = FutureOnEither {
    futureEither.flatMap {
      case Left(failure) => Future.successful(Left(failure))
      case Right(a) => f(a).futureEither
    }
  }
}

trait TestCheck {
  def getResult(number: Int): Future[Either[Exception, String]]
}

object TestCheckImpl extends TestCheck {
  override def getResult(number: Int): Future[Either[Exception, String]] = {
    if (number == 3) {
      Future(Left(new Exception("Wrong Number")))
    }
    else {
      Future(Right("Correct Number"))
    }
  }
}

object CheckMethod extends App {
  val res = for {
    result <- FutureOnEither(TestCheckImpl.getResult(3))
    thenResult = result + " added newly"
  } yield thenResult
  val finalResult = res.futureEither
  val result = Await.result(finalResult, 5.seconds)
  if (result.isLeft) {
    result.left.get.printStackTrace()
  } else {
    println(result.right.get)
  }
}
package com.sony.utils

import java.util.Scanner

import scala.concurrent.duration._

/**
  * Created by DDM
  */

trait BaseColumnConstants {
  val ID = "_id"
  val ISREMOVED = "isRemoved"
}

object BaseColumnConstants extends BaseColumnConstants

object Constants{
  val TIMEOUT = 5.seconds
  val CLIENT_TABLE = "CLIENT"
  val RUNTIME_TABLE = "RUNTIMECOL"
  val PERMISSION_TABLE = "PERMISSION"
  val ROLE_TABLE = "ROLE"
  val USER_GROUP_TABLE = "USERGROUP"
  val USER_TABLE = "USER"
}
object OperatorConstants {
  val IN = "$in"
}

object ClientColumnConstants extends BaseColumnConstants{
  val CID = "CID"
  val SECRET_KEY = "SECRETKEY"
  val REDIRECT_URL = "REDIRECTURL"
  val NAME = "NAME"
}

object PermissionColumnConstants extends BaseColumnConstants{
  val CLIENT_ID = "CLIENTID"
}

object RoleColumnConstants extends BaseColumnConstants{
  val PERMISSIONS = "PERMISSIONS"
}

object UserGroupColumnConstants extends BaseColumnConstants{
  val ROLES = "ROLES"
  val USERS = "USERS"
}
object UserColumnConstants extends BaseColumnConstants{
  val EMAIL = "EMAIL"
  val FIRST_NAME = "FIRSTNAME"
  val LAST_NAME = "LASTNAME"
  val DIRECT_ROLES = "DIRECTROLES"
}

object TestApp {

  def main(args: Array[String]): Unit = {
    val scan = new Scanner(System.in)
    val i = scan.nextInt()
    1 to i foreach { test =>
      val files = scan.nextInt()
      val speed = scan.nextInt()
      val list = 1 to files map {
        _ => scan.nextInt()
      } toList

      download(list, files, speed)
    }
  }


  def download(list: List[Int], length: Int, speed: Int) = {
    val sortedList = list.sorted
    val roundedSpeed = Math.round(speed / length)
    val result = changeList(sortedList, 0, speed, roundedSpeed)
    println(result)
  }

  def changeList(list: List[Int], timeTaken: Int, speed: Int, roundedSpeed: Int): Int = {
    if (list.isEmpty) {
      timeTaken
    }
    else {
      println(roundedSpeed)
      val addedTime = Math.round(list.head.toDouble / roundedSpeed.toDouble)
      val newTime = if (addedTime == 0) addedTime + 1 else addedTime
      println(newTime)
      val nextList = list.map { l => l - list.head }.filterNot(_ == 0)
      println(nextList)
      val nextSpeed = if (nextList.isEmpty) 0 else Math.round(speed / nextList.length)
      changeList(nextList, timeTaken + newTime.toInt, speed, nextSpeed)
    }
  }
}
object dsd extends App{
  println(Math.round(7.89))
}
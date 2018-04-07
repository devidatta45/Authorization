package com.sony.utils

import com.typesafe.config.ConfigFactory
import reactivemongo.bson.BSONObjectID

/**
  * Created by DDM
  */

// Db Constants
object MongoConstant {
  val config = ConfigFactory.load
  val server = config.getString("mongo.url")
  val dbName = config.getString("mongo.dbname")
}

//generic classes and objects
case object FindAllCommand

case class UserUpdateCommand[T](id: BSONObjectID, user: T)

case class FindByIdCommand(id: BSONObjectID)

case class DeleteByIdCommand(id: BSONObjectID)

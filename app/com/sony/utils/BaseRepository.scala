package com.sony.utils

import com.sony.utils.BaseColumnConstants._
import reactivemongo.api._
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.WriteConcern
import reactivemongo.bson._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try


/**
  * Created by DDM
  */
trait BaseEntity {
  val _id: String
  val isRemoved: Boolean = false
}

trait BaseRepository[T <: BaseEntity] {

  import MongoConstant._

  import ExecutionContext.Implicits.global

  def table: String

  val driver = new MongoDriver
  //val credentials = List(Authenticate(dbName, userName, password))
  val connection = Try {
    driver.connection(List(server), options = MongoConnectionOptions(
      readPreference = ReadPreference.primary,
      writeConcern = WriteConcern.Default,
      authMode = ScramSha1Authentication
    ))
  }
  val futureConnection = Future.fromTry(connection)
  val db = futureConnection.flatMap(_.database(dbName, FailoverStrategy(100.milliseconds, 20, { n => n })))
  val collection: Future[BSONCollection] = db.map(_.collection[BSONCollection](table))

  //All the generic methods for db manipulation required for any kind of operations for any data Model

  def getHandler[A]: Cursor.ErrorHandler[List[A]] = { (last: List[A], error: Throwable) =>
    error.printStackTrace()
    if (last.isEmpty) { // continue, skip error if no previous value
      Cursor.Cont(last)
    } else Cursor.Fail(error)
  }

  def findAll()(implicit reader: BSONDocumentReader[T]): Future[List[T]] = {
    val query = BSONDocument()
    filterQuery(query)
  }

  def save(t: T)(implicit writer: BSONDocumentWriter[T], reader: BSONDocumentReader[T]): Future[Boolean] = {
    for {
      document <- collection.flatMap(_.insert(t).map(x => x.ok))
    } yield document
  }

  def bulkSave(t: List[T])(implicit writer: BSONDocumentWriter[T], reader: BSONDocumentReader[T]): Future[Boolean] = {
    for {
      document <- collection.flatMap(x => x.insert[T](ordered = false).many(t).map(x => x.ok))
    } yield document
  }

  def findById(id: BSONObjectID)(implicit reader: BSONDocumentReader[T]): Future[List[T]] = {
    val query = BSONDocument(ID -> id)
    filterQuery(query)
  }

  def filterQuery(document: BSONDocument)(implicit reader: BSONDocumentReader[T]): Future[List[T]] = {
    val newDocument = document ++ BSONDocument(ISREMOVED -> false)
    collection.flatMap(_.find(newDocument).cursor[T]().collect[List](-1, getHandler[T]))
  }


  def updateById(id: BSONObjectID, document: BSONDocument)(implicit writer: BSONDocumentWriter[T], reader: BSONDocumentReader[T]): Future[List[T]] = {
    val selector = BSONDocument(ID -> id, ISREMOVED -> false)
    for {
      updateDocument <- collection.flatMap(_.update(selector, document).map(_.n))
      updatedUser <- findById(id)
    } yield updatedUser
  }

  def updateMultiple(id: BSONObjectID, document: BSONDocument)(implicit writer: BSONDocumentWriter[T], reader: BSONDocumentReader[T]): Future[List[T]] = {
    val selector = BSONDocument(ID -> id, ISREMOVED -> false)
    for {
      updateDocument <- collection.flatMap(_.update(selector, document, multi = true).map(_.n))
      updatedUser <- findById(id)
    } yield updatedUser
  }

  def deleteById(id: BSONObjectID)(implicit writer: BSONDocumentWriter[T]): Future[Int] = {
    val selector = BSONDocument(ID -> id, ISREMOVED -> false)
    val modifier = BSONDocument("$set" -> BSONDocument(ISREMOVED -> true))
    for {
      document <- collection.flatMap(_.update(selector, modifier).map(_.n))
    } yield document
  }

  def deleteByIds(ids: List[BSONObjectID])(implicit writer: BSONDocumentWriter[T]): Future[Int] = {
    val selector = BSONDocument(ID -> BSONDocument("$in" -> ids))
    val modifier = BSONDocument("$set" -> BSONDocument(ISREMOVED -> true))
    for {
      document <- collection.flatMap(_.update(selector, modifier, multi = true).map(_.n))
    } yield document
  }
}
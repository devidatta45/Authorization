package com.sony.controllers

import javax.inject.{Inject, Singleton}

import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import com.sony.models.User
import com.sony.repository.{ClientPermission, UserPermission, UserPermissionWithCache}
import com.sony.services.UserActor
import com.sony.utils._
import com.sony.utils.JsonImplicits._
import play.api.mvc.{AbstractController, ControllerComponents}
import reactivemongo.bson.BSONObjectID

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

/**
  * Created by DDM
  */

@Singleton
class UserController @Inject()(creator: ActorCreator, cc: ControllerComponents) extends AbstractController(cc) {
  implicit val timeout: Timeout = 5.seconds
  val userActor = creator.createActorRef(Props(classOf[UserActor]), "UserActor")

  def showPermissions(userId: String) = Action.async { request =>
    val queryParams = request.queryString
    val clientIds: Either[Exception, Seq[String]] = if (queryParams.contains("client_ids")) Right(queryParams("client_ids"))
    else Left(new Exception("Parameters missing"))
    if (clientIds.isLeft) {
      Future {
        InternalServerError(clientIds.left.get.getMessage)
      }
    }
    else {
      val userPermission = UserPermission(userId, clientIds.right.get.toList)
      val result = ask(userActor, userPermission).mapTo[Future[List[ClientPermission]]]
      val actualResult = result.flatMap(identity)
      actualResult.map { act =>
        Ok(toJson(act))
      }
    }
  }

  def showPermissionCache(userId: String) = Action.async { request =>
    val queryParams = request.queryString
    val clientIds = if (queryParams.contains("client_ids")) queryParams("client_ids") else
      throw new Exception("Parameters missing")
    val userPermission = UserPermissionWithCache(userId, clientIds.toList)
    val result = ask(userActor, userPermission).mapTo[Future[List[ClientPermission]]]
    val actualResult = result.flatMap(identity)
    actualResult.map { act =>
      Ok(toJson(act))
    }
  }

  val saveUser = Action.async { request =>
    val result = request.body.asJson.get
    val json = result.toString()
    val user = extractEntity[User](json)
    val finalResult = ask(userActor, user).mapTo[Future[Boolean]]
    val actualResult = finalResult.flatMap(identity)
    actualResult.map { act =>
      if (act) Ok("Inserted") else InternalServerError("Not Inserted")
    }
  }

  def getUserById(id: String) = Action.async { request =>
    val result = ask(userActor, FindByIdCommand(BSONObjectID.parse(id).get)).mapTo[Future[List[User]]]
    val actualResult = result.flatMap(identity)
    actualResult.map { act =>
      Ok(toJson(act.head))
    }
  }

  def updateUser(id: String) = Action.async { request =>
    val result = request.body.asJson.get
    val json = result.toString()
    val user = extractEntity[User](json)
    val command = UserUpdateCommand[User](BSONObjectID.parse(id).get, user)
    val finalResult = ask(userActor, command).mapTo[Future[List[User]]]
    val actualResult = finalResult.flatMap(identity)
    actualResult.map { act =>
      if (act.nonEmpty) Ok(toJson(act.head)) else InternalServerError("Not Updated")
    }
  }

  def deleteUserById(id: String) = Action.async { request =>
    val result = ask(userActor, DeleteByIdCommand(BSONObjectID.parse(id).get)).mapTo[Future[Int]]
    val actualResult = result.flatMap(identity)
    actualResult.map { act =>
      if (act > 0) Ok("Deleted") else Ok("Not Deleted")
    }
  }

  val getAllUser = Action.async { request =>
    val result = ask(userActor, FindAllCommand).mapTo[Future[List[User]]]
    val actualResult = result.flatMap(identity)
    actualResult.map { act =>
      Ok(toJson(act))
    }
  }
}
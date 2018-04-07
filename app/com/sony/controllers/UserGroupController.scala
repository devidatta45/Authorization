package com.sony.controllers

import javax.inject.{Inject, Singleton}

import akka.actor.Props
import akka.util.Timeout
import com.sony.services.UserGroupActor
import com.sony.utils._
import com.sony.utils.JsonImplicits._
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import akka.pattern.ask
import com.sony.models.{User, UserGroup}
import reactivemongo.bson.BSONObjectID

@Singleton
class UserGroupController @Inject()(creator: ActorCreator, cc: ControllerComponents) extends AbstractController(cc) {
  implicit val timeout: Timeout = 5.seconds
  val userGroupActor = creator.createActorRef(Props(classOf[UserGroupActor]), "UserGroupActor")

  def getUsersByUserGroup(uid: String) = Action.async { request =>
    val result = ask(userGroupActor, uid).mapTo[Future[List[User]]]
    val actualResult = result.flatMap(identity)
    actualResult.map { act =>
      Ok(toJson(act))
    }
  }

  val saveUserGroup = Action.async { request =>
    val result = request.body.asJson.get
    val json = result.toString()
    val userGroup = extractEntity[UserGroup](json)
    val finalResult = ask(userGroupActor, userGroup).mapTo[Future[Boolean]]
    val actualResult = finalResult.flatMap(identity)
    actualResult.map { act =>
      if (act) Ok("Inserted") else InternalServerError("Not Inserted")
    }
  }

  def updateUserGroup(id: String) = Action.async { request =>
    val result = request.body.asJson.get
    val json = result.toString()
    val userGroup = extractEntity[UserGroup](json)
    val command = UserUpdateCommand[UserGroup](BSONObjectID.parse(id).get, userGroup)
    val finalResult = ask(userGroupActor, command).mapTo[Future[List[UserGroup]]]
    val actualResult = finalResult.flatMap(identity)
    actualResult.map { act =>
      if (act.nonEmpty) Ok(toJson(act.head)) else InternalServerError("Not Updated")
    }
  }

  def deleteUserGroupById(id: String) = Action.async { request =>
    val result = ask(userGroupActor, DeleteByIdCommand(BSONObjectID.parse(id).get)).mapTo[Future[Int]]
    val actualResult = result.flatMap(identity)
    actualResult.map { act =>
      if (act > 0) Ok("Deleted") else Ok("Not Deleted")
    }
  }

  def getUserGroupById(id: String) = Action.async { request =>
    val result = ask(userGroupActor, FindByIdCommand(BSONObjectID.parse(id).get)).mapTo[Future[List[UserGroup]]]
    val actualResult = result.flatMap(identity)
    actualResult.map { act =>
      Ok(toJson(act.head))
    }
  }

  val getAllUserGroup = Action.async { request =>
    val result = ask(userGroupActor, FindAllCommand).mapTo[Future[List[UserGroup]]]
    val actualResult = result.flatMap(identity)
    actualResult.map { act =>
      Ok(toJson(act))
    }
  }
}

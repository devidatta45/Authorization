package com.sony.controllers

import javax.inject.{Inject, Singleton}

import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import com.sony.models.Permission
import com.sony.services.PermissionActor
import com.sony.utils._
import com.sony.utils.JsonImplicits._
import play.api.mvc.{AbstractController, ControllerComponents}
import reactivemongo.bson.BSONObjectID

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

@Singleton
class PermissionController @Inject()(creator: ActorCreator, cc: ControllerComponents) extends AbstractController(cc) {
  implicit val timeout: Timeout = 5.seconds
  val permissionActor = creator.createActorRef(Props(classOf[PermissionActor]), "PermissionActor")

  def getPermissionsByClient(client: String) = Action.async { request =>
    val result = ask(permissionActor, client).mapTo[Future[List[Permission]]]
    val actualResult = result.flatMap(identity)
    actualResult.map { act =>
      Ok(toJson(act))
    }
  }

  def savePermission(clientId: String) = Action.async { request =>
    val result = request.body.asJson.get
    val json = result.toString()
    val permission = extractEntity[Permission](json)
    val finalResult = ask(permissionActor, permission).mapTo[Future[Boolean]]
    val actualResult = finalResult.flatMap(identity)
    actualResult.map { act =>
      if (act) Ok("Inserted") else InternalServerError("Not Inserted")
    }
  }

  def updatePermission(clientId: String, id: String) = Action.async { request =>
    val result = request.body.asJson.get
    val json = result.toString()
    val permission = extractEntity[Permission](json)
    val command = UserUpdateCommand[Permission](BSONObjectID.parse(id).get, permission)
    val finalResult = ask(permissionActor, command).mapTo[Future[List[Permission]]]
    val actualResult = finalResult.flatMap(identity)
    actualResult.map { act =>
      if (act.nonEmpty) Ok(toJson(act.head)) else InternalServerError("Not Updated")
    }
  }

  def deletePermissionById(clientId: String, id: String) = Action.async { request =>
    val result = ask(permissionActor, DeleteByIdCommand(BSONObjectID.parse(id).get)).mapTo[Future[Int]]
    val actualResult = result.flatMap(identity)
    actualResult.map { act =>
      if (act > 0) Ok("Deleted") else Ok("Not Deleted")
    }
  }

  val getAllPermissions = Action.async { request =>
    val result = ask(permissionActor, FindAllCommand).mapTo[Future[List[Permission]]]
    val actualResult = result.flatMap(identity)
    actualResult.map { act =>
      Ok(toJson(act))
    }
  }

  def getPermissionById(clientId: String, id: String) = Action.async { request =>
    val result = ask(permissionActor, FindByIdCommand(BSONObjectID.parse(id).get)).mapTo[Future[List[Permission]]]
    val actualResult = result.flatMap(identity)
    actualResult.map { act =>
      Ok(toJson(act.head))
    }
  }
}

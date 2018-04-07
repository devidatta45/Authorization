package com.sony.controllers

import javax.inject.{Inject, Singleton}

import akka.actor.Props
import akka.util.Timeout
import com.sony.services.RoleActor
import com.sony.utils._
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import akka.pattern.ask
import com.sony.models.{Permission, Role}
import com.sony.utils.JsonImplicits._
import reactivemongo.bson.BSONObjectID

@Singleton
class RoleController @Inject()(creator: ActorCreator, cc: ControllerComponents) extends AbstractController(cc) {

  implicit val timeout: Timeout = 5.seconds
  val roleActor = creator.createActorRef(Props(classOf[RoleActor]), "RoleActor")

  def getPermissionsByRole(clientId: String, roleId: String) = Action.async { request =>
    val result = ask(roleActor, PermissionCommand(roleId)).mapTo[Future[List[Permission]]]
    val actualResult = result.flatMap(identity)
    actualResult.map { act =>
      Ok(toJson(act))
    }
  }

  def saveRole(clientId: String) = Action.async { request =>
    val result = request.body.asJson.get
    val json = result.toString()
    val role = extractEntity[Role](json)
    val finalResult = ask(roleActor, role).mapTo[Future[Boolean]]
    val actualResult = finalResult.flatMap(identity)
    actualResult.map { act =>
      if (act) Ok("Inserted") else InternalServerError("Not Inserted")
    }
  }

  def updateRole(clientId: String, id: String) = Action.async { request =>
    val result = request.body.asJson.get
    val json = result.toString()
    val role = extractEntity[Role](json)
    val command = UserUpdateCommand[Role](BSONObjectID.parse(id).get, role)
    val finalResult = ask(roleActor, command).mapTo[Future[List[Role]]]
    val actualResult = finalResult.flatMap(identity)
    actualResult.map { act =>
      if (act.nonEmpty) Ok(toJson(act.head)) else InternalServerError("Not Updated")
    }
  }

  def deleteRoleById(clientId: String, id: String) = Action.async { request =>
    val result = ask(roleActor, DeleteByIdCommand(BSONObjectID.parse(id).get)).mapTo[Future[Int]]
    val actualResult = result.flatMap(identity)
    actualResult.map { act =>
      if (act > 0) Ok("Deleted") else Ok("Not Deleted")
    }
  }

  def getRoleById(clientId: String, id: String) = Action.async { request =>
    val result = ask(roleActor, FindByIdCommand(BSONObjectID.parse(id).get)).mapTo[Future[List[Role]]]
    val actualResult = result.flatMap(identity)
    actualResult.map { act =>
      Ok(toJson(act.head))
    }
  }

  val getAllRoles = Action.async { request =>
    val result = ask(roleActor, FindAllCommand).mapTo[Future[List[Role]]]
    val actualResult = result.flatMap(identity)
    actualResult.map { act =>
      Ok(toJson(act))
    }
  }

  def getRolesByClient(client: String) = Action.async { request =>
    val result = ask(roleActor, client).mapTo[Future[List[Role]]]
    val actualResult = result.flatMap(identity)
    actualResult.map { act =>
      Ok(toJson(act))
    }
  }
}

case class PermissionCommand(roleId: String)
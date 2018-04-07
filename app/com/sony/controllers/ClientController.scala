package com.sony.controllers

import javax.inject.{Inject, Singleton}

import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import com.sony.models.Client
import com.sony.services.ClientActor
import com.sony.utils.JsonImplicits._
import com.sony.utils._
import play.api.mvc.{AbstractController, ControllerComponents}
import reactivemongo.bson.BSONObjectID

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

@Singleton
class ClientController @Inject()(creator: ActorCreator, cc: ControllerComponents) extends AbstractController(cc) {
  implicit val timeout: Timeout = 5.seconds
  val clientActor = creator.createActorRef(Props(classOf[ClientActor]), "ClientActor")

  val getAllClients = Action.async { request =>
    val result = ask(clientActor, FindAllCommand).mapTo[Future[List[Client]]]
    val actualResult = result.flatMap(identity)
    actualResult.map { act =>
      Ok(toJson(act))
    }
  }

  def getClientById(id: String) = Action.async { request =>
    val result = ask(clientActor, FindByIdCommand(BSONObjectID.parse(id).get)).mapTo[Future[List[Client]]]
    val actualResult = result.flatMap(identity)
    actualResult.map { act =>
      Ok(toJson(act.head))
    }
  }

  val saveClient = Action.async { request =>
    val result = request.body.asJson.get
    val json = result.toString()
    val client = extractEntity[Client](json)
    val finalResult = ask(clientActor, client).mapTo[Future[Boolean]]
    val actualResult = finalResult.flatMap(identity)
    actualResult.map { act =>
      if (act) Ok("Inserted") else InternalServerError("Not Inserted")
    }
  }

  def updateClient(id: String) = Action.async { request =>
    val result = request.body.asJson.get
    val json = result.toString()
    val client = extractEntity[Client](json)
    val command = UserUpdateCommand[Client](BSONObjectID.parse(id).get, client)
    val finalResult = ask(clientActor, command).mapTo[Future[List[Client]]]
    val actualResult = finalResult.flatMap(identity)
    actualResult.map { act =>
      if (act.nonEmpty) Ok(toJson(act.head)) else InternalServerError("Not Updated")
    }
  }

  def deleteClientById(id: String) = Action.async { request =>
    val result = ask(clientActor, DeleteByIdCommand(BSONObjectID.parse(id).get)).mapTo[Future[Int]]
    val actualResult = result.flatMap(identity)
    actualResult.map { act =>
      if (act > 0) Ok("Deleted") else Ok("Not Deleted")
    }
  }
}

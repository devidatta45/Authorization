package com.sony.services

import com.sony.models.Client
import com.sony.repository._
import com.sony.utils.ClientColumnConstants._
import com.sony.utils._
import reactivemongo.bson.{BSONDocument, BSONObjectID}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by DDM
  */

class ClientActor extends BaseActor {
  override def normalExecution: Receive = {
    case FindAllCommand => sender ! ClientRepositoryImpl.findAll()
    case cmd: FindByIdCommand => sender ! ClientRepositoryImpl.findById(cmd.id)
    case client: Client => sender ! ClientRepositoryImpl.save(client)
    case command: UserUpdateCommand[Client]@unchecked => updateClient(command)
    case cmd: DeleteByIdCommand => sender ! deleteClientWithTransaction(cmd.id)
  }

  def deleteClientWithTransaction(clientId: BSONObjectID): Future[Int] = {
    for {
      client <- ClientRepositoryImpl.findById(clientId)
      roles <- RoleRepositoryImpl.filterQuery(BSONDocument("CLIENTID" -> client.head.cid))
      permissions <- PermissionRepositoryImpl.filterQuery(BSONDocument("CLIENTID" -> client.head.cid))
      deleteRoles <- RoleRepositoryImpl.deleteByIds(roles.map(role => BSONObjectID.parse(role._id).get))
      deletePermissions <- RoleRepositoryImpl.deleteByIds(permissions.map(permission =>
        BSONObjectID.parse(permission._id).get))
      deleteRolesFromUser <- UserRepositoryImpl.deleteMultipleRolesFromUser(roles.map(role =>
        BSONObjectID.parse(role._id).get))
      deleteRolesFromUserGroup <- UserGroupRepositoryImpl.deleteMultipleRolesFromUserGroup(roles.map(role =>
        BSONObjectID.parse(role._id).get))
      deleteClient <- ClientRepositoryImpl.deleteById(clientId)
    } yield deleteClient
  }

  def updateClient(cmd: UserUpdateCommand[Client]): Unit = {
    val document = BSONDocument(
      "$set" -> BSONDocument(
        CID -> cmd.user.cid,
        NAME -> cmd.user.name,
        SECRET_KEY -> cmd.user.secretKey,
        REDIRECT_URL -> cmd.user.redirectUrl))
    sender ! ClientRepositoryImpl.updateById(cmd.id, document)
  }
}

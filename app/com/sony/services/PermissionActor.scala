package com.sony.services

import com.sony.models.Permission
import com.sony.repository.{PermissionRepositoryImpl, RoleRepositoryImpl}
import com.sony.utils.ClientColumnConstants.NAME
import com.sony.utils.PermissionColumnConstants.CLIENT_ID
import com.sony.utils._
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

class PermissionActor extends BaseActor {
  override def normalExecution: Receive = {
    case cid: String => sender ! PermissionRepositoryImpl.filterQuery(BSONDocument(PermissionColumnConstants.CLIENT_ID -> cid))
    case permission: Permission => sender ! PermissionRepositoryImpl.save(permission)
    case cmd: DeleteByIdCommand => sender ! deletePermission(cmd.id)
    case cmd: FindByIdCommand => sender ! PermissionRepositoryImpl.findById(cmd.id)
    case command: UserUpdateCommand[Permission]@unchecked => updatePermission(command)
    case FindAllCommand => sender ! PermissionRepositoryImpl.findAll()
  }

  def deletePermission(permissionId: BSONObjectID): Future[Int] = {
    for {
      deleteRoles <- RoleRepositoryImpl.deletePermissionsFromRole(permissionId)
      deletePermission <- PermissionRepositoryImpl.deleteById(permissionId)
    } yield deletePermission
  }

  def updatePermission(cmd: UserUpdateCommand[Permission]): Unit = {
    val document = BSONDocument(
      "$set" -> BSONDocument(
        NAME -> cmd.user.name,
        CLIENT_ID -> cmd.user.clientId))
    sender ! PermissionRepositoryImpl.updateById(cmd.id, document)
  }
}

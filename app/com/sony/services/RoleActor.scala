package com.sony.services

import com.sony.controllers.PermissionCommand
import com.sony.models.Role
import com.sony.repository.RoleRepositoryImpl
import com.sony.utils.ClientColumnConstants.NAME
import com.sony.utils.PermissionColumnConstants.CLIENT_ID
import com.sony.utils.RoleColumnConstants.PERMISSIONS
import com.sony.utils._
import reactivemongo.bson.{BSONDocument, BSONObjectID}

class RoleActor extends BaseActor {
  override def normalExecution: Receive = {
    case command: PermissionCommand => sender ! RoleRepositoryImpl.getPermissionsByRole(command.roleId)
    case role: Role => sender ! RoleRepositoryImpl.save(role)
    case cmd: FindByIdCommand => sender ! RoleRepositoryImpl.findById(cmd.id)
    case cmd: DeleteByIdCommand => sender ! RoleRepositoryImpl.deleteById(cmd.id)
    case command: UserUpdateCommand[Role]@unchecked => updateRole(command)
    case cid: String => sender ! RoleRepositoryImpl.filterQuery(BSONDocument(PermissionColumnConstants.CLIENT_ID -> cid))
    case FindAllCommand => sender ! RoleRepositoryImpl.findAll()
  }

  def updateRole(cmd: UserUpdateCommand[Role]): Unit = {
    val permissions = cmd.user.permissions.map(permission => BSONObjectID.parse(permission).get)
    val document = BSONDocument(
      "$set" -> BSONDocument(
        NAME -> cmd.user.name,
        PERMISSIONS -> permissions,
        CLIENT_ID -> cmd.user.clientId))
    sender ! RoleRepositoryImpl.updateById(cmd.id, document)
  }
}

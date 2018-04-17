package com.sony.services

import com.sony.controllers.PermissionCommand
import com.sony.models.Role
import com.sony.repository.{RoleRepositoryImpl, UserGroupRepositoryImpl, UserRepositoryImpl}
import com.sony.utils._
import reactivemongo.bson.{BSONDocument, BSONObjectID}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class RoleActor extends BaseActor {
  override def normalExecution: Receive = {
    case command: PermissionCommand => sender ! RoleRepositoryImpl.getPermissionsByRole(command.roleId)
    case role: Role => sender ! RoleRepositoryImpl.save(role)
    case cmd: FindByIdCommand => sender ! RoleRepositoryImpl.findById(cmd.id)
    case cmd: DeleteByIdCommand => sender ! deleteRoleWithTransaction(cmd.id)
    case command: UserUpdateCommand[Role]@unchecked => sender ! RoleRepositoryImpl.updateRole(command)
    case cid: String => sender ! RoleRepositoryImpl.filterQuery(BSONDocument(PermissionColumnConstants.CLIENT_ID -> cid))
    case FindAllCommand => sender ! RoleRepositoryImpl.findAll()
  }

  def deleteRoleWithTransaction(roleId: BSONObjectID): Future[Int] = {
    for {
      deleteRolesFromUser <- UserRepositoryImpl.deleteRolesFromUser(roleId)
      deleteRolesFromUserGroup <- UserGroupRepositoryImpl.deleteRolesFromUserGroup(roleId)
      deleteRole <- RoleRepositoryImpl.deleteById(roleId)
    } yield deleteRole
  }
}

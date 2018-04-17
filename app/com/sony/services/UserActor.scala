package com.sony.services

import com.sony.models.User
import com.sony.repository.{UserGroupRepositoryImpl, UserPermission, UserPermissionWithCache, UserRepositoryImpl}
import com.sony.utils._
import reactivemongo.bson.BSONObjectID

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by DDM
  */

class UserActor extends BaseActor {
  override def normalExecution: Receive = {
    case cmd: UserPermission => sender ! UserRepositoryImpl.getPermissions(cmd)
    case cmd: UserPermissionWithCache => sender ! UserRepositoryImpl.getPermissionWithCache(cmd)
    case user: User => sender ! UserRepositoryImpl.save(user)
    case cmd: FindByIdCommand => sender ! UserRepositoryImpl.findById(cmd.id)
    case cmd: RoleCommand => sender ! UserRepositoryImpl.getRoles(cmd.id)
    case cmd: UserGroupCommand => sender ! UserGroupRepositoryImpl.getUserGroupsByUser(cmd.id)
    case cmd: DeleteByIdCommand => sender ! UserRepositoryImpl.deleteById(cmd.id)
    case cmd: UserUpdateCommand[User]@unchecked => sender ! UserRepositoryImpl.updateUser(cmd)
    case FindAllCommand => sender ! UserRepositoryImpl.findAll()
  }

  def deleteUserWithTransaction(userId:BSONObjectID): Future[Int] ={
    for {
      deleteUsersFromUserGroup <- UserGroupRepositoryImpl.deleteUsersFromUserGroup(userId)
      deleteUser <- UserRepositoryImpl.deleteById(userId)
    } yield deleteUser
  }
}

case class RoleCommand(id: BSONObjectID)

case class UserGroupCommand(id: String)
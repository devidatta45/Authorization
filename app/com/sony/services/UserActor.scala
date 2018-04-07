package com.sony.services

import com.sony.models.User
import com.sony.repository.{UserPermission, UserPermissionWithCache, UserRepositoryImpl}
import com.sony.utils.UserColumnConstants.{DIRECT_ROLES, EMAIL, FIRST_NAME, LAST_NAME}
import com.sony.utils._
import reactivemongo.bson.{BSONDocument, BSONObjectID}

/**
  * Created by DDM
  */

class UserActor extends BaseActor {
  override def normalExecution: Receive = {
    case cmd: UserPermission => sender ! UserRepositoryImpl.getPermissions(cmd)
    case cmd: UserPermissionWithCache => sender ! UserRepositoryImpl.getPermissionWithCache(cmd)
    case user: User => sender ! UserRepositoryImpl.save(user)
    case cmd: FindByIdCommand => sender ! UserRepositoryImpl.findById(cmd.id)
    case cmd: DeleteByIdCommand => sender ! UserRepositoryImpl.deleteById(cmd.id)
    case cmd: UserUpdateCommand[User] @unchecked=> updateUser(cmd)
    case FindAllCommand => sender ! UserRepositoryImpl.findAll()
  }

  def updateUser(cmd: UserUpdateCommand[User]): Unit = {
    val directRoles = cmd.user.directRoles.map(role => BSONObjectID.parse(role).get)
    val document = BSONDocument(
      "$set" -> BSONDocument(
        FIRST_NAME -> cmd.user.firstName,
        LAST_NAME -> cmd.user.lastName,
        EMAIL -> cmd.user.email,
        DIRECT_ROLES -> directRoles))
    sender ! UserRepositoryImpl.updateById(cmd.id, document)
  }
}


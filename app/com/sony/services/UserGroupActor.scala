package com.sony.services

import com.sony.models.UserGroup
import com.sony.repository.UserGroupRepositoryImpl
import com.sony.utils.ClientColumnConstants.NAME
import com.sony.utils.UserGroupColumnConstants.{ROLES, USERS}
import com.sony.utils._
import reactivemongo.bson.{BSONDocument, BSONObjectID}

class UserGroupActor extends BaseActor {
  override def normalExecution: Receive = {
    case uid: String => sender ! UserGroupRepositoryImpl.getUsersByUserGroup(uid)
    case userGroup: UserGroup => sender ! UserGroupRepositoryImpl.save(userGroup)
    case cmd: DeleteByIdCommand => sender ! UserGroupRepositoryImpl.deleteById(cmd.id)
    case cmd: FindByIdCommand => sender ! UserGroupRepositoryImpl.findById(cmd.id)
    case cmd: UserUpdateCommand[UserGroup]@unchecked => updateUserGroup(cmd)
    case FindAllCommand => sender ! UserGroupRepositoryImpl.findAll()
  }

  def updateUserGroup(cmd: UserUpdateCommand[UserGroup]): Unit = {
    val roles = cmd.user.roles.map(role => BSONObjectID.parse(role).get)
    val users = cmd.user.users.map(user => BSONObjectID.parse(user).get)

    val document = BSONDocument(
      "$set" -> BSONDocument(
        NAME -> cmd.user.name,
        ROLES -> roles,
        USERS -> users))
    sender ! UserGroupRepositoryImpl.updateById(cmd.id, document)
  }
}

package com.sony.services

import com.sony.models.UserGroup
import com.sony.repository.UserGroupRepositoryImpl
import com.sony.utils._

class UserGroupActor extends BaseActor {
  override def normalExecution: Receive = {
    case uid: String => sender ! UserGroupRepositoryImpl.getUsersByUserGroup(uid)
    case userGroup: UserGroup => sender ! UserGroupRepositoryImpl.save(userGroup)
    case cmd: DeleteByIdCommand => sender ! UserGroupRepositoryImpl.deleteById(cmd.id)
    case cmd: FindByIdCommand => sender ! UserGroupRepositoryImpl.findById(cmd.id)
    case cmd: UserUpdateCommand[UserGroup]@unchecked => sender ! UserGroupRepositoryImpl.updateUserGroup(cmd)
    case FindAllCommand => sender ! UserGroupRepositoryImpl.findAll()
  }

}

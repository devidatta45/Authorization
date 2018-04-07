package com.sony.models

import com.sony.utils.BaseEntity
import com.sony.utils.ClientColumnConstants.NAME
import com.sony.utils.UserColumnConstants.{ID, ISREMOVED}
import com.sony.utils.UserGroupColumnConstants.{ROLES, USERS}
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID}

/**
  * Created by DDM
  */

case class UserGroup(override val _id: String, name: String, roles: List[String], users: List[String]) extends BaseEntity

object UserGroup {

  implicit object UserGroupReader extends BSONDocumentReader[UserGroup] {
    def read(doc: BSONDocument): UserGroup = {
      val id = doc.getAs[BSONObjectID](ID).get
      val name = doc.getAs[String](NAME).get
      val roles = doc.getAs[List[BSONObjectID]](ROLES).get.map(_.stringify)
      val users = doc.getAs[List[BSONObjectID]](USERS).get.map(_.stringify)
      UserGroup(id.stringify, name, roles, users)
    }
  }

  implicit object UserGroupWriter extends BSONDocumentWriter[UserGroup] {
    def write(userGroup: UserGroup): BSONDocument = {
      val userGroupId = BSONObjectID.parse(userGroup._id)
      val id = if (userGroupId.isSuccess) userGroupId.get else BSONObjectID.generate()
      val roles = userGroup.roles.map(role => BSONObjectID.parse(role).get)
      val users = userGroup.users.map(user => BSONObjectID.parse(user).get)
      BSONDocument(ID -> id,
        NAME -> userGroup.name,
        ROLES -> roles,
        USERS -> users,
        ISREMOVED -> userGroup.isRemoved)
    }
  }

}

package com.sony.models

import com.sony.utils.BaseEntity
import com.sony.utils.ClientColumnConstants.NAME
import com.sony.utils.PermissionColumnConstants.CLIENT_ID
import com.sony.utils.RoleColumnConstants.PERMISSIONS
import com.sony.utils.UserColumnConstants.{ID, ISREMOVED}
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID}

/**
  * Created by DDM
  */

case class Role(override val _id: String, name: String, permissions: List[CompositeCommand], clientId: String) extends BaseEntity

object Role {

  implicit object CompositeReader extends BSONDocumentReader[CompositeCommand] {
    def read(doc: BSONDocument): CompositeCommand = {
      val id = doc.getAs[BSONObjectID]("id").get
      val name = doc.getAs[String](NAME).get
      CompositeCommand(id.stringify, name)
    }
  }

  implicit object RoleReader extends BSONDocumentReader[Role] {
    def read(doc: BSONDocument): Role = {
      val id = doc.getAs[BSONObjectID](ID).get
      val name = doc.getAs[String](NAME).get
      val permissions = doc.getAs[List[CompositeCommand]](PERMISSIONS).get
      val clientId = doc.getAs[String](CLIENT_ID).get
      Role(id.stringify, name, permissions, clientId)
    }
  }

  implicit object RoleWriter extends BSONDocumentWriter[Role] {
    def write(role: Role): BSONDocument = {
      val roleId = BSONObjectID.parse(role._id)
      val id = if (roleId.isSuccess) roleId.get else BSONObjectID.generate()
      val permissions = role.permissions.map(permission => BSONDocument("id" ->
        BSONObjectID.parse(permission.id).get, NAME -> permission.name))
      BSONDocument(ID -> id,
        NAME -> role.name,
        PERMISSIONS -> permissions,
        CLIENT_ID -> role.clientId,
        ISREMOVED -> role.isRemoved)
    }
  }

}


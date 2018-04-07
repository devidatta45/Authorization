package com.sony.models

import com.sony.utils.BaseEntity
import com.sony.utils.ClientColumnConstants.NAME
import com.sony.utils.PermissionColumnConstants.CLIENT_ID
import com.sony.utils.UserColumnConstants.{ID, ISREMOVED}
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID}

/**
  * Created by DDM
  */
case class Permission(override val _id: String, name: String, clientId: String) extends BaseEntity

object Permission {

  implicit object PermissionReader extends BSONDocumentReader[Permission] {
    def read(doc: BSONDocument): Permission = {
      val id = doc.getAs[BSONObjectID](ID).get
      val name = doc.getAs[String](NAME).get
      val clientId = doc.getAs[String](CLIENT_ID).get
      Permission(id.stringify, name, clientId)
    }
  }

  implicit object PermissionWriter extends BSONDocumentWriter[Permission] {
    def write(permission: Permission): BSONDocument = {
      val permId = BSONObjectID.parse(permission._id)
      val id = if (permId.isSuccess) permId.get else BSONObjectID.generate()
      BSONDocument(ID -> id,
        NAME -> permission.name,
        CLIENT_ID -> permission.clientId,
        ISREMOVED -> permission.isRemoved)
    }
  }

}

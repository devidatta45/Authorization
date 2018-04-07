package com.sony.models

import com.sony.utils.{BaseColumnConstants, BaseEntity}
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID}

case class RuntimeCollection(override val _id: String, email: String, mappings: List[ClientPermissionMapping]) extends BaseEntity

case class ClientPermissionMapping(clientId: String, permissions: List[String])

object RuntimeCollection {

  implicit object ClientPermissionMappingReader extends BSONDocumentReader[ClientPermissionMapping] {
    def read(doc: BSONDocument): ClientPermissionMapping = {
      val clientId = doc.getAs[BSONObjectID]("ClientId").get
      val permissions = doc.getAs[List[BSONObjectID]]("Permissions").get.map(_.stringify)
      ClientPermissionMapping(clientId.stringify, permissions)
    }
  }

  implicit object RuntimeCollectionReader extends BSONDocumentReader[RuntimeCollection] {
    def read(doc: BSONDocument): RuntimeCollection = {
      val id = doc.getAs[BSONObjectID](BaseColumnConstants.ID).get
      val email = doc.getAs[String]("Email").get
      val mappings = doc.getAs[List[ClientPermissionMapping]]("Mappings").get
      RuntimeCollection(id.stringify, email, mappings)
    }
  }

  implicit object ClientPermissionMappingWriter extends BSONDocumentWriter[ClientPermissionMapping] {
    def write(clientPermissionMapping: ClientPermissionMapping): BSONDocument = {
      val clientId = BSONObjectID.parse(clientPermissionMapping.clientId).get
      val permissions = clientPermissionMapping.permissions.map(cm => BSONObjectID.parse(cm).get)
      BSONDocument("ClientId" -> clientId,
        "Permissions" -> permissions)
    }
  }

  implicit object RuntimeCollectionWriter extends BSONDocumentWriter[RuntimeCollection] {
    def write(runtimeCollection: RuntimeCollection): BSONDocument = {
      val collectionId = BSONObjectID.parse(runtimeCollection._id)
      val id = if (collectionId.isSuccess) collectionId.get else BSONObjectID.generate()
      BSONDocument(BaseColumnConstants.ID -> id,
        "Email" -> runtimeCollection.email,
        "Mappings" -> runtimeCollection.mappings,
        BaseColumnConstants.ISREMOVED -> runtimeCollection.isRemoved)
    }
  }

}

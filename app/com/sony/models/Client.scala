package com.sony.models

import com.sony.utils.BaseEntity
import com.sony.utils.ClientColumnConstants._
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID}

/**
  * Created by DDM
  */
case class Client(override val _id: String, cid: String, name: String, secretKey: String, redirectUrl: String) extends BaseEntity

object Client {

  implicit object ClientReader extends BSONDocumentReader[Client] {
    def read(doc: BSONDocument): Client = {
      val id = doc.getAs[BSONObjectID](ID).get
      val cid = doc.getAs[String](CID).get
      val name = doc.getAs[String](NAME).get
      val secretKey = doc.getAs[String](SECRET_KEY).get
      val redirectUrl = doc.getAs[String](REDIRECT_URL).get
      Client(id.stringify, cid, name, secretKey, redirectUrl)
    }
  }

  implicit object ClientWriter extends BSONDocumentWriter[Client] {
    def write(client: Client): BSONDocument = {
      val clientId = BSONObjectID.parse(client._id)
      val id = if (clientId.isSuccess) clientId.get else BSONObjectID.generate()
      BSONDocument(ID -> id,
        CID -> client.cid,
        NAME -> client.name,
        SECRET_KEY -> client.secretKey,
        REDIRECT_URL -> client.redirectUrl,
        ISREMOVED -> client.isRemoved)
    }
  }

}








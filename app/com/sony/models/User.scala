package com.sony.models

import com.sony.utils.BaseEntity
import com.sony.utils.ClientColumnConstants.NAME
import com.sony.utils.UserColumnConstants._
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID}

/**
  * Created by DDM
  */

case class User(override val _id: String, firstName: String, lastName: String,
                email: String, directRoles: List[CompositeCommand]) extends BaseEntity

object User {

  import Role._

  implicit object UserReader extends BSONDocumentReader[User] {
    def read(doc: BSONDocument): User = {
      val id = doc.getAs[BSONObjectID](ID).get
      //val name = doc.getAs[String](NAME).get
      val firstName = doc.getAs[String](FIRST_NAME).get
      val lastName = doc.getAs[String](LAST_NAME).get
      val email = doc.getAs[String](EMAIL).get
      val directRoles = doc.getAs[List[CompositeCommand]](DIRECT_ROLES).get
      User(id.stringify, firstName, lastName, email, directRoles)
    }
  }

  implicit object UserWriter extends BSONDocumentWriter[User] {
    def write(user: User): BSONDocument = {
      val userId = BSONObjectID.parse(user._id)
      val id = if (userId.isSuccess) userId.get else BSONObjectID.generate()
      val directRoles = user.directRoles.map(role => BSONDocument("id" ->
        BSONObjectID.parse(role.id).get, NAME -> role.name))
      BSONDocument(ID -> id,
        FIRST_NAME -> user.firstName,
        LAST_NAME -> user.lastName,
        EMAIL -> user.email,
        DIRECT_ROLES -> directRoles,
        ISREMOVED -> user.isRemoved)
    }
  }

}

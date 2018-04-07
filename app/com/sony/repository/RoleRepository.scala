package com.sony.repository

import com.sony.models.{Permission, Role}
import com.sony.services.DataUtils
import com.sony.utils.{BaseColumnConstants, BaseRepository, Constants, OperatorConstants}
import reactivemongo.bson.{BSONDocument, BSONObjectID}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by DDM
  */

class RoleRepository extends BaseRepository[Role] {
  override def table: String = Constants.ROLE_TABLE

  def getRoles(ids: List[String]): Future[List[Role]] = {
    filterQuery(BSONDocument(BaseColumnConstants.ID -> BSONDocument(OperatorConstants.IN -> ids.map(id => BSONObjectID.parse(id).get))))
  }

  def getRolesWithCache(ids: List[String]): List[Role] = {
    val roles = DataUtils.roleMap.get("Roles")
    if (roles.isDefined) {
      ids.map(id => roles.get(id).head)
    }
    else Nil
  }

  def getPermissionsByRole(roleId: String): Future[List[Permission]] = {
    for {
      role <- findById(BSONObjectID.parse(roleId).get)
      permissions <- PermissionRepositoryImpl.filterQuery(BSONDocument(BaseColumnConstants.ID ->
        BSONDocument(OperatorConstants.IN -> role.head.permissions.map(permId => BSONObjectID.parse(permId).get))))
    } yield permissions
  }
}

object RoleRepositoryImpl extends RoleRepository
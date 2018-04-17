package com.sony.repository

import com.sony.models.{Permission, Role}
import com.sony.services.DataUtils
import com.sony.utils.ClientColumnConstants.NAME
import com.sony.utils.PermissionColumnConstants.CLIENT_ID
import com.sony.utils.RoleColumnConstants.PERMISSIONS
import com.sony.utils._
import reactivemongo.bson.{BSONDocument, BSONObjectID}

import scala.concurrent.{Await, Future}
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

  def deletePermissionsFromRole(permissionId: BSONObjectID): Future[List[Future[List[Role]]]] = {
    for {
      roles <- filterQuery(BSONDocument("PERMISSIONS.id" -> permissionId))
      res = roles.map(role => {
        val permissions = role.permissions.filterNot(perm => perm.id == permissionId.stringify)
        val newRole = role.copy(permissions = permissions)
        updateRole(UserUpdateCommand[Role](BSONObjectID.parse(newRole._id).get, newRole))
      })
    } yield res
  }

  def updateRole(cmd: UserUpdateCommand[Role]): Future[List[Role]] = {
    val permissions = cmd.user.permissions.map(permission => BSONDocument("id" ->
      BSONObjectID.parse(permission.id).get, ClientColumnConstants.NAME -> permission.name))
    val document = BSONDocument(
      "$set" -> BSONDocument(
        NAME -> cmd.user.name,
        PERMISSIONS -> permissions,
        CLIENT_ID -> cmd.user.clientId))
    RoleRepositoryImpl.updateById(cmd.id, document)
  }

  def getPermissionsByRole(roleId: String): Future[List[Permission]] = {
    for {
      role <- findById(BSONObjectID.parse(roleId).get)
      permissions <- PermissionRepositoryImpl.filterQuery(BSONDocument(BaseColumnConstants.ID ->
        BSONDocument(OperatorConstants.IN -> role.head.permissions.map(permId => BSONObjectID.parse(permId.id).get))))
    } yield permissions
  }
}

object RoleRepositoryImpl extends RoleRepository
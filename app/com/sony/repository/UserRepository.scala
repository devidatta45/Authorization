package com.sony.repository

import com.sony.models.User
import com.sony.utils.{BaseRepository, Constants, UserColumnConstants}
import reactivemongo.bson.BSONDocument

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by DDM
  */

class UserRepository extends BaseRepository[User] {
  override def table: String = Constants.USER_TABLE

  def getPermissions(userPermission: UserPermission): Future[List[ClientPermission]] = {
    for {
      user <- filterQuery(BSONDocument(UserColumnConstants.EMAIL -> userPermission.email))
      directRoles = user.head.directRoles
      groupRoles <- UserGroupRepositoryImpl.getUserGroups(user.head._id)
      totalRoles = directRoles ++ groupRoles
      roles <- RoleRepositoryImpl.getRoles(totalRoles.distinct)
      permissions = roles.flatMap(_.permissions).distinct
      clientPermissions <- PermissionRepositoryImpl.getPermissionByClient(permissions, userPermission.clients)
    } yield clientPermissions
  }

  def getPermissionWithCache(userPermission: UserPermissionWithCache): Future[List[ClientPermission]] = {
    for {
      user <- filterQuery(BSONDocument(UserColumnConstants.EMAIL -> userPermission.email))
      directRoles = user.head.directRoles
      groupRoles = UserGroupRepositoryImpl.getUserGroupsWithCache(user.head._id)
      totalRoles = directRoles ++ groupRoles
      roles = RoleRepositoryImpl.getRolesWithCache(totalRoles.distinct)
      permissions = roles.flatMap(_.permissions).distinct
      clientPermissions = PermissionRepositoryImpl.getPermissionByClientByCache(permissions, userPermission.clients)
    } yield clientPermissions
  }
}

object UserRepositoryImpl extends UserRepository

case class UserPermission(email: String, clients: List[String])

case class UserPermissionWithCache(email: String, clients: List[String])

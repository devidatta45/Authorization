package com.sony.repository

import com.sony.models.{Role, User}
import com.sony.utils.UserColumnConstants.{DIRECT_ROLES, EMAIL, FIRST_NAME, LAST_NAME}
import com.sony.utils._
import reactivemongo.bson.{BSONDocument, BSONObjectID}

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
      directRoles = user.head.directRoles.map(_.id)
      groupRoles <- UserGroupRepositoryImpl.getUserGroups(user.head._id)
      totalRoles = directRoles ++ groupRoles
      roles <- RoleRepositoryImpl.getRoles(totalRoles.distinct)
      permissions = roles.flatMap(_.permissions.map(_.id)).distinct
      clientPermissions <- PermissionRepositoryImpl.getPermissionByClient(permissions, userPermission.clients)
    } yield clientPermissions
  }

  def getRoles(id: BSONObjectID): Future[List[Role]] = {
    for {
      user <- findById(id)
      directRoles = user.head.directRoles.map(_.id)
      groupRoles <- UserGroupRepositoryImpl.getUserGroups(user.head._id)
      totalRoles = directRoles ++ groupRoles
      roles <- RoleRepositoryImpl.getRoles(totalRoles.distinct)
    } yield roles
  }

  def getPermissionWithCache(userPermission: UserPermissionWithCache): Future[List[ClientPermission]] = {
    for {
      user <- filterQuery(BSONDocument(UserColumnConstants.EMAIL -> userPermission.email))
      directRoles = user.head.directRoles.map(_.id)
      groupRoles = UserGroupRepositoryImpl.getUserGroupsWithCache(user.head._id)
      totalRoles = directRoles ++ groupRoles
      roles = RoleRepositoryImpl.getRolesWithCache(totalRoles.distinct)
      permissions = roles.flatMap(_.permissions.map(_.id)).distinct
      clientPermissions = PermissionRepositoryImpl.getPermissionByClientByCache(permissions, userPermission.clients)
    } yield clientPermissions
  }

  def deleteRolesFromUser(roleId: BSONObjectID): Future[List[Future[List[User]]]] = {
    for {
      users <- filterQuery(BSONDocument("DIRECTROLES.id" -> roleId))
      res = users.map(user => {
        val roles = user.directRoles.filterNot(rol => rol.id == roleId.stringify)
        val newUser = user.copy(directRoles = roles)
        updateUser(UserUpdateCommand[User](BSONObjectID.parse(newUser._id).get, newUser))
      })
    } yield res
  }

  def deleteMultipleRolesFromUser(roleIds: List[BSONObjectID]): Future[List[Future[List[User]]]] = {
    for {
      users <- filterQuery(BSONDocument("DIRECTROLES.id" -> BSONDocument("$in" -> roleIds)))
      res = users.map(user => {
        val roles = user.directRoles.filterNot(rol => roleIds.map(_.stringify) contains rol.id)
        val newUser = user.copy(directRoles = roles)
        updateUser(UserUpdateCommand[User](BSONObjectID.parse(newUser._id).get, newUser))
      })
    } yield res
  }

  def updateUser(cmd: UserUpdateCommand[User]): Future[List[User]] = {
    val directRoles = cmd.user.directRoles.map(role => BSONDocument("id" ->
      BSONObjectID.parse(role.id).get, ClientColumnConstants.NAME -> role.name))
    val document = BSONDocument(
      "$set" -> BSONDocument(
        FIRST_NAME -> cmd.user.firstName,
        LAST_NAME -> cmd.user.lastName,
        EMAIL -> cmd.user.email,
        DIRECT_ROLES -> directRoles))
    UserRepositoryImpl.updateById(cmd.id, document)
  }
}

object UserRepositoryImpl extends UserRepository

case class UserPermission(email: String, clients: List[String])

case class UserPermissionWithCache(email: String, clients: List[String])

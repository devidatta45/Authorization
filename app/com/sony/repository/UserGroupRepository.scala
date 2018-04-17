package com.sony.repository

import com.sony.models.{User, UserGroup}
import com.sony.services.DataUtils
import com.sony.utils.ClientColumnConstants.NAME
import com.sony.utils.UserGroupColumnConstants.{ROLES, USERS}
import com.sony.utils._
import reactivemongo.bson.{BSONDocument, BSONObjectID}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by DDM
  */

class UserGroupRepository extends BaseRepository[UserGroup] {
  override def table: String = Constants.USER_GROUP_TABLE

  def getUserGroups(userId: String): Future[List[String]] = {
    for {
      userGroups <- filterQuery(BSONDocument(UserGroupColumnConstants.USERS -> BSONObjectID.parse(userId).get))
      groupRoles = userGroups.flatMap(x => x.roles.map(_.id))
    } yield groupRoles
  }

  def getUserGroupsByUser(userId: String): Future[List[UserGroup]] = {
    filterQuery(BSONDocument(UserGroupColumnConstants.USERS -> BSONObjectID.parse(userId).get))
  }

  def getUserGroupsWithCache(userId: String): List[String] = {
    val userGroups = DataUtils.UserGroupMap.get("UserGroups")
    if (userGroups.isDefined) {
      val grps = userGroups.get.get(userId)
      if (grps.isDefined) {
        grps.get.flatMap(x => x._2.roles.map(_.id)).toList
      } else Nil
    }
    else {
      Nil
    }
  }

  def getUsersByUserGroup(usergroupId: String): Future[List[User]] = {
    for {
      userGroup <- findById(BSONObjectID.parse(usergroupId).get)
      users <- UserRepositoryImpl.filterQuery(BSONDocument(BaseColumnConstants.ID ->
        BSONDocument(OperatorConstants.IN -> userGroup.head.users.map(userId => BSONObjectID.parse(userId.id).get))))
    } yield users
  }

  def updateUserGroup(cmd: UserUpdateCommand[UserGroup]): Future[List[UserGroup]] = {
    val roles = cmd.user.roles.map(role => BSONDocument("id" ->
      BSONObjectID.parse(role.id).get, NAME -> role.name))
    val users = cmd.user.users.map(user => BSONDocument("id" ->
      BSONObjectID.parse(user.id).get, NAME -> user.name))
    val document = BSONDocument(
      "$set" -> BSONDocument(
        NAME -> cmd.user.name,
        ROLES -> roles,
        USERS -> users))
    UserGroupRepositoryImpl.updateById(cmd.id, document)
  }

  def deleteRolesFromUserGroup(roleId: BSONObjectID): Future[List[Future[List[UserGroup]]]] = {
    for {
      userGroups <- filterQuery(BSONDocument("ROLES.id" -> roleId))
      res = userGroups.map(ugrs => {
        val roles = ugrs.roles.filterNot(rol => rol.id == roleId.stringify)
        val newUserGroup = ugrs.copy(roles = roles)
        updateUserGroup(UserUpdateCommand[UserGroup](BSONObjectID.parse(newUserGroup._id).get, newUserGroup))
      })
    } yield res
  }

  def deleteMultipleRolesFromUserGroup(roleIds: List[BSONObjectID]): Future[List[Future[List[UserGroup]]]] = {
    for {
      userGroups <- filterQuery(BSONDocument("ROLES.id" -> BSONDocument("$in" -> roleIds)))
      res = userGroups.map(ugrs => {
        val roles = ugrs.roles.filterNot(rol => roleIds.map(_.stringify) contains rol.id)
        val newUserGroup = ugrs.copy(roles = roles)
        updateUserGroup(UserUpdateCommand[UserGroup](BSONObjectID.parse(newUserGroup._id).get, newUserGroup))
      })
    } yield res
  }

  def deleteUsersFromUserGroup(userId: BSONObjectID): Future[List[Future[List[UserGroup]]]] = {
    for {
      userGroups <- filterQuery(BSONDocument("USERS.id" -> userId))
      res = userGroups.map(ugrs => {
        val users = ugrs.users.filterNot(user => user.id == userId.stringify)
        val newUserGroup = ugrs.copy(users = users)
        updateUserGroup(UserUpdateCommand[UserGroup](BSONObjectID.parse(newUserGroup._id).get, newUserGroup))
      })
    } yield res
  }

}

object UserGroupRepositoryImpl extends UserGroupRepository

object TestQuery extends App {
  //  val result = UserGroupRepositoryImpl.getUserGroups("5aa9fef2f473d0f7e007d2ef")
  //
  //  val finalResult = Await.result(result, 5.seconds)
  //  println(finalResult)

  //  val list = List(1,2)
  //  val list1 = List(3,4)
  //  val list3 = list ++ list1
  //  println(list3)

}
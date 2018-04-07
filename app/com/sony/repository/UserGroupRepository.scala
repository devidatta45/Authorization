package com.sony.repository

import com.sony.models.{User, UserGroup}
import com.sony.services.DataUtils
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
      groupRoles = userGroups.flatMap(_.roles)
    } yield groupRoles
  }

  def getUserGroupsWithCache(userId: String): List[String] = {
    val userGroups = DataUtils.UserGroupMap.get("UserGroups")
    if (userGroups.isDefined) {
      val grps = userGroups.get.get(userId)
      if (grps.isDefined) {
        grps.get.flatMap(x => x._2.roles).toList
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
        BSONDocument(OperatorConstants.IN -> userGroup.head.users.map(userId => BSONObjectID.parse(userId).get))))
    } yield users
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
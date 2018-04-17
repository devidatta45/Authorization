package com.sony.repository

import java.util.UUID

import com.sony.models._
import com.sony.services.DataUtils
import com.sony.utils.{BaseColumnConstants, BaseRepository, Constants, OperatorConstants}
import reactivemongo.bson.{BSONDocument, BSONObjectID}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.util.Random
import scala.concurrent.duration._

/**
  * Created by DDM
  */
class PermissionRepository extends BaseRepository[Permission] {
  override def table: String = Constants.PERMISSION_TABLE

  def getPermissionByClient(permissionIds: List[String], clients: List[String]): Future[List[ClientPermission]] = {
    for {
      permissions <- filterQuery(BSONDocument(BaseColumnConstants.ID -> BSONDocument(OperatorConstants.IN -> permissionIds.map(id => BSONObjectID.parse(id).get))))
      clientPermissions = clients.map(client => ClientPermission(client, permissions.filter(_.clientId == client)))
    } yield clientPermissions
  }

  def getPermissionByClientByCache(permissionIds: List[String], clients: List[String]): List[ClientPermission] = {
    val permissions = DataUtils.permissionMap.get("Permissions")
    val perms = if (permissions.isDefined) {
      permissionIds.map(id => permissions.get(id).head)
    }
    else Nil
    clients.map(client => ClientPermission(client, perms.filter(_.clientId == client)))
  }
}

object PermissionRepositoryImpl extends PermissionRepository

case class ClientPermission(clientId: String, permissions: List[Permission])

object ClientInsert extends App {
  val clients = 1 to 100 map {
    id => {
      Client("", "" + id, "client:" + id, "secret:" + id, s"https://www.$id.com")
    }
  }

  val finalResult = for {
    clnts <- ClientRepositoryImpl.bulkSave(clients.toList)
    result = 1 to 100 map {
      id => createPermissions("" + id)
    }
    res = Future.sequence(result)
  } yield res

  val lastResult = Await.result(finalResult.flatMap(identity), 10.minutes)
  println("finished")

  def createPermissions(clientId: String): Future[Boolean] = {
    val permissions = 1 to 1000 map {
      perId => {
        val permissionId = BSONObjectID.generate().stringify
        Permission(permissionId, s"PERM:$clientId", clientId)
      }
    }
    val dualPermissions = DualFactory.getDualList(10, permissions.map(_._id).toList, Nil)

    val roles = dualPermissions.map { perm =>
      val id = BSONObjectID.generate().stringify
      val role = Role(id, "Role:" + perm.head, perm.map(x => CompositeCommand(x, x)), clientId)
      role
    }
    val dualRoles = DualFactory.getDualList(10, roles.map(_._id), Nil)

    val users = 1 to 10000 map {
      userId => {
        val id = BSONObjectID.generate().stringify
        val random = Random.nextInt(dualRoles.size - 1)
        val uuid = UUID.randomUUID().toString
        val user = User(id, "User:" + userId, "User:" + userId, s"user$uuid.com", dualRoles(random).
          map(x => CompositeCommand(x, x)))
        user
      }
    }

    val dualUsers = DualFactory.getDualList(20, users.map(_._id).toList, Nil)

    val userGroups = 1 to 100 map {
      grp => {
        val random1 = Random.nextInt(dualRoles.size - 1)
        val random2 = Random.nextInt(dualUsers.size - 1)
        UserGroup("", "Group:1", dualRoles(random1).map(x => CompositeCommand(x, x)),
          dualUsers(random2).map(x => CompositeCommand(x, x)))
      }
    }

    for {
      perms <- PermissionRepositoryImpl.bulkSave(permissions.toList)
      rll <- RoleRepositoryImpl.bulkSave(roles)
      usrs <- UserRepositoryImpl.bulkSave(users.toList)
      userGroups <- UserGroupRepositoryImpl.bulkSave(userGroups.toList)
    } yield userGroups
  }

}

object DualFactory {

  def getDualList(n: Int, list: List[String], finalList: List[List[String]]): List[List[String]] = {
    if (list.isEmpty) {
      finalList
    }
    else {
      val lastList = finalList ++ List(list.take(n))
      val deletedList = list.drop(n)
      getDualList(n, deletedList, lastList)
    }
  }
}
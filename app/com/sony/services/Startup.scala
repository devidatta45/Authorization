package com.sony.services

import javax.inject._

import com.sony.models._
import com.sony.repository._
import play.api.inject.ApplicationLifecycle

import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait Startup {
  def getAllData(): Unit

  def goodbye(): Unit
}

@Singleton
class StartupImpl @Inject()(appLifecycle: ApplicationLifecycle) extends Startup {

  import DataUtils._

  override def getAllData(): Unit = {
    println("Started")
    for {
      permissions <- PermissionRepositoryImpl.findAll()
      perMap = permissions.groupBy(x => x._id)
      _ = permissionMap.put("Permissions", perMap)
      roles <- RoleRepositoryImpl.findAll()
      rlMap = roles.groupBy(x => x._id)
      _ = roleMap.put("Roles", rlMap)
      userGroups <- UserGroupRepositoryImpl.findAll()
      lastMap = userGroups.flatMap(grp => grp.users.map(user => user.id -> grp)).toMap.groupBy(_._1)
      _ = UserGroupMap.put("UserGroups", lastMap)
      _ = println("Done")
    } yield ""
  }

  override def goodbye(): Unit = println("Goodbye!")

  // You can do this, or just explicitly call `hello()` at the end
  def start(): Unit = getAllData()

  // When the application starts, register a stop hook with the
  // ApplicationLifecycle object. The code inside the stop hook will
  // be run when the application stops.
  appLifecycle.addStopHook { () =>
    goodbye()
    Future.successful(())
  }

  // Called when this singleton is constructed (could be replaced by `hello()`)
  start()
}

object DataUtils {
  // val clientMap: mutable.Map[String, List[Client]] = mutable.Map[String, List[Client]]()
  val permissionMap: mutable.Map[String, Map[String, List[Permission]]] = mutable.Map[String, Map[String, List[Permission]]]()
  val roleMap: mutable.Map[String, Map[String, List[Role]]] = mutable.Map[String, Map[String, List[Role]]]()
  val UserGroupMap: mutable.Map[String, Map[String, Map[String,UserGroup]]] = mutable.Map[String, Map[String, Map[String,UserGroup]]]()
  //val userMap: mutable.Map[String, List[User]] = mutable.Map[String, List[User]]()
}
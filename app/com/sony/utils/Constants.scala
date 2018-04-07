package com.sony.utils

import scala.concurrent.duration._

/**
  * Created by DDM
  */

trait BaseColumnConstants {
  val ID = "_id"
  val ISREMOVED = "isRemoved"
}

object BaseColumnConstants extends BaseColumnConstants

object Constants{
  val TIMEOUT = 5.seconds
  val CLIENT_TABLE = "CLIENT"
  val RUNTIME_TABLE = "RUNTIMECOL"
  val PERMISSION_TABLE = "PERMISSION"
  val ROLE_TABLE = "ROLE"
  val USER_GROUP_TABLE = "USERGROUP"
  val USER_TABLE = "USER"
}
object OperatorConstants {
  val IN = "$in"
}

object ClientColumnConstants extends BaseColumnConstants{
  val CID = "CID"
  val SECRET_KEY = "SECRETKEY"
  val REDIRECT_URL = "REDIRECTURL"
  val NAME = "NAME"
}

object PermissionColumnConstants extends BaseColumnConstants{
  val CLIENT_ID = "CLIENTID"
}

object RoleColumnConstants extends BaseColumnConstants{
  val PERMISSIONS = "PERMISSIONS"
}

object UserGroupColumnConstants extends BaseColumnConstants{
  val ROLES = "ROLES"
  val USERS = "USERS"
}
object UserColumnConstants extends BaseColumnConstants{
  val EMAIL = "EMAIL"
  val FIRST_NAME = "FIRSTNAME"
  val LAST_NAME = "LASTNAME"
  val DIRECT_ROLES = "DIRECTROLES"
}
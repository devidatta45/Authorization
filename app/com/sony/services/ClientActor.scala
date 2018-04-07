package com.sony.services

import com.sony.models.Client
import com.sony.repository.ClientRepositoryImpl
import com.sony.utils.ClientColumnConstants._
import com.sony.utils._
import reactivemongo.bson.BSONDocument

/**
  * Created by DDM
  */

class ClientActor extends BaseActor {
  override def normalExecution: Receive = {
    case FindAllCommand => sender ! ClientRepositoryImpl.findAll()
    case cmd: FindByIdCommand => sender ! ClientRepositoryImpl.findById(cmd.id)
    case client: Client => sender ! ClientRepositoryImpl.save(client)
    case command: UserUpdateCommand[Client] @unchecked => updateClient(command)
    case cmd: DeleteByIdCommand => sender ! ClientRepositoryImpl.deleteById(cmd.id)
  }

  def updateClient(cmd: UserUpdateCommand[Client]): Unit = {
    val document = BSONDocument(
      "$set" -> BSONDocument(
        CID -> cmd.user.cid,
        NAME -> cmd.user.name,
        SECRET_KEY -> cmd.user.secretKey,
        REDIRECT_URL -> cmd.user.redirectUrl))
    sender ! ClientRepositoryImpl.updateById(cmd.id, document)
  }
}

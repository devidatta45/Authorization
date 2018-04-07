package com.sony.repository

import com.sony.models.Client
import com.sony.utils.{BaseRepository, Constants}

/**
  * Created by DDM
  */
class ClientRepository extends BaseRepository[Client]{
  override def table: String = Constants.CLIENT_TABLE
}

object ClientRepositoryImpl extends ClientRepository


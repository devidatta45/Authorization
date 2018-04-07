package com.sony.repository

import com.sony.models.RuntimeCollection
import com.sony.utils.{BaseRepository, Constants}

class RuntimeRepository extends BaseRepository[RuntimeCollection] {
  override def table: String = Constants.RUNTIME_TABLE
}

object RuntimeRepositoryImpl extends RuntimeRepository
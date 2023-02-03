/*
 * Copyright (C) 2016 AriaLyy(https://github.com/AriaLyy/Aria)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.arialyy.aria.orm

import androidx.room.Database
import androidx.room.RoomDatabase
import com.arialyy.aria.orm.dao.DEntityDao
import com.arialyy.aria.orm.dao.DGEntityDao
import com.arialyy.aria.orm.dao.MEntityDao
import com.arialyy.aria.orm.dao.RecordDao
import com.arialyy.aria.orm.dao.UEntityDao
import com.arialyy.aria.orm.entity.BlockRecord
import com.arialyy.aria.orm.entity.DGEntity
import com.arialyy.aria.orm.entity.MEntity
import com.arialyy.aria.orm.entity.MKeyInfo
import com.arialyy.aria.orm.entity.TaskRecord
import com.arialyy.aria.orm.entity.UEntity

@Database(
  entities = [DbEntity::class, UEntity::class, DGEntity::class, MEntity::class, MKeyInfo::class, TaskRecord::class, BlockRecord::class],
  version = 1
)
abstract class DuaDb : RoomDatabase() {
  abstract fun getDEntityDao(): DEntityDao

  abstract fun getUEntityDao(): UEntityDao

  abstract fun getDGEntityDao(): DGEntityDao

  abstract fun getMEntityDao(): MEntityDao

  abstract fun getRecordDao(): RecordDao
}
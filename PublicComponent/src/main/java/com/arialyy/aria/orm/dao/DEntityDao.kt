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
package com.arialyy.aria.orm.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.arialyy.aria.orm.entiry.DEntity

/**
 * @Author laoyuyu
 * @Description
 * @Date 19:23 AM 2023/1/16
 **/
@Dao
interface DEntityDao {
  @Query("SELECT * FROM DEntity WHERE :dId=dId")
  suspend fun queryDEntityById(dId: String): DEntity

  @Query("SELECT * FROM DEntity WHERE :sourceUrl=sourceUrl")
  suspend fun queryDEntityBySource(sourceUrl: String): DEntity

  @Insert
  suspend fun insert(dEntity: DEntity)

  @Update
  suspend fun update(dEntity: DEntity)

  @Delete
  suspend fun delete(dEntity: DEntity)
}
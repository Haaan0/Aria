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
import com.arialyy.aria.orm.entiry.UEntity

/**
 * @Author laoyuyu
 * @Description
 * @Date 19:23 AM 2023/1/16
 **/
@Dao
interface UEntityDao {
  @Query("SELECT * FROM DEntity WHERE :uId=uId")
  suspend fun queryUEntityById(uId: String): UEntity

  @Query("SELECT * FROM UEntity WHERE :filePath=filePath")
  suspend fun queryUEntityBySource(filePath: String): UEntity

  @Insert
  suspend fun insert(uEntity: UEntity)

  @Update
  suspend fun update(uEntity: UEntity)

  @Delete
  suspend fun delete(uEntity: UEntity)
}
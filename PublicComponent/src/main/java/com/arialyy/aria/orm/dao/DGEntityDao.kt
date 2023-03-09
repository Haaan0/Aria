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
import androidx.room.Transaction
import androidx.room.Update
import com.arialyy.aria.orm.entity.DEntity
import com.arialyy.aria.orm.entity.DGEntity
import com.arialyy.aria.orm.entity.DGSubRelation

/**
 * @Author laoyuyu
 * @Description
 * @Date 8:55 AM 2023/1/19
 **/
@Dao
interface DGEntityDao {

  @Transaction
  @Query("SELECT * FROM DGEntity")
  suspend fun getDGEntityList(): List<DGSubRelation>

  @Transaction
  @Query("SELECT * FROM DGEntity WHERE :path=savePath")
  suspend fun getDGEntityByPath(path: String): DGSubRelation?

  @Transaction
  @Query("SELECT * FROM DGEntity WHERE :gid=gid")
  suspend fun getDGEntityByGid(gid: Int): DGSubRelation

  @Transaction
  @Query("SELECT * FROM DGEntity WHERE :gid=gid")
  suspend fun getDGList(): List<DGSubRelation>

  @Insert
  suspend fun insertSubList(subList: List<DEntity>)

  @Update
  suspend fun update(dgEntity: DGEntity)

  @Delete
  suspend fun deleteSubList(subList: List<DEntity>)

  @Delete
  @Deprecated(
    "please use ",
    ReplaceWith("delete(dgEntity)", "com.arialyy.aria.orm.dao.DGEntityDao.delete")
  )
  suspend fun deleteDg(dgEntity: DGEntity)

  @Insert
  @Deprecated(
    "please use ",
    ReplaceWith("insert(dgEntity)", "com.arialyy.aria.orm.dao.DGEntityDao.insert")
  )
  suspend fun insertDg(dgEntity: DGEntity): Int

  @Transaction
  suspend fun delete(dgEntity: DGEntity) {
    deleteSubList(dgEntity.subList)
    deleteDg(dgEntity)
  }

  @Transaction
  suspend fun insert(dgEntity: DGEntity) {
    val gid = insertDg(dgEntity)
    dgEntity.subList.forEach {
      it.parentId = gid
    }
    insertSubList(dgEntity.subList)
  }
}
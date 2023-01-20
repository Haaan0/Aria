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
import com.arialyy.aria.orm.entity.BlockRecord
import com.arialyy.aria.orm.entity.RecordRelation
import com.arialyy.aria.orm.entity.TaskRecord

/**
 * @Author laoyuyu
 * @Description
 * @Date 10:34 AM 2023/1/19
 **/
@Dao
interface RecordDao {

  @Transaction
  @Query("SELECT * FROM TaskRecord WHERE :key=taskKey")
  suspend fun getTaskRecordByKey(key: String): RecordRelation

  @Insert
  @Deprecated(
    "please use ",
    ReplaceWith(
      "deleteTaskRecord(taskRecord)",
      "com.arialyy.aria.orm.dao.RecordDao.insert"
    )
  )
  suspend fun insertTaskRecord(taskRecord: TaskRecord)

  @Insert
  suspend fun insertSubList(blockList: List<BlockRecord>)

  @Update
  suspend fun update(record: TaskRecord)

  @Delete
  suspend fun deleteTaskRecord(record: TaskRecord)

  @Delete
  suspend fun deleteBlockRecord(blockRecord: List<BlockRecord>)

  @Transaction
  suspend fun insert(taskRecord: TaskRecord) {
    insertTaskRecord(taskRecord)
    insertSubList(taskRecord.blockList)
  }
}
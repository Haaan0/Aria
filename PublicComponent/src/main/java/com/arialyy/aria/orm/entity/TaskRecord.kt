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
package com.arialyy.aria.orm.entity

import android.net.Uri
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.arialyy.aria.core.task.ITask

@Entity(indices = [Index(value = ["taskKey"])])
@TypeConverters(FilePathConverter::class)
data class TaskRecord(
  @PrimaryKey(autoGenerate = true) val tId: Int = 0,
  val taskKey: String,
  /**
   * is uri
   */
  val filePath: Uri,
  /**
   * [ITask.SINGLE_DOWNLOAD] ...
   */
  val taskType: Int,
  val fileLen: Long,
  val blockNum: Int,
  val blockSize: Long
) {
  @Ignore
  val blockList: MutableList<BlockRecord> = mutableListOf()
}

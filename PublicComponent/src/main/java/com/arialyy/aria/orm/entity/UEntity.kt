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
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(indices = [Index(value = ["serverUrl", "filePath"])])
@TypeConverters(FilePathConverter::class)
data class UEntity(
  @PrimaryKey(autoGenerate = true) val uId: Int = 0,
  /**
   * uploader server url
   */
  val serverUrl: String,

  /**
   * file path
   */
  val filePath: Uri,

  /**
   * extended Information
   */
  var ext: String? = null,

  val createTime: Long,

  val updateTime: Long
)
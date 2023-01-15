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
package com.arialyy.aria.orm.entiry

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "u_entity", indices = [Index(value = ["serverUrl", "filePath"])])
data class UEntity(
  @PrimaryKey(autoGenerate = true) val uId: Int = 0,
  /**
   * uploader server url
   */
  val serverUrl: String,

  /**
   * file path
   */
  val filePath: String,

  /**
   * extended Information
   */
  var ext: String? = null
)
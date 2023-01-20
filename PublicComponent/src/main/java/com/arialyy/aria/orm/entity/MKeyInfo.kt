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

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MKeyInfo(

  @PrimaryKey(autoGenerate = true) val kId: Int,

  /**
   * 加密key保存地址
   */
  val keyPath: String,

  /**
   * 加密key的下载地址
   */
  val keyUrl: String,

  /**
   * 加密算法
   */
  val method: String,

  /**
   * key的iv值
   */
  val iv: String,

  /**
   * key的格式，可能为空
   */
  val keyFormat: String? = null,

  /**
   * key的格式版本，默认为1，如果是多个版本，使用"/"分隔，如："1", "1/2", or "1/2/5"
   */
  val keyFormatVersion: String = "1",
)

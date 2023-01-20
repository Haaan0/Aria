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
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.arialyy.aria.orm.DGUrlConverter

/**
 * @Author laoyuyu
 * @Description
 * @Date 4:32 PM 2023/1/16
 **/
@Entity(indices = [Index(value = ["savePath"])])
@TypeConverters(DGUrlConverter::class)
data class DGEntity(
  @PrimaryKey(autoGenerate = true) val dgId: Int = 0,

  /**
   * 任务组别名
   */
  val alias: String? = null,

  /**
   * 保存路径
   */
  val savePath: String,

  /**
   * 子任务url地址
   */
  val urls: List<String>,

  /**
   * extended Information
   */
  var ext: String? = null,

  val createTime: Long,

  val updateTime: Long
) {
  @Ignore
  internal var subList: MutableList<DEntity> = mutableListOf()
}
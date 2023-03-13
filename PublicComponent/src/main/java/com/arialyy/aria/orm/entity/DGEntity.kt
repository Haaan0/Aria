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
import com.arialyy.aria.core.DuaContext
import com.arialyy.aria.core.inf.BaseEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

/**
 * @Author laoyuyu
 * @Description
 * @Date 4:32 PM 2023/1/16
 **/
@Entity(indices = [Index(value = ["savePath"])])
@TypeConverters(DGUrlConverter::class, FilePathConverter::class)
@Parcelize
data class DGEntity(
  @PrimaryKey(autoGenerate = true) val dgId: Int = 0,

  /**
   * 任务组别名
   */
  val alias: String? = null,

  /**
   * 保存路径
   */
  val savePath: Uri,

  /**
   * 子任务url地址
   */
  val urls: List<String>,

  val subNameList: List<String>?

) : BaseEntity() {
  @Ignore
  var subList: MutableList<DEntity> = mutableListOf()
  override fun update() {
    updateTime = System.currentTimeMillis()
    DuaContext.duaScope.launch(Dispatchers.IO) {
      DuaContext.getServiceManager().getDbService().getDuaDb().getDGEntityDao()
        .update(this@DGEntity)
    }
  }
}
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
package com.arialyy.aria.orm

import android.content.ContentValues
import android.content.Context
import com.arialyy.aria.util.ALog

/**
 * @Author laoyuyu
 * @Description
 * @Date 2:50 下午 2022/4/25
 **/
class DelegateInsert : AbsDelegate() {
  /**
   * 插入多条记录
   */
  @Synchronized fun <T : DbEntity> insertManyData(context: Context, dbEntities: List<T>) {
    for (entity in dbEntities) {
      insertData(context, entity)
    }
  }

  /**
   * 插入数据
   */
  @Synchronized fun insertData(context: Context, dbEntity: DbEntity) {
    val value: ContentValues? = DbUtil.createValues(dbEntity)
    if (value == null) {
      ALog.e(TAG, "保存记录失败，记录没有属性字段")
    } else {
      val uri = DbContentProvider.createRequestUrl(context, dbEntity.javaClass)
      val responseUri = context.contentResolver.insert(uri, value)
      val rowId = responseUri?.getQueryParameter(DbContentProvider.KEY_ROW_ID)
      if (rowId.isNullOrBlank()) {
        ALog.e(TAG, "插入失败，rowId为空")
      } else {
        dbEntity.rowID = rowId.toLong()
        ALog.d(TAG, "插入完成，responseUrl = $responseUri")
      }
    }
  }
}
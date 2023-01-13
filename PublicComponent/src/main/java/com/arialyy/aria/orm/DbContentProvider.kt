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

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import com.arialyy.aria.util.ALog
import com.arialyy.aria.util.CommonUtil

/**
 * @Author laoyuyu
 * @Description
 * @Date 2:25 下午 2022/4/25
 **/
class DbContentProvider : ContentProvider() {
  val TAG = "DbContentProvider"

  companion object {
    // const val KEY_TABLE_NAME = "tableName"
    const val KEY_ROW_ID = "rowId"
    const val KEY_TABLE_CLAZZ = "tableClazz"
    const val KEY_LIMIT = "limit"

    fun createRequestUrl(context: Context, clazz: Class<*>): Uri {
      return Uri.parse("content://${context.packageName}.com.arialyy.aria.provide/request?${KEY_TABLE_CLAZZ}=${clazz.name}")
    }

    fun getRequestUrl(context: Context): String {
      return "content://${context.packageName}.com.arialyy.aria.provide/request"
    }

    fun getResponseUrl(context: Context): String {
      return "content://${context.packageName}.com.arialyy.aria.provide/response"
    }
  }

  /**
   * key: clazzPath, value: tableName
   */
  private val tableExistMap = mutableMapOf<String, String>()

  private lateinit var helper: SqlHelper

  override fun onCreate(): Boolean {
    helper = SqlHelper.init(context)
    return true
  }

  private fun getTableName(uri: Uri): String? {
    val db = SqlUtil.checkDb(helper.db)
    val clazzName = uri.getQueryParameter(KEY_TABLE_CLAZZ)
    if (tableExistMap[clazzName!!] != null) {
      return tableExistMap[clazzName]
    }

    val clazz: Class<out DbEntity>? =
      javaClass.classLoader?.loadClass(clazzName) as Class<out DbEntity>?
    if (clazz == null) {
      ALog.e(TAG, "【$clazz】为空")
      return null
    }
    SqlUtil.checkOrCreateTable(db, clazz)
    val tableName = CommonUtil.getClassName(clazz)
    tableExistMap[clazzName] = tableName
    return tableName
  }

  override fun query(
    uri: Uri,
    projection: Array<out String>?,
    selection: String?,
    selectionArgs: Array<out String>?,
    sortOrder: String?
  ): Cursor? {
    val db = SqlUtil.checkDb(helper.db)
    val tableName = getTableName(uri)
    if (tableName.isNullOrBlank()) {
      return null
    }
    val clazzName = uri.getQueryParameter(KEY_TABLE_CLAZZ)
    val clazz = javaClass.classLoader?.loadClass(clazzName) as Class<out DbEntity>?
    val columns = SqlUtil.getColumns(clazz)
    val limit = uri.getQueryParameter(KEY_LIMIT)
    return db.query(
      tableName,
      columns.toTypedArray(),
      selection,
      selectionArgs,
      null,
      null,
      null,
      limit
    )
  }

  override fun getType(uri: Uri): String? {
    return null
  }

  override fun insert(uri: Uri, values: ContentValues?): Uri? {
    val db = SqlUtil.checkDb(helper.db)
    val tableName = getTableName(uri)
    if (tableName.isNullOrBlank()) {
      return null
    }
    db.beginTransaction()
    try {
      val rowId = db.insert(tableName, null, values)
      if (rowId == -1L) {
        return null
      }
      return Uri.parse("${getResponseUrl(context!!)}?${KEY_ROW_ID}=$rowId")
    } finally {
      db.endTransaction()
    }
  }

  override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
    val db = SqlUtil.checkDb(helper.db)
    val tableName = getTableName(uri)
    if (tableName.isNullOrBlank()) {
      return -1
    }
    db.beginTransaction()
    try {
      val rowId = db.delete(tableName, selection, selectionArgs)
      if (rowId == -1) {
        return -1
      }
      return rowId
    } finally {
      db.endTransaction()
    }
  }

  override fun update(
    uri: Uri,
    values: ContentValues?,
    selection: String?,
    selectionArgs: Array<out String>?
  ): Int {
    val db = SqlUtil.checkDb(helper.db)
    val tableName = getTableName(uri)
    if (tableName.isNullOrBlank()) {
      return -1
    }
    db.beginTransaction()
    try {
      val rowId = db.update(tableName, values, selection, selectionArgs)
      if (rowId == -1) {
        return -1
      }
      return rowId
    } finally {
      db.endTransaction()
    }
  }
}
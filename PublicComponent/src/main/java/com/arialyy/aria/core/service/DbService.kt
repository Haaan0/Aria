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
package com.arialyy.aria.core.service

import android.content.Context
import androidx.room.RoomDatabase.Builder
import com.arialyy.aria.core.config.AutoGenerateConstance
import com.arialyy.aria.orm.DefaultDbProvider
import com.arialyy.aria.orm.DuaDb
import com.arialyy.aria.util.ReflectionUtil

/**
 * @Author laoyuyu
 * @Description
 * @Date 19:36 AM 2023/1/16
 **/
open class DbService : IService {
  private var duaDb: DuaDb? = null

  /**
   * Find a user-defined database
   */
  private fun findCustomDatabase(context: Context): Builder<DuaDb>? {
    try {
      val clazz = javaClass.classLoader.loadClass(AutoGenerateConstance.GenerateClassName)
        ?: return null

      val obj = clazz.newInstance()

      val method = ReflectionUtil.getMethod(clazz, "generateDb", Context::class.java) ?: return null

      return method.invoke(obj, context) as Builder<DuaDb>?
    } catch (e: java.lang.Exception) {
      return null
    }
  }

  // fun findDEntity(dId: Int): DEntity? {
  //   if (duaDb == null) {
  //     return null
  //   }
  // }

  override fun init(context: Context) {
    var customDb = findCustomDatabase(context)
    if (customDb == null) {
      customDb = DefaultDbProvider().generateDb(context)
    }
    duaDb = customDb
      .build()
  }
}
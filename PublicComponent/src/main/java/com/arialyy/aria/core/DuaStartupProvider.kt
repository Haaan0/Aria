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
package com.arialyy.aria.core

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.RoomDatabase.Builder
import androidx.startup.Initializer
import com.arialyy.aria.core.config.AutoGenerateConstance
import com.arialyy.aria.orm.DefaultDbProvider
import com.arialyy.aria.util.ReflectionUtil

class DuaStartupProvider : Initializer<Unit> {
  /**
   * Find a user-defined database
   */
  private fun findCustomDatabase(context: Context): Builder<RoomDatabase>? {
    try {
      val clazz = javaClass.classLoader.loadClass(AutoGenerateConstance.GenerateClassName)
        ?: return null

      val obj = clazz.newInstance()

      val method = ReflectionUtil.getMethod(clazz, "generateDb", Context::class.java) ?: return null

      return method.invoke(obj, context) as Builder<RoomDatabase>?
    } catch (e: java.lang.Exception) {
      return null
    }
  }

  override fun create(context: Context) {
    var customDb = findCustomDatabase(context)
    if (customDb == null) {
      customDb = DefaultDbProvider().generateDb(context)
    }
    customDb.build()
    // .addMigrations(MIGRATION_2_3(), MIGRATION_3_4())
  }

  override fun dependencies(): MutableList<Class<out Initializer<*>>> {
    return mutableListOf()
  }
}
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
package com.arialyy.aria.core.provider

import android.content.Context
import androidx.startup.Initializer
import com.arialyy.aria.core.DuaContext
import com.arialyy.aria.core.service.DbService
import timber.log.Timber
import timber.log.Timber.DebugTree

class DuaStartupProvider : Initializer<Unit> {

  override fun create(context: Context) {
    DuaContext.getServiceManager().let {
      it.registerService(DuaContext.DB_SERVICE, context, DbService::class.java)
    }
    initLog()
  }

  private fun initLog() {
    if (Timber.treeCount == 0) {
      Timber.plant(DebugTree())
    }
  }

  override fun dependencies(): MutableList<Class<out Initializer<*>>> {
    return mutableListOf()
  }
}
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

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * @Author laoyuyu
 * @Description
 * @Date 7:24 PM 2023/1/16
 **/
@ProvidedTypeConverter
class DGUrlConverter {
  private val gson by lazy {
    Gson()
  }

  @TypeConverter
  fun stringToList(string: String?): List<String> {
    if (string.isNullOrEmpty()) return emptyList()
    return gson.fromJson(string, object : TypeToken<List<String>>() {}.type)
  }

  @TypeConverter
  fun listToString(strList: List<String>): String {
    return gson.toJson(strList)
  }
}
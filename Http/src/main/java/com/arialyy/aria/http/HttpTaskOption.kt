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
package com.arialyy.aria.http

import com.arialyy.aria.core.common.TaskOption
import com.arialyy.aria.core.task.ITaskInterceptor

/**
 * @Author laoyuyu
 * @Description
 * @Date 12:47 PM 2023/1/22
 **/
class HttpTaskOption : TaskOption() {

  var httpOption: HttpOption? = null
  var taskInterceptor = mutableListOf<ITaskInterceptor>()
  lateinit var taskOptionDelegate: IHttpTaskOptionDelegate

  fun <T : IHttpTaskOptionDelegate> getOptionDelegate(clazz: Class<T>): T {
    return taskOptionDelegate as T
  }
}
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

import com.arialyy.aria.core.task.ITaskInterceptor
import timber.log.Timber

/**
 * @Author laoyuyu
 * @Description
 * @Date 12:40 PM 2023/1/22
 **/
open class HttpBaseStartController(val target: Any) {
  protected var httpTaskOption = HttpTaskOption()

  /**
   * set http params, link Header
   */
  open fun setHttpOption(httpOption: HttpOption): HttpBaseStartController {
    httpTaskOption.httpOption = httpOption
    return this
  }

  /**
   * use multi-threaded download file, if file size <= 5m, this setting is not valid
   * @param threadNum  range [1 - 32]
   */
  open fun setThreadNum(threadNum: Int): HttpBaseStartController {
    if (threadNum !in 1..32) {
      Timber.e("set thread num fail, only 0 < threadNum < 33, threadNum: $threadNum")
      return this
    }
    httpTaskOption.threadNum = threadNum
    return this
  }

  /**
   * if you want to do something before the task is executed, you can set up a task interceptor
   * eg: determine the network status before task execution
   */
  open fun setTaskInterceptor(taskInterceptor: ITaskInterceptor): HttpBaseStartController {
    httpTaskOption.taskInterceptor.add(taskInterceptor)
    return this
  }
}
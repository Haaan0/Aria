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
package com.arialyy.dua.group

import com.arialyy.aria.core.task.ITaskInterceptor
import com.arialyy.aria.core.task.TaskChain
import com.arialyy.aria.core.task.TaskResp
import com.arialyy.aria.util.CheckUtil
import timber.log.Timber

/**
 * 1. Check if the save path is valid
 * 2. Check all sub-task download addresses
 * @Author laoyuyu
 * @Description
 * @Date 8:56 PM 2023/3/6
 **/
internal class HttpDGCheckInterceptor : ITaskInterceptor {

  override suspend fun interceptor(chain: TaskChain): TaskResp {
    // if (optionAdapter.subUrl.isEmpty()){
    //   Timber.e("sub-task list is empty")
    //   return -1
    // }
    //
    // optionAdapter.subUrl.forEach {
    //   if (!CheckUtil.checkUrl(it)){
    //     Timber.e("invalid url: $it")
    //     return -1
    //   }
    // }

  }
}
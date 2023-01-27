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
package com.arialyy.aria.core.command

import com.arialyy.aria.core.command.ICmdInterceptor.IChain
import timber.log.Timber

/**
 * check task state
 * @Author laoyuyu
 * @Description
 * @Date 4:15 PM 2023/1/27
 **/
internal class TaskCheckInterceptor : ICmdInterceptor {
  /**
   * check task state
   * 1、if task already in queue, interrupt cmd
   * 2、if task already complete, interrupt cmd
   */
  override fun interceptor(chain: IChain): CmdResp {
    if (chain.getQueue().taskExists(chain.getTask().taskId)) {
      Timber.d("task already in queue")
      return CmdResp(CmdResp.CODE_INTERRUPT)
    }
    if (chain.getTask().taskState.isCompleted()) {
      Timber.d("task already complete")
      return CmdResp(CmdResp.CODE_INTERRUPT)
    }
    return chain.proceed(chain.getTask())
  }
}
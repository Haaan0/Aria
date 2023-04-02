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

package com.arialyy.aria.core.command;

import com.arialyy.aria.core.DuaContext
import com.arialyy.aria.core.task.ITask
import com.arialyy.aria.core.task.ThreadTaskManager2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Created by lyy on 2016/9/20.
 * 取消命令
 */
class DeleteCmd(task: ITask) : AbsCmd(task) {

  override fun executeCmd(): CmdResp {
    val resp = interceptor()
    if (resp.isInterrupt()) {
      Timber.w("interruption occurred, cancel cmd")
      return resp
    }
    DuaContext.duaScope.launch(Dispatchers.IO) {
      ThreadTaskManager2.stopThreadTask(mTask!!.taskId, true)
    }
    return CmdResp(CmdResp.CODE_COMPLETE)
  }

}
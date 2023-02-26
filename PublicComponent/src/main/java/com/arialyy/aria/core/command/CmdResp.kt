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

/**
 * @Author laoyuyu
 * @Description
 * @Date 10:39 AM 2023/1/27
 **/
class CmdResp(val code: Int = CODE_DEF) {
  companion object {
    const val CODE_COMPLETE = 1
    const val CODE_INTERRUPT = 999
    const val CODE_DEF = 0
    const val CODE_TASK_NOT_FOUND = 2
  }

  /**
   * Whether to interrupt or not
   */
  fun isInterrupt() = code == CODE_INTERRUPT

  fun isComplete() = code == CODE_COMPLETE
}
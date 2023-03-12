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

import com.arialyy.aria.core.listener.IEventListener
import com.arialyy.aria.exception.AriaException

/**
 * @Author laoyuyu
 * @Description
 * @Date 10:13 2023/3/12
 **/
internal class HttpSubListener : IEventListener {
  override fun onPre() {
    TODO("Not yet implemented")
  }

  override fun onStart(startLocation: Long) {
    TODO("Not yet implemented")
  }

  override fun onResume(resumeLocation: Long) {
    TODO("Not yet implemented")
  }

  override fun onProgress(currentLocation: Long) {
    TODO("Not yet implemented")
  }

  override fun onStop(stopLocation: Long) {
    TODO("Not yet implemented")
  }

  override fun onComplete() {
    TODO("Not yet implemented")
  }

  override fun onCancel() {
    TODO("Not yet implemented")
  }

  override fun onFail(needRetry: Boolean, e: AriaException?) {
    TODO("Not yet implemented")
  }
}
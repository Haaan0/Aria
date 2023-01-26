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

import androidx.lifecycle.LifecycleOwner
import com.arialyy.aria.core.common.receiver.LifLifecycleReceiver
import com.arialyy.aria.core.config.Configuration

/**
 * @Author laoyuyu
 * @Description
 * @Date 10:48 AM 2023/1/20
 **/
@Deprecated("有问题，微软有dua的工程在github上")// 使用cof? exco?
object Dua {

  fun with(lifecycle: LifecycleOwner): LifLifecycleReceiver {
    return LifLifecycleReceiver(lifecycle)
  }

  fun getCommonConfig() = Configuration.getInstance().cCommonCfg
}
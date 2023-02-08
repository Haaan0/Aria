/*
 * Copyright (C) 2016  AriaLyy(https://github.com/AriaLyy/Aria)
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
package com.arialyy.aria.core.task

import com.arialyy.aria.core.config.Configuration
import com.arialyy.aria.core.inf.IEntity
import com.arialyy.aria.util.CommonUtil

/**
 * @Author laoyuyu
 * @Description
 * @Date 10:04 PM 2023/1/24
 **/
class TaskState {

  /**
   * need to try again?，default: false
   */
  var needRetry = false

  /**
   * already fail num
   */
  var failNum = 0

  var state: Int = IEntity.STATE_WAIT

  /**
   * current task progress, unit: byte
   */
  var curProgress: Long = 0


  /**
   * Bytes transferred in 1 second, if file size 0, return 0
   * curSpeed, unit: byte/s
   */
  var speed: Long = 0
    get() {
      return if (fileSize == 0L) 0 else field
    }
    set(value) {
      if (value <= 0L) {
        timeLeft = Int.MAX_VALUE
        field = 0L
        return
      }
      if (fileSize == 0L) {
        timeLeft = Int.MAX_VALUE
      }
      timeLeft = ((fileSize - curProgress) / value).toInt()
      field = value
    }

  var fileSize: Long = 0

  /**
   * task time left, unit: s
   */
  var timeLeft: Int = Int.Companion.MAX_VALUE

  val blockSize = BlockState.BLOCK_SIZE

  fun getPercent() = ((curProgress * 100) / fileSize).toInt()

  fun isCompleted() = state == IEntity.STATE_COMPLETE

  fun isStopped() = state == IEntity.STATE_STOP

  fun isRunning() = state == IEntity.STATE_RUNNING

  /**
   * you need set params in config
   * @return Returns the converted speed:1b/s、1kb/s、1mb/s、1gb/s、1tb/s
   * xml:
   * <pre>
   * `<xml>
   * <download>
   * ...
   * <convertSpeed value="true"/>
   * </download>
   *
   * code:
   * Dua.getCommonConfig().setConvertSpeed(true);
   * </xml>
   *
   * </pre>
   */
  fun getConvertSpeed(): String {
    if (Configuration.getInstance().cCommonCfg.isConvertSpeed) {
      return CommonUtil.formatFileSize((if (speed < 0L) 0L else speed.toDouble()) as Double) + "/s"
    }
    return "0b/s"
  }
}
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
package com.arialyy.aria.core.inf

/**
 * @Author laoyuyu
 * @Description
 * @Date 12:32 PM 2023/1/22
 **/
interface IStartController {
  /**
   * 添加任务
   *
   * @return 正常添加，返回任务id，否则返回-1
   */
  fun add(): Long

  /**
   * 创建并开始任务
   *
   * @return 正常启动，返回任务id，否则返回-1
   */
  fun create(): Long

  /**
   * 恢复任务
   * @return 正常启动，返回任务id，否则返回-1
   */
  fun resume(): Long

  /**
   * 正常来说，当执行队列满时，调用恢复任务接口，只能将任务放到缓存队列中。
   * 如果希望调用恢复接口，马上进入执行队列，需要使用该方法
   *
   * @param newStart true 立即将任务恢复到执行队列中
   * @return 正常启动，返回任务id，否则返回-1
   */
  fun resume(newStart: Boolean): Long
}
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
package com.arialyy.aria.core.task

/**
 * @Author laoyuyu
 * @Description
 * @Date 11:06 AM 2023/1/27
 **/
class TaskChain(
  private val interceptors: List<ITaskInterceptor>,
  private val index: Int = 0,
  private val task: ITask,
) : ITaskInterceptor.IChain {

  override fun getTask(): ITask {
    return task
  }

  override fun proceed(task: ITask): TaskResp {
    val next = TaskChain(interceptors, index, task)
    val interceptor = interceptors[index]
    return interceptor.interceptor(next)
  }
}
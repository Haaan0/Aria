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

import com.arialyy.aria.core.inf.ITaskUtil
import com.arialyy.aria.core.listener.IEventListener
import com.arialyy.aria.core.task.ITaskInterceptor.IChain

/**
 * @Author laoyuyu
 * @Description
 * @Date 1:12 PM 2023/1/28
 **/
abstract class AbsTaskUtil : ITaskUtil {
  private lateinit var mTask: ITask
  private lateinit var mEventListener: IEventListener

  private val mUserInterceptor = mutableListOf<ITaskInterceptor>()
  private val mCoreInterceptor = mutableListOf<ITaskInterceptor>()

  override fun init(task: ITask, listener: IEventListener) {
    mTask = task
    mEventListener = listener
  }

  protected fun getTask() = mTask

  /**
   * add user interceptor
   */
  protected fun addInterceptors(userInterceptors: List<ITaskInterceptor>) {
    mUserInterceptor.addAll(userInterceptors)
  }

  protected fun addCoreInterceptor(interceptor: ITaskInterceptor) {
    mCoreInterceptor.add(interceptor)
  }

  /**
   * if interruption occurred, stop cmd
   */
  protected open fun interceptor(): TaskResp? {
    if (mUserInterceptor.isEmpty()) {
      return null
    }
    val interceptors: MutableList<ITaskInterceptor> = ArrayList()
    interceptors.addAll(mUserInterceptor)
    interceptors.addAll(mCoreInterceptor)
    val chain: IChain = TaskChain(interceptors, 0, mTask)
    return chain.proceed(mTask)
  }
}
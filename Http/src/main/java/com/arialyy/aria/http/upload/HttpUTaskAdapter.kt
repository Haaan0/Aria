///*
// * Copyright (C) 2016 AriaLyy(https://github.com/AriaLyy/Aria)
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.arialyy.aria.http.upload
//
//import android.os.Looper
//import com.arialyy.aria.core.DuaContext
//import com.arialyy.aria.core.inf.ITaskManager
//import com.arialyy.aria.core.task.AbsTaskAdapter
//import com.arialyy.aria.core.task.DBlockManager
//import com.arialyy.aria.core.task.TaskResp
//import com.arialyy.aria.exception.AriaException
//import com.arialyy.aria.http.HttpTaskOption
//import com.arialyy.aria.http.download.HttpBlockThreadInterceptor
//import com.arialyy.aria.http.download.TimerInterceptor
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//
///**
// * @Author laoyuyu
// * @Description
// * @Date 9:21 PM 2023/2/21
// **/
//class HttpUTaskAdapter : AbsTaskAdapter() {
//  private var blockManager: DBlockManager? = null
//
//  override fun getBlockManager(): ITaskManager {
//    if (blockManager == null) {
//      blockManager = DBlockManager(getTask())
//    }
//    return blockManager!!
//  }
//
//  override fun isRunning(): Boolean {
//    return blockManager?.isRunning ?: false
//  }
//
//  override fun cancel() {
//    TODO("Not yet implemented")
//  }
//
//  override fun stop() {
//    TODO("Not yet implemented")
//  }
//
//  override fun start() {
//    getTask().getTaskOption(HttpTaskOption::class.java).taskInterceptor.let {
//      if (it.isNotEmpty()) {
//        addInterceptors(it)
//      }
//    }
//    DuaContext.duaScope.launch(Dispatchers.IO) {
//      Looper.prepare()
//      addCoreInterceptor(TimerInterceptor())
//      addCoreInterceptor(HttpUBlockInterceptor())
//      addCoreInterceptor(HttpBlockThreadInterceptor())
//      val resp = interceptor()
//      if (resp == null || resp.code != TaskResp.CODE_SUCCESS) {
//        getTask().getTaskOption(HttpTaskOption::class.java).eventListener.onFail(
//          false,
//          AriaException("start task fail, task interrupt, code: ${resp?.code ?: TaskResp.CODE_INTERRUPT}")
//        )
//        blockManager?.stop()
//        return@launch
//      }
//      Looper.loop()
//    }
//  }
//}
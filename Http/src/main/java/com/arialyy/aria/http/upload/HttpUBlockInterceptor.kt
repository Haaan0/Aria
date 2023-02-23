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
//import com.arialyy.aria.core.inf.IBlockManager
//import com.arialyy.aria.core.task.ITask
//import com.arialyy.aria.core.task.ITaskInterceptor
//import com.arialyy.aria.core.task.TaskChain
//import com.arialyy.aria.core.task.TaskResp
//import com.arialyy.aria.http.HttpTaskOption
//import com.arialyy.aria.orm.entity.TaskRecord
//
///**
// * @Author laoyuyu
// * @Description
// * @Date 9:47 PM 2023/2/21
// **/
//class HttpUBlockInterceptor: ITaskInterceptor {
//
//  private lateinit var task: ITask
//  private lateinit var option: HttpTaskOption
//  private lateinit var blockManager: IBlockManager
//  private lateinit var taskRecord: TaskRecord
//
//  override suspend fun interceptor(chain: TaskChain): TaskResp {
//
//  }
//}
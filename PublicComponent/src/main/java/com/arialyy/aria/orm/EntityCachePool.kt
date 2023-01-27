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
package com.arialyy.aria.orm

import com.arialyy.aria.core.inf.BaseEntity
import com.arialyy.aria.core.inf.IEntity
import timber.log.Timber

/**
 * @Author laoyuyu
 * @Description
 * @Date 21:43 AM 2023/1/22
 **/
object EntityCachePool {
  /**
   * key: taskId
   */
  private val entityMap = hashMapOf<Int, BaseEntity>()

  fun putEntity(taskId: Int, entity: BaseEntity) {
    if (taskId <= 0) {
      Timber.e("invalid taskId: ${taskId}")
      return
    }
    entityMap[taskId] = entity
  }

  /**
   * get entity by taskId
   */
  fun getEntity(taskId: Int): IEntity? {
    return entityMap[taskId]
  }

  /**
   * update entity state, if [entityMap] no cache, update fail
   * @param state [IEntity]
   * @param currentProgress task current progress
   */
  fun updateState(taskId: Int, state: Int, currentProgress: Long) {
    if (taskId <= 0) {
      Timber.e("invalid taskId: $taskId")
      return
    }
    val cacheE = entityMap[taskId]
    if (cacheE == null) {
      Timber.e("update state fail, taskId not found, taskId: $taskId")
      return
    }
    cacheE.state = state
    cacheE.progress = currentProgress
    cacheE.update()
  }
}
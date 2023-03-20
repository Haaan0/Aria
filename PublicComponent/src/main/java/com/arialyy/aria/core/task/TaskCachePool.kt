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

import android.net.Uri
import com.arialyy.aria.core.inf.BaseEntity
import com.arialyy.aria.core.inf.IEntity
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

/**
 * @Author laoyuyu
 * @Description
 * @Date 21:43 AM 2023/1/22
 **/
object TaskCachePool {
  /**
   * key: taskId
   */
  private val entityMap = ConcurrentHashMap<Int, BaseEntity>()

  private val taskMap = ConcurrentHashMap<String, ITask>()

  fun removeTask(task: ITask) {
    when {
      task is SingleDownloadTask -> {
        taskMap.remove(task.url)
      }
      task::javaClass.name == "com.arialyy.dua.group.HttpDGroupTask" -> {
        taskMap.remove(task.filePath)
      }
    }
  }

  /**
   * if task is completed, stopped, canceled, return null
   */
  fun getTaskByKey(key: String) = taskMap[key]

  fun getTaskById(taskId: Int): ITask? {
    return taskMap.values.find { it.taskId == taskId }
  }

  fun putTask(task: ITask) {
    when {
      task is SingleDownloadTask -> {
        taskMap[task.url] = task
      }
      task::javaClass.name == "com.arialyy.dua.group.HttpDGroupTask" -> {
        taskMap[task.filePath] = task
      }
    }
  }

  /**
   * find task by filePath
   */
  fun findTaskByPath(filePath: Uri): ITask? {
    taskMap.values.forEach {
      if (it.filePath == filePath.toString()) {
        return it
      }
    }
    return null
  }

  fun putEntity(taskId: Int, entity: BaseEntity) {
    if (taskId <= 0) {
      Timber.e("invalid taskId: $taskId")
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
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

import com.arialyy.aria.core.DuaContext
import com.arialyy.aria.core.inf.IEntity
import com.arialyy.aria.core.listener.AbsEventListener
import com.arialyy.aria.core.listener.ISchedulers
import com.arialyy.aria.core.task.TaskCachePool
import com.arialyy.aria.orm.entity.DEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @Author laoyuyu
 * @Description
 * @Date 9:15 PM 2023/3/6
 **/
internal class HttpDGEventListener(task: HttpDGTask) : AbsEventListener(task) {
  override fun onComplete() {
    handleSpeed(0)
    sendInState2Target(ISchedulers.COMPLETE)
    saveData(IEntity.STATE_COMPLETE, task.taskState.fileSize)
  }

  override fun handleCancel() {
    DuaContext.duaScope.launch(Dispatchers.IO) {
      val entity = TaskCachePool.getEntity(taskId = task.taskId)
      val db = DuaContext.getServiceManager().getDbService().getDuaDb()
      db.getRecordDao().deleteTaskRecord(task.taskState.taskRecord)
      db.getDEntityDao().delete(entity as DEntity)
    }
  }
}
package com.arialyy.aria.queue

import android.content.Context
import com.arialyy.aria.core.DuaContext
import com.arialyy.aria.core.inf.IComponentInit

/**
 * @Author laoyuyu
 * @Description
 * @Date 4:08 PM 2023/1/26
 **/
class QueueComponent : IComponentInit {
  override fun init(context: Context) {
    DuaContext.getServiceManager().registerService(DuaContext.D_QUEUE, DTaskQueue.getInstance())
    DuaContext.getServiceManager().registerService(DuaContext.U_QUEUE, UTaskQueue.getInstance())
  }
}
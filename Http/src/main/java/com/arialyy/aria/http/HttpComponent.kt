package com.arialyy.aria.http

import android.content.Context
import com.arialyy.aria.core.DuaContext
import com.arialyy.aria.core.inf.IComponentInit
import com.arialyy.aria.queue.DTaskQueue
import com.arialyy.aria.queue.UTaskQueue

/**
 * @Author laoyuyu
 * @Description
 * @Date 3:40 PM 2023/1/26
 **/
class HttpComponent : IComponentInit {
  override fun init(context: Context) {
    DuaContext.getServiceManager().registerService(DuaContext.D_QUEUE, com.arialyy.aria.queue.DTaskQueue.getInstance())
    DuaContext.getServiceManager().registerService(DuaContext.U_QUEUE, com.arialyy.aria.queue.UTaskQueue.getInstance())

  }
}
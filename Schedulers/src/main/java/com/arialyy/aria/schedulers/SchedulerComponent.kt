package com.arialyy.aria.schedulers

import android.content.Context
import com.arialyy.aria.core.DuaContext
import com.arialyy.aria.core.inf.IComponentInit

/**
 * @Author laoyuyu
 * @Description
 * @Date 4:08 PM 2023/1/26
 **/
class SchedulerComponent : IComponentInit {
  override fun init(context: Context) {
    DuaContext.getServiceManager()
      .registerService(DuaContext.SCHEDULER, TaskSchedulers.getInstance())
  }
}
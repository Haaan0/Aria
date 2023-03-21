package com.arialyy.aria.http

import android.content.Context
import com.arialyy.aria.core.event.Event
import com.arialyy.aria.core.event.EventMsgUtil
import com.arialyy.aria.core.event.ResumeAllEvent
import com.arialyy.aria.core.inf.IComponentInit

/**
 * @Author laoyuyu
 * @Description
 * @Date 3:40 PM 2023/1/26
 **/
class HttpComponent : IComponentInit {
  override fun init(context: Context) {
    EventMsgUtil.getDefault().register(this)
  }

  @Event
  fun resumeAll(resumeAllEvent: ResumeAllEvent) {

  }

}
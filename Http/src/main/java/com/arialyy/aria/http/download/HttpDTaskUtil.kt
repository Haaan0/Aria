package com.arialyy.aria.http.download

import com.arialyy.aria.core.DuaContext
import com.arialyy.aria.core.task.AbsTaskUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @Author laoyuyu
 * @Description
 * @Date 1:47 PM 2023/1/28
 **/
internal class HttpDTaskUtil : AbsTaskUtil() {

  override fun isRunning(): Boolean {
    TODO("Not yet implemented")
  }

  override fun cancel() {
    TODO("Not yet implemented")
  }

  override fun stop() {
    TODO("Not yet implemented")
  }

  override fun start() {
    getTask().getTaskOption(HttpDTaskOption::class.java).taskInterceptor.let {
      if (it.isNotEmpty()) {
        addInterceptors(it)
      }
    }
    addCoreInterceptor(HttpDHeaderInterceptor())
    DuaContext.duaScope.launch(Dispatchers.IO) {
      val resp = interceptor()
    }
  }
}
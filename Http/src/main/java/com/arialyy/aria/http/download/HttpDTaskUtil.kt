package com.arialyy.aria.http.download

import android.os.Looper
import com.arialyy.aria.core.DuaContext
import com.arialyy.aria.core.inf.IBlockManager
import com.arialyy.aria.core.task.AbsTaskUtil
import com.arialyy.aria.core.task.BlockManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @Author laoyuyu
 * @Description
 * @Date 1:47 PM 2023/1/28
 **/
internal class HttpDTaskUtil : AbsTaskUtil() {

  private var blockManager: BlockManager? = null
  override fun getBlockManager(): IBlockManager {
    if (blockManager == null) {
      blockManager = BlockManager(getTask().getTaskOption(HttpDTaskOption::class.java).taskListener)
    }
    return blockManager!!
  }

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
    DuaContext.duaScope.launch(Dispatchers.IO) {
      Looper.prepare()
      blockManager?.setLopper(Looper.myLooper()!!)
      addCoreInterceptor(TimerInterceptor())
      addCoreInterceptor(HttpDHeaderInterceptor())
      addCoreInterceptor(HttpDBlockInterceptor())
      val resp = interceptor()
      Looper.loop()
    }
  }
}
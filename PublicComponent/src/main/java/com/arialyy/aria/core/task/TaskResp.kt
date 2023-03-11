package com.arialyy.aria.core.task

/**
 * @Author laoyuyu
 * @Description
 * @Date 1:43 PM 2023/1/28
 **/
class TaskResp(val code: Int = CODE_DEF) {
  companion object {
    const val CODE_SUCCESS = 1
    const val CODE_INTERRUPT = 999
    const val CODE_DEF = 0
  }

  var fileSize: Long = 0
}
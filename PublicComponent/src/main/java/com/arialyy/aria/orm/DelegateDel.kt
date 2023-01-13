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

import android.content.Context
import com.arialyy.aria.util.ALog
import com.arialyy.aria.util.CommonUtil

/**
 * @Author laoyuyu
 * @Description
 * @Date 3:55 下午 2022/4/25
 **/
class DelegateDel : AbsDelegate() {

  /**
   * 删除某条数据
   */
  @Synchronized fun <T : DbEntity?> delData(
    context: Context,
    clazz: Class<T>,
    vararg expression: String
  ) {
    if (!CommonUtil.checkSqlExpression(*expression)) {
      return
    }
    val selectionArgs = arrayOfNulls<String>(expression.size - 1)
    expression.forEachIndexed { index, s ->
      if (index == 0) {
        return@forEachIndexed
      }
      selectionArgs[index - 1] = s
    }

    val uri = DbContentProvider.createRequestUrl(context, clazz)
    val rowId = context.contentResolver.delete(uri, expression[0], selectionArgs)
    if (rowId != -1) {
      ALog.d(TAG, "删除成功，删除的rowId = $rowId")
    }
  }
}
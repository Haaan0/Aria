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
package com.arialyy.aria.http.download

import com.arialyy.aria.core.processor.IHttpFileLenAdapter
import com.arialyy.aria.core.task.IThreadTask
import com.arialyy.aria.http.IHttpTaskOptionAdapter
import com.arialyy.aria.orm.entity.BlockRecord

/**
 * @Author laoyuyu
 * @Description
 * @Date 2:03 PM 2023/3/6
 **/
class HttpDOptionAdapter : IHttpTaskOptionAdapter {
  var fileSizeAdapter: IHttpFileLenAdapter? = null
  var isChunkTask = false

  /**
   * whether block is supported, true: supported
   */
  var isSupportResume = true

  /**
   * whether resume task is supported
   * 1. in download task, if file length not obtained, isSupportResume = false
   * 2. in upload task, if service not supported resume, isSupportResume = false
   */
  var isSupportBlock = true

  var fileName: String? = null

  private val unfinishedBlock = mutableListOf<BlockRecord>()
  var threadList = mutableListOf<IThreadTask>()
    set(value) {
      field.clear()
      field.addAll(value)
    }

  fun putUnfinishedBlock(record: BlockRecord) {
    unfinishedBlock.add(record)
  }

  fun getUnfinishedBlockList(): List<BlockRecord> {
    return unfinishedBlock
  }

}
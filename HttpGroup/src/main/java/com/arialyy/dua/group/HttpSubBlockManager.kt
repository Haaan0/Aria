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

import android.os.Handler
import com.arialyy.aria.core.inf.IBlockManager
import com.arialyy.aria.core.task.IThreadTask
import com.arialyy.aria.orm.entity.BlockRecord

/**
 * @Author laoyuyu
 * @Description
 * @Date 10:04 2023/3/12
 **/
class HttpSubBlockManager(val handler: Handler) : IBlockManager {
  private val unfinishedBlock = mutableListOf<BlockRecord>()
  private var blockNum: Int = 1

  override fun putUnfinishedBlock(record: BlockRecord) {
    unfinishedBlock.add(record)
  }

  override fun getUnfinishedBlockList(): List<BlockRecord> {
    return unfinishedBlock
  }

  override fun getHandler() = handler

  override fun start(threadTaskList: List<IThreadTask>) {
    threadTaskList.forEach { tt ->
      tt.run()
    }
  }

  override fun setBlockNum(blockNum: Int) {
    this.blockNum = blockNum
  }

}
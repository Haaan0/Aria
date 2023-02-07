package com.arialyy.aria.core.task

import android.net.Uri
import com.arialyy.aria.orm.entity.BlockRecord
import com.arialyy.aria.orm.entity.TaskRecord
import com.arialyy.aria.util.FileUri
import com.arialyy.aria.util.FileUtil
import com.arialyy.aria.util.FileUtils
import timber.log.Timber
import java.io.File

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
object BlockUtil {

  fun getBlockPathFormUri(fileSaveUri: Uri, blockId: Int): String {
    val filePath = FileUri.getPathByUri(fileSaveUri) ?: "/"
    val fileName = FileUtils.getFileNameFromPath(filePath)
    val dirPath = FileUtils.getFilePathFromFullPath(filePath)

    return BlockRecord.BLOCK_PATH.format(dirPath, fileName, blockId)
  }

  /**
   * create block record
   */
  fun createBlockRecord(fileSaveUri: Uri, fileSize: Long): List<BlockRecord> {
    val filePath = FileUri.getPathByUri(fileSaveUri) ?: "/"
    val fileName = FileUtils.getFileNameFromPath(filePath)
    val dirPath = FileUtils.getFilePathFromFullPath(filePath)

    val blockNumInfo = getBlockNum(fileSize)
    val lastIndex = blockNumInfo.first - 1
    val brList = mutableListOf<BlockRecord>()
    for (bi in 0 until blockNumInfo.first) {
      val sl = bi * BlockRecord.BLOCK_SIZE
      val blockSize = if (bi == lastIndex) blockNumInfo.second else BlockRecord.BLOCK_SIZE
      val el = sl + blockSize
      val blockRecord = BlockRecord(
        bId = bi,
        startLocation = bi * BlockRecord.BLOCK_SIZE,
        endLocation = el,
        blockSize = blockSize,
        blockPath = BlockRecord.BLOCK_PATH.format(dirPath, fileName, bi)
      )
      brList.add(blockRecord)
    }
    return brList
  }

  /**
   * Get the number of blocks according to the file length
   * @return pair<blockNum, lastBlockSize>
   */
  fun getBlockNum(fileLen: Long): Pair<Int, Long> {
    if (fileLen <= BlockRecord.BLOCK_SIZE) {
      return Pair(1, 0)
    }
    val blockNum = (fileLen / BlockRecord.BLOCK_SIZE).toInt()
    val lastBlockSize = fileLen % BlockRecord.BLOCK_SIZE
    return Pair(if (lastBlockSize != 0L) blockNum + 1 else blockNum, lastBlockSize)
  }

  /**
   * merge block file,if success,return true else return false
   */
  fun mergeFile(record: TaskRecord): Boolean {
    val targetF = File(record.filePath)
    val dir = targetF.parentFile ?: return false
    val fileName = targetF.name
    if (record.blockNum == 1) {
      // if this task not support blocks or fileSize < 5m, just need rename
      return File(BlockRecord.BLOCK_PATH.format(dir, fileName, 0)).renameTo(targetF)
    }
    val blockList = mutableListOf<File>()
    for (i in 0 until record.blockNum) {
      val subF = File(BlockRecord.BLOCK_PATH.format(dir, fileName, i))
      if (!subF.exists()) {
        Timber.e("this block: $i not exists")
        return false
      }
      if (subF.length() != BlockRecord.BLOCK_SIZE.toLong()) {
        Timber.e("this block: $i size abnormal, size: ${subF.length()}")
        return false
      }
      blockList.add(subF)
    }
    return FileUtil.mergeFile(record.filePath, blockList)
  }
}
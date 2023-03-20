package com.arialyy.aria.util

import android.net.Uri
import com.arialyy.aria.core.DuaContext
import com.arialyy.aria.core.task.ITask
import com.arialyy.aria.core.task.TaskCachePool
import com.arialyy.aria.orm.entity.BlockRecord
import com.arialyy.aria.orm.entity.DEntity
import com.arialyy.aria.orm.entity.TaskRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

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


  /**
   * 1.remove all block
   * 2.remove record
   */
  fun removeTaskBlock(task: ITask) {
    val file = FileUri.getPathByUri(task.taskState.taskRecord.filePath)?.let { File(it) }
    file?.parentFile?.let {
      FileUtils.deleteDir(it)
    }

    DuaContext.duaScope.launch(Dispatchers.IO) {
      val entity = TaskCachePool.getEntity(taskId = task.taskId)
      val db = DuaContext.getServiceManager().getDbService().getDuaDb()
      db.getRecordDao().deleteTaskRecord(task.taskState.taskRecord)
      db.getDEntityDao().delete(entity as DEntity)
    }
  }

  fun getBlockPathFormUri(fileSaveUri: Uri, blockId: Int): String {
    val filePath = FileUri.getPathByUri(fileSaveUri) ?: "/"
    val fileName = FileUtils.getFileNameFromPath(filePath)
    val dirPath = FileUtils.getFilePathFromFullPath(filePath)

    return BlockRecord.BLOCK_PATH.format(dirPath, fileName, blockId)
  }

  /**
   * create block record
   */
  fun createBlockRecord(filePath: File, fileSize: Long): List<BlockRecord> {
    val fileName = filePath.name
    val dirPath = filePath.parent

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
    val blockRecordList = record.blockList
    val targetPath = FileUri.getPathByUri(record.filePath)
    if (blockRecordList.isEmpty()) {
      Timber.e("block record list empty")
      return false
    }
    if (targetPath.isNullOrEmpty()) {
      Timber.e("invalid uri: $targetPath")
      return false
    }
    val fileList = arrayListOf<File>()
    blockRecordList.forEach {
      fileList.add(File(it.blockPath))
    }

    val targetF = File(targetPath)
    val dir = targetF.parentFile
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
    return mergeFile(targetF, blockList)
  }

  /**
   * 合并文件
   *
   * @param targetFile 目标文件
   * @param blockList 分块列表
   * @return `true` 合并成功，`false`合并失败
   */
  fun mergeFile(targetFile: File, blockList: List<File>): Boolean {
    Timber.d("开始合并文件")
    if (targetFile.exists() && targetFile.isDirectory) {
      Timber.w("路径【%s】是文件夹，将删除该文件夹", targetFile.path)
      FileUtils.deleteDir(targetFile)
    }
    if (!targetFile.exists()) {
      FileUtils.createFile(targetFile)
    }

    val startTime = System.currentTimeMillis()
    var fileLen: Long = 0
    FileOutputStream(targetFile).use { fos ->
      fos.channel.use { foc ->
        for (block in blockList) {
          if (!block.exists()) {
            Timber.d("合并文件失败，文件【${block.path}】不存在")
            return false
          }
          FileInputStream(block).channel.use { fic ->
            foc.transferFrom(fic, fileLen, block.length())
            fileLen += block.length()
          }
        }
      }
    }
    Timber.d("merge file time：${System.currentTimeMillis() - startTime}ms, fileSize = $fileLen")
    return false
  }
}
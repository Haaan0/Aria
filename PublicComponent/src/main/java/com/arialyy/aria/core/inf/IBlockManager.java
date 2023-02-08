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
package com.arialyy.aria.core.inf;

import android.os.Handler;
import com.arialyy.aria.core.task.IThreadTask;
import com.arialyy.aria.orm.entity.BlockRecord;
import java.util.List;

/**
 * 线程任务状态
 */
public interface IBlockManager {
  int STATE_STOP = 0x01;
  int STATE_FAIL = 0x02;
  int STATE_CANCEL = 0x03;
  int STATE_COMPLETE = 0x04;
  int STATE_RUNNING = 0x05;
  int STATE_UPDATE_PROGRESS = 0x06;
  int STATE_PRE = 0x07;
  int STATE_START = 0x08;
  String DATA_RETRY = "DATA_RETRY";
  String DATA_ERROR_INFO = "DATA_ERROR_INFO";
  String DATA_THREAD_NAME = "DATA_THREAD_NAME";
  String DATA_THREAD_LOCATION = "DATA_THREAD_LOCATION";
  String DATA_ADD_LEN = "DATA_ADD_LEN"; // 增加的长度

  void setLooper();

  void start(List<IThreadTask> threadTaskList);

  void setBlockNum(int blockNum);

  void putUnfinishedBlock(BlockRecord record);

  List<BlockRecord> getUnfinishedBlockList();

  /**
   * 是否有失败的快
   *
   * @return true 有失败的快
   */
  boolean hasFailedBlock();

  /**
   * 任务是否已经完成
   *
   * @return true 任务已完成
   */
  boolean isCompleted();

  /**
   * 获取当前任务进度
   *
   * @return 任务当前进度
   */
  long getCurrentProgress();

  boolean isStopped();

  boolean isCanceled();

  boolean isRunning();

  /**
   * 创建handler 回调
   */
  Handler getHandler();
}

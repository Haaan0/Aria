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

package com.arialyy.aria.queue;

import android.content.Context;
import com.arialyy.aria.core.AriaConfig;
import com.arialyy.aria.core.task.DownloadTask;

/**
 * Created by lyy on 2016/8/17.
 * 下载任务队列
 */
public class DTaskQueue extends AbsTaskQueue<DownloadTask> {
  private static volatile DTaskQueue INSTANCE = null;

  public static DTaskQueue getInstance() {
    if (INSTANCE == null) {
      synchronized (DTaskQueue.class) {
        INSTANCE = new DTaskQueue();
      }
    }
    return INSTANCE;
  }

  private DTaskQueue() {
  }

  @Override protected int getMaxTaskSize() {
    return AriaConfig.getInstance().getDConfig().getMaxTaskNum();
  }

  @Override public int getOldMaxSize() {
    return AriaConfig.getInstance().getDConfig().oldMaxTaskNum;
  }

  @Override public void init(@NonNull Context context) {

  }
}
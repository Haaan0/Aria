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

package com.arialyy.aria.core.task;

import android.net.Uri;
import com.arialyy.aria.core.download.TaskOption;
import com.arialyy.aria.core.inf.ITaskUtil;
import java.util.Objects;

/**
 * Created by lyy on 2016/8/11.
 * 下载任务类
 */
public class DownloadTask extends AbsTask {

  public DownloadTask(TaskOption taskOption, ITaskUtil util) {
    super(taskOption, util);
  }

  public Uri getSavePath() {
    return getTaskOption(TaskOption.class).getSavePathUri();
  }

  public String getSourceUrl() {
    return getTaskOption(TaskOption.class).getSourUrl();
  }

  /**
   * 获取当前下载任务的下载地址
   */
  @Override public int getTaskType() {
    return ITask.DOWNLOAD;
  }

  @Override public String getFilePath() {
    return Objects.requireNonNull(getTaskOption(TaskOption.class).getSavePathUri()).toString();
  }
}
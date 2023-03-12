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
package com.arialyy.dua.group;

import com.arialyy.aria.core.common.TaskOption;
import com.arialyy.aria.core.inf.ITaskOption;
import com.arialyy.aria.core.task.AbsTask;
import java.util.Objects;

/**
 * Created by AriaL on 2017/6/27.
 * 任务组任务
 */
public class HttpGroupTask extends AbsTask {

  public HttpGroupTask(ITaskOption taskOption) {
    super(taskOption);
  }

  @Override public int getTaskType() {
    return HTTP_GROUP;
  }

  /**
   * @return Always return null
   */
  @Deprecated
  @Override public String getUrl() {
    return "";
  }

  @Override public String getFilePath() {
    return Objects.requireNonNull(getTaskOption(TaskOption.class).getSavePathDir()).toString();
  }
}

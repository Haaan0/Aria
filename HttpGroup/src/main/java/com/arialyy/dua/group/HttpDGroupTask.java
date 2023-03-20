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
import com.arialyy.aria.core.task.SingleDownloadTask;
import com.arialyy.aria.http.HttpTaskOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by AriaL on 2017/6/27.
 * 任务组任务
 */
public class HttpDGroupTask extends AbsTask {

  private final List<SingleDownloadTask> incompleteTaskList = new ArrayList<>();

  private final List<SingleDownloadTask> subTaskList = new ArrayList<>();

  public HttpDGroupTask(ITaskOption taskOption) {
    super(taskOption);
  }

  public HttpDGOptionAdapter getDGOptionAdapter() {
    return getTaskOption(HttpTaskOption.class).getOptionAdapter(HttpDGOptionAdapter.class);
  }

  void setIncompleteTaskList(List<SingleDownloadTask> list) {
    incompleteTaskList.clear();
    incompleteTaskList.addAll(list);
  }

  void addIncompleteTaskList(SingleDownloadTask task) {
    incompleteTaskList.add(task);
  }

  List<SingleDownloadTask> getIncompleteTaskList() {
    return incompleteTaskList;
  }

  void setSubTaskList(List<SingleDownloadTask> list) {
    this.subTaskList.clear();
    this.subTaskList.addAll(list);
  }

  void addSubTask(SingleDownloadTask task) {
    this.subTaskList.add(task);
  }

  public List<SingleDownloadTask> getSubTaskList() {
    return subTaskList;
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

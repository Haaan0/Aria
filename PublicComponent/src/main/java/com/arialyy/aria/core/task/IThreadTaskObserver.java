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

/**
 * 线程任务观察者
 *
 * @author lyy
 * Date: 2019-09-18
 */
public interface IThreadTaskObserver {

  /**
   * 更新进度
   *
   * @param len 新增的长度
   */
  void onProgress(long len);

  /**
   * 获取线程当前进度
   */
  long getThreadProgress();

  void onFail(Exception e);

  void onComplete();
}

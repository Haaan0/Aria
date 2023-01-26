/*
 * Copyright (C) 2016  AriaLyy(https://github.com/AriaLyy/Aria)
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

public interface TaskSchedulerType {
  int TYPE_DEFAULT = 1;
  /**
   * 停止当前任务并且不自动启动下一任务
   */
  int TYPE_STOP_NOT_NEXT = 2;
  /**
   * 停止任务并让当前任务处于等待状态
   */
  int TYPE_STOP_AND_WAIT = 3;

  /**
   * 删除任务并且不通知回调
   */
  int TYPE_CANCEL_AND_NOT_NOTIFY = 4;

  /**
   * 重置状态并启动任务
   */
  int TYPE_START_AND_RESET_STATE = 5;
}
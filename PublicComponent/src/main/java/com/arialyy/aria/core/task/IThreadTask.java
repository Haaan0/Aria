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
 * @author lyy
 * Date: 2019-09-18
 */
public interface IThreadTask extends Runnable {

  /**
   * 取消任务
   */
  void cancel();

  /**
   * 停止任务
   */
  void stop();

  /**
   * 设置当前线程最大下载速度
   *
   * @param speed 单位为：kb
   */
  void setMaxSpeed(int speed);

  /**
   * 线程是否存活
   *
   * @return {@code true}存活
   */
  boolean isRunning();

}

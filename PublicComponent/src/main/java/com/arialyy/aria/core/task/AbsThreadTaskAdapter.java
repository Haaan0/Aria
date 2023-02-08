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

import com.arialyy.aria.orm.entity.BlockRecord;
import com.arialyy.aria.util.BandwidthLimiter;

/**
 * @author lyy
 * Date: 2019-09-18
 */
public abstract class AbsThreadTaskAdapter implements IThreadTaskAdapter {

  /**
   * 速度限制工具
   */
  protected BandwidthLimiter mSpeedBandUtil;
  private IThreadTaskObserver mObserver;
  private final ThreadConfig mThreadConfig;
  private boolean breakTask = false;

  protected AbsThreadTaskAdapter(ThreadConfig threadConfig) {
    mThreadConfig = threadConfig;
    if (threadConfig.getSpeed() > 0) {
      mSpeedBandUtil =
          new BandwidthLimiter(threadConfig.getSpeed(), mThreadConfig.getOption().threadNum);
    }
  }

  @Override public void breakTask() {
    breakTask = true;
  }

  protected boolean isBreakTask() {
    return breakTask;
  }

  protected BlockRecord getBlockRecord() {
    return mThreadConfig.getBlockRecord();
  }

  protected ThreadConfig getThreadConfig() {
    return mThreadConfig;
  }

  @Override public void run() {
    try {
      handlerThreadTask();
    } catch (Exception e) {
      fail(e);
    }
  }

  /**
   * 开始处理线程任务
   */
  protected abstract void handlerThreadTask();

  /**
   * 当前线程的下去区间的进度
   */
  protected long getRangeProgress() {
    return mObserver.getThreadProgress();
  }

  @Override public void attach(IThreadTaskObserver observer) {
    mObserver = observer;
  }

  @Override public void setMaxSpeed(int speed) {
    if (mSpeedBandUtil == null) {
      mSpeedBandUtil =
          new BandwidthLimiter(mThreadConfig.getSpeed(), mThreadConfig.getOption().threadNum);
    }
    mSpeedBandUtil.setMaxRate(speed);
  }

  protected void complete() {
    if (mObserver != null) {
      mObserver.onComplete();
    }
  }

  protected void fail(Exception ex) {
    if (mObserver != null) {
      mObserver.onFail(ex);
    }
  }

  protected void progress(long len) {
    if (mObserver != null) {
      mObserver.onProgress(len);
    }
  }
}

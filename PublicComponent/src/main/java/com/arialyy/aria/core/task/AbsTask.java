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

import android.text.TextUtils;
import com.arialyy.aria.core.common.AbsEntity;
import com.arialyy.aria.core.inf.ITaskOption;
import com.arialyy.aria.core.inf.IUtil;
import com.arialyy.aria.core.inf.TaskSchedulerType;
import com.arialyy.aria.util.CommonUtil;
import com.arialyy.aria.util.ComponentUtil;
import java.util.HashMap;
import java.util.Map;
import timber.log.Timber;

/**
 * Created by AriaL on 2017/6/29.
 */
public abstract class AbsTask implements ITask {
  protected ITaskOption mTaskOption;
  private boolean isCancel = false, isStop = false;
  private IUtil mUtil;
  /**
   * 该任务的调度类型
   */
  private int mSchedulerType = TaskSchedulerType.TYPE_DEFAULT;
  protected TaskState mTaskState = new TaskState();
  private int taskId = -1;
  private final Map<String, Object> mExpand = new HashMap<>();

  protected AbsTask(ITaskOption taskOption) {
    mTaskOption = taskOption;
    taskId = TaskStatePool.INSTANCE.buildTaskId$PublicComponent_debug();
    TaskStatePool.INSTANCE.putTaskState(getTaskId(), mTaskState);
  }

  @Override public void setState(int state) {
    mTaskState.setState(state);
  }

  synchronized IUtil getUtil() {
    if (mUtil == null) {
      mUtil = ComponentUtil.getInstance().buildUtil(mTaskWrapper, mListener);
    }
    return mUtil;
  }

  @Override public <T extends ITaskOption> T getTaskOption(Class<T> clazz) {
    return (T) mTaskOption;
  }

  @Override public int getTaskId() {
    return taskId;
  }

  /**
   * 获取剩余时间，单位：s
   * 如果是m3u8任务，无法获取剩余时间；m2u8任务如果需要获取剩余时间，请设置文件长度{@link AbsEntity#setFileSize(long)}
   */
  public int getTimeLeft() {
    return mTaskState.getTimeLeft();
  }

  /**
   * 转换时间
   * 时间＜1 小时，显示分秒，显示样式 00:20
   * 时间 ≥1 小时，显示时分秒，显示样式 01:11:12
   * 时间 ≥1 天，显示天时分，显示样式 1d 01:11
   * 时间 ≥7 天，显示样式 ∞
   */
  public String getConvertTimeLeft() {
    return CommonUtil.formatTime(getTimeLeft());
  }

  /**
   * 添加扩展数据 读取扩展数据{@link #getExpand(String)}
   */
  public void putExpand(String key, Object obj) {
    if (TextUtils.isEmpty(key)) {
      Timber.e("key 为空");
      return;
    }
    if (obj == null) {
      Timber.w("扩展数据为空");
      return;
    }
    mExpand.put(key, obj);
  }

  public Object getExpand(String key) {
    return mExpand.get(key);
  }

  public TaskState getTaskState() {
    return mTaskState;
  }

  /**
   * 任务是否完成
   *
   * @return {@code true} 已经完成，{@code false} 未完成
   */
  public boolean isComplete() {
    return mTaskState.isCompleted();
  }

  /**
   * 获取当前下载进度
   */
  public long getCurrentProgress() {
    return mTaskState.getCurProgress();
  }

  /**
   * 获取单位转换后的进度
   *
   * @return 如：已经下载3mb的大小，则返回{@code 3mb}
   */
  public String getConvertCurrentProgress() {
    if (getCurrentProgress() == 0) {
      return "0b";
    }
    return CommonUtil.formatFileSize(getCurrentProgress());
  }

  /**
   * 获取文件大小
   */
  public long getFileSize() {
    return mTaskState.getFileSize();
  }

  /**
   * 获取百分比进度
   *
   * @return 返回百分比进度，如果文件长度为0，返回0
   */
  public int getPercent() {
    return mTaskState.getPercent();
  }

  @Override public void start(int type) {
    mSchedulerType = type;
    mUtil = getUtil();
    if (mUtil == null) {
      Timber.e("util is  null");
      return;
    }
    if (type == TaskSchedulerType.TYPE_START_AND_RESET_STATE) {
      if (getUtil().isRunning()) {
        Timber.e("task restart fail");
        return;
      }
      mUtil.start();
      Timber.e("task restart success");
      return;
    }
    if (getUtil().isRunning()) {
      Timber.d("task is running");
      return;
    }
    getUtil().start();
  }

  @Override public void stop(int type) {
    mUtil = getUtil();
    if (mUtil == null) {
      Timber.e("util is  null");
      return;
    }
    isStop = true;
    mSchedulerType = type;
    getUtil().stop();
  }

  @Override public void cancel(int type) {
    mUtil = getUtil();
    if (mUtil == null) {
      Timber.e("util is  null");
      return;
    }
    isCancel = true;
    mSchedulerType = type;
    getUtil().cancel();
  }

  /**
   * 是否真正下载
   *
   * @return {@code true} 正在下载
   */
  @Override public boolean isRunning() {
    return getUtil().isRunning();
  }

  /**
   * 任务的调度类型
   */
  @Override
  public int getSchedulerType() {
    return mSchedulerType;
  }

  /**
   * 任务是否取消了
   *
   * @return {@code true}任务已经取消
   */
  @Override
  public boolean isCancel() {
    return isCancel;
  }

  /**
   * 任务是否停止了
   *
   * @return {@code true}任务已经停止
   */
  @Override
  public boolean isStop() {
    return isStop;
  }

  /**
   * Bytes transferred in 1 second, if file size 0, return 0
   * curSpeed, unit: byte/s
   */
  public long getSpeed() {
    return mTaskState.getSpeed();
  }

  /**
   * you need set params in config
   *
   * @return Returns the converted speed:1b/s、1kb/s、1mb/s、1gb/s、1tb/s
   * xml:
   * <pre>
   * `<xml>
   * <download>
   * ...
   * <convertSpeed value="true"/>
   * </download>
   *
   * code:
   * Dua.getCommonConfig().setConvertSpeed(true);
   * </xml>
   *
   * </pre>
   */
  public String getConvertSpeed() {
    return mTaskState.getConvertSpeed();
  }
}

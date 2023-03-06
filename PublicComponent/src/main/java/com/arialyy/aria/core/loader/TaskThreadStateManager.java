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
package com.arialyy.aria.core.loader;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.arialyy.aria.core.inf.ITaskManager;
import com.arialyy.aria.core.listener.IEventListener;
import com.arialyy.aria.exception.AriaException;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.FileUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import timber.log.Timber;

/**
 * 线程任务管理器，用于处理多线程下载时任务的状态回调
 */
public class TaskThreadStateManager implements ITaskManager {
  /**
   * 分块文件路径: 文件路径.blockId.part
   */
  public static String SUB_PATH = "%s.%s.part";
  /**
   * 任务状态回调
   */
  private final IEventListener mListener;
  private final int mThreadNum;    // 启动的线程总数
  private final AtomicInteger mCancelNum = new AtomicInteger(0); // 已经取消的线程的数
  private final AtomicInteger mStopNum = new AtomicInteger(0);  // 已经停止的线程数
  private final AtomicInteger mFailNum = new AtomicInteger(0);  // 失败的线程数
  private final AtomicInteger mCompleteNum = new AtomicInteger(0);  // 完成的线程数
  private long mProgress; //当前总进度
  private Looper mLooper;

  /**
   * @param listener 任务事件
   */
  public TaskThreadStateManager(IEventListener listener, int threadNum) {
    mListener = listener;
    mThreadNum = threadNum;
  }

  @Override public void setLooper(Looper looper) {
    mLooper = looper;
  }

  private void checkLooper() {
    if (mLooper == null) {
      throw new NullPointerException("Looper为空");
    }
  }

  private final Handler.Callback callback = new Handler.Callback() {
    @Override public boolean handleMessage(Message msg) {
      checkLooper();
      switch (msg.what) {
        case STATE_STOP:
          mStopNum.getAndIncrement();
          if (isStop()) {
            quitLooper();
          }
          break;
        case STATE_CANCEL:
          mCancelNum.getAndIncrement();
          if (isCancel()) {
            quitLooper();
          }
          break;
        case STATE_FAIL:
          mFailNum.getAndIncrement();
          if (hasFailedBlock()) {
            Bundle b = msg.getData();
            mListener.onFail(b.getBoolean(DATA_RETRY, false),
                (AriaException) b.getSerializable(DATA_ERROR_INFO));
            quitLooper();
          }
          break;
        case STATE_COMPLETE:
          mCompleteNum.getAndIncrement();
          if (isCompleted()) {
            Timber.d("isComplete, completeNum = %s", mCompleteNum);
            if (mTaskRecord.isBlock || mTaskRecord.threadNum == 1) {
              if (mergeFile()) {
                mListener.onComplete();
              } else {
                mListener.onFail(false, null);
              }
              quitLooper();
              break;
            }
            mListener.onComplete();
            quitLooper();
          }
          break;
        case STATE_RUNNING:
          Bundle b = msg.getData();
          if (b != null) {
            long len = b.getLong(ITaskManager.DATA_ADD_LEN, 0);
            mProgress += len;
          }

          break;
        case STATE_UPDATE_PROGRESS:
          if (msg.obj == null) {
            mProgress = updateBlockProgress();
          } else if (msg.obj instanceof Long) {
            mProgress = (long) msg.obj;
          }
          break;
      }
      return false;
    }
  };

  @Override public void updateCurrentProgress(long currentProgress) {
    mProgress = currentProgress;
  }

  /**
   * 退出looper循环
   */
  private void quitLooper() {
    mLooper.quit();
  }

  /**
   * 获取当前任务下载进度
   *
   * @return 当前任务下载进度
   */
  @Override
  public long getCurrentProgress() {
    return mProgress;
  }

  @Override public Handler.Callback getHandlerCallback() {
    return callback;
  }

  /**
   * 所有子线程是否都已经停止
   */
  public boolean isStop() {
    //ALog.d(TAG,
    //    String.format("isStop; stopNum: %s, cancelNum: %s, failNum: %s, completeNum: %s", mStopNum,
    //        mCancelNum, mFailNum, mCompleteNum));
    return mStopNum.get() == mThreadNum || mStopNum.get() + mCompleteNum.get() == mThreadNum;
  }

  /**
   * 所有子线程是否都已经失败
   */
  @Override
  public boolean hasFailedBlock() {
    //ALog.d(TAG,
    //    String.format("isFail; stopNum: %s, cancelNum: %s, failNum: %s, completeNum: %s", mStopNum,
    //        mCancelNum, mFailNum, mCompleteNum));
    return mCompleteNum.get() != mThreadNum
        && (mFailNum.get() == mThreadNum || mFailNum.get() + mCompleteNum.get() == mThreadNum);
  }

  /**
   * 所有子线程是否都已经完成
   */
  @Override
  public boolean isCompleted() {
    //ALog.d(TAG,
    //    String.format("isComplete; stopNum: %s, cancelNum: %s, failNum: %s, completeNum: %s",
    //        mStopNum,
    //        mCancelNum, mFailNum, mCompleteNum));
    return mCompleteNum.get() == mThreadNum;
  }

  /**
   * 所有子线程是否都已经取消
   */
  public boolean isCancel() {
    //ALog.d(TAG, String.format("isCancel; stopNum: %s, cancelNum: %s, failNum: %s, completeNum: %s",
    //    mStopNum,
    //    mCancelNum, mFailNum, mCompleteNum));
    return mCancelNum.get() == mThreadNum;
  }

  /**
   * 更新分块任务s的真实进度
   */
  private long updateBlockProgress() {
    long size = 0;
    for (int i = 0, len = mTaskRecord.threadRecords.size(); i < len; i++) {
      File temp = new File(String.format(IRecordHandler.SUB_PATH, mTaskRecord.filePath, i));
      if (temp.exists()) {
        size += temp.length();
      }
    }
    return size;
  }

  /**
   * 合并文件
   *
   * @return {@code true} 合并成功，{@code false}合并失败
   */
  private boolean mergeFile() {
    if (mTaskRecord.threadNum == 1) {
      File targetFile = new File(mTaskRecord.filePath);
      if (targetFile.exists()) {
        //没有获得文件长度：不支持断点续传
        if (mTaskRecord.fileLength == 0 && targetFile.length() != 0) {
          return true;
        }
        if (targetFile.length() != 0 && targetFile.length() == mTaskRecord.fileLength) {
          return true;
        }
      }
      FileUtil.deleteFile(targetFile);
      File partFile = new File(String.format(IRecordHandler.SUB_PATH, mTaskRecord.filePath, 0));
      return partFile.renameTo(targetFile);
    }

    List<String> partPath = new ArrayList<>();
    for (int i = 0, len = mTaskRecord.threadNum; i < len; i++) {
      partPath.add(String.format(IRecordHandler.SUB_PATH, mTaskRecord.filePath, i));
    }
    boolean isSuccess = FileUtil.mergeFile(mTaskRecord.filePath, partPath);
    if (isSuccess) {
      for (String pp : partPath) {
        FileUtil.deleteFile(pp);
      }
      File targetFile = new File(mTaskRecord.filePath);
      if (targetFile.exists() && targetFile.length() > mTaskRecord.fileLength) {
        ALog.e(TAG, String.format("任务【%s】分块文件合并失败，下载长度超出文件真实长度，downloadLen: %s，fileSize: %s",
            targetFile.getName(), targetFile.length(), mTaskRecord.fileLength));
        return false;
      }
      return true;
    } else {
      ALog.e(TAG, "合并失败");
      return false;
    }
  }
}

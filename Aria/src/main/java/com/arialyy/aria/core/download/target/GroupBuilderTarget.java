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
package com.arialyy.aria.core.download.target;

import androidx.annotation.CheckResult;
import com.arialyy.aria.core.common.AbsBuilderTarget;
import com.arialyy.aria.core.common.HttpDelegate;
import com.arialyy.aria.core.download.DGTaskWrapper;
import com.arialyy.aria.core.inf.IOptionConstant;
import com.arialyy.aria.core.inf.Suggest;
import com.arialyy.aria.core.manager.SubTaskManager;
import com.arialyy.aria.core.processor.IHttpFileLenAdapter;
import com.arialyy.aria.core.wrapper.ITaskWrapper;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CheckUtil;
import java.util.List;

/**
 * Created by AriaL on 2017/6/29.
 * 下载任务组
 */
public class GroupBuilderTarget extends AbsBuilderTarget<GroupBuilderTarget> {
  private HttpGroupConfigHandler<GroupBuilderTarget> mConfigHandler;

  GroupBuilderTarget(List<String> urls) {
    mConfigHandler = new HttpGroupConfigHandler<>(this, -1);
    getTaskWrapper().setRequestType(ITaskWrapper.DG_HTTP);
    mConfigHandler.setGroupUrl(urls);
  }

  /**
   * 设置http请求参数，header等信息
   */
  @CheckResult(suggest = Suggest.TASK_CONTROLLER)
  public HttpDelegate<GroupBuilderTarget> option() {
    return new HttpDelegate<>(this, getTaskWrapper());
  }

  /**
   * 任务组总任务大小，任务组是一个抽象的概念，没有真实的数据实体，任务组的大小是Aria动态获取子任务大小相加而得到的，
   * 如果你知道当前任务组总大小，你也可以调用该方法给任务组设置大小
   *
   * 为了更好的用户体验，组合任务最好设置文件大小，默认需要强制设置文件大小。如果无法获取到总长度，请调用{@link #unknownSize()}
   *
   * @param fileSize 任务组总大小
   */
  @CheckResult(suggest = Suggest.TASK_CONTROLLER)
  public GroupBuilderTarget setFileSize(long fileSize) {
    if (fileSize <= 0) {
      ALog.e(TAG, "文件大小不能小于 0");
      return this;
    }
    if (getEntity().getFileSize() <= 1 || getEntity().getFileSize() != fileSize) {
      getEntity().setFileSize(fileSize);
    }
    return this;
  }

  /**
   * 如果无法获取到组合任务到总长度，请调用该方法，
   * 请注意：
   * 1、如果组合任务到子任务数过多，请不要使用该标志位，否则Aria将需要消耗大量的时间获取组合任务的总长度，直到获取完成组合任务总长度后才会执行下载。
   * 2、如果你的知道组合任务的总长度，请使用{@link #setFileSize(long)}设置组合任务的长度。
   * 3、由于网络或其它原因的存在，这种方式获取的组合任务大小有可能是不准确的。
   */
  @CheckResult(suggest = Suggest.TASK_CONTROLLER)
  public GroupBuilderTarget unknownSize() {
    ((DGTaskWrapper) getTaskWrapper()).setUnknownSize(true);
    return this;
  }

  /**
   * 获取子任务管理器
   *
   * @return 子任务管理器
   */
  @CheckResult(suggest = Suggest.TASK_CONTROLLER)
  public SubTaskManager getSubTaskManager() {
    return mConfigHandler.getSubTaskManager();
  }

  /**
   * 设置子任务文件名，该方法必须在{@link #setDirPath(String)}之后调用，否则不生效
   *
   * @deprecated {@link #setSubFileName(List)} 请使用该api
   */
  @CheckResult(suggest = Suggest.TASK_CONTROLLER)
  @Deprecated public GroupBuilderTarget setSubTaskFileName(List<String> subTaskFileName) {
    return setSubFileName(subTaskFileName);
  }

  /**
   * 设置任务组别名
   */
  @CheckResult(suggest = Suggest.TASK_CONTROLLER)
  public GroupBuilderTarget setGroupAlias(String alias) {
    mConfigHandler.setGroupAlias(alias);
    return this;
  }

  /**
   * 设置任务组的文件夹路径，在Aria中，任务组的所有子任务都会下载到以任务组组名的文件夹中。
   * 如：groupDirPath = "/mnt/sdcard/download/group_test"
   * <pre>
   *   {@code
   *      + mnt
   *        + sdcard
   *          + download
   *            + group_test
   *              - task1.apk
   *              - task2.apk
   *              - task3.apk
   *              ....
   *
   *   }
   * </pre>
   *
   * @param dirPath 任务组保存文件夹路径
   */
  @CheckResult(suggest = Suggest.TASK_CONTROLLER)
  public GroupBuilderTarget setDirPath(String dirPath) {
    return mConfigHandler.setDirPath(dirPath);
  }

  /**
   * 设置子任务文件名，该方法必须在{@link #setDirPath(String)}之后调用，否则不生效
   */
  @CheckResult(suggest = Suggest.TASK_CONTROLLER)
  public GroupBuilderTarget setSubFileName(List<String> subTaskFileName) {
    return mConfigHandler.setSubFileName(subTaskFileName);
  }

  /**
   * 如果你需要使用header中特定的key来设置文件长度，或有定制文件长度的需要，那么你可以通过该方法自行处理文件长度
   */
  @CheckResult(suggest = Suggest.TASK_CONTROLLER)
  public GroupBuilderTarget setFileLenAdapter(IHttpFileLenAdapter adapter) {

    if (adapter == null) {
      throw new IllegalArgumentException("adapter为空");
    }
    CheckUtil.checkMemberClass(adapter.getClass());
    getTaskWrapper().getOptionParams().setObjs(IOptionConstant.fileLenAdapter, adapter);
    return this;
  }
}

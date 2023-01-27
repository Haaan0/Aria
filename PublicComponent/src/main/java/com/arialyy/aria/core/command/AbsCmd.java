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

package com.arialyy.aria.core.command;

import com.arialyy.aria.core.DuaContext;
import com.arialyy.aria.core.inf.ITaskQueue;
import com.arialyy.aria.core.task.ITask;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by AriaL on 2017/6/29.
 */
public abstract class AbsCmd implements ICmd {

  protected ITask mTask;

  protected List<ICmdInterceptor> userInterceptors;

  protected List<ICmdInterceptor> coreInterceptors = new ArrayList<>();

  protected AbsCmd(ITask task) {
    mTask = task;
    addCoreInterceptor(new TaskCheckInterceptor());
  }

  /**
   * add user interceptor
   */
  public void setInterceptors(List<ICmdInterceptor> userInterceptors) {
    this.userInterceptors.addAll(userInterceptors);
  }

  protected void addCoreInterceptor(ICmdInterceptor interceptor) {
    coreInterceptors.add(interceptor);
  }

  /**
   * if interruption occurred, stop cmd
   */
  protected CmdResp interceptor() {
    if (userInterceptors == null || userInterceptors.isEmpty()) {
      return null;
    }
    List<ICmdInterceptor> interceptors = new ArrayList<>();
    interceptors.addAll(userInterceptors);
    interceptors.addAll(coreInterceptors);
    ICmdInterceptor.IChain chain = new CmdChain(interceptors, 0, mTask, getTaskQueue());
    return chain.proceed(mTask);
  }

  public ITaskQueue<ITask> getTaskQueue() {
    ITaskQueue<?> itq = null;
    switch (mTask.getTaskType()) {
      case ITask.DOWNLOAD: {
        itq = DuaContext.INSTANCE.getServiceManager().getDownloadQueue();
        break;
      }
      case ITask.UPLOAD: {
        itq = DuaContext.INSTANCE.getServiceManager().getUploadQueue();
        break;
      }
    }
    return (ITaskQueue<ITask>) itq;
  }
}

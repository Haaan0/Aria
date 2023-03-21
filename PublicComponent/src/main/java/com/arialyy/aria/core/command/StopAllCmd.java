package com.arialyy.aria.core.command;


/**
 * Created by AriaL on 2017/6/13.
 * 停止所有任务的命令，并清空所有等待队列
 */
final class StopAllCmd extends AbsCmd {
  StopAllCmd(T entity, int taskType) {
    super(entity, taskType);
  }

  @Override public void executeCmd() {
    stopAll();
  }
}

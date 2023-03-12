package com.arialyy.simple.core.download;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;
import com.arialyy.annotations.Download;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.inf.IEntity;
import com.arialyy.aria.core.task.SingleDownloadTask;
import com.arialyy.aria.util.CommonUtil;
import com.arialyy.simple.R;
import com.arialyy.simple.base.BaseDialog;
import com.arialyy.simple.databinding.DialogFragmentDownloadBinding;
import com.arialyy.simple.util.AppUtil;

/**
 * Created by lyy on 2017/8/8.
 */
@SuppressLint("ValidFragment") public class DownloadDialogFragment
    extends BaseDialog<DialogFragmentDownloadBinding> implements View.OnClickListener {

  private DownloadEntity mEntity;

  private static final String DOWNLOAD_URL =
      "http://res3.d.cn/android/new/game/2/78702/fzjh_1499390260312.apk?f=web_1";

  protected DownloadDialogFragment(Object obj) {
    super(obj);
  }

  @Override protected void init(Bundle savedInstanceState) {
    super.init(savedInstanceState);
    Aria.download(this).register();
    mEntity = Aria.download(this).getFirstDownloadEntity(DOWNLOAD_URL);
    if (mEntity != null) {
      if (mEntity.getState() == IEntity.STATE_RUNNING) {
        getBinding().setStateStr(getString(R.string.stop));
      } else {
        getBinding().setStateStr(getString(R.string.resume));
      }

      getBinding().setFileSize(CommonUtil.formatFileSize(mEntity.getFileSize()));
      getBinding().setProgress((int) (mEntity.getCurrentProgress() * 100 / mEntity.getFileSize()));
    }
    mRootView.findViewById(R.id.start).setOnClickListener(this);
    mRootView.findViewById(R.id.cancel).setOnClickListener(this);
  }

  @Override protected int setLayoutId() {
    return R.layout.dialog_fragment_download;
  }

  @Override public void onDestroy() {
    super.onDestroy();
    Aria.download(this).unRegister();
  }

  @Download.onPre(DOWNLOAD_URL) protected void onPre(SingleDownloadTask task) {
  }

  @Download.onTaskStart(DOWNLOAD_URL) void taskStart(SingleDownloadTask task) {
    getBinding().setFileSize(task.getConvertFileSize());
  }

  @Download.onTaskRunning(DOWNLOAD_URL) protected void running(SingleDownloadTask task) {
    long len = task.getFileSize();
    if (len == 0) {
      getBinding().setProgress(0);
    } else {
      getBinding().setProgress(task.getPercent());
    }
    getBinding().setSpeed(task.getConvertSpeed());
  }

  @Download.onTaskResume(DOWNLOAD_URL) void taskResume(SingleDownloadTask task) {
  }

  @Download.onTaskStop(DOWNLOAD_URL) void taskStop(SingleDownloadTask task) {
    getBinding().setSpeed("");
  }

  @Download.onTaskCancel(DOWNLOAD_URL) void taskCancel(SingleDownloadTask task) {
    getBinding().setProgress(0);
    Toast.makeText(getContext(), "取消下载", Toast.LENGTH_SHORT).show();
    getBinding().setSpeed("");
  }

  @Download.onTaskFail(DOWNLOAD_URL) void taskFail(SingleDownloadTask task) {
    Toast.makeText(getContext(), "下载失败", Toast.LENGTH_SHORT).show();
  }

  @Download.onTaskComplete(DOWNLOAD_URL) void taskComplete(SingleDownloadTask task) {
    getBinding().setProgress(100);
    Toast.makeText(getContext(), "下载完成", Toast.LENGTH_SHORT).show();
    getBinding().setSpeed("");
  }

  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.start:
        if (!AppUtil.chekEntityValid(mEntity)) {
          Aria.download(this)
              .load(DOWNLOAD_URL)
              .setFilePath(Environment.getExternalStorageDirectory().getPath() + "/放置江湖.apk")
              .create();
          getBinding().setStateStr(getString(R.string.stop));
          break;
        }
        if (Aria.download(this).load(mEntity.getId()).isRunning()) {
          Aria.download(this).load(mEntity.getId()).stop();
          getBinding().setStateStr(getString(R.string.resume));
        } else {
          Aria.download(this).load(mEntity.getId()).resume();
          getBinding().setStateStr(getString(R.string.stop));
        }
        break;

      case R.id.cancel:
        if (AppUtil.chekEntityValid(mEntity)) {
          Aria.download(this).load(mEntity.getId()).cancel();
        }
        break;
    }
  }
}

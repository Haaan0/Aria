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
package com.arialyy.aria.orm;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import com.arialyy.aria.util.ALog;
import java.util.List;

/**
 * Created by laoyuyu on 2018/3/22. 增加数据、更新数据
 */
class DelegateUpdate extends AbsDelegate {
  private DelegateUpdate() {
  }

  /**
   * 修改某行数据
   */
  synchronized void updateData(Context context, DbEntity dbEntity) {
    Uri uri = DbContentProvider.Companion.createRequestUrl(context, dbEntity.getClass());
    ContentValues values = DbUtil.INSTANCE.createValues(dbEntity);
    if (values != null) {
      int rowId = context.getContentResolver()
          .update(uri, values, "rowid=?", new String[] { String.valueOf(dbEntity.rowID) });
      if (rowId != -1) {
        ALog.d(TAG, "更新数据成功，rowid = " + rowId);
      } else {
        ALog.e(TAG, "更新数据成功，rowid = " + rowId);
      }
      return;
    }
    ALog.e(TAG, "更新记录失败，记录没有属性字段");
  }

  /**
   * 更新多条记录
   */
  synchronized <T extends DbEntity> void updateManyData(Context context, List<T> dbEntities) {
    for (DbEntity entity : dbEntities) {
      updateData(context, entity);
    }
  }
}

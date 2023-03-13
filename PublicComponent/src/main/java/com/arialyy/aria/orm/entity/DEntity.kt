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
package com.arialyy.aria.orm.entity

import android.net.Uri
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.arialyy.aria.core.DuaContext
import com.arialyy.aria.core.inf.BaseEntity
import com.arialyy.aria.util.FileUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import java.io.File

/**
 * Download Entity
 */
@Entity(indices = [Index(value = ["sourceUrl", "savePath"])])
@TypeConverters(FilePathConverter::class)
@Parcelize
data class DEntity(
  @PrimaryKey(autoGenerate = true) val did: Int = 0,

  var parentId: Int = -1,

  /**
   * file source url
   */
  val sourceUrl: String,

  /**
   * file save path, it's uri
   * eg: /mnt/Sdcard/Download/
   */
  private val savePath: Uri,

  var fileName: String = "",

  val isSub: Boolean = false
) : BaseEntity() {
  private var dirFile: File? = null

  /**
   * 1. file exist
   * 2. correct file length
   */
  fun fileIsComplete(): Boolean {
    val f = getFilePath()
    return f.exists() && f.length() == fileSize
  }

  fun getFilePath(): File {
    if (dirFile == null) {
      dirFile = File(FileUri.getPathByUri(savePath)!!)
    }
    return File(dirFile, fileName)
  }

  override fun update() {
    updateTime = System.currentTimeMillis()
    DuaContext.duaScope.launch(Dispatchers.IO) {
      DuaContext.getServiceManager().getDbService().getDuaDb().getDEntityDao()
        .update(this@DEntity)
    }
  }
}
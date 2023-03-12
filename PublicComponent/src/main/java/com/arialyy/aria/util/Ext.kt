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
package com.arialyy.aria.util

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.arialyy.aria.core.DuaContext
import timber.log.Timber
import java.io.File
import java.math.BigInteger

internal const val AUTHORITY = ".andoFileProvider"

fun File.uri(): Uri {
  return Uri.parse(path)
}

fun String.uri():Uri{
  return Uri.parse(this)
}

fun ByteArray.toBigInteger(): BigInteger {
  return BigInteger(1, this)
}

/**
 * @return 传入的Uri是否已具备访问权限 (Whether the incoming Uri has access permission)
 */
fun giveUriPermission(uri: Uri?): Boolean {
  return uri?.run {
    when (DuaContext.context.checkUriPermission(
      this,
      android.os.Process.myPid(),
      android.os.Process.myUid(),
      Intent.FLAG_GRANT_READ_URI_PERMISSION
    )) {
      PackageManager.PERMISSION_GRANTED -> true
      PackageManager.PERMISSION_DENIED -> {
        DuaContext.context.grantUriPermission(
          DuaContext.context.packageName, this, Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        false
      }
      else -> false
    }
  } ?: false
}

fun revokeUriPermission(uri: Uri?) {
  DuaContext.context.revokeUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
}

inline fun <R> Uri.use(block: Uri.() -> R): R {
  var isAlreadyHavePermission = false
  try {
    isAlreadyHavePermission = giveUriPermission(this)
    return block()
  } catch (t: Throwable) {
    Timber.e("giveUriPermission Error ${t.message}")
  } finally {
    if (!isAlreadyHavePermission) {
      try {
        revokeUriPermission(this)
      } catch (t: Throwable) {
        Timber.e("revokeUriPermission Error ${t.message}")
      }
    }
  }
  return block()
}
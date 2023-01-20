package com.arialyy.aria.orm.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MKeyInfo(

  @PrimaryKey(autoGenerate = true) val kId: Int,

  /**
   * 加密key保存地址
   */
  val keyPath: String,

  /**
   * 加密key的下载地址
   */
  val keyUrl: String,

  /**
   * 加密算法
   */
  val method: String,

  /**
   * key的iv值
   */
  val iv: String,

  /**
   * key的格式，可能为空
   */
  val keyFormat: String? = null,

  /**
   * key的格式版本，默认为1，如果是多个版本，使用"/"分隔，如："1", "1/2", or "1/2/5"
   */
  val keyFormatVersion: String = "1",
)

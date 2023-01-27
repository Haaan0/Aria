package com.arialyy.aria.orm

import android.content.ContentValues
import android.text.TextUtils
import com.arialyy.aria.orm.annotation.Primary
import com.arialyy.aria.util.CommonUtil
import java.lang.reflect.Field
import java.lang.reflect.Type

/**
 * @Author laoyuyu
 * @Description
 * @Date 2:58 下午 2022/4/25
 **/
internal object DbUtil {

  /**
   * 创建存储数据\更新数据时使用的ContentValues
   *
   * @return 如果没有字段属性，返回null
   */
  fun createValues(dbEntity: DbEntity): ContentValues? {
    val fields = CommonUtil.getAllFields(dbEntity.javaClass)
    if (fields.size > 0) {
      val values = ContentValues()
      try {
        for (field in fields) {
          field.isAccessible = true
          if (isIgnore(dbEntity, field)) {
            continue
          }
          var value: String? = null
          val type: Type = field.type
          if (type === MutableMap::class.java && SqlUtil.checkMap(field)) {
            value = SqlUtil.map2Str(field[dbEntity] as Map<String?, String?>)
          } else if (type === MutableList::class.java && SqlUtil.checkList(field)) {
            value = SqlUtil.list2Str(dbEntity, field)
          } else {
            val obj = field[dbEntity]
            if (obj != null) {
              value = field[dbEntity].toString()
            }
          }
          values.put(field.name, SqlUtil.encodeStr(value))
        }
        return values
      } catch (e: IllegalAccessException) {
        e.printStackTrace()
      }
    }
    return null
  }

  /**
   * `true`自动增长的主键和需要忽略的字段
   */
  @Throws(IllegalAccessException::class) private fun isIgnore(obj: Any, field: Field): Boolean {
    if (SqlUtil.isIgnore(field)) {
      return true
    }
    // 忽略为空的字段
    val value = field[obj] ?: return true
    if (value is String) {
      if (TextUtils.isEmpty(value.toString())) {
        return true
      }
    }
    if (value is List<*>) {
      if (value.size == 0) {
        return true
      }
    }
    if (value is Map<*, *>) {
      if (value.size == 0) {
        return true
      }
    }
    if (SqlUtil.isPrimary(field)) {   //忽略自动增长的主键
      val p = field.getAnnotation(Primary::class.java)
      return p.autoincrement
    }
    return false
  }
}
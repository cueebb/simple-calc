package com.example

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class HistoryItem(
  val id: Long = System.currentTimeMillis(),
  val equation: String,
  val result: String,
  val time: String
)

class SettingsManager(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("calculator_preferences", Context.MODE_PRIVATE)

  var isVibrationEnabled: Boolean
    get() = prefs.getBoolean(KEY_VIBRATION, true)
    set(value) = prefs.edit().putBoolean(KEY_VIBRATION, value).apply()

  fun loadHistory(): List<HistoryItem> {
    val jsonStr = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
    return try {
      val array = JSONArray(jsonStr)
      val list = mutableListOf<HistoryItem>()
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        list.add(
          HistoryItem(
            id = obj.optLong("id", System.currentTimeMillis()),
            equation = obj.getString("equation"),
            result = obj.getString("result"),
            time = obj.getString("time")
          )
        )
      }
      list
    } catch (e: Exception) {
      emptyList()
    }
  }

  fun saveHistory(history: List<HistoryItem>) {
    val array = JSONArray()
    for (item in history.take(50)) {
      val obj = JSONObject().apply {
        put("id", item.id)
        put("equation", item.equation)
        put("result", item.result)
        put("time", item.time)
      }
      array.put(obj)
    }
    prefs.edit().putString(KEY_HISTORY, array.toString()).apply()
  }

  companion object {
    private const val KEY_VIBRATION = "pref_vibration_enabled"
    private const val KEY_HISTORY = "pref_calculator_history"
  }
}


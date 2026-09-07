package com.example

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("calculator_preferences", Context.MODE_PRIVATE)

  var isVibrationEnabled: Boolean
    get() = prefs.getBoolean(KEY_VIBRATION, true)
    set(value) = prefs.edit().putBoolean(KEY_VIBRATION, value).apply()

  companion object {
    private const val KEY_VIBRATION = "pref_vibration_enabled"
  }
}

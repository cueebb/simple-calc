package com.example

import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CalculatorUiState(
  val displayText: String = "0",
  val formulaText: String = "",
  val rawDisplay: String = "0",
  val activeOperator: Operator? = null,
  val isAllClear: Boolean = true,
  val isError: Boolean = false,
  val isVibrationEnabled: Boolean = true,
  val history: List<HistoryItem> = emptyList()
)

class CalculatorViewModel(
  private val engine: CalculatorEngine = CalculatorEngine(),
  private val settingsManager: SettingsManager? = null
) : ViewModel() {

  private var isVibration: Boolean = settingsManager?.isVibrationEnabled ?: true
  private var historyList: List<HistoryItem> = settingsManager?.loadHistory() ?: emptyList()
  private val _uiState = MutableStateFlow(createUiState(engine.getState()))
  val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

  fun onDigit(digit: Char) {
    val state = engine.onDigit(digit)
    _uiState.update { createUiState(state) }
  }

  fun onDecimal() {
    val state = engine.onDecimal()
    _uiState.update { createUiState(state) }
  }

  fun onOperator(operator: Operator) {
    val state = engine.onOperator(operator)
    _uiState.update { createUiState(state) }
  }

  fun onEquals() {
    val beforeState = engine.getState()
    val state = engine.onEquals()

    if (!state.isError && state.formula.endsWith("=") && state.formula != beforeState.formula) {
      val equation = state.formula.removeSuffix("=").trim()
      val result = engine.formatDisplayForUi(state.display)
      val time = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
      val item = HistoryItem(
        id = System.currentTimeMillis(),
        equation = equation,
        result = result,
        time = time
      )
      historyList = listOf(item) + historyList.filterNot { it.equation == equation && it.result == result }
      settingsManager?.saveHistory(historyList)
    }

    _uiState.update { createUiState(state) }
  }

  fun onToggleSign() {
    val state = engine.onToggleSign()
    _uiState.update { createUiState(state) }
  }

  fun onClear() {
    val state = engine.onClear()
    _uiState.update { createUiState(state) }
  }

  fun onSwipeDelete() {
    val state = engine.onDeleteDigit()
    _uiState.update { createUiState(state) }
  }

  fun onClearHistory() {
    historyList = emptyList()
    settingsManager?.saveHistory(emptyList())
    _uiState.update { it.copy(history = emptyList()) }
  }

  fun onSelectHistoryItem(item: HistoryItem) {
    val rawNum = item.result.replace(",", "")
    val state = engine.loadValue(rawNum)
    _uiState.update { createUiState(state) }
  }

  fun setVibrationEnabled(enabled: Boolean) {
    isVibration = enabled
    settingsManager?.isVibrationEnabled = enabled
    _uiState.update { it.copy(isVibrationEnabled = enabled) }
  }

  private fun createUiState(state: CalculatorState): CalculatorUiState {
    return CalculatorUiState(
      displayText = engine.formatDisplayForUi(state.display),
      formulaText = state.formula,
      rawDisplay = state.display,
      activeOperator = if (state.isOperatorActive) state.pendingOperator else null,
      isAllClear = state.isAllClear,
      isError = state.isError,
      isVibrationEnabled = isVibration,
      history = historyList
    )
  }
}

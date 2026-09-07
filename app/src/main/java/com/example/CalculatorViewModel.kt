package com.example

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CalculatorUiState(
  val displayText: String = "0",
  val rawDisplay: String = "0",
  val activeOperator: Operator? = null,
  val isAllClear: Boolean = true,
  val isError: Boolean = false
)

class CalculatorViewModel(
  private val engine: CalculatorEngine = CalculatorEngine()
) : ViewModel() {

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
    val state = engine.onEquals()
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

  private fun createUiState(state: CalculatorState): CalculatorUiState {
    return CalculatorUiState(
      displayText = engine.formatDisplayForUi(state.display),
      rawDisplay = state.display,
      activeOperator = if (state.isOperatorActive) state.pendingOperator else null,
      isAllClear = state.isAllClear,
      isError = state.isError
    )
  }
}

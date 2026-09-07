package com.example

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

enum class Operator(val symbol: String) {
  ADD("+"),
  SUBTRACT("-"),
  MULTIPLY("×"),
  DIVIDE("÷"),
  POWER("^")
}

data class RepeatAction(
  val operator: Operator,
  val operand: BigDecimal
)

data class CalculatorState(
  val display: String = "0",
  val storedValue: BigDecimal? = null,
  val pendingOperator: Operator? = null,
  val isOperatorActive: Boolean = false,
  val isTyping: Boolean = false,
  val lastAction: RepeatAction? = null,
  val isError: Boolean = false
) {
  val isAllClear: Boolean
    get() = display == "0" || !isTyping
}

class CalculatorEngine {

  private var state = CalculatorState()

  fun getState(): CalculatorState = state

  fun reset(initialState: CalculatorState = CalculatorState()) {
    state = initialState
  }

  fun onDigit(digit: Char): CalculatorState {
    if (state.isError) {
      state = CalculatorState(display = digit.toString(), isTyping = true)
      return state
    }

    val currentDisplay = state.display
    val newDisplay = if (!state.isTyping || currentDisplay == "0") {
      digit.toString()
    } else if (currentDisplay == "-0") {
      "-$digit"
    } else {
      // Limit to 10 significant digits
      val rawDigits = currentDisplay.replace("-", "").replace(".", "")
      if (rawDigits.length >= 10) {
        currentDisplay
      } else {
        currentDisplay + digit
      }
    }

    state = state.copy(
      display = newDisplay,
      isTyping = true,
      isOperatorActive = false,
      isError = false
    )
    return state
  }

  fun onDecimal(): CalculatorState {
    if (state.isError) {
      state = CalculatorState(display = "0.", isTyping = true)
      return state
    }

    val newDisplay = if (!state.isTyping) {
      "0."
    } else if (!state.display.contains('.')) {
      state.display + "."
    } else {
      state.display
    }

    state = state.copy(
      display = newDisplay,
      isTyping = true,
      isOperatorActive = false,
      isError = false
    )
    return state
  }

  fun onOperator(operator: Operator): CalculatorState {
    if (state.isError) return state

    val currentNum = parseDisplay(state.display)

    // If user previously entered an operator and was typing, calculate intermediate result
    if (state.storedValue != null && state.pendingOperator != null && state.isTyping) {
      val result = calculate(state.storedValue!!, currentNum, state.pendingOperator!!)
      if (result == null) {
        state = CalculatorState(display = "Error", isError = true)
        return state
      }
      val formattedResult = formatResult(result)
      state = state.copy(
        display = formattedResult,
        storedValue = result,
        pendingOperator = operator,
        isOperatorActive = true,
        isTyping = false,
        lastAction = RepeatAction(state.pendingOperator!!, currentNum)
      )
    } else {
      // Operator selected or changed
      state = state.copy(
        storedValue = currentNum,
        pendingOperator = operator,
        isOperatorActive = true,
        isTyping = false,
        lastAction = null
      )
    }
    return state
  }

  fun onEquals(): CalculatorState {
    if (state.isError) return state

    val currentNum = parseDisplay(state.display)

    if (state.storedValue != null && state.pendingOperator != null) {
      // Standard calculation: storedValue [pendingOperator] currentNum
      val op = state.pendingOperator!!
      val result = calculate(state.storedValue!!, currentNum, op)
      if (result == null) {
        state = CalculatorState(display = "Error", isError = true)
        return state
      }
      val formatted = formatResult(result)
      state = state.copy(
        display = formatted,
        storedValue = null,
        pendingOperator = null,
        isOperatorActive = false,
        isTyping = false,
        lastAction = RepeatAction(op, currentNum)
      )
    } else if (state.lastAction != null) {
      // Repeat last action on current display value!
      val (op, operand) = state.lastAction!!
      val result = calculate(currentNum, operand, op)
      if (result == null) {
        state = CalculatorState(display = "Error", isError = true)
        return state
      }
      val formatted = formatResult(result)
      state = state.copy(
        display = formatted,
        storedValue = null,
        pendingOperator = null,
        isOperatorActive = false,
        isTyping = false,
        lastAction = RepeatAction(op, operand) // Keep repeating
      )
    } else {
      // Nothing to calculate
      state = state.copy(
        isTyping = false,
        isOperatorActive = false
      )
    }
    return state
  }

  fun onToggleSign(): CalculatorState {
    if (state.isError) return state

    val cur = state.display
    val newDisplay = when {
      cur == "0" -> "-0"
      cur == "-0" -> "0"
      cur.startsWith("-") -> cur.substring(1)
      else -> "-$cur"
    }

    state = state.copy(
      display = newDisplay,
      isTyping = if (cur == "0" || cur == "-0") true else state.isTyping
    )
    return state
  }

  fun onClear(): CalculatorState {
    // If typing and display != 0, C clears entry back to 0 but keeps pending operator & storedValue
    if (state.isTyping && state.display != "0") {
      state = state.copy(
        display = "0",
        isTyping = false
      )
    } else {
      // All Clear (AC)
      state = CalculatorState()
    }
    return state
  }

  fun onDeleteDigit(): CalculatorState {
    if (state.isError) {
      state = CalculatorState(display = "0")
      return state
    }

    val cur = state.display
    if (cur == "0") return state

    val newDisplay = when {
      cur.length == 1 -> "0"
      cur.length == 2 && cur.startsWith("-") -> "0"
      cur == "-0" -> "0"
      else -> cur.dropLast(1)
    }

    state = state.copy(
      display = if (newDisplay == "-") "0" else newDisplay,
      isTyping = true
    )
    return state
  }

  private fun parseDisplay(text: String): BigDecimal {
    return try {
      BigDecimal(text.replace(",", ""))
    } catch (e: Exception) {
      BigDecimal.ZERO
    }
  }

  private fun calculate(a: BigDecimal, b: BigDecimal, op: Operator): BigDecimal? {
    val mc = MathContext(16, RoundingMode.HALF_UP)
    return try {
      when (op) {
        Operator.ADD -> a.add(b, mc)
        Operator.SUBTRACT -> a.subtract(b, mc)
        Operator.MULTIPLY -> a.multiply(b, mc)
        Operator.DIVIDE -> {
          if (b.compareTo(BigDecimal.ZERO) == 0) null
          else a.divide(b, mc)
        }
        Operator.POWER -> {
          val bDouble = b.toDouble()
          if (b.scale() <= 0 || b.stripTrailingZeros().scale() <= 0) {
            val exponent = b.toInt()
            if (exponent >= 0 && exponent <= 999) {
              a.pow(exponent, mc)
            } else if (exponent < 0 && exponent >= -999) {
              BigDecimal.ONE.divide(a.pow(-exponent, mc), mc)
            } else {
              val powDouble = Math.pow(a.toDouble(), bDouble)
              if (powDouble.isNaN() || powDouble.isInfinite()) null
              else BigDecimal(powDouble, mc)
            }
          } else {
            val powDouble = Math.pow(a.toDouble(), bDouble)
            if (powDouble.isNaN() || powDouble.isInfinite()) null
            else BigDecimal(powDouble, mc)
          }
        }
      }
    } catch (e: Exception) {
      null
    }
  }

  fun formatResult(value: BigDecimal): String {
    val stripped = value.stripTrailingZeros()
    val plain = stripped.toPlainString()

    // If fits within 10 characters, display plain
    if (plain.length <= 10 && !plain.contains("E") && !plain.contains("e")) {
      return plain
    }

    // Check if number is extremely large or small
    val absValue = value.abs()
    if ((absValue >= BigDecimal("1e10") || (absValue > BigDecimal.ZERO && absValue < BigDecimal("1e-6")))) {
      // Use scientific notation
      val symbols = DecimalFormatSymbols(Locale.US)
      val formatter = DecimalFormat("0.######E0", symbols)
      return formatter.format(value.toDouble()).lowercase()
    }

    // Format with max 8 decimal places
    val mc = MathContext(10, RoundingMode.HALF_UP)
    val rounded = value.round(mc).stripTrailingZeros()
    val roundedPlain = rounded.toPlainString()
    return if (roundedPlain.length <= 10) roundedPlain else {
      val symbols = DecimalFormatSymbols(Locale.US)
      val formatter = DecimalFormat("0.######E0", symbols)
      formatter.format(value.toDouble()).lowercase()
    }
  }

  fun formatDisplayForUi(display: String): String {
    if (display == "Error" || display == "-0") return display
    if (display.contains("e") || display.contains("E")) return display

    val isNegative = display.startsWith("-")
    val clean = if (isNegative) display.substring(1) else display

    val parts = clean.split(".", limit = 2)
    val integerPart = parts[0]
    val hasDecimal = clean.contains(".")
    val decimalPart = if (parts.size > 1) parts[1] else ""

    // Format integer part with commas
    val formattedInteger = try {
      val symbols = DecimalFormatSymbols(Locale.US)
      val df = DecimalFormat("#,###", symbols)
      if (integerPart.isEmpty()) "0" else df.format(integerPart.toBigInteger())
    } catch (e: Exception) {
      integerPart
    }

    return buildString {
      if (isNegative) append("-")
      append(formattedInteger)
      if (hasDecimal) {
        append(".")
        append(decimalPart)
      }
    }
  }
}

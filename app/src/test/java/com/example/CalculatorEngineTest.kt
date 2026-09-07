package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CalculatorEngineTest {

  private lateinit var engine: CalculatorEngine

  @Before
  fun setUp() {
    engine = CalculatorEngine()
  }

  @Test
  fun `test simple addition`() {
    engine.onDigit('5')
    engine.onOperator(Operator.ADD)
    engine.onDigit('3')
    val state = engine.onEquals()
    assertEquals("8", state.display)
  }

  @Test
  fun `test repeat equals repeats last action`() {
    // 5 + 3 = 8
    engine.onDigit('5')
    engine.onOperator(Operator.ADD)
    engine.onDigit('3')
    var state = engine.onEquals()
    assertEquals("8", state.display)

    // Tap = again -> 8 + 3 = 11
    state = engine.onEquals()
    assertEquals("11", state.display)

    // Tap = again -> 11 + 3 = 14
    state = engine.onEquals()
    assertEquals("14", state.display)
  }

  @Test
  fun `test repeat equals with multiplication`() {
    // 4 * 3 = 12
    engine.onDigit('4')
    engine.onOperator(Operator.MULTIPLY)
    engine.onDigit('3')
    var state = engine.onEquals()
    assertEquals("12", state.display)

    // 12 * 3 = 36
    state = engine.onEquals()
    assertEquals("36", state.display)

    // 36 * 3 = 108
    state = engine.onEquals()
    assertEquals("108", state.display)
  }

  @Test
  fun `test repeat equals with subtraction`() {
    // 10 - 2 = 8
    engine.onDigit('1')
    engine.onDigit('0')
    engine.onOperator(Operator.SUBTRACT)
    engine.onDigit('2')
    var state = engine.onEquals()
    assertEquals("8", state.display)

    // 8 - 2 = 6
    state = engine.onEquals()
    assertEquals("6", state.display)

    // 6 - 2 = 4
    state = engine.onEquals()
    assertEquals("4", state.display)
  }

  @Test
  fun `test power operator and repeat equals`() {
    // 2 ^ 3 = 8
    engine.onDigit('2')
    engine.onOperator(Operator.POWER)
    engine.onDigit('3')
    var state = engine.onEquals()
    assertEquals("8", state.display)

    // 8 ^ 3 = 512
    state = engine.onEquals()
    assertEquals("512", state.display)
  }

  @Test
  fun `test toggle sign`() {
    engine.onDigit('7')
    var state = engine.onToggleSign()
    assertEquals("-7", state.display)

    state = engine.onToggleSign()
    assertEquals("7", state.display)

    // Toggle on zero
    engine.onClear()
    state = engine.onToggleSign()
    assertEquals("-0", state.display)
    state = engine.onDigit('9')
    assertEquals("-9", state.display)
  }

  @Test
  fun `test delete last digit via swipe`() {
    // User types 123
    engine.onDigit('1')
    engine.onDigit('2')
    engine.onDigit('3')
    assertEquals("123", engine.getState().display)

    // Swipe 1: 123 -> 12
    var state = engine.onDeleteDigit()
    assertEquals("12", state.display)

    // Swipe 2: 12 -> 1
    state = engine.onDeleteDigit()
    assertEquals("1", state.display)

    // Swipe 3: 1 -> 0
    state = engine.onDeleteDigit()
    assertEquals("0", state.display)

    // Swipe on 0 stays 0
    state = engine.onDeleteDigit()
    assertEquals("0", state.display)
  }

  @Test
  fun `test delete on negative single digit becomes zero`() {
    engine.onDigit('5')
    engine.onToggleSign()
    assertEquals("-5", engine.getState().display)

    val state = engine.onDeleteDigit()
    assertEquals("0", state.display)
  }

  @Test
  fun `test clear button logic AC vs C`() {
    // Initially AC
    assertTrue(engine.getState().isAllClear)

    // Type digit -> now C
    engine.onDigit('5')
    assertFalse(engine.getState().isAllClear)

    // Set operator +
    engine.onOperator(Operator.ADD)
    // Type 9
    engine.onDigit('9')
    assertFalse(engine.getState().isAllClear)

    // Tap C: clears current display to 0, preserves stored 5 and +
    engine.onClear()
    assertEquals("0", engine.getState().display)
    assertEquals(Operator.ADD, engine.getState().pendingOperator)

    // Now type 3, tap = -> 5 + 3 = 8
    engine.onDigit('3')
    val state = engine.onEquals()
    assertEquals("8", state.display)
  }

  @Test
  fun `test divide by zero shows error`() {
    engine.onDigit('8')
    engine.onOperator(Operator.DIVIDE)
    engine.onDigit('0')
    val state = engine.onEquals()
    assertEquals("Error", state.display)
    assertTrue(state.isError)
  }
}

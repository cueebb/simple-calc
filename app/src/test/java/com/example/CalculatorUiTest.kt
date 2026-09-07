package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import com.example.ui.theme.MyApplicationTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CalculatorUiTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun testCalculatorKeypadAndRepeatEquals() {
    composeTestRule.setContent {
      MyApplicationTheme {
        CalculatorScreen()
      }
    }

    // Check display initially shows 0
    composeTestRule.onNodeWithTag("display_text").assertTextEquals("0")
    composeTestRule.onNodeWithTag("btn_clear").assertTextEquals("AC")

    // Tap 5
    composeTestRule.onNodeWithTag("btn_5").performClick()
    composeTestRule.onNodeWithTag("display_text").assertTextEquals("5")
    composeTestRule.onNodeWithTag("btn_clear").assertTextEquals("C")

    // Tap +
    composeTestRule.onNodeWithTag("btn_add").performClick()

    // Tap 3
    composeTestRule.onNodeWithTag("btn_3").performClick()
    composeTestRule.onNodeWithTag("display_text").assertTextEquals("3")

    // Tap = -> 8
    composeTestRule.onNodeWithTag("btn_equals").performClick()
    composeTestRule.onNodeWithTag("display_text").assertTextEquals("8")

    // Tap = again -> 11 (repeat last action + 3)
    composeTestRule.onNodeWithTag("btn_equals").performClick()
    composeTestRule.onNodeWithTag("display_text").assertTextEquals("11")

    // Tap = again -> 14
    composeTestRule.onNodeWithTag("btn_equals").performClick()
    composeTestRule.onNodeWithTag("display_text").assertTextEquals("14")
  }

  @Test
  fun testSwipeToDeleteDigits() {
    composeTestRule.setContent {
      MyApplicationTheme {
        CalculatorScreen()
      }
    }

    // Type 1, 2, 3
    composeTestRule.onNodeWithTag("btn_1").performClick()
    composeTestRule.onNodeWithTag("btn_2").performClick()
    composeTestRule.onNodeWithTag("btn_3").performClick()
    composeTestRule.onNodeWithTag("display_text").assertTextEquals("123")

    // Swipe left on display area
    composeTestRule.onNodeWithTag("display_area").performTouchInput {
      swipeLeft()
    }
    composeTestRule.onNodeWithTag("display_text").assertTextEquals("12")

    // Swipe right on display area
    composeTestRule.onNodeWithTag("display_area").performTouchInput {
      swipeRight()
    }
    composeTestRule.onNodeWithTag("display_text").assertTextEquals("1")

    // Swipe left again -> becomes 0
    composeTestRule.onNodeWithTag("display_area").performTouchInput {
      swipeLeft()
    }
    composeTestRule.onNodeWithTag("display_text").assertTextEquals("0")
  }

  @Test
  fun testPowerAndToggleSign() {
    composeTestRule.setContent {
      MyApplicationTheme {
        CalculatorScreen()
      }
    }

    // Tap 2, ^, 3, = -> 8
    composeTestRule.onNodeWithTag("btn_2").performClick()
    composeTestRule.onNodeWithTag("btn_power").performClick()
    composeTestRule.onNodeWithTag("btn_3").performClick()
    composeTestRule.onNodeWithTag("btn_equals").performClick()
    composeTestRule.onNodeWithTag("display_text").assertTextEquals("8")

    // Toggle +/- -> -8
    composeTestRule.onNodeWithTag("btn_sign").performClick()
    composeTestRule.onNodeWithTag("display_text").assertTextEquals("-8")
  }

  @Test
  fun testZeroAndDecimal() {
    composeTestRule.setContent {
      MyApplicationTheme {
        CalculatorScreen()
      }
    }

    composeTestRule.onNodeWithTag("btn_0").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_0").performClick()
    composeTestRule.onNodeWithTag("display_text").assertTextEquals("0")

    composeTestRule.onNodeWithTag("btn_decimal").performClick()
    composeTestRule.onNodeWithTag("display_text").assertTextEquals("0.")

    composeTestRule.onNodeWithTag("btn_5").performClick()
    composeTestRule.onNodeWithTag("display_text").assertTextEquals("0.5")
  }

  @Test
  fun testSettingsDialogAndVibrationToggle() {
    composeTestRule.setContent {
      MyApplicationTheme {
        CalculatorScreen()
      }
    }

    // Open settings from top-left button
    composeTestRule.onNodeWithTag("btn_settings").assertIsDisplayed().performClick()

    // Vibration switch should be visible and initially ON
    composeTestRule.onNodeWithTag("switch_vibration").assertIsDisplayed().assertIsOn()

    // Toggle OFF
    composeTestRule.onNodeWithTag("switch_vibration").performClick()
    composeTestRule.onNodeWithTag("switch_vibration").assertIsOff()

    // Close settings dialog
    composeTestRule.onNodeWithTag("btn_close_settings").assertIsDisplayed().performClick()

    // Reopen settings dialog and verify state persisted
    composeTestRule.onNodeWithTag("btn_settings").performClick()
    composeTestRule.onNodeWithTag("switch_vibration").assertIsOff()
  }

  @Test
  fun testHistoryDialog() {
    composeTestRule.setContent {
      MyApplicationTheme {
        CalculatorScreen()
      }
    }

    // Top-left settings and top-right history should be displayed in the corners
    composeTestRule.onNodeWithTag("btn_settings").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_history").assertIsDisplayed()

    // Perform calculation 7 x 6 = 42
    composeTestRule.onNodeWithTag("btn_7").performClick()
    composeTestRule.onNodeWithTag("btn_multiply").performClick()
    composeTestRule.onNodeWithTag("btn_6").performClick()
    composeTestRule.onNodeWithTag("btn_equals").performClick()
    composeTestRule.onNodeWithTag("display_text").assertTextEquals("42")

    // Open history dialog
    composeTestRule.onNodeWithTag("btn_history").performClick()

    // Close button should be displayed
    composeTestRule.onNodeWithTag("btn_close_history").assertIsDisplayed().performClick()
  }
}

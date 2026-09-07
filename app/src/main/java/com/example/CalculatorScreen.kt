package com.example

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.CalcBackgroundDark
import com.example.ui.theme.CalcFunctionButtonDark
import com.example.ui.theme.CalcFunctionTextDark
import com.example.ui.theme.CalcNumberButtonDark
import com.example.ui.theme.CalcNumberTextDark
import com.example.ui.theme.CalcOperatorActiveBg
import com.example.ui.theme.CalcOperatorActiveText
import com.example.ui.theme.CalcOperatorButton
import com.example.ui.theme.CalcOperatorText
import kotlin.math.abs
import kotlin.math.min

private enum class ButtonType {
  NUMBER,
  FUNCTION,
  OPERATOR
}

@Composable
fun CalculatorScreen(
  viewModel: CalculatorViewModel = viewModel(),
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val haptic = LocalHapticFeedback.current

  Surface(
    modifier = modifier
      .fillMaxSize()
      .testTag("calculator_screen"),
    color = CalcBackgroundDark
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.Bottom
    ) {
      // Upper corners are left blank for now as requested
      Spacer(modifier = Modifier.weight(1f))

      // Display Area with horizontal swipe-to-delete
      DisplaySection(
        displayText = uiState.displayText,
        onSwipeDelete = {
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          viewModel.onSwipeDelete()
        }
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Keypad Grid (5 rows x 4 columns)
      KeypadSection(
        uiState = uiState,
        onDigit = { digit ->
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          viewModel.onDigit(digit)
        },
        onDecimal = {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          viewModel.onDecimal()
        },
        onOperator = { op ->
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          viewModel.onOperator(op)
        },
        onEquals = {
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          viewModel.onEquals()
        },
        onToggleSign = {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          viewModel.onToggleSign()
        },
        onClear = {
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          viewModel.onClear()
        }
      )
    }
  }
}

@Composable
private fun DisplaySection(
  displayText: String,
  onSwipeDelete: () -> Unit,
  modifier: Modifier = Modifier
) {
  var accumulatedDragX by remember { mutableFloatStateOf(0f) }
  var dragConsumed by remember { mutableStateOf(false) }

  // Dynamic font sizing based on length of displayed string
  val fontSize: TextUnit = when {
    displayText.length <= 6 -> 74.sp
    displayText.length <= 8 -> 60.sp
    displayText.length <= 10 -> 48.sp
    displayText.length <= 12 -> 38.sp
    else -> 30.sp
  }

  Box(
    modifier = modifier
      .fillMaxWidth()
      .testTag("display_area")
      .pointerInput(Unit) {
        detectHorizontalDragGestures(
          onDragStart = {
            accumulatedDragX = 0f
            dragConsumed = false
          },
          onDragEnd = {
            accumulatedDragX = 0f
            dragConsumed = false
          },
          onDragCancel = {
            accumulatedDragX = 0f
            dragConsumed = false
          },
          onHorizontalDrag = { change, dragAmount ->
            change.consume()
            if (!dragConsumed) {
              accumulatedDragX += dragAmount
              if (abs(accumulatedDragX) > 24f) {
                dragConsumed = true
                onSwipeDelete()
              }
            }
          }
        )
      }
      .padding(horizontal = 8.dp, vertical = 8.dp),
    contentAlignment = Alignment.BottomEnd
  ) {
    Text(
      text = displayText,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("display_text"),
      textAlign = TextAlign.End,
      color = Color.White,
      fontSize = fontSize,
      fontWeight = FontWeight.Light,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}

@Composable
private fun KeypadSection(
  uiState: CalculatorUiState,
  onDigit: (Char) -> Unit,
  onDecimal: () -> Unit,
  onOperator: (Operator) -> Unit,
  onEquals: () -> Unit,
  onToggleSign: () -> Unit,
  onClear: () -> Unit
) {
  val clearLabel = if (uiState.isAllClear) "AC" else "C"

  BoxWithConstraints(
    modifier = Modifier
      .fillMaxWidth()
      .padding(bottom = 8.dp)
  ) {
    val spacing: Dp = 12.dp
    // 4 columns: 3 gaps
    val buttonWidth: Dp = (maxWidth - (spacing * 3)) / 4
    val maxRowHeight: Dp = if (maxHeight != Dp.Infinity && maxHeight > 0.dp) {
      (maxHeight - (spacing * 4)) / 5
    } else {
      buttonWidth
    }
    val buttonSize: Dp = min(buttonWidth.value, maxRowHeight.value).dp

    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
      // Row 1: Clear (AC/C), +/-, ^, ÷
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing)
      ) {
        CalcButton(
          text = clearLabel,
          type = ButtonType.FUNCTION,
          size = buttonSize,
          testTag = "btn_clear",
          onClick = onClear
        )
        CalcButton(
          text = "+/-",
          type = ButtonType.FUNCTION,
          size = buttonSize,
          testTag = "btn_sign",
          onClick = onToggleSign
        )
        CalcButton(
          text = "^",
          type = ButtonType.FUNCTION,
          size = buttonSize,
          testTag = "btn_power",
          onClick = { onOperator(Operator.POWER) },
          isActive = uiState.activeOperator == Operator.POWER
        )
        CalcButton(
          text = "÷",
          type = ButtonType.OPERATOR,
          size = buttonSize,
          testTag = "btn_divide",
          onClick = { onOperator(Operator.DIVIDE) },
          isActive = uiState.activeOperator == Operator.DIVIDE
        )
      }

      // Row 2: 7, 8, 9, ×
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing)
      ) {
        CalcButton(
          text = "7",
          type = ButtonType.NUMBER,
          size = buttonSize,
          testTag = "btn_7",
          onClick = { onDigit('7') }
        )
        CalcButton(
          text = "8",
          type = ButtonType.NUMBER,
          size = buttonSize,
          testTag = "btn_8",
          onClick = { onDigit('8') }
        )
        CalcButton(
          text = "9",
          type = ButtonType.NUMBER,
          size = buttonSize,
          testTag = "btn_9",
          onClick = { onDigit('9') }
        )
        CalcButton(
          text = "×",
          type = ButtonType.OPERATOR,
          size = buttonSize,
          testTag = "btn_multiply",
          onClick = { onOperator(Operator.MULTIPLY) },
          isActive = uiState.activeOperator == Operator.MULTIPLY
        )
      }

      // Row 3: 4, 5, 6, -
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing)
      ) {
        CalcButton(
          text = "4",
          type = ButtonType.NUMBER,
          size = buttonSize,
          testTag = "btn_4",
          onClick = { onDigit('4') }
        )
        CalcButton(
          text = "5",
          type = ButtonType.NUMBER,
          size = buttonSize,
          testTag = "btn_5",
          onClick = { onDigit('5') }
        )
        CalcButton(
          text = "6",
          type = ButtonType.NUMBER,
          size = buttonSize,
          testTag = "btn_6",
          onClick = { onDigit('6') }
        )
        CalcButton(
          text = "-",
          type = ButtonType.OPERATOR,
          size = buttonSize,
          testTag = "btn_subtract",
          onClick = { onOperator(Operator.SUBTRACT) },
          isActive = uiState.activeOperator == Operator.SUBTRACT
        )
      }

      // Row 4: 1, 2, 3, +
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing)
      ) {
        CalcButton(
          text = "1",
          type = ButtonType.NUMBER,
          size = buttonSize,
          testTag = "btn_1",
          onClick = { onDigit('1') }
        )
        CalcButton(
          text = "2",
          type = ButtonType.NUMBER,
          size = buttonSize,
          testTag = "btn_2",
          onClick = { onDigit('2') }
        )
        CalcButton(
          text = "3",
          type = ButtonType.NUMBER,
          size = buttonSize,
          testTag = "btn_3",
          onClick = { onDigit('3') }
        )
        CalcButton(
          text = "+",
          type = ButtonType.OPERATOR,
          size = buttonSize,
          testTag = "btn_add",
          onClick = { onOperator(Operator.ADD) },
          isActive = uiState.activeOperator == Operator.ADD
        )
      }

      // Row 5: 0 (takes 2 sections in bottom), ., =
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing)
      ) {
        val zeroWidth = (buttonSize * 2) + spacing
        CalcPillButton(
          text = "0",
          width = zeroWidth,
          height = buttonSize,
          leadingIndent = buttonSize / 2.5f,
          testTag = "btn_0",
          onClick = { onDigit('0') }
        )
        CalcButton(
          text = ".",
          type = ButtonType.NUMBER,
          size = buttonSize,
          testTag = "btn_decimal",
          onClick = onDecimal
        )
        CalcButton(
          text = "=",
          type = ButtonType.OPERATOR,
          size = buttonSize,
          testTag = "btn_equals",
          onClick = onEquals
        )
      }
    }
  }
}

@Composable
private fun CalcButton(
  text: String,
  type: ButtonType,
  size: Dp,
  testTag: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  isActive: Boolean = false
) {
  val targetBgColor = when (type) {
    ButtonType.FUNCTION -> CalcFunctionButtonDark
    ButtonType.NUMBER -> CalcNumberButtonDark
    ButtonType.OPERATOR -> if (isActive) CalcOperatorActiveBg else CalcOperatorButton
  }

  val targetTextColor = when (type) {
    ButtonType.FUNCTION -> CalcFunctionTextDark
    ButtonType.NUMBER -> CalcNumberTextDark
    ButtonType.OPERATOR -> if (isActive) CalcOperatorActiveText else CalcOperatorText
  }

  val animatedBg by animateColorAsState(
    targetValue = targetBgColor,
    animationSpec = tween(150),
    label = "button_bg"
  )
  val animatedText by animateColorAsState(
    targetValue = targetTextColor,
    animationSpec = tween(150),
    label = "button_text"
  )

  val fontSize = when {
    text.length > 2 -> 24.sp
    text == "+/-" -> 24.sp
    type == ButtonType.OPERATOR -> 34.sp
    else -> 32.sp
  }

  Box(
    modifier = modifier
      .size(size)
      .clip(CircleShape)
      .background(animatedBg)
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(bounded = true),
        onClick = onClick
      )
      .testTag(testTag),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = text,
      color = animatedText,
      fontSize = fontSize,
      fontWeight = if (type == ButtonType.FUNCTION) FontWeight.Medium else FontWeight.Normal,
      textAlign = TextAlign.Center
    )
  }
}

@Composable
private fun CalcPillButton(
  text: String,
  width: Dp,
  height: Dp,
  leadingIndent: Dp,
  testTag: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .width(width)
      .height(height)
      .clip(RoundedCornerShape(percent = 50))
      .background(CalcNumberButtonDark)
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(bounded = true),
        onClick = onClick
      )
      .testTag(testTag)
      .padding(start = leadingIndent),
    contentAlignment = Alignment.CenterStart
  ) {
    Text(
      text = text,
      color = CalcNumberTextDark,
      fontSize = 32.sp,
      fontWeight = FontWeight.Normal,
      textAlign = TextAlign.Start
    )
  }
}

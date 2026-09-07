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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.GoogleCalcBackground
import com.example.ui.theme.GoogleCalcEqualsBg
import com.example.ui.theme.GoogleCalcEqualsText
import com.example.ui.theme.GoogleCalcFormulaText
import com.example.ui.theme.GoogleCalcFunctionBg
import com.example.ui.theme.GoogleCalcFunctionText
import com.example.ui.theme.GoogleCalcNumberBg
import com.example.ui.theme.GoogleCalcNumberText
import com.example.ui.theme.GoogleCalcOperatorActiveBg
import com.example.ui.theme.GoogleCalcOperatorActiveText
import com.example.ui.theme.GoogleCalcOperatorBg
import com.example.ui.theme.GoogleCalcOperatorText
import kotlin.math.abs

private enum class ButtonType {
  NUMBER,
  FUNCTION,
  OPERATOR,
  EQUALS
}

@Composable
fun CalculatorScreen(
  modifier: Modifier = Modifier,
  viewModel: CalculatorViewModel = defaultCalculatorViewModel()
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val haptic = LocalHapticFeedback.current
  var showSettingsDialog by remember { mutableStateOf(false) }

  Surface(
    modifier = modifier
      .fillMaxSize()
      .testTag("calculator_screen"),
    color = GoogleCalcBackground
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .padding(horizontal = 12.dp, vertical = 6.dp),
      verticalArrangement = Arrangement.Bottom
    ) {
      // Top header bar with Settings button in upper left corner
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = { showSettingsDialog = true },
          modifier = Modifier.testTag("btn_settings")
        ) {
          Icon(
            imageVector = Icons.Outlined.Settings,
            contentDescription = "Settings",
            tint = GoogleCalcFormulaText
          )
        }
        // Upper right corner remains blank as requested
        Spacer(modifier = Modifier.size(48.dp))
      }

      Spacer(modifier = Modifier.weight(1f, fill = false))

      // Display Area with horizontal swipe-to-delete
      DisplaySection(
        displayText = uiState.displayText,
        formulaText = uiState.formulaText,
        onSwipeDelete = {
          performHaptic(haptic, uiState.isVibrationEnabled, HapticFeedbackType.LongPress)
          viewModel.onSwipeDelete()
        }
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Keypad Section (occupying 100% of the screen width)
      KeypadSection(
        uiState = uiState,
        onDigit = { digit ->
          performHaptic(haptic, uiState.isVibrationEnabled, HapticFeedbackType.TextHandleMove)
          viewModel.onDigit(digit)
        },
        onDecimal = {
          performHaptic(haptic, uiState.isVibrationEnabled, HapticFeedbackType.TextHandleMove)
          viewModel.onDecimal()
        },
        onOperator = { op ->
          performHaptic(haptic, uiState.isVibrationEnabled, HapticFeedbackType.LongPress)
          viewModel.onOperator(op)
        },
        onEquals = {
          performHaptic(haptic, uiState.isVibrationEnabled, HapticFeedbackType.LongPress)
          viewModel.onEquals()
        },
        onToggleSign = {
          performHaptic(haptic, uiState.isVibrationEnabled, HapticFeedbackType.TextHandleMove)
          viewModel.onToggleSign()
        },
        onClear = {
          performHaptic(haptic, uiState.isVibrationEnabled, HapticFeedbackType.LongPress)
          viewModel.onClear()
        }
      )
    }

    if (showSettingsDialog) {
      SettingsDialog(
        isVibrationEnabled = uiState.isVibrationEnabled,
        onVibrationChanged = { viewModel.setVibrationEnabled(it) },
        onDismiss = { showSettingsDialog = false }
      )
    }
  }
}

@Composable
private fun defaultCalculatorViewModel(): CalculatorViewModel {
  val context = LocalContext.current
  return viewModel(
    factory = object : ViewModelProvider.Factory {
      @Suppress("UNCHECKED_CAST")
      override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val settings = SettingsManager(context.applicationContext)
        return CalculatorViewModel(settingsManager = settings) as T
      }
    }
  )
}

private fun performHaptic(haptic: HapticFeedback, isEnabled: Boolean, type: HapticFeedbackType) {
  if (isEnabled) {
    haptic.performHapticFeedback(type)
  }
}

@Composable
private fun DisplaySection(
  displayText: String,
  formulaText: String,
  onSwipeDelete: () -> Unit,
  modifier: Modifier = Modifier
) {
  var accumulatedDragX by remember { mutableFloatStateOf(0f) }
  var dragConsumed by remember { mutableStateOf(false) }

  val fontSize: TextUnit = when {
    displayText.length <= 6 -> 68.sp
    displayText.length <= 8 -> 54.sp
    displayText.length <= 10 -> 44.sp
    displayText.length <= 12 -> 34.sp
    else -> 26.sp
  }

  Column(
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
      .padding(horizontal = 6.dp, vertical = 2.dp),
    horizontalAlignment = Alignment.End,
    verticalArrangement = Arrangement.Bottom
  ) {
    // Formula / secondary line
    if (formulaText.isNotEmpty()) {
      Text(
        text = formulaText,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("formula_text")
          .padding(bottom = 4.dp),
        textAlign = TextAlign.End,
        color = GoogleCalcFormulaText,
        fontSize = 22.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = FontFamily.SansSerif,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }

    // Main result/number display
    Text(
      text = displayText,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("display_text"),
      textAlign = TextAlign.End,
      color = Color.White,
      fontSize = fontSize,
      fontWeight = FontWeight.Light,
      fontFamily = FontFamily.SansSerif,
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
      .padding(bottom = 4.dp)
  ) {
    val spacing: Dp = 10.dp
    // 4 columns: exactly 3 gaps, guaranteed to take 100% of the screen width
    val buttonWidth: Dp = (maxWidth - (spacing * 3)) / 4
    val buttonHeight: Dp = (buttonWidth * 0.85f).coerceIn(48.dp, 72.dp)

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
          width = buttonWidth,
          height = buttonHeight,
          testTag = "btn_clear",
          onClick = onClear
        )
        CalcButton(
          text = "+/−",
          type = ButtonType.FUNCTION,
          width = buttonWidth,
          height = buttonHeight,
          testTag = "btn_sign",
          onClick = onToggleSign
        )
        CalcButton(
          text = "^",
          type = ButtonType.FUNCTION,
          width = buttonWidth,
          height = buttonHeight,
          testTag = "btn_power",
          onClick = { onOperator(Operator.POWER) },
          isActive = uiState.activeOperator == Operator.POWER
        )
        CalcButton(
          text = "÷",
          type = ButtonType.OPERATOR,
          width = buttonWidth,
          height = buttonHeight,
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
          width = buttonWidth,
          height = buttonHeight,
          testTag = "btn_7",
          onClick = { onDigit('7') }
        )
        CalcButton(
          text = "8",
          type = ButtonType.NUMBER,
          width = buttonWidth,
          height = buttonHeight,
          testTag = "btn_8",
          onClick = { onDigit('8') }
        )
        CalcButton(
          text = "9",
          type = ButtonType.NUMBER,
          width = buttonWidth,
          height = buttonHeight,
          testTag = "btn_9",
          onClick = { onDigit('9') }
        )
        CalcButton(
          text = "×",
          type = ButtonType.OPERATOR,
          width = buttonWidth,
          height = buttonHeight,
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
          width = buttonWidth,
          height = buttonHeight,
          testTag = "btn_4",
          onClick = { onDigit('4') }
        )
        CalcButton(
          text = "5",
          type = ButtonType.NUMBER,
          width = buttonWidth,
          height = buttonHeight,
          testTag = "btn_5",
          onClick = { onDigit('5') }
        )
        CalcButton(
          text = "6",
          type = ButtonType.NUMBER,
          width = buttonWidth,
          height = buttonHeight,
          testTag = "btn_6",
          onClick = { onDigit('6') }
        )
        CalcButton(
          text = "−",
          type = ButtonType.OPERATOR,
          width = buttonWidth,
          height = buttonHeight,
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
          width = buttonWidth,
          height = buttonHeight,
          testTag = "btn_1",
          onClick = { onDigit('1') }
        )
        CalcButton(
          text = "2",
          type = ButtonType.NUMBER,
          width = buttonWidth,
          height = buttonHeight,
          testTag = "btn_2",
          onClick = { onDigit('2') }
        )
        CalcButton(
          text = "3",
          type = ButtonType.NUMBER,
          width = buttonWidth,
          height = buttonHeight,
          testTag = "btn_3",
          onClick = { onDigit('3') }
        )
        CalcButton(
          text = "+",
          type = ButtonType.OPERATOR,
          width = buttonWidth,
          height = buttonHeight,
          testTag = "btn_add",
          onClick = { onOperator(Operator.ADD) },
          isActive = uiState.activeOperator == Operator.ADD
        )
      }

      // Row 5: 0 (takes two sections in bottom), ., =
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing)
      ) {
        val zeroWidth = (buttonWidth * 2) + spacing
        CalcPillButton(
          text = "0",
          width = zeroWidth,
          height = buttonHeight,
          testTag = "btn_0",
          onClick = { onDigit('0') }
        )
        CalcButton(
          text = ".",
          type = ButtonType.NUMBER,
          width = buttonWidth,
          height = buttonHeight,
          testTag = "btn_decimal",
          onClick = onDecimal
        )
        CalcButton(
          text = "=",
          type = ButtonType.EQUALS,
          width = buttonWidth,
          height = buttonHeight,
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
  width: Dp,
  height: Dp,
  testTag: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  isActive: Boolean = false
) {
  val targetBgColor = when (type) {
    ButtonType.FUNCTION -> GoogleCalcFunctionBg
    ButtonType.NUMBER -> GoogleCalcNumberBg
    ButtonType.OPERATOR -> if (isActive) GoogleCalcOperatorActiveBg else GoogleCalcOperatorBg
    ButtonType.EQUALS -> GoogleCalcEqualsBg
  }

  val targetTextColor = when (type) {
    ButtonType.FUNCTION -> GoogleCalcFunctionText
    ButtonType.NUMBER -> GoogleCalcNumberText
    ButtonType.OPERATOR -> if (isActive) GoogleCalcOperatorActiveText else GoogleCalcOperatorText
    ButtonType.EQUALS -> GoogleCalcEqualsText
  }

  val animatedBg by animateColorAsState(
    targetValue = targetBgColor,
    animationSpec = tween(150),
    label = "btn_bg"
  )
  val animatedText by animateColorAsState(
    targetValue = targetTextColor,
    animationSpec = tween(150),
    label = "btn_text"
  )

  val fontSize = when {
    text.length > 2 -> 22.sp
    text == "+/−" -> 22.sp
    type == ButtonType.OPERATOR -> 30.sp
    type == ButtonType.EQUALS -> 34.sp
    else -> 28.sp
  }

  val fontWeight = when (type) {
    ButtonType.FUNCTION -> FontWeight.Medium
    ButtonType.NUMBER -> FontWeight.Normal
    ButtonType.OPERATOR -> FontWeight.Medium
    ButtonType.EQUALS -> FontWeight.Bold
  }

  Box(
    modifier = modifier
      .width(width)
      .height(height)
      .clip(RoundedCornerShape(percent = 50))
      .background(animatedBg)
      .testTag(testTag)
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(bounded = true),
        onClick = onClick
      ),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = text,
      color = animatedText,
      fontSize = fontSize,
      fontWeight = fontWeight,
      fontFamily = FontFamily.SansSerif,
      textAlign = TextAlign.Center
    )
  }
}

@Composable
private fun CalcPillButton(
  text: String,
  width: Dp,
  height: Dp,
  testTag: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .width(width)
      .height(height)
      .clip(RoundedCornerShape(percent = 50))
      .background(GoogleCalcNumberBg)
      .testTag(testTag)
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(bounded = true),
        onClick = onClick
      ),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = text,
      color = GoogleCalcNumberText,
      fontSize = 28.sp,
      fontWeight = FontWeight.Normal,
      fontFamily = FontFamily.SansSerif,
      textAlign = TextAlign.Center
    )
  }
}

@Composable
private fun SettingsDialog(
  isVibrationEnabled: Boolean,
  onVibrationChanged: (Boolean) -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = GoogleCalcFunctionBg,
    title = {
      Text(
        text = "Settings",
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        color = Color.White,
        fontFamily = FontFamily.SansSerif
      )
    },
    text = {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          Icon(
            imageVector = Icons.Outlined.TouchApp,
            contentDescription = null,
            tint = GoogleCalcEqualsBg,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "Vibration",
              fontSize = 16.sp,
              fontWeight = FontWeight.Medium,
              color = Color.White,
              fontFamily = FontFamily.SansSerif
            )
            Text(
              text = "Haptic feedback on tap & swipe",
              fontSize = 13.sp,
              color = GoogleCalcFormulaText,
              fontFamily = FontFamily.SansSerif
            )
          }
        }
        Switch(
          checked = isVibrationEnabled,
          onCheckedChange = onVibrationChanged,
          modifier = Modifier.testTag("switch_vibration")
        )
      }
    },
    confirmButton = {
      TextButton(
        onClick = onDismiss,
        modifier = Modifier.testTag("btn_close_settings")
      ) {
        Text(
          text = "Done",
          color = GoogleCalcEqualsBg,
          fontWeight = FontWeight.SemiBold
        )
      }
    }
  )
}

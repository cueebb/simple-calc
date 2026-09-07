package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GoogleCalcColorScheme = darkColorScheme(
  primary = GoogleCalcEqualsBg,
  onPrimary = GoogleCalcEqualsText,
  surface = GoogleCalcBackground,
  onSurface = GoogleCalcNumberText,
  background = GoogleCalcBackground,
  onBackground = GoogleCalcNumberText,
  surfaceVariant = GoogleCalcNumberBg,
  onSurfaceVariant = GoogleCalcFormulaText,
  secondaryContainer = GoogleCalcOperatorBg,
  onSecondaryContainer = GoogleCalcOperatorText,
  tertiaryContainer = GoogleCalcFunctionBg,
  onTertiaryContainer = GoogleCalcFunctionText
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = GoogleCalcColorScheme,
    typography = Typography,
    content = content
  )
}

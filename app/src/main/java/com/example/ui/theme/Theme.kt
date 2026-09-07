package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = PrimaryOrange,
  onPrimary = OnPrimaryOrange,
  surface = CalcBackgroundDark,
  onSurface = OnSurfaceDark,
  background = CalcBackgroundDark,
  onBackground = OnSurfaceDark,
  surfaceVariant = CalcNumberButtonDark,
  onSurfaceVariant = CalcNumberTextDark,
  secondaryContainer = CalcFunctionButtonDark,
  onSecondaryContainer = CalcFunctionTextDark
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = DarkColorScheme,
    typography = Typography,
    content = content
  )
}

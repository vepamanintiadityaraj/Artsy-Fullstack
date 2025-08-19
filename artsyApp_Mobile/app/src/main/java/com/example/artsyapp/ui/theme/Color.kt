package com.example.artsyapp.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Light theme colors
val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Dark theme colors
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

// Optional semantic extensions
val BackgroundLight = Color(0xFFFFFBFE)
val SurfaceLight = Color(0xFFFFFFFF)
val OnPrimaryLight = Color.White
val OnSecondaryLight = Color.White
val OnSurfaceLight = Color(0xFF1C1B1F)

val BackgroundDark = Color(0xFF121212)
val SurfaceDark = Color(0xFF1E1E1E)
val OnPrimaryDark = Color.Black
val OnSecondaryDark = Color.Black
val OnSurfaceDark = Color(0xFFEDEDED)



val LightTopBar = Color(0xFFE0E4FC)
val LightCard = Color(0xFFE0E4FC).copy(alpha = 0.85f)
val LightText = Color.Black
val LightdialogButtonColor = Color(0xFF435C8C)
val LightdialogTextColor = Color.White


val DarkTopBar = Color(0xFF30447c)
val DarkCard = Color(0xFF30447c).copy(alpha = 0.85f)
val DarkText = Color.White
val DarkdialogButtonColor = Color(0xFFB1C3FA)
val DarkdialogTextColor = Color(0xFF1F1E21)


data class ArtsyColors(
    val topBarColor: Color,
    val cardColor: Color,
    val textColor: Color,
    val dialogButtonColor: Color,
    val dialogTextColor: Color
)

val LocalArtsyColors = staticCompositionLocalOf {
    ArtsyColors(
        topBarColor = LightTopBar,
        cardColor = LightCard,
        textColor = LightText,
        dialogButtonColor = LightdialogButtonColor,
        dialogTextColor = LightdialogTextColor
    )
}
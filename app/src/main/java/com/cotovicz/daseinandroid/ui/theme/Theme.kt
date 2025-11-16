package com.cotovicz.daseinandroid.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = VerdeSalvia,
    onPrimary = BrancoPuro,
    secondary = LavandaPoetica,
    onSecondary = CarvaoSuave,
    background = PapelAntigo,
    onBackground = CarvaoSuave,
    surface = BrancoPuro,
    onSurface = CarvaoSuave,
    onSurfaceVariant = Nevoa
)

private val DarkColorScheme = darkColorScheme(
    primary = VerdeSalvia,
    onPrimary = BrancoPuro,
    secondary = LavandaPoetica,
    onSecondary = CarvaoSuave,
    background = NoiteIntrospectiva,
    onBackground = TextoClaro,
    surface = CardEscuro,
    onSurface = TextoClaro,
    onSurfaceVariant = TextoClaroSecundario
)

@Composable
fun DaseInAndroidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
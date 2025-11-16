package com.cotovicz.daseinandroid.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.compose.material3.MaterialTheme
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun AppBottomNavBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        NavigationBarItem(
            selected = currentRoute == "feed",
            onClick = { navController.navigate("feed") },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = "Feed"
                )
            }
        )

        NavigationBarItem(
            selected = currentRoute == "create",
            onClick = { navController.navigate("create") },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Criar"
                )
            }
        )

        NavigationBarItem(
            selected = currentRoute == "saved",
            onClick = { navController.navigate("saved") },
            icon = {
                Icon(
                    imageVector = if (currentRoute == "saved") Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = "Salvos"
                )
            }
        )
    }
}
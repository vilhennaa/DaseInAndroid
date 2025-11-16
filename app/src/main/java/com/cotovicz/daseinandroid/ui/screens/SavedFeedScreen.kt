package com.cotovicz.daseinandroid.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.cotovicz.daseinandroid.ui.components.AppBottomNavBar
import com.cotovicz.daseinandroid.ui.components.CreationCard
import com.cotovicz.daseinandroid.ui.viewmodel.AuthViewModel
import com.cotovicz.daseinandroid.ui.viewmodel.CreationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedFeedScreen(
    navController: NavController,
    creationViewModel: CreationViewModel,
    authViewModel: AuthViewModel
) {
    val userProfile by authViewModel.userProfile.collectAsStateWithLifecycle()
    val savedCreations by creationViewModel.savedCreations.collectAsStateWithLifecycle()

    LaunchedEffect(userProfile) {
        userProfile?.savedPostIds?.let {
            creationViewModel.loadSavedCreations(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Itens Salvos") })
        },
        bottomBar = {
            AppBottomNavBar(navController = navController)
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 8.dp)
        ) {
            items(savedCreations) { creation ->
                val navigateToDetail = {
                    navController.navigate("detail/${creation.id}")
                }

                val isSaved = userProfile?.savedPostIds?.contains(creation.id) ?: false

                CreationCard(
                    creation = creation,
                    onClick = navigateToDetail,
                    onCommentClick = navigateToDetail,
                    isSaved = isSaved,
                    onSaveClick = {
                        authViewModel.toggleSavePost(creation.id)
                    }
                )
            }
        }
    }
}
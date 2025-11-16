package com.cotovicz.daseinandroid.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.cotovicz.daseinandroid.ui.components.AppBottomNavBar
import com.cotovicz.daseinandroid.ui.components.AppTopBar
import com.cotovicz.daseinandroid.ui.components.CreationCard
import com.cotovicz.daseinandroid.ui.viewmodel.AuthViewModel
import com.cotovicz.daseinandroid.ui.viewmodel.CreationViewModel
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    navController: NavController,
    viewModel: CreationViewModel,
    authViewModel: AuthViewModel
) {
    val userProfile by authViewModel.userProfile.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filteredCreations by viewModel.filteredCreations.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permissão concedida
        } else {
            // Permissão negada
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            Log.d("FCM_TOKEN", "Token de teste (pego manualmente): $token")
        } catch (e: Exception) {
            Log.e("FCM_TOKEN", "Falha ao pegar o token", e)
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(navController = navController)
        },
        bottomBar = {
            AppBottomNavBar(navController = navController)
        }

    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Pesquisar...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Pesquisar") },
                    singleLine = true
                )
                IconButton(onClick = { navController.navigate("filter") }) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filtrar por Tags")
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
            ) {
                items(filteredCreations) { creation ->
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
}
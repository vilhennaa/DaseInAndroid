package com.cotovicz.daseinandroid.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.cotovicz.daseinandroid.data.remote.models.Creation
import com.cotovicz.daseinandroid.ui.components.CreationCard
import com.cotovicz.daseinandroid.ui.viewmodel.AuthViewModel
import com.cotovicz.daseinandroid.ui.viewmodel.CreationViewModel
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    creationViewModel: CreationViewModel
) {
    val isAuthenticated by authViewModel.isAuthenticated.collectAsStateWithLifecycle()
    val userProfile by authViewModel.userProfile.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()

    val userPosts by remember(currentUser) {
        if (currentUser != null) {
            creationViewModel.getCreationsForUser(currentUser!!.uid)
        } else {
            flowOf(emptyList<Creation>())
        }
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    val savedPosts by creationViewModel.savedCreations.collectAsStateWithLifecycle()
    LaunchedEffect(userProfile) {
        userProfile?.savedPostIds?.let {
            creationViewModel.loadSavedCreations(it)
        }
    }

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Minhas Publicações", "Salvos")

    LaunchedEffect(isAuthenticated) {
        if (!isAuthenticated) {
            navController.navigate("login") {
                popUpTo(0)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.height(32.dp))

                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Foto de Perfil",
                        modifier = Modifier.size(120.dp),
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = userProfile?.displayName ?: "Carregando...",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = userProfile?.bio ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { navController.navigate("editProfile") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Editar Perfil")
                    }

                    Button(
                        onClick = { authViewModel.signOut() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text("Sair")
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                TabRow(selectedTabIndex = selectedTabIndex) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            when (selectedTabIndex) {
                0 -> {
                    items(userPosts) { creation ->
                        val isSaved = userProfile?.savedPostIds?.contains(creation.id) ?: false
                        CreationCard(
                            creation = creation,
                            onClick = { navController.navigate("detail/${creation.id}") },
                            onCommentClick = { navController.navigate("detail/${creation.id}") },
                            isSaved = isSaved,
                            onSaveClick = { authViewModel.toggleSavePost(creation.id) }
                        )
                    }
                }
                1 -> {
                    items(savedPosts) { creation ->
                        CreationCard(
                            creation = creation,
                            onClick = { navController.navigate("detail/${creation.id}") },
                            onCommentClick = { navController.navigate("detail/${creation.id}") },
                            isSaved = true,
                            onSaveClick = { authViewModel.toggleSavePost(creation.id) }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
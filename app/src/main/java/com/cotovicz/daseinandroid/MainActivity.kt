package com.cotovicz.daseinandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import com.cotovicz.daseinandroid.data.remote.auth.FirebaseAuthManager
import com.cotovicz.daseinandroid.data.repository.CreationRepository
import com.cotovicz.daseinandroid.data.repository.UserProfileRepository
import com.cotovicz.daseinandroid.navigation.AppNavHost
import com.cotovicz.daseinandroid.ui.theme.DaseInAndroidTheme
import com.cotovicz.daseinandroid.ui.viewmodel.AuthViewModelFactory
import com.cotovicz.daseinandroid.ui.viewmodel.CreationViewModelFactory
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class MainActivity : ComponentActivity() {

    private val firestore by lazy { Firebase.firestore }
    private val firebaseAuthManager by lazy { FirebaseAuthManager() }

    private val creationRepository by lazy { CreationRepository(firestore, firebaseAuthManager) }
    private val userProfileRepository by lazy { UserProfileRepository(firestore) }

    private val creationViewModelFactory by lazy { CreationViewModelFactory(creationRepository) }
    private val authViewModelFactory by lazy {
        AuthViewModelFactory(firebaseAuthManager, userProfileRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DaseInAndroidTheme {
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    val startDestination = remember {
                        if (firebaseAuthManager.isAuthenticated()) {
                            "feed"
                        } else {
                            "login"
                        }
                    }

                    AppNavHost(
                        authViewModelFactory = authViewModelFactory,
                        creationViewModelFactory = creationViewModelFactory,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}
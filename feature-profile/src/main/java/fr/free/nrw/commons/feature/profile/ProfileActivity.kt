package fr.free.nrw.commons.feature.profile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import fr.free.nrw.commons.feature.profile.presentation.ProfileScreen
import fr.free.nrw.commons.feature.profile.ui.theme.CommonsAppTheme

@AndroidEntryPoint
class ProfileActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            CommonsAppTheme {
                ProfileScreen()
            }
        }
    }
}
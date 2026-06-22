package com.doximity.realtimewatchlist_krishna_doximity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.doximity.realtimewatchlist_krishna_doximity.ui.search.SearchScreen
import com.doximity.realtimewatchlist_krishna_doximity.ui.search.SearchViewModel
import com.doximity.realtimewatchlist_krishna_doximity.ui.theme.RealtimeWatchListKrishnaDoximityTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RealtimeWatchListKrishnaDoximityTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val searchViewModel: SearchViewModel = hiltViewModel()
                    SearchScreen(
                        viewModel = searchViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

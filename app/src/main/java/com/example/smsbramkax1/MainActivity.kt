package com.example.smsbramkax1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.example.smsbramkax1.ui.MainContent
import com.example.smsbramkax1.ui.theme.Smsbramkax1Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Smsbramkax1Theme {
                val selectedTab = remember { mutableStateOf("Dashboard") }

                MainContent(
                    selectedTab = selectedTab.value,
                    onTabSelected = { selectedTab.value = it }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Smsbramkax1Theme {
        MainContent(selectedTab = "Dashboard", onTabSelected = {})
    }
}
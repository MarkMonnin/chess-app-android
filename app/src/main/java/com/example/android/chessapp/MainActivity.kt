package com.example.android.chessapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.android.chessapp.ui.theme.ChessAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChessAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChessApp()
                }
            }
        }
    }
}

@Composable
fun ChessApp() {
    var showBoard by remember { mutableStateOf(false) }
    var gameMode by remember { mutableStateOf(GameMode.HUMAN_VS_AI) }
    var gameResetKey by remember { mutableStateOf(0) }

    if (showBoard) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                Button(
                    onClick = { showBoard = false }
                ) {
                    Text("← Back")
                }
                Spacer(modifier = Modifier.width(8.dp)) // Add some space between buttons
                Button(
                    onClick = { gameResetKey++ }
                ) {
                    Text("New Game")
                }
            }

            // Chess board with selected game mode
            ChessBoard(
                modifier = Modifier.weight(1f),
                gameMode = gameMode,
                resetKey = gameResetKey
            )
        }
    } else {
        // Home screen with game mode selection
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Chess App",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                // Game mode buttons
                Button(
                    onClick = {
                        gameMode = GameMode.HUMAN_VS_HUMAN
                        showBoard = true
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Play vs Human")
                }

                Button(
                    onClick = {
                        gameMode = GameMode.HUMAN_VS_AI
                        showBoard = true
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Play vs AI")
                }
            }
        }
    }
}
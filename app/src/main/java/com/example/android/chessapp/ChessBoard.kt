package com.example.android.chessapp

import com.example.android.chessapp.ChessLogic.getValidMoves
import com.example.android.chessapp.ui.theme.Dimensions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.platform.LocalConfiguration
import kotlinx.coroutines.*
import kotlinx.coroutines.delay
import android.util.Log

enum class GameMode {
    HUMAN_VS_HUMAN,
    HUMAN_VS_AI
}

@Composable
fun ChessBoard(
    modifier: Modifier = Modifier,
    gameMode: GameMode = GameMode.HUMAN_VS_AI,
    resetKey: Any? = null
) {
    // --- Responsive Sizing ---
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val topBarHeight = 56.dp
    val bottomButtonsHeight = 48.dp
    val textLabelsHeight = 60.dp
    val verticalPadding = 28.dp
    val availableHeightForBoard = screenHeight - topBarHeight - bottomButtonsHeight - textLabelsHeight - verticalPadding
    val squareSize = (availableHeightForBoard / 8).coerceAtMost(screenWidth / 8)

    // --- Game State ---
    val initialBoard = remember(resetKey) {
        Array(8) { row ->
            Array(8) { col ->
                when (row) {
                    0 -> when (col) {
                        0, 7 -> ChessPiece(PieceType.ROOK, PieceColor.BLACK)
                        1, 6 -> ChessPiece(PieceType.KNIGHT, PieceColor.BLACK)
                        2, 5 -> ChessPiece(PieceType.BISHOP, PieceColor.BLACK)
                        3 -> ChessPiece(PieceType.QUEEN, PieceColor.BLACK)
                        4 -> ChessPiece(PieceType.KING, PieceColor.BLACK)
                        else -> null
                    }
                    1 -> ChessPiece(PieceType.PAWN, PieceColor.BLACK)
                    6 -> ChessPiece(PieceType.PAWN, PieceColor.WHITE)
                    7 -> when (col) {
                        0, 7 -> ChessPiece(PieceType.ROOK, PieceColor.WHITE)
                        1, 6 -> ChessPiece(PieceType.KNIGHT, PieceColor.WHITE)
                        2, 5 -> ChessPiece(PieceType.BISHOP, PieceColor.WHITE)
                        3 -> ChessPiece(PieceType.QUEEN, PieceColor.WHITE)
                        4 -> ChessPiece(PieceType.KING, PieceColor.WHITE)
                        else -> null
                    }
                    else -> null
                }
            }
        }
    }
    // Game state with built-in undo/redo
    val initialState by remember(resetKey) { mutableStateOf(ChessGameState(board = initialBoard)) }
    var gameState by remember(resetKey) { mutableStateOf(initialState) }
    var selectedPosition by remember(resetKey) { mutableStateOf<ChessPosition?>(null) }
    var validMoves by remember(resetKey) { mutableStateOf(emptyList<ChessPosition>()) }
    var lastMove by remember(resetKey) { mutableStateOf<Pair<ChessPosition, ChessPosition>?>(null) }

    // --- User Interaction ---
    val onSquareSelected: (ChessPosition) -> Unit = onSquare@{ pos ->
        // Disable new moves while browsing history
        if (gameState.moveHistory.isNotEmpty() && gameState.futureMoves.isNotEmpty()) return@onSquare
        if (selectedPosition == null) {
            // Select a piece
            val piece = gameState.board[pos.row][pos.col]
            if (piece != null && piece.color == gameState.currentPlayer) {
                selectedPosition = pos
                validMoves = getValidMoves(pos, gameState.board, gameState)
            }
        } else {
            // Try to move
            if (pos in validMoves) {
                val move = ChessMove(selectedPosition!!, pos, gameState.board[selectedPosition!!.row][selectedPosition!!.col]!!)
                val newState = gameState.applyMove(move)
                gameState = newState
                lastMove = Pair(selectedPosition!!, pos)
            }
            selectedPosition = null
            validMoves = emptyList()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        
        Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        IconButton(onClick = {
            gameState.undoMove()?.let { prevState ->
                gameState = prevState
                val last = prevState.moveHistory.lastOrNull()?.move
                if (last != null) lastMove = last.from to last.to else lastMove = null
            }
            selectedPosition = null
            validMoves = emptyList()
        }) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Undo")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (gameState.currentPlayer == PieceColor.WHITE) "White's turn" else "Black's turn",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.width(8.dp))

        IconButton(onClick = {
            gameState.redoMove()?.let { nextState ->
                gameState = nextState
                gameState.moveHistory.lastOrNull()?.move?.let { lastMove = it.from to it.to }
            }
            selectedPosition = null
            validMoves = emptyList()
        }) {
            Icon(Icons.Filled.ArrowForward, contentDescription = "Redo")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        ChessBoardCanvas(
            board = gameState.board,
            selected = selectedPosition,
            validMoves = validMoves,
            lastMove = lastMove,
            onSquareSelected = onSquareSelected,
            modifier = Modifier.fillMaxSize()
        )
    }
    }
    // Auto-trigger AI move after human
    LaunchedEffect(lastMove) {
        if (gameMode == GameMode.HUMAN_VS_AI && lastMove != null && gameState.futureMoves.isEmpty() && gameState.currentPlayer != PieceColor.WHITE) {
            val aiMove = OptimizedChessAI.findBestMove(gameState)
            val newState = gameState.applyMove(aiMove)
            gameState = newState
            lastMove = aiMove.from to aiMove.to
        }
    }
}
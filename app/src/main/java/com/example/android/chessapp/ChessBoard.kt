package com.example.android.chessapp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

enum class GameMode {
    HUMAN_VS_HUMAN,
    HUMAN_VS_AI
}

@Composable
fun ChessBoard(
    modifier: Modifier = Modifier,
    gameMode: GameMode = GameMode.HUMAN_VS_AI
) {
    // Game state
    var gameState by remember { mutableStateOf(ChessGameState(initializeBoard())) }
    var selectedPosition by remember { mutableStateOf<ChessPosition?>(null) }
    var validMoves by remember { mutableStateOf<List<ChessPosition>>(emptyList()) }
    var isAITurn by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val chessAI = remember { OptimizedChessAI() }

    // Update game status text based on current state
    val gameStatus = when {
        gameState.isCheckmate -> "Checkmate! ${gameState.currentPlayer.opposite().toString().replaceFirstChar { it.uppercase() }} wins!"
        gameState.isStalemate -> "Stalemate! Game is a draw."
        gameState.isCheck -> "${gameState.currentPlayer.toString().replaceFirstChar { it.uppercase() }} is in check!"
        else -> "${gameState.currentPlayer.toString().replaceFirstChar { it.uppercase() }}'s turn"
    }

    // AI move handler
    val handleAiMove = { move: ChessMove ->
        // Apply the move to the current game state
        val newState = gameState.applyMove(move)
        gameState = newState
        selectedPosition = null
        validMoves = emptyList()
    }

    // Trigger AI move when it's AI's turn
    LaunchedEffect(gameState.currentPlayer, gameMode) {
        if (gameMode == GameMode.HUMAN_VS_AI &&
            gameState.currentPlayer == PieceColor.BLACK &&
            !isAITurn) {
            isAITurn = true
            coroutineScope.launch(Dispatchers.Default) {
                chessAI.makeMove(
                    board = gameState.board,
                    color = gameState.currentPlayer,
                    gameState = gameState
                ) { aiMove ->
                    handleAiMove(aiMove)
                    isAITurn = false
                }
            }
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Game mode indicator
        Text(
            text = when (gameMode) {
                GameMode.HUMAN_VS_HUMAN -> "Human vs Human"
                GameMode.HUMAN_VS_AI -> "Human vs AI"
            },
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(8.dp)
        )

        // Game status and controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                // Undo button
                IconButton(
                    onClick = {
                        gameState.undoMove()?.let { newState ->
                            gameState = newState
                            selectedPosition = null
                            validMoves = emptyList()
                        }
                    },
                    enabled = gameState.canUndo() && !isAITurn
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Undo last move"
                    )
                }
                
                // Game status text
                Text(
                    text = gameStatus,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                // Redo button
                IconButton(
                    onClick = {
                        gameState.redoMove()?.let { newState ->
                            gameState = newState
                            selectedPosition = null
                            validMoves = emptyList()
                        }
                    },
                    enabled = gameState.canRedo() && !isAITurn
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Redo last move"
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        gameState = ChessGameState(initializeBoard())
                        selectedPosition = null
                        validMoves = emptyList()
                        isAITurn = false
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("New Game")
                }

                // Add a button to show game state for debugging
                if (gameState.isCheck) {
                    Text(
                        text = "CHECK!",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }

        // Chess board
        LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            modifier = Modifier
                .aspectRatio(1f)
                .padding(16.dp)
        ) {
            itemsIndexed(gameState.board.flatten()) { index, piece ->
                val row = index / 8
                val col = index % 8
                val position = ChessPosition(row, col)
                val isSelected = selectedPosition == position
                val isValidMove = validMoves.contains(position)

                ChessSquare(
                    piece = piece,
                    position = position,
                    isLight = (row + col) % 2 == 0,
                    isSelected = isSelected,
                    isValidMove = isValidMove,
                    lastMove = null,
                    onClick = {
                        if (isAITurn || gameState.isCheckmate || gameState.isStalemate) return@ChessSquare

                        val clickedPiece = gameState.board[position.row][position.col]

                        // If no piece is selected, select the piece if it's the current player's piece
                        if (selectedPosition == null) {
                            if (clickedPiece?.color == gameState.currentPlayer) {
                                selectedPosition = position
                                validMoves = gameState.getValidMovesOptimized(position, gameState.board)
                            }
                        }
                        // If a piece is already selected
                        else {
                            // If clicking on the same piece, deselect it
                            if (position == selectedPosition) {
                                selectedPosition = null
                                validMoves = emptyList()
                            }
                            // If clicking on a valid move, make the move
                            else if (validMoves.any { it.row == position.row && it.col == position.col }) {
                                val from = selectedPosition!!
                                val pieceToMove = gameState.board[from.row][from.col]!!
                                val capturedPiece = gameState.board[position.row][position.col]
                                val move = ChessMove(from, position, pieceToMove, capturedPiece)

                                // Apply the move to the game state
                                val newState = gameState.applyMove(move)

                                // Update the game state
                                gameState = newState
                                selectedPosition = null
                                validMoves = emptyList()

                                // If it's AI's turn and we're in AI mode, make AI move
                                if (gameMode == GameMode.HUMAN_VS_AI && newState.currentPlayer == PieceColor.BLACK) {
                                    isAITurn = true
                                    coroutineScope.launch(Dispatchers.Default) {
                                        chessAI.makeMove(
                                            board = newState.board,
                                            color = newState.currentPlayer,
                                            gameState = newState
                                        ) { aiMove ->
                                            handleAiMove(aiMove)
                                            isAITurn = false
                                        }
                                    }
                                }
                            }
                            // If clicking on another piece of the same color, select that piece
                            else if (piece?.color == gameState.currentPlayer) {
                                selectedPosition = position
                                validMoves = gameState.getValidMovesOptimized(position, gameState.board)
                            }
                            // If clicking on an invalid square, deselect the piece
                            else {
                                selectedPosition = null
                                validMoves = emptyList()
                            }
                        }
                    }
                )
            }
        }

        // Game info section
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Selected piece info
            selectedPosition?.let { pos ->
                val piece = gameState.board[pos.row][pos.col]
                Text(
                    text = "Selected: ${ChessLogic.positionToAlgebraic(pos)}" +
                            if (piece != null) " - ${piece.color} ${piece.type}" else " - Empty",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(4.dp)
                )
            }

            // Last move info
            gameState.lastMove?.let { lastMove ->
                Text(
                    text = "Last move: ${lastMove.piece.color} ${lastMove.piece.type} " +
                            "${ChessLogic.positionToAlgebraic(lastMove.from)} → ${ChessLogic.positionToAlgebraic(lastMove.to)}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(4.dp)
                )
            }

            // Move count
            Text(
                text = "Move ${(gameState.fullMoveNumber)}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(4.dp)
            )
            
            // Castling availability
            val castlingRights = buildString {
                if (gameState.whiteCanCastleKingside) append("O-O ")
                if (gameState.whiteCanCastleQueenside) append("O-O-O ")
                if (gameState.blackCanCastleKingside) append("o-o ")
                if (gameState.blackCanCastleQueenside) append("o-o-o")
                if (isEmpty()) append("No castling available")
            }
            
            Text(
                text = "Castling: $castlingRights",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}

@Composable
fun ChessSquare(
    piece: ChessPiece?,
    position: ChessPosition,
    isLight: Boolean,
    isSelected: Boolean,
    isValidMove: Boolean,
    lastMove: ChessMove?,
    onClick: () -> Unit
) {
    val isLastMove = lastMove?.let {
        it.from == position || it.to == position
    } ?: false

    val backgroundColor = when {
        isSelected -> Color(0xFF4CAF50) // Green for selected
        isLastMove -> Color(0xFFFFEB3B) // Yellow for last AI move
        isValidMove -> Color(0xFF81C784) // Light green for valid moves
        isLight -> Color(0xFFF0D9B5) // Light squares
        else -> Color(0xFFB58863) // Dark squares
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .background(backgroundColor)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = Color.Black
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        piece?.let {
            Text(
                text = ChessLogic.getPieceSymbol(it),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = if (it.color == PieceColor.WHITE) Color.White else Color.Black
            )
        }

        // Show dot for valid move on empty square
        if (isValidMove && piece == null) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(Color.Green.copy(alpha = 0.7f), shape = androidx.compose.foundation.shape.CircleShape)
            )
        }
    }
}

private fun initializeBoard(): Array<Array<ChessPiece?>> {
    val board = Array(8) { Array<ChessPiece?>(8) { null } }

    // Place black pieces (top of board)
    board[0][0] = ChessPiece(PieceType.ROOK, PieceColor.BLACK)
    board[0][1] = ChessPiece(PieceType.KNIGHT, PieceColor.BLACK)
    board[0][2] = ChessPiece(PieceType.BISHOP, PieceColor.BLACK)
    board[0][3] = ChessPiece(PieceType.QUEEN, PieceColor.BLACK)
    board[0][4] = ChessPiece(PieceType.KING, PieceColor.BLACK)
    board[0][5] = ChessPiece(PieceType.BISHOP, PieceColor.BLACK)
    board[0][6] = ChessPiece(PieceType.KNIGHT, PieceColor.BLACK)
    board[0][7] = ChessPiece(PieceType.ROOK, PieceColor.BLACK)

    // Place black pawns
    for (col in 0..7) {
        board[1][col] = ChessPiece(PieceType.PAWN, PieceColor.BLACK)
    }

    // Place white pawns
    for (col in 0..7) {
        board[6][col] = ChessPiece(PieceType.PAWN, PieceColor.WHITE)
    }

    // Place white pieces (bottom of board)
    board[7][0] = ChessPiece(PieceType.ROOK, PieceColor.WHITE)
    board[7][1] = ChessPiece(PieceType.KNIGHT, PieceColor.WHITE)
    board[7][2] = ChessPiece(PieceType.BISHOP, PieceColor.WHITE)
    board[7][3] = ChessPiece(PieceType.QUEEN, PieceColor.WHITE)
    board[7][4] = ChessPiece(PieceType.KING, PieceColor.WHITE)
    board[7][5] = ChessPiece(PieceType.BISHOP, PieceColor.WHITE)
    board[7][6] = ChessPiece(PieceType.KNIGHT, PieceColor.WHITE)
    board[7][7] = ChessPiece(PieceType.ROOK, PieceColor.WHITE)

    return board
}
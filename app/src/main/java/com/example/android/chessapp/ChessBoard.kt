package com.example.android.chessapp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

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
    var board by remember { mutableStateOf(initializeBoard()) }
    var currentPlayer by remember { mutableStateOf(PieceColor.WHITE) }
    var selectedPosition by remember { mutableStateOf<ChessPosition?>(null) }
    var validMoves by remember { mutableStateOf<List<ChessPosition>>(emptyList()) }
    var moveHistory by remember { mutableStateOf<List<ChessMove>>(emptyList()) }
    var gameStatus by remember { mutableStateOf("White to move") }
    var isAiThinking by remember { mutableStateOf(false) }
    var lastMove by remember { mutableStateOf<ChessMove?>(null) }

    // AI instance
    val chessAI = remember { ChessAI() }
    val coroutineScope = rememberCoroutineScope()

    // AI move handler
    val handleAiMove = { move: ChessMove ->
        val newBoard = board.map { it.clone() }.toTypedArray()
        newBoard[move.to.row][move.to.col] = move.piece
        newBoard[move.from.row][move.from.col] = null

        board = newBoard
        moveHistory = moveHistory + move
        currentPlayer = if (currentPlayer == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
        selectedPosition = null
        validMoves = emptyList()
        isAiThinking = false
        gameStatus = "${if (currentPlayer == PieceColor.WHITE) "White" else "Black"} to move"
        lastMove = move
    }

    // Trigger AI move when it's AI's turn
    LaunchedEffect(currentPlayer, gameMode) {
        if (gameMode == GameMode.HUMAN_VS_AI &&
            currentPlayer == PieceColor.BLACK &&
            !isAiThinking) {
            isAiThinking = true
            gameStatus = "AI is thinking..."

            coroutineScope.launch {
                chessAI.makeMove(board, PieceColor.BLACK, handleAiMove)
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

        // Game status
        Text(
            text = gameStatus,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )

        // Chess board
        LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            modifier = Modifier
                .aspectRatio(1f)
                .padding(16.dp)
        ) {
            itemsIndexed(board.flatten()) { index, piece ->
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
                    lastMove = lastMove,
                    onClick = {
                        // Only allow human moves when it's not AI's turn or AI is not thinking
                        if (gameMode == GameMode.HUMAN_VS_HUMAN ||
                            (gameMode == GameMode.HUMAN_VS_AI && currentPlayer == PieceColor.WHITE && !isAiThinking)) {
                            handleSquareClick(
                                position = position,
                                board = board,
                                selectedPosition = selectedPosition,
                                currentPlayer = currentPlayer,
                                validMoves = validMoves,
                                onMove = { newBoard, move ->
                                    board = newBoard
                                    moveHistory = moveHistory + move
                                    currentPlayer = if (currentPlayer == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
                                    selectedPosition = null
                                    validMoves = emptyList()
                                    gameStatus = "${if (currentPlayer == PieceColor.WHITE) "White" else "Black"} to move"
                                    lastMove = move
                                },
                                onSelection = { pos, moves ->
                                    selectedPosition = pos
                                    validMoves = moves
                                }
                            )
                        }
                    }
                )
            }
        }

        // Selected position and move info
        selectedPosition?.let { pos ->
            val piece = board[pos.row][pos.col]
            Text(
                text = "Selected: ${positionToAlgebraic(pos)}" +
                        if (piece != null) " - ${piece.color} ${piece.type}" else " - Empty",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(8.dp)
            )
        }

        // Last move info
        moveHistory.lastOrNull()?.let { lastMove ->
            Text(
                text = "Last move: ${lastMove.piece.color} ${lastMove.piece.type} " +
                        "${positionToAlgebraic(lastMove.from)} → ${positionToAlgebraic(lastMove.to)}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(8.dp)
            )
        }

        // Move count
        Text(
            text = "Move ${moveHistory.size + 1}",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(4.dp)
        )
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
                text = getPieceSymbol(it),
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

private fun handleSquareClick(
    position: ChessPosition,
    board: Array<Array<ChessPiece?>>,
    selectedPosition: ChessPosition?,
    currentPlayer: PieceColor,
    validMoves: List<ChessPosition>,
    onMove: (Array<Array<ChessPiece?>>, ChessMove) -> Unit,
    onSelection: (ChessPosition?, List<ChessPosition>) -> Unit
) {
    if (selectedPosition != null && validMoves.contains(position)) {
        // Execute move
        val piece = board[selectedPosition.row][selectedPosition.col]!!
        val capturedPiece = board[position.row][position.col]

        val newBoard = board.map { it.clone() }.toTypedArray()
        newBoard[position.row][position.col] = piece
        newBoard[selectedPosition.row][selectedPosition.col] = null

        val move = ChessMove(selectedPosition, position, piece, capturedPiece)
        onMove(newBoard, move)
    } else {
        // Select piece
        val piece = board[position.row][position.col]
        if (piece != null && piece.color == currentPlayer) {
            val moves = getValidMoves(position, board)
            onSelection(position, moves)
        } else {
            onSelection(null, emptyList())
        }
    }
}

private fun getValidMoves(position: ChessPosition, board: Array<Array<ChessPiece?>>): List<ChessPosition> {
    return ChessLogic.getValidMoves(position, board)
}

private fun positionToAlgebraic(position: ChessPosition): String {
    return "${('a' + position.col)}${8 - position.row}"
}

private fun getPieceSymbol(piece: ChessPiece): String {
    return when (piece.type) {
        PieceType.KING -> if (piece.color == PieceColor.WHITE) "♔" else "♚"
        PieceType.QUEEN -> if (piece.color == PieceColor.WHITE) "♕" else "♛"
        PieceType.ROOK -> if (piece.color == PieceColor.WHITE) "♖" else "♜"
        PieceType.BISHOP -> if (piece.color == PieceColor.WHITE) "♗" else "♝"
        PieceType.KNIGHT -> if (piece.color == PieceColor.WHITE) "♘" else "♞"
        PieceType.PAWN -> if (piece.color == PieceColor.WHITE) "♙" else "♟"
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
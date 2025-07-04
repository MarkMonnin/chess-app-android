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

data class ChessPosition(val row: Int, val col: Int)

enum class PieceType {
    KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN
}

enum class PieceColor {
    WHITE, BLACK
}

data class ChessPiece(
    val type: PieceType,
    val color: PieceColor
)

@Composable
fun ChessBoard(
    modifier: Modifier = Modifier
) {
    // Initialize board with starting positions
    var board by remember { mutableStateOf(initializeBoard()) }
    var selectedPosition by remember { mutableStateOf<ChessPosition?>(null) }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
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

                ChessSquare(
                    piece = piece,
                    isLight = (row + col) % 2 == 0,
                    isSelected = isSelected,
                    onClick = {
                        selectedPosition = if (isSelected) null else position
                    }
                )
            }
        }

        // Show selected position info
        selectedPosition?.let { pos ->
            val piece = board[pos.row][pos.col]
            Text(
                text = "Selected: ${('a' + pos.col)}${8 - pos.row}" +
                        if (piece != null) " - ${piece.color} ${piece.type}" else " - Empty",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
fun ChessSquare(
    piece: ChessPiece?,
    isLight: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> Color(0xFF4CAF50) // Green for selected
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
    }
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
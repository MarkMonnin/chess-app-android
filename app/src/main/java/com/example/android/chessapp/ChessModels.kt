package com.example.android.chessapp

data class ChessPosition(val row: Int, val col: Int)

data class ChessPiece(val type: PieceType, val color: PieceColor)

data class ChessMove(
    val from: ChessPosition,
    val to: ChessPosition,
    val piece: ChessPiece,
    val capturedPiece: ChessPiece? = null
)

enum class PieceType {
    PAWN, ROOK, KNIGHT, BISHOP, QUEEN, KING
}

enum class PieceColor {
    WHITE, BLACK
}
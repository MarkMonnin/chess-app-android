package com.example.android.chessapp

data class ChessPosition(val row: Int, val col: Int)

data class ChessPiece(val type: PieceType, val color: PieceColor)

data class ChessMove(
    val from: ChessPosition,
    val to: ChessPosition,
    val piece: ChessPiece,
    val capturedPiece: ChessPiece? = null,
    val promotionPiece: PieceType? = null
)

enum class PieceType {
    PAWN, ROOK, KNIGHT, BISHOP, QUEEN, KING
}

enum class PieceColor {
    WHITE, BLACK
}

// Extension function to check if a move is a promotion
fun ChessMove.isPromotion(): Boolean {
    // Only pawns can promote
    if (piece.type != PieceType.PAWN) return false
    val promotionRank = if (piece.color == PieceColor.WHITE) 0 else 7
    return to.row == promotionRank
}

// Extension function to return a copy of the move with a promotion piece
fun ChessMove.withPromotion(promotionType: PieceType): ChessMove {
    return this.copy(promotionPiece = promotionType)
}

// Extension function to get the Unicode symbol for a chess piece
fun ChessPiece.getPieceSymbol(): String {
    return when (this.type) {
        PieceType.KING -> if (this.color == PieceColor.WHITE) "♔" else "♚"
        PieceType.QUEEN -> if (this.color == PieceColor.WHITE) "♕" else "♛"
        PieceType.ROOK -> if (this.color == PieceColor.WHITE) "♖" else "♜"
        PieceType.BISHOP -> if (this.color == PieceColor.WHITE) "♗" else "♝"
        PieceType.KNIGHT -> if (this.color == PieceColor.WHITE) "♘" else "♞"
        PieceType.PAWN -> if (this.color == PieceColor.WHITE) "♙" else "♟"
    }
}
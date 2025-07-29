package com.example.android.chessapp

import kotlinx.coroutines.delay
import kotlin.random.Random

class OptimizedChessAI {
    suspend fun makeMove(
        board: Array<Array<ChessPiece?>>,
        color: PieceColor,
        gameState: ChessGameState,
        onMove: (ChessMove) -> Unit
    ) {
        delay(Random.nextLong(300, 1000))
        val allPossibleMoves = generateAllLegalMoves(board, color, gameState)
        if (allPossibleMoves.isNotEmpty()) {
            val selectedMove = allPossibleMoves.random()
            onMove(selectedMove)
        }
    }

    private fun isKingInCheck(board: Array<Array<ChessPiece?>>, color: PieceColor): Boolean {
        val kingPosition = findKingPosition(board, color)
        val validator = IncrementalMoveValidator()
        return validator.isKingInCheck(kingPosition, color, board)
    }

    private fun generateAllLegalMoves(
        board: Array<Array<ChessPiece?>>,
        color: PieceColor,
        gameState: ChessGameState,
        prioritizeKingSafety: Boolean = true
    ): List<ChessMove> {
        val legalMoves = mutableListOf<ChessMove>()
        val kingInCheck = prioritizeKingSafety && isKingInCheck(board, color)
        val kingSafetyMoves = mutableListOf<ChessMove>()

        for (row in 0..7) {
            for (col in 0..7) {
                val piece = board[row][col]
                if (piece != null && piece.color == color) {
                    val position = ChessPosition(row, col)
                    val possibleMoves = ChessLogic.getValidMoves(position, board, gameState)

                    possibleMoves.forEach { targetPosition ->
                        val move = ChessMove(position, targetPosition, piece)
                        if (kingInCheck) {
                            val validator = IncrementalMoveValidator()
                            if (validator.isMoveLegal(move, board, gameState)) {
                                kingSafetyMoves.add(move)
                            }
                        } else {
                            legalMoves.add(move)
                        }
                    }
                }
            }
        }

        return if (kingInCheck && kingSafetyMoves.isNotEmpty()) {
            kingSafetyMoves
        } else {
            legalMoves
        }
    }

    private fun findKingPosition(board: Array<Array<ChessPiece?>>, color: PieceColor): ChessPosition {
        for (r in 0..7) {
            for (c in 0..7) {
                val p = board[r][c]
                if (p != null && p.type == PieceType.KING && p.color == color) {
                    return ChessPosition(r, c)
                }
            }
        }
        throw IllegalStateException("King not found for $color")
    }

    companion object {
        fun findBestMove(gameState: ChessGameState): ChessMove {
            val moves = mutableListOf<ChessMove>()
            for (row in 0..7) {
                for (col in 0..7) {
                    val piece = gameState.board[row][col]
                    if (piece != null && piece.color == gameState.currentPlayer) {
                        val from = ChessPosition(row, col)
                        val validMoves = ChessLogic.getValidMoves(from, gameState.board, gameState)
                        validMoves.forEach { to ->
                            moves.add(ChessMove(from, to, piece))
                        }
                    }
                }
            }
            return moves.random()
        }
    }
}

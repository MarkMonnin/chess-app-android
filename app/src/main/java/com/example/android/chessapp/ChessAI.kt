package com.example.android.chessapp

import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Simple AI bot that makes random legal moves
 * This is the foundation for the difficulty system that will be added in Step 5
 */
class ChessAI {

    /**
     * Makes a move for the AI bot
     * Currently uses random move selection
     * @param board Current board state
     * @param color AI's color (BLACK or WHITE)
     * @param onMove Callback when move is selected
     */
    suspend fun makeMove(
        board: Array<Array<ChessPiece?>>,
        color: PieceColor,
        onMove: (ChessMove) -> Unit
    ) {
        // Add thinking delay for realistic feel
        delay(Random.nextLong(500, 1500))

        val allPossibleMoves = getAllPossibleMoves(board, color)

        if (allPossibleMoves.isNotEmpty()) {
            val randomMove = allPossibleMoves.random()
            onMove(randomMove)
        }
    }

    /**
     * Gets all possible moves for the given color
     */
    private fun getAllPossibleMoves(
        board: Array<Array<ChessPiece?>>,
        color: PieceColor
    ): List<ChessMove> {
        val moves = mutableListOf<ChessMove>()

        // Find all pieces of the given color
        for (row in 0..7) {
            for (col in 0..7) {
                val piece = board[row][col]
                if (piece?.color == color) {
                    val position = ChessPosition(row, col)
                    val validMoves = getValidMoves(position, board)

                    // Convert positions to moves
                    validMoves.forEach { targetPos ->
                        val capturedPiece = board[targetPos.row][targetPos.col]
                        moves.add(ChessMove(position, targetPos, piece, capturedPiece))
                    }
                }
            }
        }

        return moves
    }

    /**
     * Gets valid moves for a piece at the given position
     * This uses the same logic as the human player
     */
    private fun getValidMoves(position: ChessPosition, board: Array<Array<ChessPiece?>>): List<ChessPosition> {
        return ChessLogic.getValidMoves(position, board)
    }
}
package com.example.android.chessapp

import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Chess AI that makes legal moves based on the current game state
 * Currently implements random move selection as a foundation for future difficulty levels
 */
class ChessAI {

    /**
     * Makes a move for the AI bot
     * @param board Current board state
     * @param color AI's color (BLACK or WHITE)
     * @param onMove Callback when move is selected with the chosen move
     */
    suspend fun makeMove(
        board: Array<Array<ChessPiece?>>,
        color: PieceColor,
        onMove: (ChessMove) -> Unit
    ) {
        // Add thinking delay for realistic feel (shorter delay for easier difficulty)
        delay(Random.nextLong(300, 1000))

        // Create a temporary game state for move validation
        // Using the board and current player color to create the state
        val tempState = ChessGameState(
            board = board,
            currentPlayer = color
        )
        val allPossibleMoves = mutableListOf<ChessMove>()

        // Find all pieces of the AI's color and collect their valid moves
        for (row in 0..7) {
            for (col in 0..7) {
                val piece = board[row][col]
                if (piece != null && piece.color == color) {
                    val position = ChessPosition(row, col)
                    val moves = ChessLogic.getValidMoves(ChessPosition(row, col), board, tempState)

                    // Create move objects for each valid move
                    moves.forEach { targetPos ->
                        val capturedPiece = board[targetPos.row][targetPos.col]
                        allPossibleMoves.add(ChessMove(position, targetPos, piece, capturedPiece))
                    }
                }
            }
        }

        // If we have valid moves, pick one at random
        if (allPossibleMoves.isNotEmpty()) {
            // For now, just pick a random move
            // In the future, this is where we'll implement smarter move selection based on difficulty
            val selectedMove = allPossibleMoves.random()
            onMove(selectedMove)
        }
    }
}
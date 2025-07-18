package com.example.android.chessapp

import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Optimized Chess AI that makes legal moves using incremental move validation
 * for better performance compared to the original ChessAI implementation.
 */
class OptimizedChessAI {
    /**
     * Makes a move for the AI bot using optimized move validation
     * @param board Current board state
     * @param color AI's color (BLACK or WHITE)
     * @param gameState Current game state for validation
     * @param onMove Callback when move is selected with the chosen move
     */
    suspend fun makeMove(
        board: Array<Array<ChessPiece?>>,
        color: PieceColor,
        gameState: ChessGameState,
        onMove: (ChessMove) -> Unit
    ) {
        // Add thinking delay for realistic feel
        delay(Random.nextLong(300, 1000))
        
        // Get all legal moves using optimized validation
        val allPossibleMoves = generateAllLegalMoves(board, color, gameState)
        
        // If we have valid moves, pick one at random
        if (allPossibleMoves.isNotEmpty()) {
            // For now, just pick a random move
            // In the future, this is where we'll implement smarter move selection based on difficulty
            val selectedMove = allPossibleMoves.random()
            onMove(selectedMove)
        }
    }
    
    /**
     * Checks if the given color's king is in check by using the game state's validation
     */
    private fun isKingInCheck(board: Array<Array<ChessPiece?>>, color: PieceColor, gameState: ChessGameState): Boolean {
        // Find the king's position
        for (row in 0..7) {
            for (col in 0..7) {
                val piece = board[row][col]
                if (piece?.type == PieceType.KING && piece.color == color) {
                    // Use the game state's validation to check if the king is in check
                    return gameState.isKingInCheck(color)
                }
            }
        }
        return false
    }
    
    /**
     * Generates all legal moves for the given color using optimized validation
     * @param prioritizeKingSafety If true, will prioritize moves that get the king out of check
     */
    private fun generateAllLegalMoves(
        board: Array<Array<ChessPiece?>>,
        color: PieceColor,
        gameState: ChessGameState,
        prioritizeKingSafety: Boolean = true
    ): List<ChessMove> {
        val legalMoves = mutableListOf<ChessMove>()
        val kingInCheck = prioritizeKingSafety && isKingInCheck(board, color, gameState)
        val kingSafetyMoves = mutableListOf<ChessMove>()
        
        // Iterate through all pieces of the given color
        for (row in 0..7) {
            for (col in 0..7) {
                val piece = board[row][col]
                if (piece != null && piece.color == color) {
                    val position = ChessPosition(row, col)
                    
                    // Get legal moves with built-in check validation
                    val possibleMoves = ChessLogic.getPossibleMoves(
                        position = position,
                        board = board,
                        gameState = gameState,
                        validateChecks = true  // Enable check validation during move generation
                    )
                    
                    // Iterate through ChessMove objects directly
                    possibleMoves.forEach { move ->
                        
                        // If king is in check, check if this move gets us out of check
                        if (kingInCheck) {
                            // Instead of using the private applyMove/undoMove, we'll use the game state's validation
                            // which already handles check validation properly
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
        
        // If we found moves that get the king out of check, return only those
        // Otherwise, return all legal moves (which might be empty if it's checkmate)
        return if (kingInCheck && kingSafetyMoves.isNotEmpty()) {
            kingSafetyMoves
        } else {
            legalMoves
        }
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

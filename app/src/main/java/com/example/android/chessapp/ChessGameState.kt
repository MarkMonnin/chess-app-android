package com.example.android.chessapp


/**
 * Represents the complete state of a chess game
 */
/**
 * Data class representing a single move in the game history
 */
data class ChessMoveRecord(
    val move: ChessMove,
    val capturedPiece: ChessPiece?,
    val previousState: ChessGameState
)

data class ChessGameState(
    val board: Array<Array<ChessPiece?>>,
    val currentPlayer: PieceColor = PieceColor.WHITE,
    val whiteKingPosition: ChessPosition = ChessPosition(7, 4),
    val blackKingPosition: ChessPosition = ChessPosition(0, 4),
    val whiteCanCastleKingside: Boolean = true,
    val whiteCanCastleQueenside: Boolean = true,
    val blackCanCastleKingside: Boolean = true,
    val blackCanCastleQueenside: Boolean = true,
    val enPassantTarget: ChessPosition? = null,
    val halfMoveClock: Int = 0,
    val fullMoveNumber: Int = 1,
    val isCheck: Boolean = false,
    val isCheckmate: Boolean = false,
    val isStalemate: Boolean = false,
    val lastMove: ChessMove? = null,
    // Move history for undo/redo functionality
    val moveHistory: List<ChessMoveRecord> = emptyList(),
    val futureMoves: List<ChessMoveRecord> = emptyList()
) {
    /**
     * Custom equals implementation to properly compare board arrays
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChessGameState

        if (!board.contentDeepEquals(other.board)) return false
        if (currentPlayer != other.currentPlayer) return false
        if (whiteKingPosition != other.whiteKingPosition) return false
        if (blackKingPosition != other.blackKingPosition) return false
        if (whiteCanCastleKingside != other.whiteCanCastleKingside) return false
        if (whiteCanCastleQueenside != other.whiteCanCastleQueenside) return false
        if (blackCanCastleKingside != other.blackCanCastleKingside) return false
        if (blackCanCastleQueenside != other.blackCanCastleQueenside) return false
        if (enPassantTarget != other.enPassantTarget) return false
        if (halfMoveClock != other.halfMoveClock) return false
        if (fullMoveNumber != other.fullMoveNumber) return false
        if (isCheck != other.isCheck) return false
        if (isCheckmate != other.isCheckmate) return false
        if (isStalemate != other.isStalemate) return false
        if (lastMove != other.lastMove) return false

        return true
    }

    /**
     * Custom hashCode implementation to match our custom equals
     */
    override fun hashCode(): Int {
        var result = board.contentDeepHashCode()
        result = 31 * result + currentPlayer.hashCode()
        result = 31 * result + whiteKingPosition.hashCode()
        result = 31 * result + blackKingPosition.hashCode()
        result = 31 * result + whiteCanCastleKingside.hashCode()
        result = 31 * result + whiteCanCastleQueenside.hashCode()
        result = 31 * result + blackCanCastleKingside.hashCode()
        result = 31 * result + blackCanCastleQueenside.hashCode()
        result = 31 * result + (enPassantTarget?.hashCode() ?: 0)
        result = 31 * result + halfMoveClock
        result = 31 * result + fullMoveNumber
        result = 31 * result + isCheck.hashCode()
        result = 31 * result + isCheckmate.hashCode()
        result = 31 * result + isStalemate.hashCode()
        result = 31 * result + (lastMove?.hashCode() ?: 0)
        return result
    }
    /**
     * Creates a new game state by applying a move to the current state
     */
    /**
     * Applies a move to the current game state and returns a new game state
     * @param move The move to apply
     * @param recordMove Whether to record this move in the history (default: true)
     * @return A new ChessGameState with the move applied
     */
    fun applyMove(move: ChessMove, recordMove: Boolean = true): ChessGameState {
        // Create a deep copy of the board
        val newBoard = board.map { it.clone() }.toTypedArray()
        val piece = newBoard[move.from.row][move.from.col]!!
        
        // Handle castling
        if (piece.type == PieceType.KING && kotlin.math.abs(move.from.col - move.to.col) == 2) {
            // This is a castling move
            val row = move.from.row
            if (move.to.col == 6) {
                // Kingside castling - move the rook
                newBoard[row][5] = newBoard[row][7]?.copy()
                newBoard[row][7] = null
            } else {
                // Queenside castling - move the rook
                newBoard[row][3] = newBoard[row][0]?.copy()
                newBoard[row][0] = null
            }
        }
        
        // Get the captured piece (if any)
        val capturedPiece = newBoard[move.to.row][move.to.col]
        
        // Apply the move
        newBoard[move.from.row][move.from.col] = null
        newBoard[move.to.row][move.to.col] = piece
        
        // Update king positions if needed
        val updatedWhiteKingPos = if (piece.type == PieceType.KING && piece.color == PieceColor.WHITE) {
            move.to
        } else {
            whiteKingPosition
        }
        
        val updatedBlackKingPos = if (piece.type == PieceType.KING && piece.color == PieceColor.BLACK) {
            move.to
        } else {
            blackKingPosition
        }
        
        // Update castling rights based on moved pieces
        val newWhiteCanCastleKingside = when {
            piece.type == PieceType.KING && piece.color == PieceColor.WHITE -> false
            piece.type == PieceType.ROOK && move.from == ChessPosition(7, 7) -> false
            else -> whiteCanCastleKingside
        }
        
        val newWhiteCanCastleQueenside = when {
            piece.type == PieceType.KING && piece.color == PieceColor.WHITE -> false
            piece.type == PieceType.ROOK && move.from == ChessPosition(7, 0) -> false
            else -> whiteCanCastleQueenside
        }
        
        val newBlackCanCastleKingside = when {
            piece.type == PieceType.KING && piece.color == PieceColor.BLACK -> false
            piece.type == PieceType.ROOK && move.from == ChessPosition(0, 7) -> false
            else -> blackCanCastleKingside
        }
        
        val newBlackCanCastleQueenside = when {
            piece.type == PieceType.KING && piece.color == PieceColor.BLACK -> false
            piece.type == PieceType.ROOK && move.from == ChessPosition(0, 0) -> false
            else -> blackCanCastleQueenside
        }
        
        // Handle rook capture for castling rights
        var finalWhiteCanCastleKingside = newWhiteCanCastleKingside
        var finalWhiteCanCastleQueenside = newWhiteCanCastleQueenside
        var finalBlackCanCastleKingside = newBlackCanCastleKingside
        var finalBlackCanCastleQueenside = newBlackCanCastleQueenside
        
        if (capturedPiece?.type == PieceType.ROOK) {
            when (move.to) {
                ChessPosition(0, 0) -> finalBlackCanCastleQueenside = false
                ChessPosition(0, 7) -> finalBlackCanCastleKingside = false
                ChessPosition(7, 0) -> finalWhiteCanCastleQueenside = false
                ChessPosition(7, 7) -> finalWhiteCanCastleKingside = false
                else -> { /* No change */ }
            }
        }
        
        // Create a new state with the move applied
        val newState = copy(
            board = newBoard,
            currentPlayer = currentPlayer.opposite(),
            whiteKingPosition = updatedWhiteKingPos,
            blackKingPosition = updatedBlackKingPos,
            whiteCanCastleKingside = finalWhiteCanCastleKingside,
            whiteCanCastleQueenside = finalWhiteCanCastleQueenside,
            blackCanCastleKingside = finalBlackCanCastleKingside,
            blackCanCastleQueenside = finalBlackCanCastleQueenside,
            lastMove = move,
            isCheck = false,
            isCheckmate = false,
            isStalemate = false,
            // Clear future moves when making a new move (we're creating a new timeline)
            futureMoves = emptyList()
        )
        
        // Check if the opponent is in check after the move
        val opponentInCheck = newState.isInCheck(newState.currentPlayer)
        
        // Check for checkmate or stalemate
        val hasLegalMoves = newState.hasLegalMoves(newState.currentPlayer)
        
        // Create the final state with check/checkmate/stalemate status
        val finalState = newState.copy(
            isCheck = opponentInCheck,
            isCheckmate = opponentInCheck && !hasLegalMoves,
            isStalemate = !opponentInCheck && !hasLegalMoves
        )
        
        // If we're recording this move, add it to the history
        return if (recordMove) {
            val moveRecord = ChessMoveRecord(
                move = move.copy(capturedPiece = capturedPiece),
                capturedPiece = capturedPiece,
                previousState = this
            )
            finalState.copy(
                moveHistory = moveHistory + moveRecord
            )
        } else {
            finalState
        }
    }

    /**
     * Checks if the specified color's king is in check
     * @param color The color to check for check
     * @return true if the king is in check, false otherwise
     */
    private fun isInCheck(color: PieceColor): Boolean {
        val kingPosition = if (color == PieceColor.WHITE) whiteKingPosition else blackKingPosition
        val opponentColor = color.opposite()
        
        // Check all opponent's pieces to see if any can capture the king
        for (row in 0..7) {
            for (col in 0..7) {
                val piece = board[row][col]
                if (piece != null && piece.color == opponentColor) {
                    // Get pseudo-legal moves (without check validation to avoid infinite recursion)
                    val moves = ChessLogic.getValidMoves(ChessPosition(row, col), board, this)
                    if (moves.any { it == kingPosition }) {
                        return true
                    }
                }
            }
        }
        return false
    }

    /**
     * Gets all valid moves for a piece at the given position using the optimized validator
     * @param position The position of the piece
     * @param board The current board state
     * @param checkForCheck Whether to validate that moves don't leave the king in check
     * @return List of valid target positions
     */
    fun getValidMovesOptimized(
        position: ChessPosition,
        board: Array<Array<ChessPiece?>>,
        checkForCheck: Boolean = true
    ): List<ChessPosition> {
        val piece = board[position.row][position.col] ?: return emptyList()
        
        // Get all pseudo-legal moves (basic movement rules)
        val pseudoLegalMoves = ChessLogic.getValidMoves(position, board, this)
        
        if (!checkForCheck) {
            return pseudoLegalMoves
        }
        
        // Filter out moves that would leave the king in check using incremental validation
        val validator = IncrementalMoveValidator()
        val legalMoves = mutableListOf<ChessPosition>()
        
        pseudoLegalMoves.forEach { targetPos ->
            val move = ChessMove(
                from = position,
                to = targetPos,
                piece = piece,
                capturedPiece = board[targetPos.row][targetPos.col]
            )
            
            if (validator.isMoveLegal(move, board, this)) {
                legalMoves.add(targetPos)
            }
        }
        
        return legalMoves
    }
    
    /**
     * Gets all valid moves for a piece at the given position
     * @param position The position of the piece
     * @param board The current board state
     * @param checkForCheck Whether to validate that moves don't leave the king in check
     * @return List of valid target positions
     * @deprecated Use getValidMovesOptimized for better performance
     */
    @Deprecated(
        message = "Use getValidMovesOptimized for better performance",
        replaceWith = ReplaceWith("getValidMovesOptimized(position, board, checkForCheck)", 
                               imports = ["com.example.android.chessapp.getValidMovesOptimized"])
    )
    fun getValidMoves(position: ChessPosition, board: Array<Array<ChessPiece?>>, checkForCheck: Boolean = true): List<ChessPosition> {
        // Default to using the optimized version
        return getValidMovesOptimized(position, board, checkForCheck)
    }

    /**
     * Checks if the king of the given color is in check
     * @param color The color of the king to check
     * @return true if the king is in check, false otherwise
     */
    fun isKingInCheck(color: PieceColor): Boolean {
        val kingPosition = if (color == PieceColor.WHITE) whiteKingPosition else blackKingPosition
        val opponentColor = color.opposite()
        
        // Check if any opponent piece can attack the king's position
        for (row in 0..7) {
            for (col in 0..7) {
                val piece = board[row][col]
                if (piece != null && piece.color == opponentColor) {
                    val moves = ChessLogic.getValidMoves(
                        position = ChessPosition(row, col),
                        board = board,
                        gameState = this,
                        validateChecks = false // Don't validate checks here to avoid infinite recursion
                    )
                    if (moves.any { it == kingPosition }) {
                        return true
                    }
                }
            }
        }
        return false
    }
    
    /**
     * Checks if the current player has any legal moves
     * A move is legal if it doesn't leave the king in check
     */
    /**
     * Undoes the last move and returns the previous game state
     * @return The previous game state, or null if there's no move to undo
     */
    fun undoMove(): ChessGameState? {
        if (moveHistory.isEmpty()) return null
        
        val lastMoveRecord = moveHistory.last()
        val previousState = lastMoveRecord.previousState
        
        // Return the previous state with the current move added to future moves
        return previousState.copy(
            futureMoves = listOf(
                ChessMoveRecord(
                    move = lastMoveRecord.move,
                    capturedPiece = lastMoveRecord.capturedPiece,
                    previousState = this
                )
            ) + previousState.futureMoves
        )
    }
    
    /**
     * Redoes the last undone move and returns the next game state
     * @return The next game state, or null if there's no move to redo
     */
    fun redoMove(): ChessGameState? {
        if (futureMoves.isEmpty()) return null
        
        val nextMoveRecord = futureMoves.first()
        val nextState = nextMoveRecord.previousState.applyMove(nextMoveRecord.move, recordMove = false)
        
        // Return the next state with the move removed from future moves
        return nextState.copy(
            futureMoves = futureMoves.drop(1),
            // Preserve the move history from the original state
            moveHistory = moveHistory
        )
    }
    
    /**
     * Checks if there are any moves that can be undone
     */
    fun canUndo(): Boolean = moveHistory.isNotEmpty()
    
    /**
     * Checks if there are any moves that can be redone
     */
    fun canRedo(): Boolean = futureMoves.isNotEmpty()
    
    private fun hasLegalMoves(color: PieceColor): Boolean {
        val validator = IncrementalMoveValidator()
        
        for (row in 0..7) {
            for (col in 0..7) {
                val piece = board[row][col]
                if (piece != null && piece.color == color) {
                    val position = ChessPosition(row, col)
                    
                    // Get all pseudo-legal moves (basic movement rules)
                    val pseudoLegalMoves = ChessLogic.getValidMoves(position, board, this)
                    
                    // Check if any of the pseudo-legal moves are actually legal
                    for (targetPos in pseudoLegalMoves) {
                        val move = ChessMove(
                            from = position,
                            to = targetPos,
                            piece = piece,
                            capturedPiece = board[targetPos.row][targetPos.col]
                        )
                        
                        // Use the incremental validator to check if the move is legal
                        if (validator.isMoveLegal(move, board, this)) {
                            return true
                        }
                    }
                }
            }
        }
        return false
    }
}

/**
 * Extension function to get the opposite color
 */
fun PieceColor.opposite(): PieceColor {
    return if (this == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
}

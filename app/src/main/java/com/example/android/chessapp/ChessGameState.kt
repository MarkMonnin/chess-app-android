package com.example.android.chessapp

/**
 * Represents the complete state of a chess game
 */
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
    val lastMove: ChessMove? = null
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
    fun applyMove(move: ChessMove): ChessGameState {
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
        
        // Update castling rights
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
        
        val newState = copy(
            board = newBoard,
            currentPlayer = currentPlayer.opposite(),
            whiteKingPosition = updatedWhiteKingPos,
            blackKingPosition = updatedBlackKingPos,
            whiteCanCastleKingside = newWhiteCanCastleKingside,
            whiteCanCastleQueenside = newWhiteCanCastleQueenside,
            blackCanCastleKingside = newBlackCanCastleKingside,
            blackCanCastleQueenside = newBlackCanCastleQueenside,
            lastMove = move,
            isCheck = false,
            isCheckmate = false,
            isStalemate = false
        )
        
        // Check if the opponent is in check after the move
        val opponentInCheck = newState.isInCheck(newState.currentPlayer)
        
        // Check for checkmate or stalemate
        val hasLegalMoves = newState.hasLegalMoves(newState.currentPlayer)
        
        return newState.copy(
            isCheck = opponentInCheck,
            isCheckmate = opponentInCheck && !hasLegalMoves,
            isStalemate = !opponentInCheck && !hasLegalMoves
        )
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
     * Gets all valid moves for a piece at the given position
     * @param position The position of the piece
     * @param board The current board state
     * @param checkForCheck Whether to validate that moves don't leave the king in check
     * @return List of valid target positions
     */
    fun getValidMoves(position: ChessPosition, board: Array<Array<ChessPiece?>>, checkForCheck: Boolean = true): List<ChessPosition> {
        val piece = board[position.row][position.col] ?: return emptyList()
        
        // Get all pseudo-legal moves (basic movement rules)
        var moves = ChessLogic.getValidMoves(position, board, this)
        
        if (checkForCheck) {
            // Filter out moves that would leave the king in check
            moves = moves.filter { move ->
                // Create a copy of the board to test the move
                val testBoard = board.map { it.clone() }.toTypedArray()
                testBoard[move.row][move.col] = piece
                testBoard[position.row][position.col] = null
                
                // Create a temporary game state with the move applied and king position updated if needed
                val updatedWhiteKingPos = if (piece.type == PieceType.KING && piece.color == PieceColor.WHITE) {
                    move
                } else {
                    whiteKingPosition
                }
                
                val updatedBlackKingPos = if (piece.type == PieceType.KING && piece.color == PieceColor.BLACK) {
                    move
                } else {
                    blackKingPosition
                }
                
                val tempState = copy(
                    board = testBoard,
                    whiteKingPosition = updatedWhiteKingPos,
                    blackKingPosition = updatedBlackKingPos
                )
                
                // The move is valid if it doesn't leave the king in check
                !tempState.isInCheck(piece.color)
            }
        }
        
        return moves
    }

    /**
     * Checks if the current player has any legal moves
     * A move is legal if it doesn't leave the king in check
     */
    private fun hasLegalMoves(color: PieceColor): Boolean {
        for (row in 0..7) {
            for (col in 0..7) {
                val piece = board[row][col]
                if (piece != null && piece.color == color) {
                    val position = ChessPosition(row, col)
                    val moves = getValidMoves(position, board)
                    
                    // Check if any of the valid moves don't leave the king in check
                    for (move in moves) {
                        // Create a copy of the board to test the move
                        val testBoard = board.map { it.clone() }.toTypedArray()
                        testBoard[move.row][move.col] = piece
                        testBoard[position.row][position.col] = null
                        
                        // Calculate updated king positions if the moved piece is a king
                        val updatedWhiteKingPos = if (piece.type == PieceType.KING && color == PieceColor.WHITE) {
                            move
                        } else {
                            whiteKingPosition
                        }
                        
                        val updatedBlackKingPos = if (piece.type == PieceType.KING && color == PieceColor.BLACK) {
                            move
                        } else {
                            blackKingPosition
                        }
                        
                        // Create a temporary game state with updated board and king positions
                        val tempState = copy(
                            board = testBoard,
                            whiteKingPosition = updatedWhiteKingPos,
                            blackKingPosition = updatedBlackKingPos
                        )
                        
                        // If this move doesn't leave the king in check, it's a legal move
                        if (!tempState.isInCheck(color)) {
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

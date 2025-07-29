package com.example.android.chessapp

import kotlin.math.abs

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
    val initialBoard: Array<Array<ChessPiece?>> = Array(8) { Array(8) { null } },
    val board: Array<Array<ChessPiece?>> = initialBoard,
    val currentPlayer: PieceColor = PieceColor.WHITE,
    val moveHistory: List<ChessMoveRecord> = emptyList(),
    val capturedPieces: List<ChessPiece> = emptyList(),
    val whiteCanCastleKingside: Boolean = true,
    val whiteCanCastleQueenside: Boolean = true,
    val blackCanCastleKingside: Boolean = true,
    val blackCanCastleQueenside: Boolean = true,
    val enPassantTarget: ChessPosition? = null,
    val halfMoveClock: Int = 0, // Number of half-moves since last capture or pawn move
    val fullMoveNumber: Int = 1, // Starts at 1, increments after black's move
    val lastMove: ChessMove? = moveHistory.lastOrNull()?.move,
    val futureMoves: List<ChessMoveRecord> = emptyList(),
    // Keep these as parameters but use lazy initialization
    private val _whiteKingPosition: ChessPosition? = null,
    private val _blackKingPosition: ChessPosition? = null
) {
    /** Whether the current player's king is in check */
    val isCheck: Boolean get() = isInCheck(currentPlayer)
    /** Whether the game is in checkmate for the current player */
    val isCheckmate: Boolean get() = isCheck && !hasLegalMoves(currentPlayer)
    /** Whether the game is in stalemate for the current player */
    val isStalemate: Boolean get() = !isCheck && !hasLegalMoves(currentPlayer)

    val isDrawByFiftyMoveRule: Boolean get() = halfMoveClock >= 100
    val isDrawByInsufficientMaterial: Boolean get() = isInsufficientMaterial()
    val whiteKingPosition: ChessPosition get() = _whiteKingPosition ?: findKingPosition(PieceColor.WHITE)
    val blackKingPosition: ChessPosition get() = _blackKingPosition ?: findKingPosition(PieceColor.BLACK)

    private fun findKingPosition(color: PieceColor): ChessPosition {
        for (row in 0..7) {
            for (col in 0..7) {
                val piece = board[row][col]
                if (piece?.type == PieceType.KING && piece.color == color) {
                    return ChessPosition(row, col)
                }
            }
        }
        throw IllegalStateException("${color.name} king not found on the board")
    }

    private fun isInsufficientMaterial(): Boolean {
        // Count pieces for both sides
        val whitePieces = mutableListOf<ChessPiece>()
        val blackPieces = mutableListOf<ChessPiece>()

        for (row in board) {
            for (piece in row) {
                if (piece != null) {
                    when (piece.color) {
                        PieceColor.WHITE -> whitePieces.add(piece)
                        PieceColor.BLACK -> blackPieces.add(piece)
                    }
                }
            }
        }

        // King vs King
        if (whitePieces.size == 1 && blackPieces.size == 1) return true

        // King and bishop vs king
        if (whitePieces.size == 1 && blackPieces.size == 2 &&
            blackPieces.any { it.type == PieceType.BISHOP }) return true
        if (blackPieces.size == 1 && whitePieces.size == 2 &&
            whitePieces.any { it.type == PieceType.BISHOP }) return true

        // King and knight vs king
        if (whitePieces.size == 1 && blackPieces.size == 2 &&
            blackPieces.any { it.type == PieceType.KNIGHT }) return true
        if (blackPieces.size == 1 && whitePieces.size == 2 &&
            whitePieces.any { it.type == PieceType.KNIGHT }) return true

        // King and bishop vs king and bishop with bishops on same color
        if (whitePieces.size == 2 && blackPieces.size == 2) {
            val whiteBishop = whitePieces.find { it.type == PieceType.BISHOP }
            val blackBishop = blackPieces.find { it.type == PieceType.BISHOP }

            if (whiteBishop != null && blackBishop != null) {
                // Find bishop positions
                var whiteBishopPos: ChessPosition? = null
                var blackBishopPos: ChessPosition? = null

                for (row in 0..7) {
                    for (col in 0..7) {
                        val piece = board[row][col]
                        if (piece == whiteBishop) whiteBishopPos = ChessPosition(row, col)
                        if (piece == blackBishop) blackBishopPos = ChessPosition(row, col)
                    }
                }

                // If both bishops are on the same color square, it's a draw
                if (whiteBishopPos != null && blackBishopPos != null) {
                    val whiteSquareColor = (whiteBishopPos.row + whiteBishopPos.col) % 2 == 0
                    val blackSquareColor = (blackBishopPos.row + blackBishopPos.col) % 2 == 0
                    if (whiteSquareColor == blackSquareColor) return true
                }
            }
        }

        return false
    }

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
     * Applies a move to the current game state and returns a new game state
     * @param move The move to apply
     * @param recordMove Whether to record this move in the history (default: true)
     * @return A new ChessGameState with the move applied
     */
    fun applyMove(move: ChessMove, recordMove: Boolean = true, calculateEndState: Boolean = true): ChessGameState {
        val newBoard = board.map { it.clone() }.toTypedArray()
        val piece = newBoard[move.from.row][move.from.col]!!

        // Handle castling
        if (piece.type == PieceType.KING && abs(move.from.col - move.to.col) == 2) {
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

        // Handle en passant capture
        if (piece.type == PieceType.PAWN && move.to == enPassantTarget) {
            // This is an en passant capture
            val capturedPawnRow = if (piece.color == PieceColor.WHITE) move.to.row + 1 else move.to.row - 1
            newBoard[capturedPawnRow][move.to.col] = null
        }

        // Get the captured piece (if any)
        val capturedPiece = newBoard[move.to.row][move.to.col]

        // Apply the move
        newBoard[move.from.row][move.from.col] = null

        // Handle pawn promotion
        if (move.isPromotion() && move.promotionPiece != null) {
            // Replace the pawn with the promoted piece
            val promotedPiece = ChessPiece(move.promotionPiece, piece.color)
            newBoard[move.to.row][move.to.col] = promotedPiece
        } else {
            // Normal move - place the original piece
            newBoard[move.to.row][move.to.col] = piece
        }

        val updatedWhiteKingPos = if (piece.type == PieceType.KING && piece.color == PieceColor.WHITE) move.to else whiteKingPosition
        val updatedBlackKingPos = if (piece.type == PieceType.KING && piece.color == PieceColor.BLACK) move.to else blackKingPosition

        var newWhiteCanCastleKingside = whiteCanCastleKingside
        var newWhiteCanCastleQueenside = whiteCanCastleQueenside
        var newBlackCanCastleKingside = blackCanCastleKingside
        var newBlackCanCastleQueenside = blackCanCastleQueenside

        // A move can revoke castling rights if a king or a rook moves for the first time,
        // or if a rook is captured.

        // 1. King move
        if (piece.type == PieceType.KING) {
            if (piece.color == PieceColor.WHITE) {
                newWhiteCanCastleKingside = false
                newWhiteCanCastleQueenside = false
            } else {
                newBlackCanCastleKingside = false
                newBlackCanCastleQueenside = false
            }
        }

        // 2. Rook move/capture
        if (move.from == ChessPosition(7, 7) || move.to == ChessPosition(7, 7)) newWhiteCanCastleKingside = false
        if (move.from == ChessPosition(7, 0) || move.to == ChessPosition(7, 0)) newWhiteCanCastleQueenside = false
        if (move.from == ChessPosition(0, 7) || move.to == ChessPosition(0, 7)) newBlackCanCastleKingside = false
        if (move.from == ChessPosition(0, 0) || move.to == ChessPosition(0, 0)) newBlackCanCastleQueenside = false

        val newEnPassantTarget = if (piece.type == PieceType.PAWN && abs(move.from.row - move.to.row) == 2) {
            ChessPosition((move.from.row + move.to.row) / 2, move.from.col)
        } else {
            null
        }

        val newHalfMoveClock = if (piece.type == PieceType.PAWN || capturedPiece != null) 0 else halfMoveClock + 1
        val newFullMoveNumber = if (currentPlayer == PieceColor.BLACK) fullMoveNumber + 1 else fullMoveNumber

        val newCapturedPieces = if (capturedPiece != null) {
            capturedPieces + capturedPiece
        } else if (piece.type == PieceType.PAWN && move.to == enPassantTarget) {
            capturedPieces + ChessPiece(PieceType.PAWN, currentPlayer.opposite())
        } else {
            capturedPieces
        }

        var newState = copy(
            board = newBoard,
            currentPlayer = currentPlayer.opposite(),
            whiteCanCastleKingside = newWhiteCanCastleKingside,
            whiteCanCastleQueenside = newWhiteCanCastleQueenside,
            blackCanCastleKingside = newBlackCanCastleKingside,
            blackCanCastleQueenside = newBlackCanCastleQueenside,
            enPassantTarget = newEnPassantTarget,
            halfMoveClock = newHalfMoveClock,
            fullMoveNumber = newFullMoveNumber,
            capturedPieces = newCapturedPieces,
            lastMove = move,
            futureMoves = emptyList(),
            _whiteKingPosition = updatedWhiteKingPos,
            _blackKingPosition = updatedBlackKingPos
        )

        if (calculateEndState) {
            val opponentInCheck = newState.isInCheck(newState.currentPlayer)

            // Check for checkmate or stalemate
            val hasLegalMoves = newState.hasLegalMoves(newState.currentPlayer)
        }

        // If we're recording this move, add it to the history
        return if (recordMove) {
            val moveRecord = ChessMoveRecord(
                move = move.copy(capturedPiece = capturedPiece),
                capturedPiece = capturedPiece,
                previousState = this
            )
            newState.copy(moveHistory = moveHistory + moveRecord)
        } else {
            newState
        }
    }

    fun isInCheck(color: PieceColor): Boolean {
        val kingPosition = if (color == PieceColor.WHITE) whiteKingPosition else blackKingPosition
        val validator = IncrementalMoveValidator()
        return validator.isKingInCheck(kingPosition, color, board)
    }



    /**
     * Undoes the last move and returns the previous game state
     * @return The previous game state, or null if there's no move to undo
     */
    fun undoMove(): ChessGameState? {
        if (moveHistory.isEmpty()) return null

        val lastMoveRecord = moveHistory.last()
        val previousState = lastMoveRecord.previousState

        return previousState.copy(
            moveHistory = moveHistory.dropLast(1),
            futureMoves = listOf(lastMoveRecord) + futureMoves
        )
    }

    /**
     * Redoes the last undone move and returns the next game state
     * @return The next game state, or null if there's no move to redo
     */
    fun redoMove(): ChessGameState? {
        if (futureMoves.isEmpty()) return null

        val nextMoveRecord = futureMoves.first()
        val newFutureMoves = futureMoves.drop(1)

        // Apply the move from the record and record it in history
        // Apply the move from the record to the previous state of that record
        val stateAfterRedoMove = nextMoveRecord.previousState.applyMove(nextMoveRecord.move, recordMove = true)

        return stateAfterRedoMove.copy(
            moveHistory = moveHistory + nextMoveRecord, // Add the re-done move record to history
            futureMoves = newFutureMoves
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

    /**
     * Checks if a move would result in pawn promotion
     * @param move The move to check
     * @return true if the move would result in pawn promotion
     */
    fun wouldPromote(move: ChessMove): Boolean {
        val piece = board[move.from.row][move.from.col] ?: return false
        if (piece.type != PieceType.PAWN) return false

        val promotionRank = if (piece.color == PieceColor.WHITE) 0 else 7
        return move.to.row == promotionRank
    }

    /**
     * Creates promotion moves for a given pawn move
     * @param baseMove The base pawn move (without promotion piece specified)
     * @return List of moves with each possible promotion piece
     */
    fun createPromotionMoves(baseMove: ChessMove): List<ChessMove> {
        if (!wouldPromote(baseMove)) return listOf(baseMove)

        return listOf(
            baseMove.withPromotion(PieceType.QUEEN),
            baseMove.withPromotion(PieceType.ROOK),
            baseMove.withPromotion(PieceType.BISHOP),
            baseMove.withPromotion(PieceType.KNIGHT)
        )
    }

    private fun hasLegalMoves(color: PieceColor): Boolean {
        val validator = IncrementalMoveValidator()

        for (row in 0..7) {
            for (col in 0..7) {
                val piece = board[row][col]
                if (piece != null && piece.color == color) {
                    val position = ChessPosition(row, col)

                    // Get all pseudo-legal moves (basic movement rules)
                    val pseudoLegalMoves = ChessLogic.getValidMoves(position, board, this, validateChecks = false)

                    // Check if any of the pseudo-legal moves are actually legal
                    for (targetPos in pseudoLegalMoves) {
                        val move = ChessMove(
                            from = position,
                            to = targetPos,
                            piece = piece,
                            capturedPiece = board[targetPos.row][targetPos.col]
                        )

                        // For pawn promotion moves, check all promotion possibilities
                        if (wouldPromote(move)) {
                            val promotionMoves = createPromotionMoves(move)
                            for (promotionMove in promotionMoves) {
                                if (validator.isMoveLegal(promotionMove, board, this)) {
                                    return true
                                }
                            }
                        } else {
                            // Use the incremental validator to check if the move is legal
                            if (validator.isMoveLegal(move, board, this)) {
                                return true
                            }
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
package com.example.android.chessapp

/**
 * A high-performance move validator that uses incremental updates instead of deep copying the board.
 * This provides significant performance improvements for move validation and check detection.
 */
class IncrementalMoveValidator {
    /**
     * Data class to store the result of applying a move, used for undoing the move
     */
    data class MoveResult(
        val originalPiece: ChessPiece?,
        val kingPositionChanged: Boolean,
        val newKingPosition: ChessPosition?
    )

    /**
     * Applies a move to the board without creating a full copy.
     * Returns information needed to undo the move.
     */
    private fun applyMove(
        board: Array<Array<ChessPiece?>>,
        move: ChessMove
    ): MoveResult {
        val originalPiece = board[move.to.row][move.to.col]
        val movingPiece = board[move.from.row][move.from.col]!!

        // Apply the move
        val pieceToPlace = if (move.promotionPiece != null) {
            ChessPiece(move.promotionPiece!!, movingPiece.color)
        } else {
            movingPiece
        }
        board[move.to.row][move.to.col] = pieceToPlace
        board[move.from.row][move.from.col] = null
        
        // Handle castling rook movement
        if (movingPiece.type == PieceType.KING && kotlin.math.abs(move.from.col - move.to.col) == 2) {
            val row = move.from.row
            if (move.to.col == 6) {
                // Kingside castling
                board[row][5] = board[row][7]
                board[row][7] = null
            } else {
                // Queenside castling
                board[row][3] = board[row][0]
                board[row][0] = null
            }
        }
        
        val kingPositionChanged = movingPiece.type == PieceType.KING
        val newKingPosition = if (kingPositionChanged) move.to else null
        
        return MoveResult(originalPiece, kingPositionChanged, newKingPosition)
    }
    
    /**
     * Undoes a move using the MoveResult information
     */
    private fun undoMove(
        board: Array<Array<ChessPiece?>>,
        move: ChessMove,
        moveResult: MoveResult
    ) {
        val movingPiece = board[move.to.row][move.to.col]!!

        // Undo the basic move - restore original pawn if this was a promotion
        val originalMovingPiece = if (move.promotionPiece != null) {
            ChessPiece(PieceType.PAWN, movingPiece.color)
        } else {
            movingPiece
        }
        board[move.from.row][move.from.col] = originalMovingPiece
        board[move.to.row][move.to.col] = moveResult.originalPiece
        
        // Undo castling rook movement
        if (movingPiece.type == PieceType.KING && kotlin.math.abs(move.from.col - move.to.col) == 2) {
            val row = move.from.row
            if (move.to.col == 6) {
                // Undo kingside castling
                board[row][7] = board[row][5]
                board[row][5] = null
            } else {
                // Undo queenside castling
                board[row][0] = board[row][3]
                board[row][3] = null
            }
        }
    }
    
    /**
     * Efficiently checks if a move leaves the king in check
     */
    fun isMoveLegal(
        move: ChessMove,
        board: Array<Array<ChessPiece?>>,
        gameState: ChessGameState
    ): Boolean {
        // The gameState parameter is kept for future extensions, such as en passant and castling rights
        val movingPiece = board[move.from.row][move.from.col] ?: return false
        
        // Apply the move
        val moveResult = applyMove(board, move)
        
        // Determine king position after move
        val kingPosition = if (moveResult.kingPositionChanged) {
            moveResult.newKingPosition!!
        } else {
            if (movingPiece.color == PieceColor.WHITE) gameState.whiteKingPosition else gameState.blackKingPosition
        }
        
        // Check if king is in check
        val isInCheck = isKingInCheck(kingPosition, movingPiece.color, board)
        
        // Undo the move
        undoMove(board, move, moveResult)
        
        return !isInCheck
    }
    
    /**
     * Optimized check detection that stops at first attacking piece found
     */
    private fun isKingInCheck(
        kingPosition: ChessPosition,
        kingColor: PieceColor,
        board: Array<Array<ChessPiece?>>
    ): Boolean {
        val opponentColor = kingColor.opposite()
        
        // Check for pawn attacks first (most common)
        if (isAttackedByPawn(kingPosition, kingColor, board)) return true
        
        // Check for knight attacks
        if (isAttackedByKnight(kingPosition, opponentColor, board)) return true
        
        // Check for sliding piece attacks (rook, bishop, queen)
        if (isAttackedBySlidingPiece(kingPosition, opponentColor, board)) return true
        
        // Check for king attacks (adjacent squares)
        if (isAttackedByKing(kingPosition, opponentColor, board)) return true
        
        return false
    }
    
    private fun isAttackedByPawn(
        position: ChessPosition,
        defendingColor: PieceColor,
        board: Array<Array<ChessPiece?>>
    ): Boolean {
        val attackDirection = if (defendingColor == PieceColor.WHITE) 1 else -1
        val attackRow = position.row + attackDirection
        
        if (attackRow !in 0..7) return false
        
        // Check both diagonal attack squares
        listOf(position.col - 1, position.col + 1).forEach { col ->
            if (col in 0..7) {
                val piece = board[attackRow][col]
                if (piece?.type == PieceType.PAWN && piece.color != defendingColor) {
                    return true
                }
            }
        }
        return false
    }
    
    private fun isAttackedByKnight(
        position: ChessPosition,
        attackingColor: PieceColor,
        board: Array<Array<ChessPiece?>>
    ): Boolean {
        val knightMoves = listOf(
            Pair(-2, -1), Pair(-2, 1), Pair(-1, -2), Pair(-1, 2),
            Pair(1, -2), Pair(1, 2), Pair(2, -1), Pair(2, 1)
        )
        
        knightMoves.forEach { (rowOffset, colOffset) ->
            val checkRow = position.row + rowOffset
            val checkCol = position.col + colOffset
            
            if (checkRow in 0..7 && checkCol in 0..7) {
                val piece = board[checkRow][checkCol]
                if (piece?.type == PieceType.KNIGHT && piece.color == attackingColor) {
                    return true
                }
            }
        }
        return false
    }
    
    private fun isAttackedBySlidingPiece(
        position: ChessPosition,
        attackingColor: PieceColor,
        board: Array<Array<ChessPiece?>>
    ): Boolean {
        // Check all 8 directions
        val directions = listOf(
            Pair(0, 1), Pair(0, -1), Pair(1, 0), Pair(-1, 0),  // Rook directions
            Pair(1, 1), Pair(1, -1), Pair(-1, 1), Pair(-1, -1)  // Bishop directions
        )
        
        directions.forEach { (rowDir, colDir) ->
            var currentRow = position.row + rowDir
            var currentCol = position.col + colDir
            
            while (currentRow in 0..7 && currentCol in 0..7) {
                val piece = board[currentRow][currentCol]
                if (piece != null) {
                    if (piece.color == attackingColor) {
                        // Check if this piece can attack in this direction
                        val canAttack = when (piece.type) {
                            PieceType.QUEEN -> true
                            PieceType.ROOK -> rowDir == 0 || colDir == 0
                            PieceType.BISHOP -> rowDir != 0 && colDir != 0
                            else -> false
                        }
                        if (canAttack) return true
                    }
                    break // Piece blocks further movement
                }
                currentRow += rowDir
                currentCol += colDir
            }
        }
        return false
    }
    
    private fun isAttackedByKing(
        position: ChessPosition,
        attackingColor: PieceColor,
        board: Array<Array<ChessPiece?>>
    ): Boolean {
        for (rowOffset in -1..1) {
            for (colOffset in -1..1) {
                if (rowOffset == 0 && colOffset == 0) continue
                
                val checkRow = position.row + rowOffset
                val checkCol = position.col + colOffset
                
                if (checkRow in 0..7 && checkCol in 0..7) {
                    val piece = board[checkRow][checkCol]
                    if (piece?.type == PieceType.KING && piece.color == attackingColor) {
                        return true
                    }
                }
            }
        }
        return false
    }
}

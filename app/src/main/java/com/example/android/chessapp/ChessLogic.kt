package com.example.android.chessapp

object ChessLogic {
    /**
     * Checks if the given position is within the bounds of the chess board
     */
    private fun isValidPosition(position: ChessPosition): Boolean {
        return position.row in 0..7 && position.col in 0..7
    }

    /**
     * Gets all pseudo-legal moves for a piece at the given position with optional check validation
     * @param position The position of the piece
     * @param board The current board state
     * @param gameState The current game state (required for check validation)
     * @param validateChecks Whether to validate that moves don't leave the king in check
     * @return List of valid target positions
     */
    fun getValidMoves(
        position: ChessPosition, 
        board: Array<Array<ChessPiece?>>, 
        gameState: ChessGameState? = null,
        validateChecks: Boolean = true
    ): List<ChessPosition> {
        val piece = board[position.row][position.col] ?: return emptyList()
        val moves = mutableListOf<ChessPosition>()
        val validator = IncrementalMoveValidator()

        when (piece.type) {
            PieceType.PAWN -> {
                val direction = if (piece.color == PieceColor.WHITE) -1 else 1
                val startRow = if (piece.color == PieceColor.WHITE) 6 else 1

                // Move forward
                val oneStep = ChessPosition(position.row + direction, position.col)
                if (isValidPosition(oneStep) && board[oneStep.row][oneStep.col] == null) {
                    moves.add(oneStep)

                    // Two steps from starting position
                    if (position.row == startRow) {
                        val twoSteps = ChessPosition(position.row + 2 * direction, position.col)
                        if (isValidPosition(twoSteps) && board[twoSteps.row][twoSteps.col] == null) {
                            moves.add(twoSteps)
                        }
                    }
                }

                // Capture diagonally
                listOf(-1, 1).forEach { colOffset ->
                    val capturePos = ChessPosition(position.row + direction, position.col + colOffset)
                    if (isValidPosition(capturePos)) {
                        val targetPiece = board[capturePos.row][capturePos.col]
                        if (targetPiece != null && targetPiece.color != piece.color) {
                            moves.add(capturePos)
                        }
                    }
                }
            }

            PieceType.ROOK -> {
                moves.addAll(getRookMoves(position, board, piece.color))
            }

            PieceType.BISHOP -> {
                moves.addAll(getBishopMoves(position, board, piece.color))
            }

            PieceType.QUEEN -> {
                moves.addAll(getRookMoves(position, board, piece.color))
                moves.addAll(getBishopMoves(position, board, piece.color))
            }

            PieceType.KNIGHT -> {
                val knightMoves = listOf(
                    ChessPosition(position.row - 2, position.col + 1),  // 2 up, 1 right
                    ChessPosition(position.row - 2, position.col - 1),  // 2 up, 1 left
                    ChessPosition(position.row + 2, position.col + 1),  // 2 down, 1 right
                    ChessPosition(position.row + 2, position.col - 1),  // 2 down, 1 left
                    ChessPosition(position.row - 1, position.col + 2),  // 1 up, 2 right
                    ChessPosition(position.row + 1, position.col + 2),  // 1 down, 2 right
                    ChessPosition(position.row - 1, position.col - 2),  // 1 up, 2 left
                    ChessPosition(position.row + 1, position.col - 2)   // 1 down, 2 left
                )

                knightMoves.forEach { move ->
                    if (isValidPosition(move)) {
                        val targetPiece = board[move.row][move.col]
                        if (targetPiece == null || targetPiece.color != piece.color) {
                            moves.add(move)
                        }
                    }
                }
            }

            PieceType.KING -> {
                // Regular king moves
                for (rowOffset in -1..1) {
                    for (colOffset in -1..1) {
                        if (rowOffset == 0 && colOffset == 0) continue
                        val kingMove = ChessPosition(position.row + rowOffset, position.col + colOffset)
                        if (isValidPosition(kingMove)) {
                            val targetPiece = board[kingMove.row][kingMove.col]
                            if (targetPiece == null || targetPiece.color != piece.color) {
                                // Check if the target square is adjacent to the opponent's king
                                var isAdjacentToOpponentKing = false
                                for (r in -1..1) {
                                    for (c in -1..1) {
                                        if (r == 0 && c == 0) continue
                                        val checkRow = kingMove.row + r
                                        val checkCol = kingMove.col + c
                                        if (isValidPosition(ChessPosition(checkRow, checkCol))) {
                                            val adjacentPiece = board[checkRow][checkCol]
                                            if (adjacentPiece?.type == PieceType.KING && adjacentPiece.color != piece.color) {
                                                isAdjacentToOpponentKing = true
                                                break
                                            }
                                        }
                                    }
                                    if (isAdjacentToOpponentKing) break
                                }
                                
                                if (!isAdjacentToOpponentKing) {
                                    moves.add(kingMove)
                                }
                            }
                        }
                    }
                }
                
                // Add castling moves if this is the king and we have game state
                if (gameState != null) {
                    // Kingside castling
                    if ((piece.color == PieceColor.WHITE && gameState.whiteCanCastleKingside || 
                         piece.color == PieceColor.BLACK && gameState.blackCanCastleKingside) &&
                        canCastleKingside(position, board, piece.color, gameState)) {
                        val kingSidePos = ChessPosition(position.row, position.col + 2)
                        if (!validateChecks || validator.isMoveLegal(
                                ChessMove(position, kingSidePos, piece, null),
                                board,
                                gameState
                            )
                        ) {
                            moves.add(kingSidePos)
                        }
                    }
                    // Queenside castling
                    if ((piece.color == PieceColor.WHITE && gameState.whiteCanCastleQueenside || 
                         piece.color == PieceColor.BLACK && gameState.blackCanCastleQueenside) &&
                        canCastleQueenside(position, board, piece.color, gameState)) {
                        val queenSidePos = ChessPosition(position.row, position.col - 2)
                        if (!validateChecks || validator.isMoveLegal(
                                ChessMove(position, queenSidePos, piece, null),
                                board,
                                gameState
                            )
                        ) {
                            moves.add(queenSidePos)
                        }
                    }
                }
            }
        }

        return moves
    }

    /**
     * Gets all pseudo-legal rook moves from the given position
     */
    private fun getRookMoves(position: ChessPosition, board: Array<Array<ChessPiece?>>, color: PieceColor): List<ChessPosition> {
        val moves = mutableListOf<ChessPosition>()
        val directions = listOf(
            Pair(0, 1),   // Right
            Pair(0, -1),  // Left
            Pair(1, 0),   // Down
            Pair(-1, 0)   // Up
        )

        directions.forEach { (rowDir, colDir) ->
            var currentRow = position.row + rowDir
            var currentCol = position.col + colDir

            while (isValidPosition(ChessPosition(currentRow, currentCol))) {
                val targetPiece = board[currentRow][currentCol]
                if (targetPiece == null) {
                    moves.add(ChessPosition(currentRow, currentCol))
                } else {
                    if (targetPiece.color != color) {
                        moves.add(ChessPosition(currentRow, currentCol))
                    }
                    break
                }
                currentRow += rowDir
                currentCol += colDir
            }
        }

        return moves
    }

    /**
     * Gets all pseudo-legal bishop moves from the given position
     */
    private fun getBishopMoves(position: ChessPosition, board: Array<Array<ChessPiece?>>, color: PieceColor): List<ChessPosition> {
        val moves = mutableListOf<ChessPosition>()
        val directions = listOf(
            Pair(1, 1),   // Down-right
            Pair(1, -1),  // Down-left
            Pair(-1, 1),  // Up-right
            Pair(-1, -1)  // Up-left
        )

        directions.forEach { (rowDir, colDir) ->
            var currentRow = position.row + rowDir
            var currentCol = position.col + colDir

            while (isValidPosition(ChessPosition(currentRow, currentCol))) {
                val targetPiece = board[currentRow][currentCol]
                if (targetPiece == null) {
                    moves.add(ChessPosition(currentRow, currentCol))
                } else {
                    if (targetPiece.color != color) {
                        moves.add(ChessPosition(currentRow, currentCol))
                    }
                    break
                }
                currentRow += rowDir
                currentCol += colDir
            }
        }

        return moves
    }


    /**
     * Checks if a square is under attack by the opponent
     */
    private fun isSquareUnderAttack(
        position: ChessPosition, 
        board: Array<Array<ChessPiece?>>, 
        gameState: ChessGameState, 
        defenderColor: PieceColor
    ): Boolean {
        val attackerColor = if (defenderColor == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
        
        // Check all opponent's pieces to see if they can attack the square
        for (row in 0..7) {
            for (col in 0..7) {
                val piece = board[row][col]
                if (piece != null && piece.color == attackerColor) {
                    // Get all squares this piece attacks
                    val moves = getValidMoves(ChessPosition(row, col), board, gameState)
                    
                    // If any move targets the position, it's under attack
                    if (moves.any { it.row == position.row && it.col == position.col }) {
                        return true
                    }
                }
            }
        }
        return false
    }
    
    
    /**
     * Checks if kingside castling is possible
     */
    private fun canCastleKingside(
        kingPosition: ChessPosition, 
        board: Array<Array<ChessPiece?>>, 
        color: PieceColor,
        gameState: ChessGameState
    ): Boolean {
        val row = kingPosition.row
        
        // 1. Check if squares between king and rook are empty
        if (board[row][5] != null || board[row][6] != null) {
            return false
        }
        
        // 2. Check if rook is in the correct position
        val rook = board[row][7]
        if (rook?.type != PieceType.ROOK || rook.color != color) {
            return false
        }
        
        // 3. Check if king is in check
        if (isSquareUnderAttack(kingPosition, board, gameState, color)) {
            return false
        }
        
        // 4. Check if king would move through or into check
        val kingPath = listOf(
            ChessPosition(row, 5),  // Square the king moves through
            ChessPosition(row, 6)   // Square the king moves to
        )
        
        for (square in kingPath) {
            if (isSquareUnderAttack(square, board, gameState, color)) {
                return false
            }
        }
        
        return true
    }
    
    /**
     * Checks if queenside castling is possible
     */
    private fun canCastleQueenside(
        kingPosition: ChessPosition, 
        board: Array<Array<ChessPiece?>>, 
        color: PieceColor,
        gameState: ChessGameState
    ): Boolean {
        val row = kingPosition.row
        
        // 1. Check if squares between king and rook are empty
        if (board[row][1] != null || board[row][2] != null || board[row][3] != null) {
            return false
        }
        
        // 2. Check if rook is in the correct position
        val rook = board[row][0]
        if (rook?.type != PieceType.ROOK || rook.color != color) {
            return false
        }
        
        // 3. Check if king is in check
        if (isSquareUnderAttack(kingPosition, board, gameState, color)) {
            return false
        }
        
        // 4. Check if king would move through or into check
        val kingPath = listOf(
            ChessPosition(row, 3),  // Square the king moves through
            ChessPosition(row, 2)   // Square the king moves to
        )
        
        for (square in kingPath) {
            if (isSquareUnderAttack(square, board, gameState, color)) {
                return false
            }
        }
        
        return true
    }
    
    /**
     * Gets the Unicode symbol for a chess piece
     */
    fun getPieceSymbol(piece: ChessPiece): String {
        return when (piece.type) {
            PieceType.KING -> if (piece.color == PieceColor.WHITE) "♔" else "♚"
            PieceType.QUEEN -> if (piece.color == PieceColor.WHITE) "♕" else "♛"
            PieceType.ROOK -> if (piece.color == PieceColor.WHITE) "♖" else "♜"
            PieceType.BISHOP -> if (piece.color == PieceColor.WHITE) "♗" else "♝"
            PieceType.KNIGHT -> if (piece.color == PieceColor.WHITE) "♘" else "♞"
            PieceType.PAWN -> if (piece.color == PieceColor.WHITE) "♙" else "♟"
        }
    }
    
    /**
     * Converts a ChessPosition to algebraic notation (e.g., "e4", "a1")
     * @param position The position to convert
     * @return The algebraic notation string
     */
    fun positionToAlgebraic(position: ChessPosition): String {
        if (!isValidPosition(position)) return ""
        val file = 'a' + position.col
        val rank = 8 - position.row
        return "$file$rank"
    }
}
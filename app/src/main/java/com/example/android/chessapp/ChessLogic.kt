package com.example.android.chessapp

object ChessLogic {
    /**
     * Gets all pseudo-legal moves for a piece at the given position
     * These moves don't account for check/checkmate conditions
     */
    fun getValidMoves(position: ChessPosition, board: Array<Array<ChessPiece?>>): List<ChessPosition> {
        val piece = board[position.row][position.col] ?: return emptyList()
        val moves = mutableListOf<ChessPosition>()

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
                    ChessPosition(position.row + 2, position.col + 1),
                    ChessPosition(position.row + 2, position.col - 1),
                    ChessPosition(position.row - 2, position.col + 1),
                    ChessPosition(position.row - 2, position.col - 1),
                    ChessPosition(position.row + 1, position.col + 2),
                    ChessPosition(position.row + 1, position.col - 2),
                    ChessPosition(position.row - 1, position.col + 2),
                    ChessPosition(position.row - 1, position.col - 2)
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
                                moves.add(kingMove)
                            }
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
     * Checks if the given position is within the bounds of the chess board
     */
    private fun isValidPosition(position: ChessPosition): Boolean {
        return position.row in 0..7 && position.col in 0..7
    }

    /**
     * Converts a board position to algebraic notation (e.g., a1, h8)
     */
    fun positionToAlgebraic(position: ChessPosition): String {
        return "${('a' + position.col)}${8 - position.row}"
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
}
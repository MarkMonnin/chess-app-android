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
     * @param includeCastling Whether to include castling moves
     * @return List of valid target positions
     */
    fun getValidMoves(
        position: ChessPosition,
        board: Array<Array<ChessPiece?>>,
        gameState: ChessGameState? = null,
        validateChecks: Boolean = true,
        includeCastling: Boolean = true
    ): List<ChessPosition> {
        val piece = board[position.row][position.col] ?: return emptyList()
        val moves = mutableListOf<ChessPosition>()

        when (piece.type) {
            PieceType.PAWN -> {
                moves.addAll(getPawnMoves(position, board, gameState))
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
                    ChessPosition(position.row - 2, position.col + 1),
                    ChessPosition(position.row - 2, position.col - 1),
                    ChessPosition(position.row + 2, position.col + 1),
                    ChessPosition(position.row + 2, position.col - 1),
                    ChessPosition(position.row - 1, position.col + 2),
                    ChessPosition(position.row + 1, position.col + 2),
                    ChessPosition(position.row - 1, position.col - 2),
                    ChessPosition(position.row + 1, position.col - 2)
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

                // Castling
                if (includeCastling && gameState != null) {
                    if (canCastleKingside(position, board, piece.color, gameState)) {
                        moves.add(ChessPosition(position.row, position.col + 2))
                    }
                    if (canCastleQueenside(position, board, piece.color, gameState)) {
                        moves.add(ChessPosition(position.row, position.col - 2))
                    }
                }
            }
        }

        if (validateChecks && gameState != null) {
            val validator = IncrementalMoveValidator()
            val boardCopy = board.map { it.clone() }.toTypedArray() // Use a copy for validation
            return moves.filter { move ->
                val chessMove = ChessMove(position, move, piece, board[move.row][move.col])
                validator.isMoveLegal(chessMove, boardCopy, gameState)
            }
        }

        return moves
    }

    /**
     * Gets all possible moves for a piece, including promotion moves for pawns
     * This returns ChessMove objects instead of just positions to handle promotion
     */
    fun getPossibleMoves(
        position: ChessPosition,
        board: Array<Array<ChessPiece?>>,
        gameState: ChessGameState? = null,
        validateChecks: Boolean = true
    ): List<ChessMove> {
        val piece = board[position.row][position.col] ?: return emptyList()
        val moves = mutableListOf<ChessMove>()
        val validator = IncrementalMoveValidator()

        // For non-pawn pieces, convert positions to moves
        if (piece.type != PieceType.PAWN) {
            val positions = getValidMoves(position, board, gameState, validateChecks)
            positions.forEach { targetPos ->
                val capturedPiece = board[targetPos.row][targetPos.col]
                moves.add(ChessMove(position, targetPos, piece, capturedPiece))
            }
            return moves
        }

        // Special handling for pawns to include promotion moves
        val direction = if (piece.color == PieceColor.WHITE) -1 else 1
        val startRow = if (piece.color == PieceColor.WHITE) 6 else 1
        val promotionRank = if (piece.color == PieceColor.WHITE) 0 else 7

        // Move forward
        val oneStep = ChessPosition(position.row + direction, position.col)
        if (isValidPosition(oneStep) && board[oneStep.row][oneStep.col] == null) {
            if (oneStep.row == promotionRank) {
                // Promotion moves - create one move for each possible promotion piece
                val promotionPieces = listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)
                promotionPieces.forEach { promotionType ->
                    val move = ChessMove(position, oneStep, piece, null, promotionType)
                    if (!validateChecks || validator.isMoveLegal(move, board, gameState!!)) {
                        moves.add(move)
                    }
                }
            } else {
                // Regular pawn move
                val move = ChessMove(position, oneStep, piece, null)
                if (!validateChecks || validator.isMoveLegal(move, board, gameState!!)) {
                    moves.add(move)
                }

                // Two steps from starting position
                if (position.row == startRow) {
                    val twoSteps = ChessPosition(position.row + 2 * direction, position.col)
                    if (isValidPosition(twoSteps) && board[twoSteps.row][twoSteps.col] == null) {
                        val doubleMove = ChessMove(position, twoSteps, piece, null)
                        if (!validateChecks || validator.isMoveLegal(doubleMove, board, gameState!!)) {
                            moves.add(doubleMove)
                        }
                    }
                }
            }
        }

        // Capture diagonally
        listOf(-1, 1).forEach { colOffset ->
            val capturePos = ChessPosition(position.row + direction, position.col + colOffset)
            if (isValidPosition(capturePos)) {
                val targetPiece = board[capturePos.row][capturePos.col]
                if (targetPiece != null && targetPiece.color != piece.color) {
                    if (capturePos.row == promotionRank) {
                        // Promotion capture - create one move for each possible promotion piece
                        val promotionPieces = listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)
                        promotionPieces.forEach { promotionType ->
                            val move = ChessMove(position, capturePos, piece, targetPiece, promotionType)
                            if (!validateChecks || validator.isMoveLegal(move, board, gameState!!)) {
                                moves.add(move)
                            }
                        }
                    } else {
                        // Regular capture
                        val move = ChessMove(position, capturePos, piece, targetPiece)
                        if (!validateChecks || validator.isMoveLegal(move, board, gameState!!)) {
                            moves.add(move)
                        }
                    }
                }
            }
        }

        // En passant capture
        if (gameState != null && gameState.enPassantTarget != null) {
            val enPassantTarget = gameState.enPassantTarget!!
            listOf(-1, 1).forEach { colOffset ->
                val capturePos = ChessPosition(position.row + direction, position.col + colOffset)
                if (capturePos.row == enPassantTarget.row && capturePos.col == enPassantTarget.col) {
                    // The captured pawn is at the en passant target's original position
                    val capturedPawnPos = ChessPosition(enPassantTarget.row - direction, enPassantTarget.col)
                    val capturedPawn = board[capturedPawnPos.row][capturedPawnPos.col]
                    val move = ChessMove(position, capturePos, piece, capturedPawn)
                    if (!validateChecks || validator.isMoveLegal(move, board, gameState)) {
                        moves.add(move)
                    }
                }
            }
        }

        return moves
    }

    /**
     * Checks if a pawn move results in promotion
     */
    fun isPawnPromotion(move: ChessMove, piece: ChessPiece): Boolean {
        if (piece.type != PieceType.PAWN) return false

        val promotionRank = if (piece.color == PieceColor.WHITE) 0 else 7
        return move.to.row == promotionRank
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
    private fun getPawnMoves(position: ChessPosition, board: Array<Array<ChessPiece?>>, gameState: ChessGameState?): List<ChessPosition> {
        val piece = board[position.row][position.col] ?: return emptyList()
        val moves = mutableListOf<ChessPosition>()
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

        // En passant capture
        gameState?.enPassantTarget?.let { enPassantTarget ->
            if (enPassantTarget.row == position.row + direction && (enPassantTarget.col == position.col - 1 || enPassantTarget.col == position.col + 1)) {
                moves.add(enPassantTarget)
            }
        }
        return moves
    }

    private fun getPawnAttackMoves(position: ChessPosition, color: PieceColor): List<ChessPosition> {
        val moves = mutableListOf<ChessPosition>()
        val direction = if (color == PieceColor.WHITE) -1 else 1

        // Diagonal attacks
        listOf(-1, 1).forEach { colOffset ->
            val attackPos = ChessPosition(position.row + direction, position.col + colOffset)
            if (isValidPosition(attackPos)) {
                moves.add(attackPos)
            }
        }
        return moves
    }

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
    fun isSquareUnderAttack(
        position: ChessPosition,
        board: Array<Array<ChessPiece?>>,
        gameState: ChessGameState,
        defenderColor: PieceColor
    ): Boolean {
        val validator = IncrementalMoveValidator()
        return validator.isKingInCheck(position, defenderColor, board)
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

        // 1. Use the game state to check if castling is even an option.
        val canCastle = if (color == PieceColor.WHITE) gameState.whiteCanCastleKingside else gameState.blackCanCastleKingside
        if (!canCastle) {
            return false
        }

        // 2. Check if squares between king and rook are empty
        if (board[row][5] != null || board[row][6] != null) {
            return false
        }

        // 3. Check if the king is in check or would pass through or into check.
        val tempGameState = gameState.copy(currentPlayer = color.opposite())
        if (isSquareUnderAttack(kingPosition, board, tempGameState, color)) return false
        if (isSquareUnderAttack(ChessPosition(row, 5), board, tempGameState, color)) return false
        if (isSquareUnderAttack(ChessPosition(row, 6), board, tempGameState, color)) return false

        // If all checks pass, castling is legal.
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

        // 1. Use the game state to check if castling is even an option.
        val canCastle = if (color == PieceColor.WHITE) gameState.whiteCanCastleQueenside else gameState.blackCanCastleQueenside
        if (!canCastle) {
            return false
        }

        // 2. Check if squares between king and rook are empty
        if (board[row][1] != null || board[row][2] != null || board[row][3] != null) {
            return false
        }

        // 3. Check if the king is in check or would pass through or into check.
        val tempGameState = gameState.copy(currentPlayer = color.opposite())
        if (isSquareUnderAttack(kingPosition, board, tempGameState, color)) return false
        if (isSquareUnderAttack(ChessPosition(row, 3), board, tempGameState, color)) return false
        if (isSquareUnderAttack(ChessPosition(row, 2), board, tempGameState, color)) return false

        // If all checks pass, castling is legal.
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
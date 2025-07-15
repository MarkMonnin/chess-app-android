package com.example.android.chessapp

import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility class for working with PGN (Portable Game Notation) format
 * Redesigned for better accuracy and maintainability
 */
object PgnUtils {

    /**
     * Represents a complete move context needed for PGN generation
     */
    data class MoveContext(
        val move: ChessMove,
        val boardBefore: List<List<ChessPiece?>>, // Changed from Array<Array<ChessPiece?>>
        val boardAfter: List<List<ChessPiece?>>,  // Changed from Array<Array<ChessPiece?>>
        val legalMoves: List<ChessMove>,
        val isCheck: Boolean,
        val isCheckmate: Boolean,
        val isStalemate: Boolean,
        val capturedPiece: ChessPiece?,
        val isEnPassant: Boolean = false,
        val moveNumber: Int,
        val isWhiteMove: Boolean
    )

    /**
     * Game metadata for PGN headers
     */
    data class GameMetadata(
        val event: String = "Casual Game",
        val site: String = "",
        val date: String = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(Date()),
        val round: String = "1",
        val whitePlayer: String = "White",
        val blackPlayer: String = "Black",
        val result: GameResult = GameResult.ONGOING
    )

    /**
     * Represents the final result of a chess game
     */
    enum class GameResult(val pgnNotation: String) {
        WHITE_WINS("1-0"),
        BLACK_WINS("0-1"),
        DRAW("1/2-1/2"),
        ONGOING("*")
    }

    /**
     * Converts a move to proper PGN notation
     */
    private fun moveToAlgebraicNotation(context: MoveContext): String {
        val move = context.move

        // Handle castling first
        if (move.piece.type == PieceType.KING) {
            val fileDiff = move.to.col - move.from.col
            when (fileDiff) {
                2 -> return "O-O" + getCheckNotation(context)
                -2 -> return "O-O-O" + getCheckNotation(context)
            }
        }

        val pieceSymbol = getPieceSymbol(move.piece.type)
        val disambiguation = getDisambiguation(context)
        val capture = if (context.capturedPiece != null || context.isEnPassant) "x" else ""
        val destination = squareToAlgebraic(move.to)
        val promotion = getPromotionNotation(move.promotionPiece)
        val checkNotation = getCheckNotation(context)

        return "$pieceSymbol$disambiguation$capture$destination$promotion$checkNotation"
    }

    /**
     * Gets the piece symbol for PGN notation
     */
    private fun getPieceSymbol(pieceType: PieceType): String {
        return when (pieceType) {
            PieceType.PAWN -> ""
            PieceType.KNIGHT -> "N"
            PieceType.BISHOP -> "B"
            PieceType.ROOK -> "R"
            PieceType.QUEEN -> "Q"
            PieceType.KING -> "K"
        }
    }

    /**
     * Determines if disambiguation is needed and returns the appropriate notation
     */
    private fun getDisambiguation(context: MoveContext): String {
        val move = context.move

        // Pawns use file for captures, Kings never need disambiguation
        if (move.piece.type == PieceType.PAWN) {
            return if (context.capturedPiece != null || context.isEnPassant) {
                ('a' + move.from.col).toString()
            } else ""
        }

        if (move.piece.type == PieceType.KING) return ""

        // Find other pieces of the same type that can move to the same square
        val ambiguousMoves = context.legalMoves.filter { otherMove ->
            otherMove != move &&
                    otherMove.piece.type == move.piece.type &&
                    otherMove.piece.color == move.piece.color &&
                    otherMove.to == move.to
        }

        if (ambiguousMoves.isEmpty()) return ""

        // Check if file disambiguation is sufficient
        val sameFile = ambiguousMoves.any { it.from.col == move.from.col }
        if (!sameFile) {
            return ('a' + move.from.col).toString()
        }

        // Check if rank disambiguation is sufficient
        val sameRank = ambiguousMoves.any { it.from.row == move.from.row }
        if (!sameRank) {
            return (8 - move.from.row).toString()
        }

        // Need both file and rank
        return "${('a' + move.from.col)}${8 - move.from.row}"
    }

    /**
     * Converts a square position to algebraic notation
     */
    private fun squareToAlgebraic(position: ChessPosition): String {
        return "${('a' + position.col)}${8 - position.row}"
    }

    /**
     * Gets promotion notation if the move includes promotion
     */
    private fun getPromotionNotation(promotion: PieceType?): String {
        return if (promotion != null) {
            "=" + getPieceSymbol(promotion)
        } else {
            ""
        }
    }

    /**
     * Gets check/checkmate notation
     */
    private fun getCheckNotation(context: MoveContext): String {
        return when {
            context.isCheckmate -> "#"
            context.isCheck -> "+"
            else -> ""
        }
    }

    /**
     * Exports a complete game to PGN format
     */
    fun exportGameToPgn(
        moveContexts: List<MoveContext>,
        metadata: GameMetadata
    ): String {
        val pgn = StringBuilder()

        // Add PGN headers
        pgn.appendLine("[Event \"${metadata.event}\"]")
        pgn.appendLine("[Site \"${metadata.site}\"]")
        pgn.appendLine("[Date \"${metadata.date}\"]")
        pgn.appendLine("[Round \"${metadata.round}\"]")
        pgn.appendLine("[White \"${metadata.whitePlayer}\"]")
        pgn.appendLine("[Black \"${metadata.blackPlayer}\"]")
        pgn.appendLine("[Result \"${metadata.result.pgnNotation}\"]")
        pgn.appendLine()

        // Generate move text
        val moveText = generateMoveText(moveContexts, metadata.result)
        pgn.append(moveText)

        return pgn.toString()
    }

    fun exportGameToPgn(
        gameState: ChessGameState,
        whitePlayer: String = "White",
        blackPlayer: String = "Black",
        event: String = "Casual Game",
        site: String = ""
    ): String {
        val metadata = GameMetadata(
            whitePlayer = whitePlayer,
            blackPlayer = blackPlayer,
            event = event,
            site = site
        )
        return exportFromGameState(gameState, metadata)
    }

    /**
     * Generates the move text section of a PGN
     */
    private fun generateMoveText(
        moveContexts: List<MoveContext>,
        result: GameResult
    ): String {
        val moves = mutableListOf<String>()
        var currentMoveNumber = 1

        for (context in moveContexts) {
            if (context.isWhiteMove) {
                moves.add("$currentMoveNumber.")
            }

            val algebraicMove = moveToAlgebraicNotation(context)
            moves.add(algebraicMove)

            // Increment move number after black's move
            if (!context.isWhiteMove) {
                currentMoveNumber++
            }
        }

        // Format moves with proper line breaks (PGN standard is ~80 characters per line)
        return formatMovesWithLineBreaks(moves, result)
    }

    /**
     * Formats moves with appropriate line breaks for readability
     */
    private fun formatMovesWithLineBreaks(
        moves: List<String>,
        result: GameResult
    ): String {
        val formattedMoves = StringBuilder()
        var lineLength = 0
        val maxLineLength = 80

        for (token in moves) {
            val tokenLength = token.length

            // Check if adding this token would exceed line length
            if (lineLength + tokenLength + 1 > maxLineLength && lineLength > 0) {
                formattedMoves.appendLine()
                lineLength = 0
            } else if (lineLength > 0) {
                formattedMoves.append(" ")
                lineLength++
            }

            formattedMoves.append(token)
            lineLength += tokenLength
        }

        // Add game result
        if (moves.isNotEmpty()) {
            formattedMoves.append(" ")
        }
        formattedMoves.append(result.pgnNotation)

        return formattedMoves.toString()
    }

    /**
     * Convenience method for exporting from a game state
     * This assumes you have a way to reconstruct move contexts from game state
     */
    fun exportFromGameState(
        gameState: ChessGameState,
        metadata: GameMetadata
    ): String {
        val moveContexts = reconstructMoveContexts(gameState)
        val finalMetadata = metadata.copy(
            result = determineGameResult(gameState)
        )

        return exportGameToPgn(moveContexts, finalMetadata)
    }

    /**
     * Reconstructs move contexts from game state
     * This is a placeholder - you'll need to implement based on your ChessGameState structure
     */
    private fun reconstructMoveContexts(gameState: ChessGameState): List<MoveContext> {
        // This is a simplified reconstruction - in practice, you'd need to:
        // 1. Replay the game move by move
        // 2. Calculate legal moves at each position
        // 3. Determine check/checkmate status
        // 4. Build proper MoveContext objects

        return gameState.moveHistory.mapIndexed { index, record ->
            val boardAfter = if (index < gameState.moveHistory.size - 1) {
                gameState.moveHistory[index + 1].previousState.board
            } else {
                gameState.board // Current board state
            }
            MoveContext(
                move = record.move,
                boardBefore = record.previousState.board.map { row -> row.toList() },
                boardAfter = boardAfter.map { row -> row.toList() },
                legalMoves = emptyList(), // You'd need to calculate this
                isCheck = false, // You'd need to calculate this
                isCheckmate = index == gameState.moveHistory.size - 1 && gameState.isCheckmate,
                isStalemate = index == gameState.moveHistory.size - 1 && gameState.isStalemate,
                capturedPiece = record.capturedPiece,
                isEnPassant = false, // You'd need to determine this
                moveNumber = (index / 2) + 1,
                isWhiteMove = record.move.piece.color == PieceColor.WHITE
            )
        }
    }

    /**
     * Determines the game result from game state
     */
    private fun determineGameResult(gameState: ChessGameState): GameResult {
        return when {
            gameState.isCheckmate -> {
                if (gameState.currentPlayer == PieceColor.WHITE) GameResult.BLACK_WINS
                else GameResult.WHITE_WINS
            }
            gameState.isStalemate ||
                    gameState.isDrawByFiftyMoveRule ||
                    gameState.isDrawByInsufficientMaterial -> GameResult.DRAW
            else -> GameResult.ONGOING
        }
    }

    /**
     * Validates PGN format (basic validation)
     */
    fun validatePgn(pgn: String): Boolean {
        val lines = pgn.split('\n')
        var headerSection = true
        var hasRequiredHeaders = false

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) {
                headerSection = false
                continue
            }

            if (headerSection) {
                if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                    if (trimmed.contains("Event") || trimmed.contains("Site") ||
                        trimmed.contains("Date") || trimmed.contains("White") ||
                        trimmed.contains("Black") || trimmed.contains("Result")) {
                        hasRequiredHeaders = true
                    }
                }
            }
        }

        return hasRequiredHeaders
    }
}
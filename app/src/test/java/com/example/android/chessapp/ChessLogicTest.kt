package com.example.android.chessapp

import org.junit.Assert.*
import org.junit.Test

class ChessLogicTest {

    private fun getInitialState(): ChessGameState {
        val board = Array(8) { Array<ChessPiece?>(8) { null } }
        // Setup standard chess starting position
        // White pieces
        board[7][0] = ChessPiece(PieceType.ROOK, PieceColor.WHITE)
        board[7][1] = ChessPiece(PieceType.KNIGHT, PieceColor.WHITE)
        board[7][2] = ChessPiece(PieceType.BISHOP, PieceColor.WHITE)
        board[7][3] = ChessPiece(PieceType.QUEEN, PieceColor.WHITE)
        board[7][4] = ChessPiece(PieceType.KING, PieceColor.WHITE)
        board[7][5] = ChessPiece(PieceType.BISHOP, PieceColor.WHITE)
        board[7][6] = ChessPiece(PieceType.KNIGHT, PieceColor.WHITE)
        board[7][7] = ChessPiece(PieceType.ROOK, PieceColor.WHITE)
        for (i in 0..7) {
            board[6][i] = ChessPiece(PieceType.PAWN, PieceColor.WHITE)
        }

        // Black pieces
        board[0][0] = ChessPiece(PieceType.ROOK, PieceColor.BLACK)
        board[0][1] = ChessPiece(PieceType.KNIGHT, PieceColor.BLACK)
        board[0][2] = ChessPiece(PieceType.BISHOP, PieceColor.BLACK)
        board[0][3] = ChessPiece(PieceType.QUEEN, PieceColor.BLACK)
        board[0][4] = ChessPiece(PieceType.KING, PieceColor.BLACK)
        board[0][5] = ChessPiece(PieceType.BISHOP, PieceColor.BLACK)
        board[0][6] = ChessPiece(PieceType.KNIGHT, PieceColor.BLACK)
        board[0][7] = ChessPiece(PieceType.ROOK, PieceColor.BLACK)
        for (i in 0..7) {
            board[1][i] = ChessPiece(PieceType.PAWN, PieceColor.BLACK)
        }

        return ChessGameState(board = board)
    }

    @Test
    fun `test white can castle kingside`() {
        // Arrange
        val board = getInitialState().board
        board[7][5] = null // Clear bishop
        board[7][6] = null // Clear knight
        board[6][5] = null // Clear pawn at f2
        val gameState = ChessGameState(board = board)

        // Act
        val moves = ChessLogic.getValidMoves(ChessPosition(7, 4), gameState.board, gameState)
        val canCastle = moves.any { it == ChessPosition(7, 6) }

        // Assert
        assertTrue("White should be able to castle kingside", canCastle)
    }

    @Test
    fun `test white cannot castle kingside if king moved`() {
        // Arrange
        val board = getInitialState().board
        board[7][5] = null
        board[7][6] = null
        var gameState = ChessGameState(board = board)

        // Act: Move king forth and back
        val king = gameState.board[7][4]!!
        val move1 = ChessMove(ChessPosition(7, 4), ChessPosition(7, 5), king)
        gameState = gameState.applyMove(move1)
        val movedKing = gameState.board[7][5]!!
        val move2 = ChessMove(ChessPosition(7, 5), ChessPosition(7, 4), movedKing)
        gameState = gameState.applyMove(move2)

        val moves = ChessLogic.getValidMoves(ChessPosition(7, 4), gameState.board, gameState)
        val canCastle = moves.any { it == ChessPosition(7, 6) }

        // Assert
        assertFalse("White should not be able to castle after king has moved", canCastle)
        assertFalse("whiteCanCastleKingside flag should be false", gameState.whiteCanCastleKingside)
    }

    @Test
    fun `test white cannot castle kingside if rook moved`() {
        // Arrange
        val board = getInitialState().board
        board[7][5] = null
        board[7][6] = null
        var gameState = ChessGameState(board = board)

        // Act: Move rook forth and back
        val rook = gameState.board[7][7]!!
        val move1 = ChessMove(ChessPosition(7, 7), ChessPosition(7, 6), rook)
        gameState = gameState.applyMove(move1)
        val movedRook = gameState.board[7][6]!!
        val move2 = ChessMove(ChessPosition(7, 6), ChessPosition(7, 7), movedRook)
        gameState = gameState.applyMove(move2)

        val moves = ChessLogic.getValidMoves(ChessPosition(7, 4), gameState.board, gameState)
        val canCastle = moves.any { it == ChessPosition(7, 6) }

        // Assert
        assertFalse("White should not be able to castle after rook has moved", canCastle)
        assertFalse("whiteCanCastleKingside flag should be false", gameState.whiteCanCastleKingside)
    }

    @Test
    fun `test white cannot castle through check`() {
        // Arrange
        val board = getInitialState().board
        board[7][5] = null
        board[7][6] = null
        board[3][5] = ChessPiece(PieceType.ROOK, PieceColor.BLACK) // Attacking f1 square
        val gameState = ChessGameState(board = board)

        // Act
        val moves = ChessLogic.getValidMoves(ChessPosition(7, 4), gameState.board, gameState)
        val canCastle = moves.any { it == ChessPosition(7, 6) }

        // Assert
        assertFalse("White should not be able to castle through an attacked square", canCastle)
    }

     @Test
    fun `test undo and redo move`() {
        // Arrange
        var gameState = getInitialState()
        val originalState = getInitialState()

        // Act: Make a move
        val piece = gameState.board[6][4]!!
        val move = ChessMove(ChessPosition(6, 4), ChessPosition(4, 4), piece)
        gameState = gameState.applyMove(move)

        // Assert move was made
        assertNotEquals(originalState.board, gameState.board)

        // Act: Undo the move
        val undoneState = gameState.undoMove()

        // Assert state is reverted
        assertNotNull("Undone state should not be null", undoneState)
        assertArrayEquals("Board should be reverted after undo", originalState.board, undoneState!!.board)
        assertEquals("Current player should be white after undo", PieceColor.WHITE, undoneState.currentPlayer)

        // Act: Redo the move
        val redoneState = undoneState.redoMove()

        // Assert state is restored
        assertNotNull("Redone state should not be null", redoneState)
        assertArrayEquals("Board should be restored after redo", gameState.board, redoneState!!.board)
        assertEquals("Current player should be black after redo", PieceColor.BLACK, redoneState.currentPlayer)
    }

    @Test
    fun `test checkmate`() {
        // A real checkmate setup (Scholar's Mate)
        val scholarBoard = getInitialState().board
        var scholarState = ChessGameState(board = scholarBoard)
        
        var piece = scholarState.board[6][4]!!
        scholarState = scholarState.applyMove(ChessMove(ChessPosition(6, 4), ChessPosition(4, 4), piece)) // e4
        piece = scholarState.board[1][4]!!
        scholarState = scholarState.applyMove(ChessMove(ChessPosition(1, 4), ChessPosition(3, 4), piece)) // e5
        piece = scholarState.board[7][5]!!
        scholarState = scholarState.applyMove(ChessMove(ChessPosition(7, 5), ChessPosition(4, 2), piece)) // Bc4
        piece = scholarState.board[0][6]!!
        scholarState = scholarState.applyMove(ChessMove(ChessPosition(0, 6), ChessPosition(2, 5), piece)) // Nc6
        piece = scholarState.board[7][3]!!
        scholarState = scholarState.applyMove(ChessMove(ChessPosition(7, 3), ChessPosition(3, 7), piece)) // Qh5
        piece = scholarState.board[1][5]!!
        scholarState = scholarState.applyMove(ChessMove(ChessPosition(1, 5), ChessPosition(2, 5), piece)) // f6 (mistake)
        piece = scholarState.board[3][7]!!
        scholarState = scholarState.applyMove(ChessMove(ChessPosition(3, 7), ChessPosition(3, 4), piece, capturedPiece=scholarState.board[3][4])) // Qxe5#

        // Assert
        assertTrue("Game should be in checkmate", scholarState.isCheckmate)
    }

    @Test
    fun `test stalemate`() {
        // Arrange: Stalemate position
        val board = Array(8) { Array<ChessPiece?>(8) { null } }
        board[0][7] = ChessPiece(PieceType.KING, PieceColor.BLACK)
        board[2][6] = ChessPiece(PieceType.KING, PieceColor.WHITE)
        board[1][5] = ChessPiece(PieceType.QUEEN, PieceColor.WHITE)
        val gameState = ChessGameState(board = board, currentPlayer = PieceColor.BLACK)

        // Assert
        assertTrue("Game should be in stalemate", gameState.isStalemate)
        assertFalse("Game should not be in checkmate", gameState.isCheckmate)
    }

    @Test
    fun `test en passant capture`() {
        // Arrange
        var gameState = getInitialState()
        var piece = gameState.board[6][4]!!
        gameState = gameState.applyMove(ChessMove(ChessPosition(6, 4), ChessPosition(4, 4), piece)) // White e4
        piece = gameState.board[1][0]!!
        gameState = gameState.applyMove(ChessMove(ChessPosition(1, 0), ChessPosition(3, 0), piece)) // Black a5
        piece = gameState.board[4][4]!!
        gameState = gameState.applyMove(ChessMove(ChessPosition(4, 4), ChessPosition(3, 4), piece)) // White e5
        piece = gameState.board[1][3]!!
        gameState = gameState.applyMove(ChessMove(ChessPosition(1, 3), ChessPosition(3, 3), piece)) // Black d5, creating en passant target

        // Act
        val moves = ChessLogic.getValidMoves(ChessPosition(3, 4), gameState.board, gameState)
        val canCaptureEnPassant = moves.any { it == ChessPosition(2, 3) }

        // Assert
        assertTrue("White pawn should be able to capture en passant", canCaptureEnPassant)

        // Act: Perform en passant
        piece = gameState.board[3][4]!!
        val enPassantMove = ChessMove(ChessPosition(3, 4), ChessPosition(2, 3), piece)
        val nextState = gameState.applyMove(enPassantMove)

        // Assert
        assertNull("Black pawn should be captured", nextState.board[3][3])
        assertNotNull("White pawn should be on the new square", nextState.board[2][3])
    }

    @Test
    fun `test pawn promotion`() {
        // Arrange
        val board = Array(8) { Array<ChessPiece?>(8) { null } }
        board[1][0] = ChessPiece(PieceType.PAWN, PieceColor.WHITE)
        board[6][7] = ChessPiece(PieceType.PAWN, PieceColor.BLACK)
        board[7][4] = ChessPiece(PieceType.KING, PieceColor.WHITE) // Add kings to avoid false end-game states
        board[0][4] = ChessPiece(PieceType.KING, PieceColor.BLACK)
        var gameState = ChessGameState(board = board)

        // Act: Promote white pawn
        var piece = gameState.board[1][0]!!
        val whitePromotionMove = ChessMove(ChessPosition(1, 0), ChessPosition(0, 0), piece, promotionPiece = PieceType.QUEEN)
        var nextState = gameState.applyMove(whitePromotionMove)

        // Assert
        assertEquals("White pawn should be promoted to Queen", PieceType.QUEEN, nextState.board[0][0]?.type)

        // Act: Promote black pawn (it's now black's turn)
        piece = nextState.board[6][7]!!
        val blackPromotionMove = ChessMove(ChessPosition(6, 7), ChessPosition(7, 7), piece, promotionPiece = PieceType.ROOK)
        nextState = nextState.applyMove(blackPromotionMove)

        // Assert
        assertEquals("Black pawn should be promoted to Rook", PieceType.ROOK, nextState.board[7][7]?.type)
    }
}

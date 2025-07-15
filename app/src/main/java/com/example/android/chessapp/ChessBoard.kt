package com.example.android.chessapp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.platform.LocalConfiguration
import kotlinx.coroutines.*
import kotlinx.coroutines.delay
import android.util.Log

enum class GameMode {
    HUMAN_VS_HUMAN,
    HUMAN_VS_AI
}

@Composable
fun ChessBoard(
    modifier: Modifier = Modifier,
    gameMode: GameMode = GameMode.HUMAN_VS_AI,
    resetKey: Any? = null
) {
    key(resetKey) {
        var showMoveList by remember { mutableStateOf(false) }
        var showPgnDialog by remember { mutableStateOf(false) }
        var pgnText by remember { mutableStateOf("") }

        val configuration = LocalConfiguration.current
        val screenHeight = configuration.screenHeightDp.dp
        val screenWidth = configuration.screenWidthDp.dp

        // Estimate space taken by other UI elements
        val topBarHeight = 56.dp
        val bottomButtonsHeight = 48.dp
        val textLabelsHeight = 60.dp
        val verticalPadding = 28.dp

        val availableHeightForBoard = screenHeight - topBarHeight - bottomButtonsHeight - textLabelsHeight - verticalPadding
        val squareSize = (availableHeightForBoard / 8).coerceAtMost(screenWidth / 8)

        // Game state
        val initialBoard = remember {
            Array(8) { row ->
                Array(8) { col ->
                    when (row) {
                        0 -> when (col) {
                            0, 7 -> ChessPiece(PieceType.ROOK, PieceColor.BLACK)
                            1, 6 -> ChessPiece(PieceType.KNIGHT, PieceColor.BLACK)
                            2, 5 -> ChessPiece(PieceType.BISHOP, PieceColor.BLACK)
                            3 -> ChessPiece(PieceType.QUEEN, PieceColor.BLACK)
                            4 -> ChessPiece(PieceType.KING, PieceColor.BLACK)
                            else -> null
                        }
                        1 -> ChessPiece(PieceType.PAWN, PieceColor.BLACK)
                        6 -> ChessPiece(PieceType.PAWN, PieceColor.WHITE)
                        7 -> when (col) {
                            0, 7 -> ChessPiece(PieceType.ROOK, PieceColor.WHITE)
                            1, 6 -> ChessPiece(PieceType.KNIGHT, PieceColor.WHITE)
                            2, 5 -> ChessPiece(PieceType.BISHOP, PieceColor.WHITE)
                            3 -> ChessPiece(PieceType.QUEEN, PieceColor.WHITE)
                            4 -> ChessPiece(PieceType.KING, PieceColor.WHITE)
                            else -> null
                        }
                        else -> null
                    }
                }
            }
        }

        var gameState by remember { mutableStateOf(ChessGameState(board = initialBoard)) }
        var selectedPosition by remember { mutableStateOf<ChessPosition?>(null) }
        var validMoves by remember { mutableStateOf<List<ChessPosition>>(emptyList()) }
        var promotionMove by remember { mutableStateOf<ChessMove?>(null) }
        var showPromotionDialog by remember { mutableStateOf(false) }
        var showRedoWarning by remember { mutableStateOf(false) }
        var isAITurn by remember { mutableStateOf(false) }

        // Effect to dismiss the redo warning after a few seconds
        LaunchedEffect(showRedoWarning) {
            if (showRedoWarning) {
                delay(3000) // Show warning for 3 seconds
                showRedoWarning = false
            }
        }

        val coroutineScope = rememberCoroutineScope()
        val chessAI = remember { OptimizedChessAI() }

        // Promotion Dialog
        if (showPromotionDialog && promotionMove != null) {
            AlertDialog(
                onDismissRequest = { /* Dialog is not dismissible without selection */ },
                title = { Text("Promote Pawn to:") },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val pieceColor = promotionMove!!.piece.color
                        val promotionOptions = listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)
                        promotionOptions.forEach { pieceType ->
                            Button(
                                onClick = {
                                    promotionMove = promotionMove!!.withPromotion(pieceType)
                                    showPromotionDialog = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = when (pieceType) {
                                        PieceType.QUEEN -> if (pieceColor == PieceColor.WHITE) "♕ Queen" else "♛ Queen"
                                        PieceType.ROOK -> if (pieceColor == PieceColor.WHITE) "♖ Rook" else "♜ Rook"
                                        PieceType.BISHOP -> if (pieceColor == PieceColor.WHITE) "♗ Bishop" else "♝ Bishop"
                                        PieceType.KNIGHT -> if (pieceColor == PieceColor.WHITE) "♘ Knight" else "♞ Knight"
                                        else -> ""
                                    },
                                    fontSize = 20.sp
                                )
                            }
                        }
                    }
                },
                confirmButton = { /* No confirm button, selection is made via piece buttons */ }
            )
        }

        // PGN Export Dialog
        if (showPgnDialog) {
            AlertDialog(
                onDismissRequest = { showPgnDialog = false },
                title = { Text("PGN Export") },
                text = {
                    Column {
                        Text("Copy this PGN notation:")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = pgnText,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.LightGray.copy(alpha = 0.3f))
                                .padding(8.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = { showPgnDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }

        // Update game status text based on current state
        val gameStatus = when {
            gameState.isCheckmate -> "Checkmate! ${gameState.currentPlayer.opposite().toString().replaceFirstChar { it.uppercase() }} wins!"
            gameState.isStalemate -> "Stalemate! Game is a draw."
            gameState.isCheck -> "${gameState.currentPlayer.toString().replaceFirstChar { it.uppercase() }} is in check!"
            else -> "${gameState.currentPlayer.toString().replaceFirstChar { it.uppercase() }}'s turn"
        }

        // AI move handler
        val handleAiMove = { move: ChessMove ->
            // Apply the move to the current game state
            val newState = gameState.applyMove(move)
            gameState = newState
            selectedPosition = null
            validMoves = emptyList()
        }

        // Handle promotion move execution after dialog selection
        LaunchedEffect(showPromotionDialog) {
            if (!showPromotionDialog && promotionMove != null && promotionMove!!.promotionPiece != null) {
                val newGameState = gameState.applyMove(promotionMove!!)
                gameState = newGameState
                promotionMove = null // Clear the promotion move

                // Clear selection and valid moves
                selectedPosition = null
                validMoves = emptyList()
            }
        }

        // AI makes a move after a short delay
        LaunchedEffect(gameState.currentPlayer, isAITurn) {
            if (gameMode == GameMode.HUMAN_VS_AI && gameState.currentPlayer == PieceColor.BLACK && !isAITurn && gameState.futureMoves.isEmpty()) {
                isAITurn = true
                coroutineScope.launch(Dispatchers.Default) {
                    chessAI.makeMove(
                        board = gameState.board,
                        color = gameState.currentPlayer,
                        gameState = gameState
                    ) { aiMove ->
                        gameState = gameState.applyMove(aiMove)
                        isAITurn = false
                        selectedPosition = null
                        validMoves = emptyList()
                    }
                }
            }
        }

        // Common click handler for chess squares
        val handleSquareClick = label@{ position: ChessPosition ->
            if (isAITurn || gameState.isCheckmate || gameState.isStalemate) return@label

            // If there are undone moves, prevent new moves and show a warning
            if (gameState.futureMoves.isNotEmpty()) {
                showRedoWarning = true
                return@label
            }

            val clickedPiece = gameState.board[position.row][position.col]

            // If no piece is selected, select the piece if it's the current player's piece
            if (selectedPosition == null) {
                if (clickedPiece?.color == gameState.currentPlayer) {
                    selectedPosition = position
                    validMoves = gameState.getValidMovesOptimized(position, gameState.board)
                }
            }
            // If a piece is already selected
            else {
                // If clicking on the same piece, deselect it
                if (position == selectedPosition) {
                    selectedPosition = null
                    validMoves = emptyList()
                }
                // If clicking on a valid move, make the move
                else if (validMoves.any { it.row == position.row && it.col == position.col }) {
                    val from = selectedPosition!!
                    val pieceToMove = gameState.board[from.row][from.col]!!
                    val capturedPiece = gameState.board[position.row][position.col]
                    val move = ChessMove(from, position, pieceToMove, capturedPiece)

                    // Check if this is a pawn promotion move
                    val isPromotion = pieceToMove.type == PieceType.PAWN &&
                            ((pieceToMove.color == PieceColor.WHITE && position.row == 0) ||
                                    (pieceToMove.color == PieceColor.BLACK && position.row == 7))

                    if (isPromotion) {
                        // Show promotion dialog
                        promotionMove = move
                        showPromotionDialog = true
                    } else {
                        // Apply regular move
                        val newState = gameState.applyMove(move)
                        gameState = newState
                        promotionMove = null // Clear the promotion move
                        selectedPosition = null
                        validMoves = emptyList()

                        // If it's AI's turn and we're in AI mode, make AI move
                        if (gameMode == GameMode.HUMAN_VS_AI && newState.currentPlayer == PieceColor.BLACK) {
                            isAITurn = true
                            coroutineScope.launch(Dispatchers.Default) {
                                chessAI.makeMove(
                                    board = newState.board,
                                    color = newState.currentPlayer,
                                    gameState = newState
                                ) { aiMove ->
                                    handleAiMove(aiMove)
                                    isAITurn = false
                                }
                            }
                        }
                    }
                }
                // If clicking on another piece of the same color, select that piece
                else if (clickedPiece?.color == gameState.currentPlayer) {
                    selectedPosition = position
                    validMoves = gameState.getValidMovesOptimized(position, gameState.board)
                }
                // If clicking on an invalid square, deselect the piece
                else {
                    selectedPosition = null
                    validMoves = emptyList()
                }
            }
        }

        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Game mode indicator
            Text(
                text = when (gameMode) {
                    GameMode.HUMAN_VS_HUMAN -> "Human vs Human"
                    GameMode.HUMAN_VS_AI -> "Human vs AI"
                },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
            )

            // Game status and controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    // Undo button
                    IconButton(
                        onClick = {
                            gameState.undoMove()?.let { newState ->
                                gameState = newState
                                selectedPosition = null
                                validMoves = emptyList()
                            }
                        },
                        enabled = gameState.canUndo() && !isAITurn
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Undo last move"
                        )
                    }

                    Text(
                        text = if (gameState.currentPlayer == PieceColor.WHITE) "WHITE's turn" else "BLACK's turn",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    // Redo button
                    IconButton(
                        onClick = {
                            gameState.redoMove()?.let { newState ->
                                gameState = newState
                                selectedPosition = null
                                validMoves = emptyList()
                            }
                        },
                        enabled = gameState.canRedo() && !isAITurn
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Redo last move"
                        )
                    }
                }

                if (showRedoWarning) {
                    Text(
                        text = "Please return the board to its most recent state before making a move",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (gameState.isCheck) {
                    Text(
                        text = "CHECK!",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }

            // Determine layout based on screen orientation
            val isLandscape = LocalConfiguration.current.screenWidthDp > LocalConfiguration.current.screenHeightDp

            if (isLandscape) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left side - Chess board
                    Box(
                        modifier = Modifier
                            .width(squareSize * 8)
                            .height(squareSize * 8)
                            .padding(4.dp)
                    ) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(8),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            itemsIndexed(gameState.board.flatten()) { index, piece ->
                                val row = index / 8
                                val col = index % 8
                                val position = ChessPosition(row, col)
                                val isSelected = selectedPosition == position
                                val isValidMove = validMoves.any { it.row == row && it.col == col }
                                val isLight = (row + col) % 2 == 0

                                ChessSquare(
                                    piece = piece,
                                    position = position,
                                    isLight = isLight,
                                    isSelected = isSelected,
                                    isValidMove = isValidMove,
                                    lastMove = gameState.lastMove,
                                    onClick = { handleSquareClick(position) },
                                    squareSize = squareSize
                                )
                            }
                        }
                    }

                    // Right side content for landscape
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        // Buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Toggle Move List Button
                            Button(
                                onClick = { showMoveList = !showMoveList },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = if (showMoveList) "Hide Move List" else "Show Move List"
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = if (showMoveList) "Hide Moves" else "Show Moves")
                            }

                            // Export PGN Button
                            Button(
                                onClick = {
                                    pgnText = PgnUtils.exportGameToPgn(
                                        gameState = gameState,
                                        whitePlayer = "Player 1",
                                        blackPlayer = if (gameMode == GameMode.HUMAN_VS_AI) "AI" else "Player 2"
                                    )
                                    showPgnDialog = true
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Export PGN"
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Export PGN")
                            }
                        }

                        // Castling availability
                        val castlingRights = buildString {
                            if (gameState.whiteCanCastleKingside) append("K")
                            if (gameState.whiteCanCastleQueenside) append("Q")
                            if (gameState.blackCanCastleKingside) append("k")
                            if (gameState.blackCanCastleQueenside) append("q")
                            if (isEmpty()) append("-")
                        }

                        Text(
                            text = "Castling: $castlingRights",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                        )

                        // Move count and game info
                        Text(
                            text = "Move ${(gameState.fullMoveNumber)}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                        )

                        // Conditional content
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            if (showMoveList) {
                                MoveList(
                                    moveHistory = gameState.moveHistory,
                                    onMoveSelected = { _ ->
                                        // TODO: Implement move navigation
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Selected piece info
                                    if (selectedPosition != null) {
                                        val piece = gameState.board[selectedPosition!!.row][selectedPosition!!.col]
                                        if (piece != null) {
                                            Text(
                                                text = "Selected: ${piece.color} ${piece.type}",
                                                modifier = Modifier.padding(vertical = 8.dp)
                                            )
                                        }
                                    }

                                    // Last move info
                                    if (gameState.moveHistory.isNotEmpty()) {
                                        val lastMove = gameState.lastMove!!
                                        Text(
                                            text = "Last move: ${lastMove.piece.color} ${lastMove.piece.type} " +
                                                    "from ${'a' + lastMove.from.col}${8 - lastMove.from.row} " +
                                                    "to ${'a' + lastMove.to.col}${8 - lastMove.to.row}",
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Portrait mode
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .width(squareSize * 8)
                            .height(squareSize * 8)
                            .padding(4.dp)
                    ) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(8),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            itemsIndexed(gameState.board.flatten()) { index, piece ->
                                val row = index / 8
                                val col = index % 8
                                val position = ChessPosition(row, col)
                                val isSelected = selectedPosition == position
                                val isValidMove = validMoves.any { it.row == row && it.col == col }
                                val isLight = (row + col) % 2 == 0

                                ChessSquare(
                                    piece = piece,
                                    position = position,
                                    isLight = isLight,
                                    isSelected = isSelected,
                                    isValidMove = isValidMove,
                                    lastMove = gameState.lastMove,
                                    onClick = { handleSquareClick(position) },
                                    squareSize = squareSize
                                )
                            }
                        }
                    }

                    // Castling availability
                    val castlingRights = buildString {
                        if (gameState.whiteCanCastleKingside) append("K")
                        if (gameState.whiteCanCastleQueenside) append("Q")
                        if (gameState.blackCanCastleKingside) append("k")
                        if (gameState.blackCanCastleQueenside) append("q")
                        if (isEmpty()) append("-")
                    }

                    Text(
                        text = "Castling: $castlingRights",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                    )

                    // Move count and game info
                    Text(
                        text = "Move ${(gameState.fullMoveNumber)}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChessSquare(
    piece: ChessPiece?,
    position: ChessPosition,
    isLight: Boolean,
    isSelected: Boolean,
    isValidMove: Boolean,
    lastMove: ChessMove?,
    onClick: () -> Unit,
    squareSize: Dp
) {
    Box(
        modifier = Modifier
            .size(squareSize)
            .background(
                color = when {
                    isSelected -> Color(0xFFBBCC44)
                    isValidMove -> Color(0xFF88AA33).copy(alpha = 0.5f)
                    isLight -> Color(0xFFF0F0D0)
                    else -> Color(0xFFB58863)
                }
            )
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = Color(0xFF779900)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        piece?.let {
            val symbol = it.getPieceSymbol()
            Log.d("ChessSquare", "Piece: ${it.color} ${it.type}, Symbol: $symbol, Position: ${position.row},${position.col}")
            Text(
                text = symbol,
                fontSize = 36.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Show dot for valid move on empty square
        if (isValidMove && piece == null) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(Color.Green.copy(alpha = 0.7f), shape = CircleShape)
            )
        }
    }
}
package com.example.android.chessapp

import androidx.compose.foundation.layout.*
import com.example.android.chessapp.ui.theme.Dimensions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.animation.AnimatedContent
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Composable that displays the list of moves in the game
 */
@Composable
fun MoveList(
    moveHistory: List<ChessMoveRecord>,
    modifier: Modifier = Modifier,
    onMoveSelected: ((ChessMoveRecord) -> Unit)? = null
) {
    // Group moves into pairs (white and black moves)
    val movePairs = remember(moveHistory) {
        moveHistory.chunked(2).mapIndexed { index, moves ->
            val whiteMove = moves.firstOrNull()
            val blackMove = moves.getOrNull(1)
            MovePair(index + 1, whiteMove, blackMove)
        }
    }

    Card(
        modifier = modifier.fillMaxSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.small)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimensions.small)
        ) {
            Text(
                text = "Move History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = Dimensions.small)
            )
            
            if (movePairs.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text("No moves yet")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.small)
                ) {
                    itemsIndexed(movePairs) { _, pair ->
                        MoveRow(
                            moveNumber = pair.moveNumber,
                            whiteMove = pair.whiteMove,
                            blackMove = pair.blackMove,
                            onMoveSelected = onMoveSelected
                        )
                    }
                }
            }
        }
    }
}

/**
 * Data class representing a pair of moves (white and black) in a single turn
 */
private data class MovePair(
    val moveNumber: Int,
    val whiteMove: ChessMoveRecord?,
    val blackMove: ChessMoveRecord?
)

/**
 * Composable that displays a single row in the move list (one white and one black move)
 */
@Composable
fun MoveRow(
    moveNumber: Int,
    whiteMove: ChessMoveRecord?,
    blackMove: ChessMoveRecord?,
    onMoveSelected: ((ChessMoveRecord) -> Unit)?
) {
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Move number
        Text(
            text = "$moveNumber.",
            modifier = Modifier.padding(end = Dimensions.small),
            fontSize = 14.sp
        )
        
        // White's move
        MoveButton(
            move = whiteMove,
            onClick = { onMoveSelected?.invoke(whiteMove!!) },
            modifier = Modifier
                .weight(1f)
                .padding(end = Dimensions.small)
        )
        
        // Black's move
        MoveButton(
            move = blackMove,
            onClick = { onMoveSelected?.invoke(blackMove!!) },
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Composable that displays a single move as a button
 */
@Composable
fun MoveButton(
    move: ChessMoveRecord?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (move == null) {
        Box(
            modifier = modifier
                .height(Dimensions.extraLarge)
                .fillMaxWidth()
        )
    } else {
        val moveText = remember(move.move) {
            val pieceSymbol = when (move.move.piece.type) {
                PieceType.KING -> "K"
                PieceType.QUEEN -> "Q"
                PieceType.ROOK -> "R"
                PieceType.BISHOP -> "B"
                PieceType.KNIGHT -> "N"
                else -> ""
            }
            val captureSymbol = if (move.capturedPiece != null) "x" else ""
            val fromPos = move.move.from
            val toPos = move.move.to
            val fromFile = ('a' + fromPos.col).toString()
            val fromRank = (8 - fromPos.row).toString()
            val toFile = ('a' + toPos.col).toString()
            val toRank = (8 - toPos.row).toString()
            
            "$pieceSymbol$fromFile$fromRank$captureSymbol$toFile$toRank"
        }
        
        val haptic = LocalHapticFeedback.current
        OutlinedButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
            modifier = modifier
                .height(Dimensions.extraLarge)
                
                .clip(RoundedCornerShape(Dimensions.radiusMedium)),
            shape = RoundedCornerShape(Dimensions.radiusMedium),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            interactionSource = remember { MutableInteractionSource() },
            // Ripple is default for Material 3 buttons
        ) {
            AnimatedContent(targetState = moveText) { text: String ->
                Text(
                    text = text,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
        }
    }
}

package com.example.android.chessapp

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.android.chessapp.ui.theme.ChessAppTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Card
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.example.android.chessapp.ui.theme.Dimensions

@Preview(showBackground = true)
@Composable
fun PreviewMoveList() {
    ChessAppTheme {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.medium),
            shape = RoundedCornerShape(Dimensions.radiusMedium),
            tonalElevation = Dimensions.small
        ) {
            val sampleMoves = listOf(
                ChessMoveRecord(
                    move = ChessMove(ChessPosition(6, 4), ChessPosition(4, 4), ChessPiece(PieceType.PAWN, PieceColor.WHITE)),
                    capturedPiece = null,
                    previousState = ChessGameState()
                ),
                ChessMoveRecord(
                    move = ChessMove(ChessPosition(1, 4), ChessPosition(3, 4), ChessPiece(PieceType.PAWN, PieceColor.BLACK)),
                    capturedPiece = null,
                    previousState = ChessGameState()
                )
            )
            MoveList(moveHistory = sampleMoves)
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewMoveRow() {
    ChessAppTheme {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.medium),
            shape = RoundedCornerShape(Dimensions.radiusMedium),
            tonalElevation = Dimensions.small
        ) {
            MoveRow(
                moveNumber = 1,
                whiteMove = ChessMoveRecord(
                    move = ChessMove(ChessPosition(6, 4), ChessPosition(4, 4), ChessPiece(PieceType.PAWN, PieceColor.WHITE)),
                    capturedPiece = null,
                    previousState = ChessGameState()
                ),
                blackMove = ChessMoveRecord(
                    move = ChessMove(ChessPosition(1, 4), ChessPosition(3, 4), ChessPiece(PieceType.PAWN, PieceColor.BLACK)),
                    capturedPiece = null,
                    previousState = ChessGameState()
                ),
                onMoveSelected = null
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewMoveButton() {
    ChessAppTheme {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.medium),
            shape = RoundedCornerShape(Dimensions.radiusMedium),
            tonalElevation = Dimensions.small
        ) {
            MoveButton(
                move = ChessMoveRecord(
                    move = ChessMove(ChessPosition(6, 4), ChessPosition(4, 4), ChessPiece(PieceType.PAWN, PieceColor.WHITE)),
                    capturedPiece = null,
                    previousState = ChessGameState()
                ),
                onClick = {},
                modifier = Modifier
            )
        }
    }
}


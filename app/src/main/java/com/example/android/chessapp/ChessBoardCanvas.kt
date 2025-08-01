package com.example.android.chessapp

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.math.pow
import android.util.Log
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun ChessBoardCanvas(
    board: Array<Array<ChessPiece?>>, // 8x8 board
    selected: ChessPosition?,
    validMoves: List<ChessPosition>,
    lastMove: Pair<ChessPosition, ChessPosition>?,
    onSquareSelected: (ChessPosition) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val size = this.size
                    val squareSize = min(size.width, size.height) / 8f
                    val col = (offset.x / squareSize).toInt()
                    val row = (offset.y / squareSize).toInt()
                    if (row in 0..7 && col in 0..7) {
                        onSquareSelected(ChessPosition(row, col))
                    }
                }
            }
    ) {
        val squareSize = min(size.width, size.height) / 8f
        val strokeWidth = 6f
        val originOffset = strokeWidth / 2f
        val boardSize = squareSize * 8f

        // Draw board squares
        for (row in 0..7) {
            for (col in 0..7) {
                val isLight = (row + col) % 2 == 0
                val squareColor = if (isLight) Color(0xFFF0D9B5) else Color(0xFFB58863)
                drawRect(
                    color = squareColor,
                    topLeft = Offset(originOffset + col * squareSize, originOffset + row * squareSize),
                    size = androidx.compose.ui.geometry.Size(squareSize, squareSize)
                )
            }
        }


        // Highlight last move with glow, border, and arrow
        lastMove?.let { (from, to) ->
            val borderColor = Color(0xFFFFD600).copy(alpha = 0.85f)
            val glowColor = Color(0xFFFFF59D).copy(alpha = 0.35f)
            val fromOffset = Offset(originOffset + from.col * squareSize, originOffset + from.row * squareSize)
            val toOffset = Offset(originOffset + to.col * squareSize, originOffset + to.row * squareSize)
            // glow under squares
            drawRect(color = glowColor, topLeft = fromOffset, size = androidx.compose.ui.geometry.Size(squareSize, squareSize))
            drawRect(color = glowColor, topLeft = toOffset, size = androidx.compose.ui.geometry.Size(squareSize, squareSize))
            // border under squares
            drawRect(color = borderColor, topLeft = fromOffset, size = androidx.compose.ui.geometry.Size(squareSize, squareSize), style = Stroke(width = strokeWidth))
            drawRect(color = borderColor, topLeft = toOffset, size = androidx.compose.ui.geometry.Size(squareSize, squareSize), style = Stroke(width = strokeWidth))
            // arrow
            val fromCenter = Offset(fromOffset.x + squareSize/2f, fromOffset.y + squareSize/2f)
            val toCenter = Offset(toOffset.x + squareSize/2f, toOffset.y + squareSize/2f)
            val arrowColor = Color(0xFF1976D2).copy(alpha = 0.58f)
            val shaftWidth = squareSize * 0.16f
            val headLength = squareSize * 0.55f
            val headWidth = squareSize * 0.38f
            val arrowLength = sqrt((toCenter.x - fromCenter.x).pow(2) + (toCenter.y - fromCenter.y).pow(2))
            if (arrowLength > 0.1f) {
                val shaftEnd = fromCenter + (toCenter - fromCenter) * ((arrowLength - headLength) / arrowLength)
                drawLine(color = arrowColor, start = fromCenter, end = shaftEnd, strokeWidth = shaftWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                val direction = (toCenter - fromCenter) / arrowLength
                val ortho = Offset(-direction.y, direction.x)
                val p1 = toCenter
                val p2 = toCenter - direction * headLength + ortho * headWidth / 2f
                val p3 = toCenter - direction * headLength - ortho * headWidth / 2f
                drawPath(path = androidx.compose.ui.graphics.Path().apply { moveTo(p1.x, p1.y); lineTo(p2.x, p2.y); lineTo(p3.x, p3.y); close() }, color = arrowColor)
            }
        }

        validMoves.forEach {
            val moveOffset = Offset(originOffset + it.col * squareSize, originOffset + it.row * squareSize)
            drawRect(
                color = Color(0xFF00E676).copy(alpha = 0.22f),
                topLeft = moveOffset,
                size = androidx.compose.ui.geometry.Size(squareSize, squareSize)
            )
            drawCircle(
                color = Color(0xFF00E676).copy(alpha = 0.32f),
                radius = squareSize * 0.18f,
                center = Offset(moveOffset.x + squareSize/2f, moveOffset.y + squareSize/2f)
            )
        }

        // Draw pieces with drop shadow
        for (row in 0..7) {
            for (col in 0..7) {
                val piece = board[row][col]
                if (piece != null) {
                    val symbol = piece.getUnicodeSymbol()
                    drawIntoCanvas { canvas ->
                        val fontSize = squareSize * 0.7f
                        val paint = android.graphics.Paint().apply {
                            isAntiAlias = true
                            color = android.graphics.Color.BLACK
                            textSize = fontSize
                            isFakeBoldText = true
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                        val shadowPaint = android.graphics.Paint(paint).apply {
                            color = android.graphics.Color.argb(100, 0, 0, 0)
                            setShadowLayer(fontSize * 0.18f, 0f, fontSize * 0.13f, android.graphics.Color.BLACK)
                        }
                        val centerX = originOffset + col * squareSize + squareSize / 2f
                        val centerY = originOffset + row * squareSize + squareSize / 2f
                        val fm = paint.fontMetrics
                        val baseline = centerY - (fm.ascent + fm.descent) / 2f
                        // draw shadow
                        canvas.nativeCanvas.drawText(symbol, centerX, baseline, shadowPaint)
                        // draw piece
                        canvas.nativeCanvas.drawText(symbol, centerX, baseline, paint)
                    }
                }
            }
        }

        // Draw board border and subtle gradient overlay
        drawRect(
            color = Color.Black,
            topLeft = Offset(originOffset, originOffset),
            size = androidx.compose.ui.geometry.Size(boardSize - strokeWidth, boardSize - strokeWidth),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
        )
        // Subtle gradient overlay for a modern look
        drawRect(
            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color(0x22000000)),
                startY = 0f,
                endY = boardSize - strokeWidth
            ),
            topLeft = Offset(originOffset, originOffset),
            size = androidx.compose.ui.geometry.Size(boardSize - strokeWidth, boardSize - strokeWidth)
        )
        drawRect(
            color = Color.Black,
            topLeft = Offset(0f, 0f),
            size = androidx.compose.ui.geometry.Size(squareSize * 8, squareSize * 8),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f)
        )

        // Subtle gradient overlay for a modern look
        drawRect(
            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color(0x22000000)),
                startY = 0f,
                endY = squareSize * 8
            ),
            size = androidx.compose.ui.geometry.Size(squareSize * 8, squareSize * 8)
        )
    }
}

// Helper extension to get Unicode symbol for pieces
fun ChessPiece.getUnicodeSymbol(): String = when (type) {
    PieceType.KING -> if (color == PieceColor.WHITE) "♔" else "♚"
    PieceType.QUEEN -> if (color == PieceColor.WHITE) "♕" else "♛"
    PieceType.ROOK -> if (color == PieceColor.WHITE) "♖" else "♜"
    PieceType.BISHOP -> if (color == PieceColor.WHITE) "♗" else "♝"
    PieceType.KNIGHT -> if (color == PieceColor.WHITE) "♘" else "♞"
    PieceType.PAWN -> if (color == PieceColor.WHITE) "♙" else "♟"
}
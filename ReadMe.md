# Chess App for Android

A beginner-friendly chess application built for Android using Kotlin and Jetpack Compose.

## Features (Current)

- ✅ Interactive chess board with 8x8 grid
- ✅ Chess pieces in starting positions with Unicode symbols
- ✅ **Complete chess gameplay mechanics**
- ✅ **Turn-based gameplay** (White moves first, alternating turns)
- ✅ **Full piece movement logic** for all 6 piece types:
    - Pawns: Move forward 1-2 squares, capture diagonally
    - Rooks: Move horizontally/vertically any distance
    - Bishops: Move diagonally any distance
    - Knights: Move in L-shape (2+1 squares)
    - Queens: Combine rook + bishop movement
    - Kings: Move 1 square in any direction
- ✅ **Visual feedback system** with square highlighting and valid move indicators
- ✅ **Move validation** - only legal moves allowed
- ✅ **Game status display** showing current player's turn
- ✅ **Move history** with chess notation (e.g., "e2 → e4")
- ✅ **Tap-to-play interface** (select piece, then tap destination)
- ✅ Material Design 3 UI
- ✅ Navigation between home screen and game board

## Features (Planned)

- ⏳ **AI opponent** with adjustable difficulty
- ⏳ **Advanced chess rules** (check, checkmate, stalemate)
- ⏳ **Special moves** (castling, en passant, pawn promotion)
- ⏳ **Beginner-friendly hints** and tutorials
- ⏳ **Game save/load** functionality
- ⏳ **Sound effects** and animations
- ⏳ **Captured pieces display**

## How to Play

1. **Start the game** - White pieces move first
2. **Select a piece** - Tap any of your pieces to see valid moves (highlighted in green)
3. **Make your move** - Tap on a highlighted square to move there
4. **Turns alternate** - Game automatically switches to the other player
5. **Continue playing** - Repeat until the game ends

## Technical Details

- **Target Android Version**: Android 10 (API level 29)
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: Modern Android development practices
- **Chess Logic**: Complete rule implementation for all piece types

## Development Setup

1. Clone the repository
2. Open in Android Studio
3. Build and run on device or emulator

## Current Progress

This project is being developed iteratively with the following steps:

1. ✅ **Basic Setup**: Project structure and simple UI
2. ✅ **Chess Board UI**: Interactive board with pieces
3. ✅ **Game Logic**: Complete chess gameplay mechanics (**MAJOR MILESTONE**)
4. ⏳ **AI Implementation**: Bot opponent
5. ⏳ **Advanced Rules**: Check, checkmate, special moves
6. ⏳ **UI/UX Enhancement**: Beginner-friendly features

## Testing

**Tested on:**
- Android Studio Emulator (Android 10)
- **Target**: Samsung Galaxy S9 (Android 10)

**Current Testing Status:**
- ✅ All piece movements working correctly
- ✅ Turn-based gameplay functioning
- ✅ Move validation preventing illegal moves
- ✅ Visual feedback system working
- ✅ Game state tracking operational

## Game Rules Implemented

### Basic Piece Movement
- **Pawns**: Move forward 1 square, or 2 from starting position; capture diagonally
- **Rooks**: Move any number of squares horizontally or vertically
- **Bishops**: Move any number of squares diagonally
- **Knights**: Move in L-shape (2 squares in one direction, 1 perpendicular)
- **Queens**: Combine rook and bishop movement
- **Kings**: Move 1 square in any direction

### Game Flow
- White always moves first
- Players alternate turns
- Only current player's pieces can be moved
- Visual indicators show valid moves
- Game tracks complete move history

## Development Notes

This is a **fully playable chess game** with complete basic chess rules implemented. The app currently supports human vs human gameplay with proper turn management and move validation. The next major milestone is implementing an AI opponent to enable single-player gameplay.

## Version History

- **v0.3** - Complete chess gameplay mechanics (Step 3)
- **v0.2** - Interactive chess board UI (Step 2)
- **v0.1** - Basic project setup and navigation (Step 1)
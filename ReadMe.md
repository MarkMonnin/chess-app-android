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
- ✅ **AI opponent** with random move selection
- ✅ **Game mode selection** (Human vs Human / Human vs AI)
- ✅ **Last move highlighting** for better move tracking
- ✅ **AI thinking indicators** with realistic delays
- ✅ Material Design 3 UI
- ✅ Navigation between home screen and game board

## Features (Planned)

- ⏳ **Adjustable AI difficulty** (Level 0.0-50.0)
- ⏳ **Advanced chess rules** (check, checkmate, stalemate)
- ⏳ **Special moves** (castling, en passant, pawn promotion)
- ⏳ **Beginner-friendly hints** and tutorials
- ⏳ **Game save/load** functionality
- ⏳ **Sound effects** and animations
- ⏳ **Captured pieces display**
- ⏳ **Post-game difficulty suggestions**

## How to Play

1. **Choose game mode** - Select "Play vs Human" or "Play vs AI" from the home screen
2. **Start the game** - White pieces move first
3. **Select a piece** - Tap any of your pieces to see valid moves (highlighted in green)
4. **Make your move** - Tap on a highlighted square to move there
5. **Turns alternate** - Game automatically switches to the other player
6. **AI opponent** - When playing vs AI, the computer will make its move automatically
7. **Continue playing** - Repeat until the game ends

## Technical Details

- **Target Android Version**: Android 10 (API level 29)
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: Modern Android development practices with separated concerns
- **Chess Logic**: Complete rule implementation for all piece types
- **AI System**: Random move selection (foundation for difficulty system)

## Development Setup

1. Clone the repository
2. Open in Android Studio
3. Build and run on device or emulator

## Current Progress

This project is being developed iteratively with the following steps:

1. ✅ **Basic Setup**: Project structure and simple UI
2. ✅ **Chess Board UI**: Interactive board with pieces
3. ✅ **Game Logic**: Complete chess gameplay mechanics
4. ✅ **AI Implementation**: Basic bot opponent (**CURRENT MILESTONE**)
5. ⏳ **AI Difficulty**: Adjustable difficulty system (Level 0.0-50.0)
6. ⏳ **Advanced Rules**: Check, checkmate, special moves
7. ⏳ **UI/UX Enhancement**: Beginner-friendly features

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
- ✅ AI opponent making legal moves
- ✅ Game mode selection working
- ✅ Move highlighting functional

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
- AI makes moves automatically when it's their turn

### AI Features
- Makes random but legal moves
- Realistic thinking delays
- Cannot move during human player's turn
- Provides visual feedback for AI moves

## Code Structure

```
app/src/main/java/com/example/android/chessapp/
├── MainActivity.kt          # Main activity and app navigation
├── ChessBoard.kt           # Chess board UI and game management
├── ChessAI.kt              # AI opponent logic
├── ChessLogic.kt           # Shared chess rule validation
├── ChessModels.kt          # Data classes (ChessPiece, ChessMove, etc.)
└── ui/theme/               # Material Design 3 theming
```

## Development Notes

This is a **fully playable chess game** with both human vs human and human vs AI gameplay modes. The AI currently makes random moves but provides a solid foundation for implementing an adjustable difficulty system. The app features clean separation of concerns with dedicated files for UI, game logic, and AI behavior.

## Next Development Phase

The next major milestone is implementing an adjustable difficulty system (Level 0.0-50.0) that will allow players to choose appropriate challenge levels and receive suggestions for progression.

## Version History

- **v0.4** - AI bot implementation with game mode selection
- **v0.3** - Complete chess gameplay mechanics
- **v0.2** - Interactive chess board UI
- **v0.1** - Basic project setup and navigation

## Contributing

This project follows iterative development with thorough testing at each step. Each feature is implemented completely before moving to the next milestone.
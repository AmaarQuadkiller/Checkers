package com.gmail.amaarquadri.checkers.logic;

import com.gmail.amaarquadri.checkers.utility.Utils;

/**
 * Created by Amaar on 2016-09-18.
 * This class holds the least amount of data necessary to represent a move on a CheckerBoard.
 * Only holds the final result of the move, not any of the intermediate boards.
 */
public class Move {
    /**
     * The final result of this Move.
     */
    protected final char[][] finalBoard;


    /**
     * Creates a new Move Object without any intermediate boards.
     *
     * @param finalBoard The board that this CheckerBoard will result in if this Move is executed.
     */
    public Move(char[][] finalBoard) {
        this.finalBoard = finalBoard;
    }

    /**
     * Calculates and returns the value of this board considering "depth" moves into the future, using recursion.
     *
     * @param isRedTurn Whether or not it is red's turn at the current point in the tree search.
     * @param valueToBeat The most optimal value found so far in the layer above.
     * @param depth The number of layers left to search.
     * @return The value of this board, considering "depth" boards into the future.
     */
    public final int getValue(boolean isRedTurn, int valueToBeat, int depth) {
        //special case for endpoints of the recursive tree search
        if (depth == 0) return getRawValue();

        //if it is red's turn then maximize because red wants the highest value and vice versa
        //start with the worst possible value from the perspective of maximizing/minimizing
        //this is because if no moves are possible, it is the worst possible scenario for the player whose turn it is (they have lost)
        int value = isRedTurn ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        //getAllPossibleMoves can be used because intermediateBoards are of no significance for calculating values
        for (Move move : Utils.getAllPossibleMoves(isRedTurn, finalBoard)) {
            //the valueToBeat for the next layer of tree search is the current value
            int mValue = move.getValue(!isRedTurn, value, depth - 1);
            //if mValue is more optimal value, then update it
            if (isRedTurn ? mValue > value : mValue < value) {
                value = mValue;
                //in the layer above the values will be maximized if they are minimized here and vice versa
                //if the new value (mValue) is already less optimal than the valueToBeat (in the sense outlined above)
                //then there is no way that it can beat it
                //therefore there is no point in calculating any further
                if (isRedTurn ? value >= valueToBeat : value <= valueToBeat) return value;
            }
        }
        return value;
    }

    private int getRawValueOriginal() {
        //return the value from red's perspective
        int value = 0;
        for (int i = 0; i < 8; i++) for (int j = (i + 1) % 2; j < 8; j += 2) switch (finalBoard[i][j]) {
            case 'r': value += 10; break;
            case 'R': value += 19; break;
            case 'w': value -= 10; break;
            case 'W': value -= 19;
        }
        return value;
    }

    /**
     * Returns the value of the given board from red's perspective.
     *
     * @return The value of the given board from red's perspective.
     */
    private int getRawValue() {
        int value = 0;
        int pieceCount = 0;
        for (int i = 0; i < 8; i++) for (int j = (i + 1) % 2; j < 8; j += 2) {
            if (finalBoard[i][j] == 'E') continue;
            pieceCount++;

            boolean isRed = finalBoard[i][j] == 'r' || finalBoard[i][j] == 'R';
            boolean isKing = finalBoard[i][j] == 'w' || finalBoard[i][j] == 'W';

            value += isRed ? (isKing ? 19000000 : 10000000) : (isKing ? -19000000 : -10000000); //can take up to 3 digits (max of 228000000 points)
            if (!isKing) value +=  10000 * (isRed ? 7 - j : -j); //can take up to 2 digits (max of 600000 points)
            if (i == 0 || i == 7) value += isRed ? 200 : -200; //can take up to 2 digits (max of 1600 points)
        }
        value += value > 0 ? 24 - pieceCount : pieceCount - 24; //will always take 2 digits (max of 23 points)
        return value;
    }
}

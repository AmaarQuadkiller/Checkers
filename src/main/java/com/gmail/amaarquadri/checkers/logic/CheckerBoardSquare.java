package com.gmail.amaarquadri.checkers.logic;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.IntDef;
import android.support.v7.widget.AppCompatImageView;

import com.gmail.amaarquadri.checkers.R;

/**
 * Created by Amaar on 2016-05-30.
 * This class represents a single square on a CheckerBoard.
 */
@SuppressLint("ViewConstructor")
public class CheckerBoardSquare extends AppCompatImageView {
    //STATIC MEMBERS


    /**
     * Constant indicating an empty CheckerBoardSquare.
     */
    public static final int EMPTY = 1;

    /**
     * Constant indicating a CheckerBoardSquare with a red piece.
     */
    public static final int RED_PIECE = 2;

    /**
     * Constant indicating a CheckerBoardSquare with a white piece.
     */
    public static final int WHITE_PIECE = 3;

    /**
     * Constant indicating a CheckerBoardSquare with a red king.
     */
    public static final int RED_KING = 4;

    /**
     * Constant indicating a CheckerBoardSquare with a white king.
     */
    public static final int WHITE_KING = 5;

    /**
     * Constant indicating a CheckerBoardSquare with a red piece that is highlighted.
     */
    public static final int RED_PIECE_HIGHLIGHTED = 6;

    /**
     * Constant indicating a CheckerBoardSquare with a white piece that is highlighted.
     */
    public static final int WHITE_PIECE_HIGHLIGHTED = 7;

    /**
     * Constant indicating a CheckerBoardSquare with a red king that is highlighted.
     */
    public static final int RED_KING_HIGHLIGHTED = 8;

    /**
     * Constant indicating a CheckerBoardSquare with a white king that is highlighted.
     */
    public static final int WHITE_KING_HIGHLIGHTED = 9;

    /**
     * Constant indicating a CheckerBoardSquare with a black circle on it, indicating a possible move.
     */
    public static final int BLACK_CIRCLE = 10;

    /**
     * Group all the CheckerBoardSquare states into a single @interface declaration.
     */
    @IntDef({EMPTY, RED_PIECE, WHITE_PIECE, RED_KING, WHITE_KING, RED_PIECE_HIGHLIGHTED,
            WHITE_PIECE_HIGHLIGHTED, RED_KING_HIGHLIGHTED, WHITE_KING_HIGHLIGHTED, BLACK_CIRCLE})
    public @interface State {}


    /**
     * This class holds data that is stored in a CheckerBoardSquare that has a black circle, indicating a possible move.
     * The data contains all the information needed to execute the move corresponding with clicking on the associated black circle.
     */
    public static class BlackCircleData {
        /**
         * The i-position where the piece came from.
         */
        private final int startI;

        /**
         * The j-position where the piece came from.
         */
        private final int startJ;

        //Note that endI and endJ do not need to be stored in BlackCircleData.
        //They can be found at execution time by the coordinates of this BlackCircleData itself.

        /**
         * Whether or not the piece has killed another piece in the process of making this move.
         */
        private final boolean isKill;

        /**
         * The i-position of the piece to kill. This is undefined if isKill is false.
         */
        private final int killI;

        /**
         * The j-position of the piece to kill. This is undefined if isKill is false.
         */
        private final int killJ;

        /**
         * Whether or not this move allows for the possibility that the user can double kill.
         */
        private final boolean canContinueMove;


        /**
         * Creates a new BlackCircleData Object.
         * Assumes that no pieces were killed.
         *
         * @param startI The i-position where the piece came from.
         * @param startJ The j-position where the piece came from.
         * @param canContinueMove Whether or not this move allows for the possibility that the user can double kill.
         */
        public BlackCircleData(int startI, int startJ, boolean canContinueMove) {
            this.startI = startI;
            this.startJ = startJ;
            isKill = false;
            killI = -1;
            killJ = -1;
            this.canContinueMove = canContinueMove;
        }

        /**
         * Creates a new BlackCircleData Object.
         *
         * @param startI The i-position where the piece came from.
         * @param startJ The j-position where the piece came from.
         * @param killI The i-position of the piece that was killed.
         * @param killJ The j-position of the piece that was killed.
         * @param canContinueMove Whether or not this move allows for the possibility that the user can double kill.
         */
        public BlackCircleData(int startI, int startJ, int killI, int killJ, boolean canContinueMove) {
            this.startI = startI;
            this.startJ = startJ;
            isKill = true;
            this.killI = killI;
            this.killJ = killJ;
            this.canContinueMove = canContinueMove;
        }


        /**
         * @return The i-position where the piece came from.
         */
        public int getStartI() {
            return startI;
        }

        /**
         * @return The j-position where the piece came from.
         */
        public int getStartJ() {
            return startJ;
        }

        /**
         * @return Whether or not the piece has killed another piece in the process of making this move.
         */
        public boolean isKill() {
            return isKill;
        }

        /**
         * @return The i-position of the piece to kill. This is undefined if isKill is false.
         */
        public int getKillI() {
            return killI;
        }

        /**
         * @return The j-position of the piece to kill. This is undefined if isKill is false.
         */
        public int getKillJ() {
            return killJ;
        }

        /**
         * @return Whether or not this move allows for the possibility that the user can double kill.
         */
        public boolean canContinueMove() {
            return canContinueMove;
        }
    }


    //INSTANCE VARIABLES


    /**
     * Whether or not this CheckerBoardSquare has a white background.
     * This is determined by the caller at construction based on this CheckerBoardSquare's coordinates.
     */
    private final boolean isBackgroundWhite;

    /**
     * The current State of this CheckerBoardSquare.
     */
    @State
    private int state;

    /**
     * This CheckerBoardSquare's BlackCircleData.
     * Null if this CheckerBoardSquare doesn't have any BlackCircleData.
     */
    private BlackCircleData blackCircleData;


    //INITIALIZATION


    /**
     * Creates a new CheckerBoardSquare ImageView.
     *
     * @param context The parent Context.
     * @param isBackgroundWhite Whether or not this CheckerBoardSquare has a white background.
     */
    public CheckerBoardSquare(Context context, boolean isBackgroundWhite) {
        super(context);
        this.isBackgroundWhite = isBackgroundWhite;
        state = EMPTY;
        blackCircleData = null;
        refresh();
    }

    /**
     * Ensures that the System allocates the correct amount of space for the height of this CheckerBoardSquare.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //width and height are both equal to the width (ensures square)
        int sideLength = getMeasuredWidth();
        setMeasuredDimension(sideLength, sideLength);
    }


    //GETTERS


    /**
     * @return Whether or not this CheckerBoardSquare is empty.
     */
    public boolean isEmpty() {
        return state == EMPTY || state == BLACK_CIRCLE;
    }

    /**
     * @return Whether or not this empty CheckerBoardSquare contains BlackCircleData.
     */
    public boolean hasBlackCircle() {
        return state == BLACK_CIRCLE;
    }

    /**
     * @return This CheckerBoardSquare's BlackCircleData. Will return null if hasBlackCircleData() would return false at the time that this is called.
     */
    public BlackCircleData getBlackCircleData() {
        return blackCircleData;
    }

    /**
     * @return Whether or not the piece that this CheckerBoardSquare contains is red.
     */
    public boolean isRed() {
        return state == RED_PIECE || state == RED_KING || state == RED_PIECE_HIGHLIGHTED || state == RED_KING_HIGHLIGHTED;
    }

    /**
     * @return Whether or not the piece that this CheckerBoardSquare contains is a king.
     */
    public boolean isKing() {
        return state == RED_KING || state == WHITE_KING || state == RED_KING_HIGHLIGHTED || state == WHITE_KING_HIGHLIGHTED;
    }

    /**
     * @return Whether or not the piece that this CheckerBoardSquare contains is highlighted.
     */
    public boolean isHighlighted() {
        return state == RED_PIECE_HIGHLIGHTED || state == WHITE_PIECE_HIGHLIGHTED || state == RED_KING_HIGHLIGHTED || state == WHITE_KING_HIGHLIGHTED;
    }

    /**
     * Returns a char representation of the state of this CheckerBoardSquare.
     * Does not contain information about the background colour
     * because it can be calculated based on this CheckerBoardSquare's position within a CheckerBoard.
     * Does not contain any information relating to BlackCircleData because it is transitive in nature.
     * The characters are defined as follows:
     * <ol>
     * <li>'E': Empty</li>
     * <li>'r': Red piece</li>
     * <li>'w': White piece</li>
     * <li>'R': Red king</li>
     * <li>'W': White king</li>
     * <li>'s': Highlighted red piece</li>
     * <li>'x': Highlighted white piece</li>
     * <li>'S': Highlighted red king</li>
     * <li>'X': Highlighted white king</li>
     * <li>'O': Black circle</li>
     * </ol>
     *
     * @return A char representation of the state of this CheckerBoardSquare.
     */
    public char getState() {
        switch (state) {
            case EMPTY: return 'E';
            case RED_PIECE: return 'r';
            case WHITE_PIECE: return 'w';
            case RED_KING: return 'R';
            case WHITE_KING: return 'W';
            case RED_PIECE_HIGHLIGHTED: return 's';
            case WHITE_PIECE_HIGHLIGHTED: return 'x';
            case RED_KING_HIGHLIGHTED: return 'S';
            case WHITE_KING_HIGHLIGHTED: return 'X';
            case BLACK_CIRCLE: return 'O';
            //should never happen
            default: throw new InternalError("Unknown CheckerBoardSquare.State");
        }
    }


    //SETTERS


    /**
     * Sets the state of this CheckerBoardSquare based on a given char.
     * The characters are defined as follows:
     * <ol>
     * <li>'E': Empty</li>
     * <li>'r': Red piece</li>
     * <li>'w': White piece</li>
     * <li>'R': Red king</li>
     * <li>'W': White king</li>
     * <li>'s': Highlighted red piece</li>
     * <li>'x': Highlighted white piece</li>
     * <li>'S': Highlighted red king</li>
     * <li>'X': Highlighted white king</li>
     * <li>'O': Black circle</li>
     * </ol>
     *
     * @param c The char representing the state to set this CheckerBoardSquare to.
     */
    public void setState(char c) {
        switch (c) {
            case 'E': state = EMPTY; break;
            case 'r': state = RED_PIECE; break;
            case 'w': state = WHITE_PIECE; break;
            case 'R': state = RED_KING; break;
            case 'W': state = WHITE_KING; break;
            case 's': state = RED_PIECE_HIGHLIGHTED; break;
            case 'x': state = WHITE_PIECE_HIGHLIGHTED; break;
            case 'S': state = RED_KING_HIGHLIGHTED; break;
            case 'X': state = WHITE_KING_HIGHLIGHTED; break;
            case 'O': throw new IllegalArgumentException("Can not dynamically set state to BLACK_CIRCLE");
            default: throw new IllegalArgumentException("Unknown character");
        }
        blackCircleData = null;
        refresh();
    }

    /**
     * Removes any pieces, BlackCircleData, and highlighting, leaving an empty square.
     */
    public void setEmpty() {
        setState('E');
    }

    /**
     * Sets the BlackCircleData for this CheckerBoardSquare.
     *
     * @param blackCircleData The BlackCircleData to assign to this CheckerBoardSquare.
     * @throws IllegalStateException If there is a piece in this CheckerBoardSquare.
     * (In Checkers, a piece can never move into a previously occupied spot).
     */
    public void setBlackCircleData(BlackCircleData blackCircleData) {
        state = BLACK_CIRCLE;
        this.blackCircleData = blackCircleData;
        refresh();
    }

    /**
     * Sets the piece that this CheckerBoardSquare contains.
     * The piece will not be highlighted after this method returns regardless of prior conditions.
     *
     * @param isRed Whether or not the piece that this CheckerBoardSquare will contain is red.
     * @param isKing Whether or not the piece that this CheckerBoardSquare will contain is a king.
     */
    public void setPiece(boolean isRed, boolean isKing) {
        if (isRed) state = isKing ? RED_KING : RED_PIECE;
        else state = isKing ? WHITE_KING : WHITE_PIECE;
        blackCircleData = null;
        refresh();
    }

    /**
     * Changes the highlighted state of this CheckerBoardSquare.
     *
     * @param isHighlighted Whether or not the piece that this CheckerBoardSquare will contain is highlighted.
     * @throws IllegalStateException If this CheckerBoardSquare doesn't contain a piece.
     */
    public void setHighlighted(boolean isHighlighted) {
        if (state == EMPTY || state == BLACK_CIRCLE) return;
        if (isHighlighted) {
            if (isRed()) state = isKing() ? RED_KING_HIGHLIGHTED : RED_PIECE_HIGHLIGHTED;
            else state = isKing() ? WHITE_KING_HIGHLIGHTED : WHITE_PIECE_HIGHLIGHTED;
        }
        else {
            if (isRed()) state = isKing() ? RED_KING : RED_PIECE;
            else state = isKing() ? WHITE_KING : WHITE_PIECE;
        }
        refresh();
    }

    /**
     * Refreshes this CheckerBoardSquare and updates the ImageView (if necessary).
     */
    private void refresh() {
        //update the image resource
        switch (state) {
            case EMPTY: setImageResource(isBackgroundWhite ? R.drawable.white_background : R.drawable.brown_background); break;
            case RED_PIECE: setImageResource(R.drawable.red_piece); break;
            case WHITE_PIECE: setImageResource(R.drawable.white_piece); break;
            case RED_KING: setImageResource(R.drawable.red_king); break;
            case WHITE_KING: setImageResource(R.drawable.white_king); break;
            case RED_PIECE_HIGHLIGHTED: setImageResource(R.drawable.red_piece_highlighted); break;
            case WHITE_PIECE_HIGHLIGHTED: setImageResource(R.drawable.white_piece_highlighted); break;
            case RED_KING_HIGHLIGHTED: setImageResource(R.drawable.red_king_highlighted); break;
            case WHITE_KING_HIGHLIGHTED: setImageResource(R.drawable.white_king_highlighted); break;
            case BLACK_CIRCLE: setImageResource(R.drawable.black_circle); break;
            //should never happen
            default: throw new InternalError("Unknown CheckerBoardSquare.State");
        }
    }
}

package com.gmail.amaarquadri.checkers.logic;

import java.util.ArrayList;

/**
 * Created by Amaar on 2016-09-18.
 * This class holds all the data needed to represent and execute a specific move that the AI can make.
 * This includes any intermediate boards as well as the final result of the move.
 */
public class DetailedMove extends Move {
    /**
     * An ArrayList of all the intermediate boards that the CheckerBoard will go through before the move is finished.
     * This does not include the final result of this Move.
     * This will be null unless the AI multi kills.
     */
    private final ArrayList<char[][]> intermediateBoards;


    /**
     * Creates a new Move Object without any intermediate boards.
     *
     * @param finalBoard The board that this CheckerBoard will result in if this Move is executed.
     */
    public DetailedMove(char[][] finalBoard) {
        super(finalBoard);
        intermediateBoards = null;
    }

    /**
     * Creates a new Move with intermediate boards and a resulting final board.
     *
     * @param intermediateBoards An ArrayList of intermediate boards.
     * @param finalBoard The resulting (final) board.
     */
    public DetailedMove(ArrayList<char[][]> intermediateBoards, char[][] finalBoard) {
        super(finalBoard);
        this.intermediateBoards = intermediateBoards;
    }

    /**
     * @return An ArrayList<char[][]> containing all boards for this DetailedMove (including intermediateBoards and the finalBoard).
     */
    public ArrayList<char[][]> getBoards() {
        ArrayList<char[][]> boards = intermediateBoards == null ? new ArrayList<char[][]>() : intermediateBoards;
        boards.add(finalBoard);
        return boards;
    }
}

package solver;

import game.Board;
import game.Shape;
import game.Tetrominoe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.NavigableMap;
import java.util.TreeMap;

import static game.Board.BOARD_HEIGHT;
import static game.Board.BOARD_WIDTH;

public class Solver {
    private static Shape cur;
    private static Shape next;
    private static NavigableMap<Double, ArrayList<Integer>> grades = new TreeMap<>();
    private static final double PenHeight = -0.510066;
    private static final double PenClear = 0.760666;
    private static final double PenHole = -0.35663;
    private static final double PenBump = -0.184483;


    public static void solver(Board gameBoard) {
        grades = new TreeMap<>();
        while (Board.isSolverOn) {
            Tetrominoe[] board = gameBoard.getBoard();
            cur = gameBoard.getCurPiece();
            next = gameBoard.getNextPiece();

            gradeCurrent(board);
            makeMove(gameBoard);

            grades.clear();
        }
    }

    private static void makeMove(Board gameBoard) {
        ArrayList<Integer> bestMove = grades.lastEntry().getValue();
        for (int i = 0; i < bestMove.get(2); i++) {
            cur.rotateRight();
        }
        gameBoard.tryMove(cur, bestMove.get(0), bestMove.get(1));
        gameBoard.dropDown();
    }

    private static double makeGrade(Tetrominoe[] expBoard, int clearedLines) {
        int aggregateHeight = 0;
        int holes = 0;
        int bump = 0;
        boolean[] heightCount = new boolean[BOARD_WIDTH];
        int[] heights = new int[BOARD_WIDTH];
        for (int y = BOARD_HEIGHT - 1; y >= 0; y--) {
            for (int x = BOARD_WIDTH - 1; x >= 0; x--) {
                if (expBoard[y * BOARD_WIDTH + x] != Tetrominoe.NoShape) {
                    if (!heightCount[x]) {
                        heightCount[x] = true;
                        aggregateHeight += y;
                        heights[x] = y;
                    }
                }
                if (heightCount[x] && expBoard[y * BOARD_WIDTH + x] == Tetrominoe.NoShape) {
                    holes++;
                }
            }
        }
        for (int i = 0; i < BOARD_WIDTH - 1; i++) {
            bump += Math.abs(heights[i] - heights[i + 1]);
        }
        return PenHeight * aggregateHeight + PenClear * clearedLines + PenHole * holes + PenBump * bump;
    }

    private static void gradeNext(Tetrominoe[] expCurBoard, double gradeCur, ArrayList<Integer> currentParams) {
        for (int i = 0; i < 4; i++) {
            for (int y = BOARD_HEIGHT - 1; y >= 0; y--) {
                for (int x = BOARD_WIDTH - 1; x >= 0; x--) {
                    Tetrominoe[] expNextBoard = expCurBoard.clone();
                    if (canPlace(expNextBoard, x, y - 1)) {
                        placeOnBoard(y, x, expNextBoard, next);
                        double gradeNext = makeGrade(expNextBoard, countClearedLines(expNextBoard));
                        grades.put(gradeCur + gradeNext, currentParams);
                    }
                }
            }
            next.rotateRight();
        }
    }

    private static void gradeCurrent(Tetrominoe[] board) {
        for (int i = 0; i < 4; i++) {
            for (int y = BOARD_HEIGHT - 1; y >= 0; y--) {
                for (int x = BOARD_WIDTH - 1; x >= 0; x--) {
                    Tetrominoe[] expCurBoard = board.clone();
                    if (canPlace(expCurBoard, x, y - 1)) {
                        placeOnBoard(y, x, expCurBoard, cur);
                        int clearedLines = countClearedLines(expCurBoard);
                        gradeNext(expCurBoard, makeGrade(expCurBoard, clearedLines), new ArrayList<>(Arrays.asList(x, y, i)));
                    }
                }
            }
            cur.rotateRight();
        }
    }

    private static void placeOnBoard(int y, int x, Tetrominoe[] expBoard, Shape cur) {
        for (int i = 0; i < 4; i++) {
            int xx = x + cur.x(i);
            int yy = y + cur.y(i);
            expBoard[yy * BOARD_WIDTH + xx] = cur.getShape();
        }
    }

    private static int countClearedLines(Tetrominoe[] expBoard) {
        int clearedLines = 0;
        for (int f = BOARD_HEIGHT - 1; f >= 0; --f) {
            boolean lineIsFull = true;
            for (int j = 0; j < BOARD_WIDTH; ++j) {
                if (expBoard[f * BOARD_WIDTH + j] == Tetrominoe.NoShape) {
                    lineIsFull = false;
                    break;
                }
            }
            if (lineIsFull) {
                clearedLines++;
                for (int k = f; k < BOARD_HEIGHT - 1; ++k) {
                    for (int j = 0; j < BOARD_WIDTH; ++j) {
                        expBoard[k * BOARD_WIDTH + j] = expBoard[(k + 1) * BOARD_WIDTH + j];
                    }
                }
            }
        }
        return clearedLines;
    }

    private static boolean canPlace(Tetrominoe[] expBoard, int newX, int newY) {
        for (int i = 0; i < 4; ++i) {
            int x = newX + cur.x(i);
            int y = newY + cur.y(i);
            if (x < 0 || x >= BOARD_WIDTH || y >= BOARD_HEIGHT || newY + cur.minY() < -1)
                return false;
            if (y == -1 || expBoard[y * BOARD_WIDTH + x] != Tetrominoe.NoShape)
                return true;
        }
        return false;
    }
}
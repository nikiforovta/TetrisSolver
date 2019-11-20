package solver;

import game.Board;
import game.Shape;
import game.Tetrominoe;

import javax.swing.*;
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
    private static Timer solve;


    public static void solver(Board gameBoard, boolean start) {
        grades = new TreeMap<>();
        if (solve == null) {
            solve = new Timer(1100, e -> {
                Tetrominoe[][] board = gameBoard.getBoard();
                cur = gameBoard.getCurPiece();
                next = gameBoard.getNextPiece();
                gradeCurrent(board);
                makeMove(gameBoard);
                grades.clear();
            });
        }
        solve.start();
        if (start && !gameBoard.isPaused && gameBoard.isStarted) {
            solve.start();
        } else {
            solve.stop();
        }
    }

    private static void makeMove(Board gameBoard) {
        try {
            ArrayList<Integer> bestMove = grades.lastEntry().getValue();
            for (int i = 0; i < bestMove.get(2); i++) {
                cur = cur.rotateLeft();
            }
            gameBoard.tryMove(cur, bestMove.get(0), bestMove.get(1));
            gameBoard.dropDown();
        } catch (NullPointerException ignored) {
        }
    }

    private static double makeGrade(Tetrominoe[][] tetrominoes, int clearedLines) {
        int aggregateHeight = 0;
        int holes = 0;
        int bump = 0;
        boolean[] heightCount = new boolean[BOARD_WIDTH];
        int[] heights = new int[BOARD_WIDTH];
        for (int y = BOARD_HEIGHT - 1; y >= 0; y--) {
            for (int x = BOARD_WIDTH - 1; x >= 0; x--) {
                if (tetrominoes[x][y] != Tetrominoe.NoShape) {
                    if (!heightCount[x]) {
                        heightCount[x] = true;
                        aggregateHeight += y;
                        heights[x] = y;
                    }
                }
                if (heightCount[x] && tetrominoes[x][y] == Tetrominoe.NoShape) {
                    holes++;
                }
            }
        }
        for (int i = 0; i < BOARD_WIDTH - 1; i++) {
            bump += Math.abs(heights[i] - heights[i + 1]);
        }
        return PenHeight * aggregateHeight + PenClear * clearedLines + PenHole * holes + PenBump * bump;
    }

    private static void gradeNext(Tetrominoe[][] tetrominoes, double gradeCur, ArrayList<Integer> currentParams) {
        int[] heights = gradeHeights(tetrominoes);
        for (int i = 0; i < 4; i++) {
            for (int x = BOARD_WIDTH - 1; x >= 0; x--) {
                for (int y = BOARD_HEIGHT - 2; y >= heights[x]; y--) {
                    Tetrominoe[][] expNextBoard = new Tetrominoe[BOARD_WIDTH][BOARD_HEIGHT];
                    for (int j = 0; j < tetrominoes.length; j++) {
                        System.arraycopy(tetrominoes[j], 0, expNextBoard[j], 0, tetrominoes[j].length);
                    }
                    if (!canPlace(expNextBoard, x, y - 1, next) && canPlace(expNextBoard, x, y, next)
                            && canPlace(expNextBoard, x, y + 1, next)) {
                        placeOnBoard(x, y, expNextBoard, next);
                        double gradeNext = makeGrade(expNextBoard, countClearedLines(expNextBoard));
                        grades.put(gradeCur + gradeNext, currentParams);
                    }
                }
            }
            next = next.rotateLeft();
        }
    }

    private static void gradeCurrent(Tetrominoe[][] tetrominoes) {
        int[] heights = gradeHeights(tetrominoes);
        for (int i = 0; i < 4; i++) {
            for (int x = BOARD_WIDTH - 1; x >= 0; x--) {
                for (int y = BOARD_HEIGHT - 2; y >= heights[x]; y--) {
                    Tetrominoe[][] expCurBoard = new Tetrominoe[BOARD_WIDTH][BOARD_HEIGHT];
                    for (int j = 0; j < tetrominoes.length; j++) {
                        System.arraycopy(tetrominoes[j], 0, expCurBoard[j], 0, tetrominoes[j].length);
                    }
                    if (!canPlace(expCurBoard, x, y - 1, cur) && canPlace(expCurBoard, x, y, cur)
                            && canPlace(expCurBoard, x, y + 1, cur)) {
                        placeOnBoard(x, y, expCurBoard, cur);
                        int clearedLines = countClearedLines(expCurBoard);
                        gradeNext(expCurBoard, makeGrade(expCurBoard, clearedLines),
                                new ArrayList<>(Arrays.asList(x, y, i)));
                    }
                }
            }
            cur = cur.rotateLeft();
        }
    }

    private static int[] gradeHeights(Tetrominoe[][] tetrominoes) {
        boolean[] heightCount = new boolean[BOARD_WIDTH];
        int[] heights = new int[BOARD_WIDTH];
        for (int j = BOARD_HEIGHT - 1; j >= 0; j--) {
            for (int k = BOARD_WIDTH - 1; k >= 0; k--) {
                if (tetrominoes[k][j] != Tetrominoe.NoShape) {
                    if (!heightCount[k]) {
                        heightCount[k] = true;
                        heights[k] = j;
                    }
                }
            }
        }
        return heights;
    }

    private static void placeOnBoard(int x, int y, Tetrominoe[][] tetrominoes, Shape cur) {
        for (int i = 0; i < 4; ++i) {
            int xx = x + cur.x(i);
            int yy = y + cur.y(i);
            tetrominoes[xx][yy] = cur.getShape();
        }
    }

    private static int countClearedLines(Tetrominoe[][] tetrominoes) {
        int clearedLines = 0;
        for (int f = BOARD_HEIGHT - 1; f >= 0; --f) {
            boolean lineIsFull = true;
            for (int j = 0; j < BOARD_WIDTH; ++j) {
                if (tetrominoes[j][f] == Tetrominoe.NoShape) {
                    lineIsFull = false;
                    break;
                }
            }
            if (lineIsFull) {
                clearedLines++;
                for (int k = f; k < BOARD_HEIGHT - 1; ++k) {
                    for (int j = 0; j < BOARD_WIDTH; ++j) {
                        tetrominoes[j][k] = tetrominoes[j][k + 1];
                    }
                }
            }
        }
        return clearedLines;
    }

    private static boolean canPlace(Tetrominoe[][] tetrominoes, int newX, int newY, Shape shape) {
        for (int i = 0; i < 4; i++) {
            int x = newX + shape.x(i);
            int y = newY + shape.y(i);
            if (x < 0 || x >= BOARD_WIDTH || y < 0 || y >= BOARD_HEIGHT - 2)
                return false;
            if (tetrominoes[x][y] != Tetrominoe.NoShape)
                return false;
        }
        return true;
    }
}
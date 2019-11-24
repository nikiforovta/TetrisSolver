package solver;


import game.Shape;
import game.Tetrominoe;
import jdk.internal.net.http.common.Pair;

import java.util.*;

import static game.Board.BOARD_HEIGHT;
import static game.Board.BOARD_WIDTH;
import static solver.Solver.*;

public class GeneticWeights {
    private static List<double[]> weights = new ArrayList<>();
    private static List<Pair<Integer, double[]>> gradeWeights = new ArrayList<>();
    private static Shape cur = new Shape();
    private static Shape next = new Shape();
    private static NavigableMap<Double, ArrayList<Integer>> grades = new TreeMap<>();
    private static NavigableMap<Double, ArrayList<double[]>> crossoverVictims = new TreeMap<>();
    private static final int GENERATIONS = 1;
    private static final int GENOMES_IN_GENERATION = 10;
    private static final int TETROMINOES = 5;
    private static final int GENOMES_NS = 3;
    private static Tetrominoe[] gameSet = new Tetrominoe[TETROMINOES];


    public static void main(String[] args) {
        createFirstGeneration();
        for (int i = 0; i < GENERATIONS; i++) {
            playGame();
            selection();
            createNewGeneration();
        }
        gradeWeights.sort(Comparator.comparing(o -> o.first));
        System.out.println(Arrays.toString(gradeWeights.get(gradeWeights.size() - 1).second));
    }

    private static void createFirstGeneration() {
        weights = new ArrayList<>();
        for (int i = 0; i < GENOMES_IN_GENERATION; i++) {
            double[] penalties = new double[4];
            for (int j = 0; j < 4; j++) {
                if (i != 1) {
                    penalties[j] = 10 * Math.random() - 5;
                }
            }
            weights.add(penalties);
        }
    }

    private static void playGame() {
        Random generate = new Random();
        for (int j = 0; j < TETROMINOES; j++) {
            gameSet[j] = Tetrominoe.values()[generate.nextInt(Tetrominoe.values().length)];
        }
        for (int i = 0; i < GENOMES_IN_GENERATION; i++) {
            Tetrominoe[][] board = new Tetrominoe[BOARD_WIDTH][BOARD_HEIGHT];
            for (int j = 0; j < BOARD_WIDTH; j++) {
                for (int k = 0; k < BOARD_HEIGHT; k++) {
                    board[j][k] = Tetrominoe.NoShape;
                }
            }
            int cleared = 0;
            for (int k = 0; k < TETROMINOES - 1; k++) {
                cur.setShape(gameSet[k]);
                next.setShape(gameSet[k + 1]);
                gradeCurrent(board, weights.get(i));
                cleared += makeMove(board);
                grades.clear();
            }
            Pair<Integer, double[]> weightGraded = new Pair<>(cleared, weights.get(i));
            gradeWeights.add(weightGraded);
        }
    }

    private static void selection() {
        Random chooser = new Random();
        for (int i = 0; i < GENOMES_NS; i++) {
            Pair rand1 = gradeWeights.get(chooser.nextInt(gradeWeights.size()));
            int par1Grade = (int) rand1.first;
            double[] par1 = (double[]) rand1.second;
            Pair rand2 = gradeWeights.get(chooser.nextInt(gradeWeights.size()));
            int par2Grade = (int) rand2.first;
            double[] par2 = (double[]) rand1.second;
            Double proportion = par2Grade != 0 ? ((double) par1Grade) / par2Grade : 1.0;
            ArrayList<double[]> parents = new ArrayList<>();
            parents.add(par1);
            parents.add(par2);
            crossoverVictims.put(proportion, parents);
        }
    }

    private static double[] crossover(Map.Entry<Double, ArrayList<double[]>> parents) {
        double[] child = new double[4];
        for (int i = 0; i < 4; i++) {
            child[i] = parents.getValue().get(0)[i] * parents.getKey() + parents.getValue().get(1)[i];
        }
        mutation(child);
        return child;
    }

    private static void mutation(double[] child) {
        double chance = Math.random();
        if (chance < 0.05) {
            Random mutate = new Random();
            child[mutate.nextInt(child.length)] += chance < 0.025 ? 0.3 : -0.3;
        }
    }

    private static void createNewGeneration() {
        for (int i = 0; i < GENOMES_NS; i++) {
            gradeWeights.sort(Comparator.comparing(o -> o.first));
            weights.remove(gradeWeights.get(0).second);
            weights.add(crossover(crossoverVictims.firstEntry()));
            crossoverVictims.remove(crossoverVictims.firstKey());
        }
        gradeWeights.clear();
        crossoverVictims.clear();
    }

    private static int makeMove(Tetrominoe[][] board) {
        try {
            ArrayList<Integer> bestMove = grades.lastEntry().getValue();
            for (int i = 0; i < bestMove.get(2); i++) {
                cur = cur.rotateLeft();
            }
            for (int i = 0; i < 4; ++i) {
                int xx = bestMove.get(0) + cur.x(i);
                int yy = bestMove.get(1) + cur.y(i);
                board[xx][yy] = cur.getShape();
            }
            return bestMove.get(3);
        } catch (NullPointerException ignored) {
        }
        return 0;
    }

    private static double makeGrade(Tetrominoe[][] tetrominoes, int clearedLines, double[] penalties) {
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
        return penalties[0] * aggregateHeight + penalties[1] * clearedLines + penalties[2] * holes + penalties[3] * bump;
    }

    private static void gradeNext(Tetrominoe[][] tetrominoes, double gradeCur, ArrayList<Integer> currentParams, double[] penalties) {
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
                        double gradeNext = makeGrade(expNextBoard, countClearedLines(expNextBoard), penalties);
                        grades.put(gradeCur + gradeNext, currentParams);
                    }
                }
            }
            next = next.rotateLeft();
        }
    }

    private static void gradeCurrent(Tetrominoe[][] tetrominoes, double[] penalties) {
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
                        gradeNext(expCurBoard, makeGrade(expCurBoard, clearedLines, penalties),
                                new ArrayList<>(Arrays.asList(x, y, i, clearedLines)), penalties);
                    }
                }
            }
            cur = cur.rotateLeft();
        }
    }

    private static void placeOnBoard(int x, int y, Tetrominoe[][] tetrominoes, Shape cur) {
        for (int i = 0; i < 4; ++i) {
            int xx = x + cur.x(i);
            int yy = y + cur.y(i);
            tetrominoes[xx][yy] = cur.getShape();
        }
    }
}
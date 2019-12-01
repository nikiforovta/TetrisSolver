package solver;


import game.Shape;
import game.Tetrominoe;
import javafx.util.Pair;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static game.Board.BOARD_HEIGHT;
import static game.Board.BOARD_WIDTH;
import static solver.Solver.*;

class GeneticWeights {
    private static List<double[]> weights; //Список геномов
    private static List<Pair<Integer, double[]>> gradeWeights = new ArrayList<>(); //Список пар геномов и их оценки в результате соревнования
    private static Shape cur = new Shape(); //Некоторые переменные и методы позаимствованы из класса решателя
    private static Shape next = new Shape();
    private static NavigableMap<Double, ArrayList<Integer>> grades = new TreeMap<>();
    private static List<Pair<Double, ArrayList<double[]>>> genomsForCrossover = new ArrayList<>(); //Список пар геномов, которым предстоит скрещивание и отношение их оценок в результате соревнования
    private static final int GENOMES_IN_GENERATION = 10; //Количество геномов в поколении
    private static final int TETROMINOES = 20; //Количество фигур за одно соревнование для одного генома
    private static final int GENOMES_NS = 3; //Количество геномов, заменяемых в результате естественного отбора (30% от GENOMES_IN_GENERATION)
    private static Tetrominoe[] gameSet = new Tetrominoe[TETROMINOES]; //Массив фигур, которые будут во время соревнования у геномов

    /**
     * Основной метод генетического алгоритма, в котором происходит смена поколений и в результате в консоль
     * выводятся коэффициенты для решателя
     */
    static void startGeneration() {
        createFirstGeneration();
        while (true) { //Запускаем цикл ндля заданного количества поколений
            gradeWeights.clear();
            playGame();
            selection();
            createNewGeneration();
            try (FileWriter writer = new FileWriter("src/main/resources/penalties.properties"); //Записываем коэффициенты в файл
                 BufferedWriter bw = new BufferedWriter(writer)) {
                double[] bestWeights = gradeWeights.get(gradeWeights.size() - 1).getValue();
                bw.write("PenHeight=" + bestWeights[0]);
                bw.newLine();
                bw.write("PenClear=" + bestWeights[1]);
                bw.newLine();
                bw.write("PenHole=" + bestWeights[2]);
                bw.newLine();
                bw.write("PenBump=" + gradeWeights.get(gradeWeights.size() - 1).getValue()[3]);
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * Метод создания первого поколения
     */
    private static void createFirstGeneration() {
        weights = new ArrayList<>();
        for (int i = 0; i < GENOMES_IN_GENERATION; i++) {
            double[] penalties = new double[4];
            for (int j = 0; j < 4; j++) {
                penalties[j] = 10 * Math.random() - 5; //Создание случаных чисел в диапазоне от -5.0 до 5.0
            }
            weights.add(penalties);
        }
    }

    /**
     * Метод, в котором проходит соревнование
     */
    private static void playGame() {
        Random generate = new Random();
        for (int j = 0; j < TETROMINOES; j++) {
            gameSet[j] = Tetrominoe.values()[generate.nextInt(Tetrominoe.values().length)]; //Создание массива случайных фигур заданного размера, одинакового для всех участников данного соревнования
        }
        for (int i = 0; i < GENOMES_IN_GENERATION; i++) { //Проведение одинаковой игры для каждого генома
            Tetrominoe[][] board = new Tetrominoe[BOARD_WIDTH][BOARD_HEIGHT];
            for (int j = 0; j < BOARD_WIDTH; j++) {
                for (int k = 0; k < BOARD_HEIGHT; k++) {
                    board[j][k] = Tetrominoe.NoShape;
                }
            }
            int cleared = 0; //Количество очищенных линих - показатель эффективности данного генома
            for (int k = 0; k < TETROMINOES - 1; k++) {
                cur.setShape(gameSet[k]);
                next.setShape(gameSet[k + 1]);
                gradeCurrent(board, weights.get(i));
                cleared += makeMove(board); //Метод возвращает количество очищенных данным ходом линий
                grades.clear();
            }
            Pair<Integer, double[]> weightGraded = new Pair<>(cleared, weights.get(i));
            gradeWeights.add(weightGraded);
        }
    }

    /**
     * Метод, который отбирает два случайных генома для скрещивания
     */
    private static void selection() {
        Random chooser = new Random();
        for (int i = 0; i < GENOMES_NS; i++) { //Выбор нужного количества случайных пар, потомки которых заменят худшие геномы
            Pair rand1 = gradeWeights.get(chooser.nextInt(gradeWeights.size()));
            int par1Grade = (int) rand1.getKey();
            double[] par1 = (double[]) rand1.getValue();
            Pair rand2 = gradeWeights.get(chooser.nextInt(gradeWeights.size()));
            while (rand1 == rand2) {
                rand2 = gradeWeights.get(chooser.nextInt(gradeWeights.size()));
            }
            int par2Grade = (int) rand2.getKey();
            double[] par2 = (double[]) rand2.getValue();
            Double proportion = par2Grade != 0 ? ((double) par1Grade) / par2Grade : 1.0; //Отношение оценок эффективности родителей
            ArrayList<double[]> parents = new ArrayList<>(Arrays.asList(par1, par2)); //Список родителей
            genomsForCrossover.add(new Pair(proportion, parents)); //Добавление пары случайных геномов, которые будут участвовать в скрещивании
        }
    }

    /**
     * Метод, который возвращает новый геном на основе геномов родителей и отношения их эффективности
     */
    private static double[] crossover(Pair<Double, ArrayList<double[]>> parents) {
        double[] child = new double[4];
        for (int i = 0; i < 4; i++) {
            child[i] = parents.getValue().get(0)[i] * parents.getKey() + parents.getValue().get(1)[i];  //Линейная комбинация родительских параметров
        }
        mutation(child); //Мутация ребёнка
        return child;
    }

    /**
     * Метод, добавляющий мутацию в новый геном (5% шанс на изменение случайной характеристики на 0.3)
     */
    private static void mutation(double[] child) {
        double chance = Math.random();
        if (chance < 0.05) {
            Random mutate = new Random();
            child[mutate.nextInt(child.length)] += chance < 0.025 ? 0.3 : -0.3;
        }
    }

    /**
     * Метод создания нового поколения, удаляющий наименее приспособленные геномы и добавляющий новые геномы
     */
    private static void createNewGeneration() {
        gradeWeights.sort(Comparator.comparing(Pair::getKey));
        for (int i = 0; i < GENOMES_NS; i++) {
            weights.remove(gradeWeights.get(0).getValue()); //Удаление генома с худшим результатом
            weights.add(crossover(genomsForCrossover.get(0))); //Добавление нового ребёнка
            genomsForCrossover.remove(0); //Удаление пары геномов из списка для скрещивания
        }
    }

    /**
     * Здесь и далее приведены методы из класса решателя с незначительными изменениями (в некоторых методах передаются
     * штрафные (весовые) коэффициенты из геномов, проходящих соревнование в данный момент)
     */
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
}
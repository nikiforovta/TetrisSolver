import game.Board;
import game.Shape;
import game.Tetris;
import game.Tetrominoe;
import org.junit.jupiter.api.Test;
import solver.Solver;

import static game.Board.BOARD_HEIGHT;
import static game.Board.BOARD_WIDTH;
import static org.junit.jupiter.api.Assertions.*;

class TestTetris {

    private Tetris testTetris = new Tetris();
    private Board testBoard = new Board(testTetris);

    //Проверка определения фигуры на игровом поле по координате
    @Test
    void TestShapeAt() {
        for (int i = 0; i < BOARD_WIDTH; i++) {
            for (int j = 0; j < BOARD_HEIGHT; j++) {
                assertEquals(Tetrominoe.NoShape, testBoard.shapeAt(i, j));
            }
        }
    }

    //Проверка соответствия следующей фигуры
    @Test
    void newPieceTest() {
        for (int i = 0; i < 150; i++) {
            Shape next = testBoard.getNextPiece();
            testBoard.newPiece();
            assertEquals(next, testBoard.getCurPiece());
        }
    }

    //Проверка возможности перемещения фигуры на игровом поле
    @Test
    void testTryMove() {
        Shape shape = new Shape();
        shape.setShape(Tetrominoe.I);
        for (int i = 0; i < BOARD_WIDTH; i++) {
            for (int j = 1; j < BOARD_HEIGHT - 2; j++) { //Значения связаны с координатой центра фигуры
                assertTrue(testBoard.tryMove(shape, i, j)); //На пустом игровом поле можно разместить везде, где возможно размещение центра фигуры
            }
        }
        assertFalse(testBoard.tryMove(shape, 0, 0)); //Не можем разместить центр фигуры в левом нижнем углу игрового поля
    }

    //Проверка очистки заполненных линий
    @Test
    void testRemoveFullLines() {
        Tetrominoe[][] board = testBoard.getBoard();
        for (int i = 0; i < BOARD_WIDTH; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = Tetrominoe.O;
            }
        }
        testBoard.removeFullLines();
        assertEquals(3, testBoard.numLinesRemoved);
        assertEquals(700, testBoard.scoreInt);
        for (int i = 0; i < BOARD_WIDTH; i++) {
            for (int j = 0; j < BOARD_HEIGHT; j++) {
                board[i][j] = Tetrominoe.O;
            }
        }
        assertThrows(IllegalStateException.class, () -> testBoard.removeFullLines()); //Не может быть очищено больше 4 линий одновременно
    }

    //Проверка возможности размещения фигуры на экспериментальном поле
    @Test
    void testCanPlace() {
        Tetrominoe[][] board = testBoard.getBoard();
        for (int i = 0; i < BOARD_WIDTH; i++) {
            for (int j = 0; j < BOARD_HEIGHT; j++) {
                board[i][j] = Tetrominoe.O; //Заполняем всё игровое поле
            }
        }
        Shape shape = new Shape();
        shape.setShape(Tetrominoe.I);
        for (int i = 0; i < BOARD_WIDTH; i++) {
            for (int j = 1; j < BOARD_HEIGHT - 2; j++) {
                assertFalse(Solver.canPlace(board, i, j, shape)); //Проверяем невозможность размещения фигуры на поле
            }
        }
    }

    //Проверка числа очищенных линий
    @Test
    void testCountClearedLines() {
        Tetrominoe[][] board = testBoard.getBoard();
        assertEquals(0, Solver.countClearedLines(board));
        for (int i = 0; i < BOARD_WIDTH; i++) {
            for (int j = 0; j < BOARD_HEIGHT; j++) {
                board[i][j] = Tetrominoe.O; //Заполняем экспериментальное поле
            }
        }
        assertEquals(22, Solver.countClearedLines(board)); //Проверяем, что очистилось всё поле
    }

    //Проверка размещения фигруы на экспериментальном поле
    @Test
    void testPlaceOnBoard() {
        Tetrominoe[][] board = testBoard.getBoard();
        Shape shape = new Shape();
        shape.setShape(Tetrominoe.O);
        Solver.placeOnBoard(0, 0, board, shape); //Размещаем фигуру на экспериментальном поле
        for (int i = 0; i < 1; i++) {
            for (int j = 0; j < 1; j++) {
                assertEquals(Tetrominoe.O, board[i][j]);  //Проверяем нахождение размещённой фигуры на экспериментальном поле
            }
        }
    }

    //Проверка подсчёта высоты колонок экспериментального поля
    @Test
    void testGradeHeights() {
        Tetrominoe[][] board = testBoard.getBoard();
        for (int i = 0; i < BOARD_WIDTH; i++) {
            board[i][i + 5] = Tetrominoe.T; //Рзмещаем фигуру на поле
        }
        int[] heights = Solver.gradeHeights(board); //Подсчитываем высоты колонок
        for (int i = 0; i < BOARD_WIDTH; i++) {
            assertEquals(i + 5, heights[i]); //Проверяем на равенство высот
        }
    }
}
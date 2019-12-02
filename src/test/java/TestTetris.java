import game.Board;
import game.Shape;
import game.Tetris;
import game.Tetrominoe;
import org.junit.jupiter.api.Test;

import static game.Board.BOARD_HEIGHT;
import static game.Board.BOARD_WIDTH;
import static org.junit.jupiter.api.Assertions.*;

class TestTetris {

    private Tetris testTetris = new Tetris();
    private Board testBoard = new Board(testTetris);

    @Test
    void TestShapeAt() {
        for (int i = 0; i < BOARD_WIDTH; i++) {
            for (int j = 0; j < BOARD_HEIGHT; j++) {
                assertEquals(Tetrominoe.NoShape, testBoard.shapeAt(i, j));
            }
        }
    }

    @Test
    void newPieceTest() {
        for (int i = 0; i < 150; i++) {
            Shape next = testBoard.getNextPiece();
            testBoard.newPiece();
            assertEquals(next, testBoard.getCurPiece());
        }
    }

    @Test
    void testTryMove() {
        Shape shape = new Shape();
        shape.setShape(Tetrominoe.I);
        for (int i = 0; i < BOARD_WIDTH; i++) {
            for (int j = 1; j < BOARD_HEIGHT - 2; j++) { //Значения связаны с координатой центра фигуры
                assertTrue(testBoard.tryMove(shape, i, j));
            }
        }
    }

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
        assertThrows(IllegalStateException.class, () -> testBoard.removeFullLines());
    }
}

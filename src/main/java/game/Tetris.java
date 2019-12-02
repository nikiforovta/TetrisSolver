package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import static solver.Solver.solver;

public class Tetris extends JFrame {
    private JLabel score;
    private JLabel lines;
    private JLabel time;
    private static final int TIME_TO_FALL = 1200; //Время падения фигуры на игровом поле. Чем меньше значение, тем быстрее идёт игра
    private static final int TIME_DECREASE = 200; //Величина, на которую уменьшается время падения фигуры при увеличении скорости игры

    public Tetris() {
        score = new JLabel("0");
        lines = new JLabel("0");
        time = new JLabel("0h 00mm 00ss");

        Board board = new Board(this);
        add(board, BorderLayout.WEST);
        board.start();
        board.setBorder(BorderFactory.createLineBorder(Color.black));

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        JMenu menu = new JMenu("Game");
        JMenuItem newGame = new JMenuItem("New");
        JMenu settings = new JMenu("Settings");
        JMenuItem exit = new JMenuItem("Exit");
        JCheckBoxMenuItem solver = new JCheckBoxMenuItem("Turn on/off solver");
        JMenu speed = new JMenu("Game Speed");
        JRadioButton speed1 = new JRadioButton("1", true);
        JRadioButton speed2 = new JRadioButton("2", false);
        JRadioButton speed3 = new JRadioButton("3", false);
        JRadioButton speed4 = new JRadioButton("4", false);
        JRadioButton speed5 = new JRadioButton("5", false);
        ActionListener speedIncrease = actionEvent -> {
            AbstractButton aButton = (AbstractButton) actionEvent.getSource();
            board.timer.setDelay(TIME_TO_FALL - TIME_DECREASE * Integer.parseInt(aButton.getText()));
        };
        speed1.addActionListener(speedIncrease);
        speed2.addActionListener(speedIncrease);
        speed3.addActionListener(speedIncrease);
        speed4.addActionListener(speedIncrease);
        speed5.addActionListener(speedIncrease);
        speed.add(speed1);
        speed.add(speed2);
        speed.add(speed3);
        speed.add(speed4);
        speed.add(speed5);
        ButtonGroup gameSpeed = new ButtonGroup();
        gameSpeed.add(speed1);
        gameSpeed.add(speed2);
        gameSpeed.add(speed3);
        gameSpeed.add(speed4);
        gameSpeed.add(speed5);
        solver.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.SHIFT_MASK));
        settings.add(solver).addActionListener(e -> {
            Board.isSolverOn = !Board.isSolverOn;
            solver(board, Board.isSolverOn);
        });
        settings.add(speed);
        menu.add(newGame).addActionListener(e -> board.start());
        menu.addSeparator();
        menu.add(settings);
        menu.addSeparator();
        menu.add(exit).addActionListener(e -> System.exit(0));
        JMenu help = new JMenu("Help");
        JMenuItem about = new JMenuItem("About");
        JMenuItem control = new JMenuItem("Controls");
        help.add(about).addActionListener(e -> JOptionPane.showMessageDialog(Tetris.this, "Это Тетрис. С решателем. " +
                        "Не ругайте его, он теперь учится.",
                "About", JOptionPane.INFORMATION_MESSAGE));
        help.add(control).addActionListener(e -> JOptionPane.showMessageDialog(Tetris.this,
                new String[]{"Вверх - поворот влево", "Влево - переместить влево", "Вправо - переместить вправо",
                        "Вниз - опустить на одну линию", "Пробел - опустить вниз", "P - пауза", "R - рестарт",
                        "Shift + S - включить/выключить решателя"},
                "Controls",
                JOptionPane.WARNING_MESSAGE));
        menuBar.add(menu);
        menuBar.add(help);
        Next next = Board.getNext();

        JPanel stats = new JPanel();
        stats.setLayout(new BoxLayout(stats, BoxLayout.Y_AXIS));
        stats.add(Box.createVerticalStrut(50));
        stats.add(new JLabel("Score :"));
        stats.add(Box.createVerticalStrut(20));
        stats.add(score);
        stats.add(Box.createVerticalStrut(30));
        stats.add(new JLabel("Lines :"));
        stats.add(Box.createVerticalStrut(20));
        stats.add(lines);
        stats.add(Box.createVerticalStrut(30));
        stats.add(new JLabel("Time :"));
        stats.add(Box.createVerticalStrut(20));
        stats.add(time);
        stats.add(Box.createVerticalStrut(80));
        stats.add(new JLabel("Next :"));
        stats.add(Box.createVerticalStrut(40));
        stats.add(next);
        add(stats, BorderLayout.EAST);

        setSize(500, 600);
        setTitle("Tetris solver");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        Tetris tetris = new Tetris();
        tetris.setLocationRelativeTo(null);
        tetris.setVisible(true);
        tetris.setResizable(false);
    }

    JLabel getScore() {
        return score;
    }

    JLabel getLines() {
        return lines;
    }

    JLabel getTime() {
        return time;
    }
}
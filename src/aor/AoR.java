package aor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;

public class AoR {
    public static void main(String[] args) {
        final JFrame frame = new JFrame();
        frame.setTitle("Age of Renaissance");

        final Board board = new Board(frame);
        board.load(new File("/Users/petri/src/AoR/map.dat"));
        frame.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    board.esc();
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {
            }
        });
        frame.setContentPane(board);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setUndecorated(true);
        frame.pack();
        frame.setVisible(true);
    }
}
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;

public class AoR {
    public static void main(String[] args) {
        final JFrame frame = new JFrame();
        frame.setTitle("Age of Renaissance");

        final MapEditor editor = new MapEditor();
        frame.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    editor.esc();
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

        frame.setContentPane(editor);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setUndecorated(true);
        frame.pack();
        frame.setVisible(true);
    }
}
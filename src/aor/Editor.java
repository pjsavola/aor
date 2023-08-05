package aor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

public class Editor {
    public static void main(String[] args) {
        final JFrame frame = new JFrame();
        frame.setTitle("Map Editor");

        final MapEditor editor = new MapEditor(frame);
        editor.load(new File("/Users/petri/src/AoR/map.dat"));
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
        final MenuBar menuBar = new MenuBar();
        final Menu fileMenu = new Menu("File");
        final MenuItem saveItem = new MenuItem("Save");
        final MenuItem loadItem = new MenuItem("Load");
        saveItem.addActionListener(l -> editor.save());
        loadItem.addActionListener(l -> editor.load());
        fileMenu.add(saveItem);
        fileMenu.add(loadItem);
        menuBar.add(fileMenu);
        frame.setMenuBar(menuBar);

        frame.setContentPane(editor);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setUndecorated(true);
        frame.pack();
        frame.setVisible(true);
    }
}
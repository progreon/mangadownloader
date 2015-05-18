/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package manga.downloader.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author marco
 */
public class LogPanel extends JPanel {

    private static final LogPanel instance = new LogPanel();

    private final JTextArea textPane;
    private final JScrollPane scr;

    private LogPanel() {
        textPane = new JTextArea();
        textPane.setEditable(false);
        JPanel borderPane = new JPanel(new BorderLayout());
        borderPane.add(textPane);
        scr = new JScrollPane(borderPane);
        scr.setPreferredSize(new Dimension(600, 400));
        this.add(scr);
    }

    public static LogPanel getInstance() {
        return instance;
    }

    public static void appendText(String text) {
        append(text);
    }

    public static void appendLine(String line) {
        append(line + "\n");
    }

    private static void append(String str) {
        instance.textPane.append(str);
        instance.textPane.setCaretPosition(instance.textPane.getText().length());

//        System.out.print(str);
    }

}

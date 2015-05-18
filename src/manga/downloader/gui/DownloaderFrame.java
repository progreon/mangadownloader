/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package manga.downloader.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import manga.downloader.Chapter;
import manga.downloader.Downloader;

/**
 *
 * @author marco
 */
public class DownloaderFrame extends JFrame {

    private final String baseURL = "http://mangafox.me/manga/";
    private String manga;

    private final JPanel URLPanel;
    private final JTextField txtMangaFoxLink;
    private final JButton btnProcessLink;

    private final JPanel chapterPanel;
    private final ChapterTable chapterTable;
    private final JScrollPane scrChapters;
    private final JButton btnDownload;

    public DownloaderFrame() throws HeadlessException {
        super("Manga Downloader");

        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());

        URLPanel = new JPanel(new FlowLayout());
        JLabel lblMangaFoxLink = new JLabel("Manga Fox Link:");
        JLabel lblBaseURL = new JLabel(baseURL);
        txtMangaFoxLink = new JTextField("tsubasa_reservoir_chronicle", 20);
        txtMangaFoxLink.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                btnProcessLink.doClick();
            }
        });
        btnProcessLink = new JButton("Process");
        btnProcessLink.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Thread t = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        btnProcessLink.setEnabled(false);
                        btnDownload.setEnabled(false);
                        chapterTable.setEnabled(false);
                        processURL();
                        chapterTable.setEnabled(true);
                        btnProcessLink.setEnabled(true);
                    }
                });
                t.start();
            }
        });
        URLPanel.add(lblMangaFoxLink);
        URLPanel.add(lblBaseURL);
        URLPanel.add(txtMangaFoxLink);
        URLPanel.add(btnProcessLink);

        contentPane.add(URLPanel, BorderLayout.NORTH);

        chapterPanel = new JPanel(new BorderLayout());
        chapterTable = new ChapterTable(null);
        scrChapters = new JScrollPane(chapterTable);
        scrChapters.setPreferredSize(new Dimension(100, 400));
        scrChapters.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        btnDownload = new JButton("Download selected");
        btnDownload.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Thread t = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        btnProcessLink.setEnabled(false);
                        btnDownload.setEnabled(false);
                        chapterTable.setEnabled(false);
                        downloadSelectedChapters();
                        chapterTable.setEnabled(true);
                        btnDownload.setEnabled(true);
                        btnProcessLink.setEnabled(true);
                    }
                });
                t.start();
            }
        });
        btnDownload.setEnabled(false);
        chapterPanel.add(scrChapters, BorderLayout.CENTER);
        chapterPanel.add(btnDownload, BorderLayout.SOUTH);
        contentPane.add(chapterPanel, BorderLayout.WEST);

        contentPane.add(LogPanel.getInstance(), BorderLayout.CENTER);

        this.setContentPane(contentPane);
        this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void processURL() {
        manga = txtMangaFoxLink.getText();
        if (!manga.equals("")) {
            String mangaURL = baseURL + manga;
            if (!mangaURL.endsWith("/")) {
                mangaURL += "/";
            }
            LogPanel.appendLine("Processing URL: " + mangaURL);
            try {
                String mainHtml = Downloader.getData(new URL(mangaURL)); // try if manga exists
                loadChapterList(mainHtml);
            } catch (MalformedURLException ex) {
                LogPanel.appendLine("Failed to download manga, is the URL correct?");
            } catch (IOException ex) {
                LogPanel.appendLine("Failed to download manga, is the URL correct?");
            }
        } else {
            LogPanel.appendLine("Please enter a valid Manga Fox URL!");
        }
    }

    private List<Chapter> getAllChaptersURLs(String mainPageHtml) {
        List<Chapter> chs = new ArrayList<>();

        String hrefMain = baseURL + manga;
//        String hrefMain = "";

        boolean failed = false;

        String origRegEx = "<a.*?class=\"original\".*?href=\"(.*?)\"";
        Pattern origP = Pattern.compile(origRegEx);
        Matcher origM = origP.matcher(mainPageHtml);
        if (origM.find() && origM.groupCount() > 0) {
            hrefMain = origM.group(1);
        }
        String chapRegEx = "<a.*?href=\"(" + hrefMain + "(.*?))\".*?class=\"tips\".*?>";
        Pattern currChapP = Pattern.compile(chapRegEx);
        Matcher currM = currChapP.matcher(mainPageHtml);
        while (!failed && currM.find() && currM.groupCount() > 0) {
            String chapterString = currM.group(2);
            try {
                chapterString = chapterString.substring((hrefMain.length() < chapterString.length() - 7 ? hrefMain.length() : 0), chapterString.lastIndexOf("/1.html"));
                Chapter ch = new Chapter(currM.group(1), chapterString);
                chs.add(ch);
            } catch (IndexOutOfBoundsException ex) {
                failed = true;
                chs.clear();
                LogPanel.appendLine("Could not get the correct URLs, please try again!");
                Logger.getLogger(DownloaderFrame.class.toString()).log(Level.SEVERE, "Could not get the chapter string: {0}", chapterString);
            }
        }
        try {
            chs.sort(new Chapter.ChapterComparator());
        } catch (RuntimeException ex) {
            chs.sort(new Chapter.VolumeChapterComparator());
        }

        LogPanel.appendLine("Loaded URLs:");
        for (Chapter ch : chs) {
            LogPanel.appendLine("\t" + ch.getCurrentChapter() + ": " + ch.getFirstPageURL());
        }
        return chs;
    }

    private void loadChapterList(String mainPageHtml) {
        List<Chapter> chs = getAllChaptersURLs(mainPageHtml);
        if (chs != null && chs.size() > 0) {
            chapterTable.setChapters(chs);
            btnDownload.setEnabled(true);
            scrChapters.getVerticalScrollBar().setValue(0);
        } else {
            chapterTable.setChapters(null);
            btnDownload.setEnabled(false);
        }
    }

    private void downloadSelectedChapters() {
        LogPanel.appendLine("Downloading selected chapters ...");
        for (Chapter c : chapterTable.getSelectedChapters()) {
            try {
//                c.downloadChapter(new File(System.getProperty("user.home") + File.separator + manga));
                File execDir = new File(DownloaderFrame.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
                c.downloadChapter(new File(execDir, manga));
            } catch (URISyntaxException ex) {
                Logger.getLogger(DownloaderFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        LogPanel.appendLine("Done downloading selected chapters!");
    }

    private final class ChapterTable extends JTable {

        private ChapterTableModel model;

        public ChapterTable(List<Chapter> chapters) {
            setChapters(chapters);
        }

        public void setChapters(List<Chapter> chapters) {
            this.model = new ChapterTableModel(chapters);
            this.setModel(model);
//            this.getColumnModel().getColumn(0).setPreferredWidth(0);
            this.getColumnModel().getColumn(0).setMaxWidth(25);
//            this.getColumnModel().getColumn(0).setWidth(5);
            this.getColumnModel().getColumn(0).setResizable(false);
        }

        public List<Chapter> getSelectedChapters() {
            return model.getSelectedChapters();
        }

        private class ChapterTableModel extends AbstractTableModel {

            private final List<Chapter> chapters;
            private final List<Boolean> checks;

            public ChapterTableModel(List<Chapter> chapters) {
                this.chapters = (chapters != null ? chapters : new ArrayList<>());
                this.checks = new ArrayList<>(this.chapters.size());
                for (int i = 0; i < this.chapters.size(); i++) {
                    this.checks.add(Boolean.FALSE);
                }
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return columnIndex == 0;
            }

            @Override
            public int getRowCount() {
                return this.chapters.size();
            }

            @Override
            public int getColumnCount() {
                return 2;
            }

            @Override
            public String getColumnName(int column) {
                if (column == 1) {
                    return "Chapter";
                } else {
                    return "";
                }
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return Boolean.class;
                } else {
                    return String.class;
                }
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                Object value = null;
                if (columnIndex == 0) {
                    value = checks.get(rowIndex);
                } else if (columnIndex == 1) {
                    Chapter c = chapters.get(rowIndex);
                    value = (!c.getCurrentVolume().equals("") ? c.getCurrentVolume() + "/" : "") + c.getCurrentChapter();
                }
                return value;
            }

            @Override
            public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
                if (columnIndex == 0) {
                    checks.set(rowIndex, (Boolean) aValue);
                }
            }

            public List<Chapter> getSelectedChapters() {
                List<Chapter> chsSelected = new ArrayList<>();

                for (int i = 0; i < chapters.size(); i++) {
                    if (checks.get(i)) {
                        chsSelected.add(chapters.get(i));
                    }
                }

                return chsSelected;
            }

        }
    }

}

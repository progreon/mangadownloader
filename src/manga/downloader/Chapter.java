/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package manga.downloader;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import manga.downloader.gui.LogPanel;

/**
 *
 * @author marco
 */
public class Chapter {

    private final String firstPageURL;

    private String currentVolume = "";
    private String currentChapter = "";

    public Chapter(String firstPageURL, String chapterString) {
        this.firstPageURL = firstPageURL;
        String regExpV = "(v.*)/";
        Pattern pV = Pattern.compile(regExpV);
        Matcher mV = pV.matcher(chapterString);
        if (mV.find() && mV.groupCount() > 0) {
            currentVolume = mV.group(1);
        }
        String regExpC = "(c.*)";
        Pattern pC = Pattern.compile(regExpC);
        Matcher mC = pC.matcher(chapterString);
        if (mC.find() && mC.groupCount() > 0) {
            currentChapter = mC.group(1);
        }
    }

    public String getFirstPageURL() {
        return firstPageURL;
    }

    public String getCurrentVolume() {
        return currentVolume;
    }

    public String getCurrentChapter() {
        return currentChapter;
    }

    public void downloadChapter(File parentFolder) {
        parentFolder.mkdir();
        File volumeFolder = new File(parentFolder, (currentVolume.equals("") ? "Unknown Volume" : currentVolume));
        volumeFolder.mkdir();
        File chapterFolder = new File(volumeFolder, (currentChapter.equals("") ? "Unknown Chapter" : currentChapter));
        chapterFolder.mkdir();

        LogPanel.appendText("Downloading " + (currentVolume.equals("") ? "Unknown Volume" : "Volume " + currentVolume) + " ");
        LogPanel.appendText((currentChapter.equals("") ? "Unknown Chapter" : "Chapter " + currentChapter));
        LogPanel.appendLine(" to " + parentFolder.getAbsolutePath() + " ...");

        String currentPage = firstPageURL;
        String regExImg = ".*onclick=\"return enlarge\\(\\)\".*?<img src=\"(.*?)\" onerror=.*";
        Pattern pImg = Pattern.compile(regExImg);
        String regExCurrNo = "var current_page=(.*?);";
        Pattern pCurrNo = Pattern.compile(regExCurrNo);
        String regExNext = ".*href=\"((?!http).*?)\".*?class=\"btn next_page\".*";
        Pattern pNext = Pattern.compile(regExNext);

        try {
            while (!currentPage.equals("")) {
                URL currentPageURL = new URL(currentPage);
                LogPanel.appendLine("\t" + currentPageURL.toExternalForm());
                String html = Downloader.getData(currentPageURL);
                Matcher mImg = pImg.matcher(html);
                if (mImg.find() && mImg.groupCount() > 0) {
                    String image = mImg.group(1);
                    String relImagePath = image.substring(image.lastIndexOf("/") + 1);

                    String prefix = "";
                    Matcher mCurrNo = pCurrNo.matcher(html);
                    if (mCurrNo.find() && mCurrNo.groupCount() > 0) {
                        prefix = mCurrNo.group(1) + "_";
                    }

                    File imageFile = new File(chapterFolder, prefix + relImagePath);
                    Downloader.downloadFile(new URL(image), imageFile);
                }
                Matcher mNext = pNext.matcher(html);
                if (mNext.find() && mNext.groupCount() > 0) {
                    String next = firstPageURL.substring(0, firstPageURL.lastIndexOf("/") + 1) + mNext.group(1);
                    currentPage = next;
                } else {
                    currentPage = "";
                }
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(Chapter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Chapter.class.getName()).log(Level.SEVERE, null, ex);
        }
        LogPanel.appendLine("Done");
    }

    public static class ChapterComparator implements Comparator<Chapter> {

        @Override
        public int compare(Chapter o1, Chapter o2) {
            if (o1.currentChapter.equals(o2.currentChapter)) {
                throw new RuntimeException();
            }
            return o1.getCurrentChapter().compareTo(o2.getCurrentChapter());
        }
    }
    
    public static class VolumeChapterComparator implements Comparator<Chapter> {

        @Override
        public int compare(Chapter o1, Chapter o2) {
            String v1 = o1.currentVolume;
            String v2 = o2.currentVolume;
            String c1 = o1.currentChapter;
            String c2 = o2.currentChapter;
            int compareV = v1.compareTo(v2);
            if (compareV == 0) {
                return c1.compareTo(c2);
            } else {
                return compareV;
            }
        }
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package manga.downloader;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

/**
 *
 * @author marco
 */
public class Downloader {

    public static String getData(URL url) throws IOException {
        StringBuilder strBuilder = new StringBuilder();
        Scanner s = new Scanner(url.openStream());
        while (s.hasNext()) {
            strBuilder.append(s.nextLine()).append("\n");
        }
        return strBuilder.toString();
    }

    public static void downloadFile(URL url, File dest) throws IOException {
        URLConnection conn = url.openConnection();
        InputStream is = conn.getInputStream();
//        long max = conn.getContentLength();
//        outText.setText(outText.getText() + "\n" + "Downloading file...\nUpdate Size(compressed): " + max + " Bytes");
        BufferedOutputStream fOut = new BufferedOutputStream(new FileOutputStream(dest));
        byte[] buffer = new byte[32 * 1024];
        int bytesRead = 0;
//        int in = 0;
        while ((bytesRead = is.read(buffer)) != -1) {
//            in += bytesRead;
            fOut.write(buffer, 0, bytesRead);
        }
        fOut.flush();
        fOut.close();
        is.close();
//        outText.setText(outText.getText() + "\nDownload Complete!");
    }
}

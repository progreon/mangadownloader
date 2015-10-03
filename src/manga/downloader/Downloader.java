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
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author marco
 */
public class Downloader {

    public static String getData(URL url) throws IOException {
        URLConnection conn = url.openConnection();
        String encoding = conn.getHeaderField("Content-Encoding");
//        System.out.println(encoding);
        if (encoding != null && encoding.equals("gzip")) {
            Reader reader = null;
            StringWriter writer = null;
            try {
                InputStream is = conn.getInputStream();
                GZIPInputStream gzipIs = new GZIPInputStream(is);
                reader = new InputStreamReader(gzipIs, "utf-8");
                writer = new StringWriter();

                char[] buffer = new char[32 * 1024];
                int length = 0;
                while ((length = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, length);
                }
            } catch (IOException ex) {
                Logger.getLogger(Downloader.class.getName()).log(Level.WARNING, null, ex);
            } finally {
                if (writer != null) {
                    writer.close();
                }
                if (reader != null) {
                    reader.close();
                }
            }
            if (writer != null) {
                return writer.toString();
            } else {
                throw new IOException("Could not decode html from: " + url.getPath());
            }
        } else {
            StringBuilder strBuilder = new StringBuilder();
            Scanner s = new Scanner(url.openStream());
            while (s.hasNext()) {
                strBuilder.append(s.nextLine()).append("\n");
            }
            return strBuilder.toString();
        }
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

package ru.gb.chat.client;

import javafx.application.Platform;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HistoryService {

    private static String HISTORY_FILE = "history.txt";

    private static int HISTORY_SIZE = 100;

    public static String load() {

        File file = new File(HISTORY_FILE);
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return "";
            }
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            int i = 0;
            while ((line = br.readLine()) != null)
            {
                if (i < HISTORY_SIZE)
                {
                    stringBuilder.append(line);
                    stringBuilder.append("\n");
                    i++;
                }
                else break;
            }
            System.out.println(stringBuilder.toString());
            return stringBuilder.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }
         return "";
    }


    public static void save(String text) {
        Platform.runLater(() -> {

            File file = new File(HISTORY_FILE);
            try (OutputStream out = new FileOutputStream(file)) {
                    out.write(text.getBytes(StandardCharsets.UTF_8));

            } catch (IOException e) {
                e.printStackTrace();
            }

        });
    }
}

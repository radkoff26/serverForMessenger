package org.example.crypt;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;

import static org.example.models.Constants.FILENAME;

public class Utility {

    private static final ArrayList<Character> symbols = new ArrayList<>();

    static {
        for (int i = 'a'; i <= 'z'; i++) {
            symbols.add((char) i);
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            symbols.add((char) i);
        }
        for (int i = '0'; i <= '9'; i++) {
            symbols.add((char) i);
        }
    }

    private static final int N = symbols.size(), LENGTH = 16;

    public static String generateFilename() {
        File file = Paths.get(FILENAME).toFile();
        HashSet<String> filenames = new HashSet<>();
        File[] array = file.listFiles();
        if (array != null) {
            for (File mFile : array) {
                filenames.add(mFile.getName().replace(".png", ""));
            }
        }
        StringBuilder result = new StringBuilder();
        while (filenames.contains(result.toString()) || result.toString().equals("")) {
            result = new StringBuilder();
            for (int i = 0; i < LENGTH; i++) {
                result.append(symbols.get((int) (Math.random() * N)));
            }
        }
        return result.toString();
    }
}

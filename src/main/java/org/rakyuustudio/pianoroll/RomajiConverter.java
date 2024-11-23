package org.rakyuustudio.pianoroll;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.rakyuustudio.util.MessageBox;

public class RomajiConverter {
    private static final Properties romajiMap = new Properties();
    private static final Map<String, List<String>> SPECIAL_CASES = new HashMap<>();
    
    static {
        try (InputStream input = RomajiConverter.class.getResourceAsStream("/romaji.properties");
             InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
            if (input == null) {
//                System.err.println("Could not find romaji.properties");
                MessageBox.showError("Error", "File Not Found Error",
                        "The mapping file \"romaji.properties\" can't be found.");
            } else {
                romajiMap.load(reader);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // 特殊情况
        SPECIAL_CASES.put("shi", Arrays.asList("し", "シ"));
        SPECIAL_CASES.put("chi", Arrays.asList("ち", "チ"));
        SPECIAL_CASES.put("tsu", Arrays.asList("つ", "ツ"));
    }
    
    public static List<String> convert(String romaji) {
        List<String> results = new ArrayList<>();
        String hiragana = toHiragana(romaji.toLowerCase());
        
        results.add(hiragana);  // 平假名
        results.add(romaji);    // 原始罗马音
        
        // 添加特殊情况
        if (SPECIAL_CASES.containsKey(romaji.toLowerCase())) {
            results.addAll(SPECIAL_CASES.get(romaji.toLowerCase()));
        }
        
        return results;
    }
    
    private static String toHiragana(String romaji) {
        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < romaji.length()) {
            boolean found = false;
            // 从最长的可能组合开始尝试
            for (int j = Math.min(romaji.length(), i + 4); j > i; j--) {
                String substr = romaji.substring(i, j);
                String hiragana = romajiMap.getProperty(substr);
                if (hiragana != null) {
                    result.append(hiragana);
                    i = j;
                    found = true;
                    break;
                }
            }
            if (!found) {
                result.append(romaji.charAt(i));
                i++;
            }
        }
        return result.toString();
    }
} 
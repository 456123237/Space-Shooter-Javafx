package application;

import java.io.*;
import java.util.*;

public class UserData {
    private static final String CSV_FILE = "userdata.csv";
    
    public static boolean registerUser(String username, String password) {
        if (userExists(username)) {
            return false;
        }
        
        try (FileWriter fw = new FileWriter(CSV_FILE, true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            // user_name,password,unlocked_levels,level1_score,level2_score,level3_score,level4_score,level5_score
            bw.write(String.format("%s,%s,%d,%d,%d,%d,%d,%d%n", 
                username, password, 1, 0, 0, 0, 0, 0));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean validateUser(String username, String password) {
        try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data[0].equals(username) && data[1].equals(password)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public static int getUnlockedLevel(String username) {
        try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data[0].equals(username)) {
                    return Integer.parseInt(data[2]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 1;
    }
    
    public static int getLevelHighScore(String username, int level) {
        try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data[0].equals(username)) {
                    return Integer.parseInt(data[2 + level]); // Level scores start from column 4
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    public static void updateLevelScore(String username, int level, int score) {
        List<String> lines = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data[0].equals(username)) {
                    int currentScore = Integer.parseInt(data[2 + level]);
                    if (score > currentScore) { // Only update if new score is higher
                        data[2 + level] = String.valueOf(score);
                    }
                    lines.add(String.join(",", data));
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(CSV_FILE))) {
            for (String line : lines) {
                bw.write(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void updateUnlockedLevel(String username, int level) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data[0].equals(username)) {
                    int currentLevel = Integer.parseInt(data[2]);
                    if (level > currentLevel) { // Only update if new level is higher
                        data[2] = String.valueOf(level);
                    }
                    lines.add(String.join(",", data));
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(CSV_FILE))) {
            for (String line : lines) {
                bw.write(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static boolean userExists(String username) {
        try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data[0].equals(username)) {
                    return true;
                }
            }
        } catch (IOException e) {
            if (!new File(CSV_FILE).exists()) {
                return false;
            }
            e.printStackTrace();
        }
        return false;
    }
} 
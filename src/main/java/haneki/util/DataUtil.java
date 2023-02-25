package haneki.util;

import java.awt.*;
import java.io.*;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataUtil {

    public static String readToString(File file) {
        String encoding = "UTF-8";
        Long filelength = file.length();
        byte[] filecontent = new byte[filelength.intValue()];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(filecontent);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            return new String(filecontent, encoding);
        } catch (UnsupportedEncodingException e) {
            System.err.println("[DataUtil]The OS does not support " + encoding);
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isSteam64id(String content){
        String pattern = "7656(\\d){13}";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(content);
        return m.matches();
    }

    public static boolean isNumericZidai(String str){
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))){
                return false;
            }
        }
        return true;
    }

    public static String readRandomLine(File f) {
        String result = null;
        Random rand = new Random();
        int n = 0;
        try {
            for(Scanner sc = new Scanner(f); sc.hasNext(); ) {
                ++n;
                String line = sc.nextLine();
                // System.out.println("n: "+n+" line: "+line);
                if(rand.nextInt(n) == 0)
                    result = line;
            }
        }catch (FileNotFoundException ignored){}
        return result;
    }

    /**
     * Converts a hex string to a color. If it can't be converted null is returned.
     * @param hex (i.e. #CCCCCCFF or CCCCCC)
     * @return Color
     */
    public static Color HexToColor(String hex)
    {
        hex = hex.replace("#", "");
        hex = hex.replace("0x", "");
        switch (hex.length()) {
            case 3:
                return new Color(
                        16*Integer.valueOf(hex.substring(0, 1), 16),
                        16*Integer.valueOf(hex.substring(1, 2), 16),
                        16*Integer.valueOf(hex.substring(2, 3), 16));
            case 4:
                return new Color(
                        16*Integer.valueOf(hex.substring(0, 1), 16),
                        16*Integer.valueOf(hex.substring(1, 2), 16),
                        16*Integer.valueOf(hex.substring(2, 3), 16),
                        16*Integer.valueOf(hex.substring(3, 4), 16));
            case 6:
                return new Color(
                        Integer.valueOf(hex.substring(0, 2), 16),
                        Integer.valueOf(hex.substring(2, 4), 16),
                        Integer.valueOf(hex.substring(4, 6), 16));
            case 7:
                return new Color(
                        Integer.valueOf(hex.substring(0, 2), 16),
                        Integer.valueOf(hex.substring(2, 4), 16),
                        Integer.valueOf(hex.substring(4, 6), 16),
                        16*Integer.valueOf(hex.substring(6, 7), 16));
            case 8:
                return new Color(
                        Integer.valueOf(hex.substring(0, 2), 16),
                        Integer.valueOf(hex.substring(2, 4), 16),
                        Integer.valueOf(hex.substring(4, 6), 16),
                        Integer.valueOf(hex.substring(6, 8), 16));
            default:
                return Color.decode("#"+hex);
        }
    }
}

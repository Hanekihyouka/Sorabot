package haneki.util;

import java.io.*;
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
}

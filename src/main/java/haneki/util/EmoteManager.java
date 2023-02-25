package haneki.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Date;


public class EmoteManager {
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/data_oj";
    static final String USER = "guest";
    static final String PASS = "Sumika!System2";
    Connection conn = null;
    PreparedStatement stmt = null;
    public String generator(String[] content){
        return write(combine(content));
    }

    public String write(BufferedImage bufferedImage){
        try {
            String imgPath = "./data/sender/" + (new Date().getTime()) + ".png" ;
            ImageIO.write(bufferedImage,"png",new File(imgPath));
            return imgPath;
        }catch (IOException e){
            return "./data/empty64.png";
        }
    }

    public File getImageByName(String emoteKey){
        String emoteFilePath = "./data/.emote/" + emoteKey;
        File f = new File(emoteFilePath);
        if (f.exists()){
            return f;
        }else {
            emoteFilePath = "./data/";
            if (emoteKey.contains("\r")|emoteKey.contains("\n")){
                return new File("./data/empty64.png");
            } else if (emoteKey.matches("[ _]*")) {
                return new File("./data/empty64.png");
            }
            try {
                Class.forName(JDBC_DRIVER);
                System.out.println("[Emote]连接数据库...");
                conn = DriverManager.getConnection(DB_URL, USER, PASS);
                System.out.println("[Emote]实例化Statement对象...");
                String sql = "select largeFile,smallFile from emote where emoteKey = ?;";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1,emoteKey);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()){
                    if (rs.getString("largeFile").equals("null")|rs.getString("largeFile").isEmpty()){
                        emoteFilePath += "emote/" + rs.getString("smallFile");
                    }else {
                        emoteFilePath += "emote/" + rs.getString("largeFile");
                    }
                }else {
                    emoteFilePath += "empty64.png";
                }
                rs.close();
                conn.close();
                stmt.close();
                return new File(emoteFilePath);
            }catch (Exception e){
                return new File("./data/empty64.png");
            }
        }
    }
    public BufferedImage combine(String[] content){
        int sizeH = 1;
        int sizeW = 0;
        int indexW = 0;
        // get size
        for (int i = 0; i < content.length; i++) {
            if (content[i].isEmpty()){
                continue;
            }
            if(content[i].contains("\r")|content[i].contains("\n")){
                sizeH++;
                indexW = 0;
            }else {
                indexW++;
            }
            if (indexW>sizeW){
                sizeW = indexW;
            }
        }
        if (sizeH>32){sizeH=32;}
        if (sizeW>32){sizeW=32;}
        BufferedImage bufferedImage = new BufferedImage(64*sizeW,64*sizeH,BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D graphics2D = bufferedImage.createGraphics();
        int x = 0;
        int y = 0;

        for (int indexC = 0; indexC < content.length; indexC++) {
            if (content[indexC].isEmpty()){
                continue;
            }else if (content[indexC].contains("\r")|content[indexC].contains("\n")){
                y++;
                x = 0;
                continue;
            }else if (content[indexC].matches("[ _]*")){
                x++;
                continue;
            }else if(content[indexC].startsWith("#") || content[indexC].startsWith("0x")){
                graphics2D.setColor(DataUtil.HexToColor(content[indexC]));
                graphics2D.fillRect(x*64,y*64,64,64);
                x++;
            } else {
                try {
                    graphics2D.drawImage(ImageIO.read(getImageByName(content[indexC])).getScaledInstance(64,64,Image.SCALE_FAST),x*64,y*64,64,64,null);
                    x++;
                }catch (IOException ignored){}
            }
        }

        graphics2D.dispose();
        return bufferedImage;
    }

}

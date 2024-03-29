package haneki.module.instance;

import haneki.module.BasicModule;
import haneki.module.MessageModule;
import haneki.util.DataUtil;
import haneki.util.HtmlTools;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.utils.ExternalResource;

import java.sql.*;
import java.util.Objects;

public class WebGetStat extends BasicModule implements MessageModule {
    static final String URL_HEADER = "http://interface.100oj.com/stat/render.php?steamid=";
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/data_ethene";
    static final String USER = "sora";
    static final String PASS = "***REMOVED***";
    Connection conn = null;
    PreparedStatement stmt = null;

    public WebGetStat(String module_name) {
        super(module_name);
    }

    @Override
    public void moduleReact() {

    }

    @Override
    public String getTiggerRegex() {
        return "(?i)#stat(s?).*|#sd.*";
    }

    @Override
    public MessageChain moduleReact(MessageChain message, MessageEvent messageEvent, Bot bot) {
        MessageChainBuilder messageChainBuilder = new MessageChainBuilder();
        String content = message.contentToString();
        String[] params = content.split(" ");
        if (params.length==2||params.length==3){
            switch (params[1]){
                case "bind":
                case "-b":
                    if (params.length==3){
                        if (DataUtil.isSteam64id(params[2])){
                            try {
                                Class.forName(JDBC_DRIVER);
                                System.out.println("[SD]连接数据库...");
                                conn = DriverManager.getConnection(DB_URL, USER, PASS);
                                System.out.println("[SD]实例化Statement对象...");
                                String sql = "insert into steamInfo(qq,steam64id) value(?,?) on duplicate key update steam64id = ?;";
                                stmt = conn.prepareStatement(sql);
                                stmt.setLong(1,messageEvent.getSender().getId());
                                stmt.setString(2, params[2]);
                                stmt.setString(3, params[2]);
                                stmt.executeUpdate();
                                conn.close();
                                stmt.close();
                                messageChainBuilder.append("绑定成功！你现在可以直接使用[#stat me [行数。可不填,默认为5]]来生成对应的统计图片。" +
                                        "\n重复使用本命令会覆盖旧的绑定内容。\n使用[#stat unbind|-u]来解除绑定。");
                                return messageChainBuilder.build();
                            }catch (SQLException | ClassNotFoundException e){
                                messageChainBuilder.append("JDBC错误。");
                                e.printStackTrace();
                                return messageChainBuilder.build();
                            }
                        }else {
                            messageChainBuilder.append("这不是一个有效的steam64id。它应该是以7656开头、总长度为17位的纯数字。\n如果你不知道什么是steam64id，推荐在网络上了解更多的相关知识。\n");
                            return messageChainBuilder.build();
                        }
                    }else {
                        messageChainBuilder.append("参数长度错误!\n" +
                                "正确用法[#stat bind|-b [steam64id]]");
                        return messageChainBuilder.build();
                    }
                case "unbind":
                case "-u":
                    try {
                        Class.forName(JDBC_DRIVER);
                        System.out.println("[SD]连接数据库...");
                        conn = DriverManager.getConnection(DB_URL, USER, PASS);
                        System.out.println("[SD]实例化Statement对象...");
                        String sql = "delete from steamInfo where qq = ?;";
                        stmt = conn.prepareStatement(sql);
                        stmt.setLong(1,messageEvent.getSender().getId());
                        stmt.executeUpdate();
                        conn.close();
                        stmt.close();
                        messageChainBuilder.append("删除成功！");
                        return messageChainBuilder.build();
                    }catch (SQLException | ClassNotFoundException e){
                        messageChainBuilder.append("JDBC错误。");
                        e.printStackTrace();
                        return messageChainBuilder.build();
                    }
                    /**
                     *
                     * 绷。
                     *
                     * **/
                case "type":
                case "-t":
                    if (params.length==2){
                        messageChainBuilder.append("请指定类型。");
                        return messageChainBuilder.build();
                    }else {
                        try {
                            Class.forName(JDBC_DRIVER);
                            System.out.println("[SD]连接数据库...");
                            conn = DriverManager.getConnection(DB_URL, USER, PASS);
                            System.out.println("[SD]实例化Statement对象...");
                            String sql = "update steamInfo set renderType = ? where qq = ?;";
                            stmt = conn.prepareStatement(sql);
                            stmt.setInt(1, Integer.parseInt(params[2]));
                            stmt.setLong(2,messageEvent.getSender().getId());
                            stmt.executeUpdate();
                            conn.close();
                            stmt.close();
                        }catch (SQLException | ClassNotFoundException e){
                            messageChainBuilder.append("JDBC错误。\n在数据库中查找你的信息失败。\n如果你还没有绑定过id，使用[#stat bind [steam64id]]来进行绑定。");
                            e.printStackTrace();
                            return messageChainBuilder.build();
                        }
                        messageChainBuilder.append("Done.");
                        return messageChainBuilder.build();
                    }
                case "pin":
                case "-p":
                    if (params.length==2){
                        messageChainBuilder.append("请指定类型。\n在 https://interface.100oj.com/stat/pininfo.html 查看所有类型。");
                        return messageChainBuilder.build();
                    }else {
                        String pin_name = "sp1_" +  Integer.parseInt(params[2]);
                        try {
                            Class.forName(JDBC_DRIVER);
                            System.out.println("[SD]连接数据库...");
                            conn = DriverManager.getConnection(DB_URL, USER, PASS);
                            System.out.println("[SD]实例化Statement对象...");
                            String sql = "select " + pin_name + " from steamInfo where qq = ?;";
                            // if reset pin
                            if (Objects.equals(params[2], "0")){
                                sql = "update steamInfo set sp1 = 0 where qq = ?;";
                                stmt = conn.prepareStatement(sql);
                                stmt.setLong(1,messageEvent.getSender().getId());
                                stmt.executeUpdate();
                                conn.close();
                                stmt.close();
                                messageChainBuilder.append("Done.");
                                System.out.println("[SD]重置pin");
                                return messageChainBuilder.build();
                            }
                            //check if user has the pin
                            stmt = conn.prepareStatement(sql);
                            stmt.setLong(1,messageEvent.getSender().getId());
                            ResultSet rs = stmt.executeQuery();
                            if (rs.next()){
                                if (!rs.wasNull()){
                                    if (rs.getBoolean(pin_name)){
                                        //im not sure if it should close this
                                        rs.close();
                                        stmt.clearParameters();
                                        //set pin
                                        sql = "update steamInfo set sp1 = ? where qq = ?;";
                                        stmt = conn.prepareStatement(sql);
                                        stmt.setInt(1, Integer.parseInt(params[2]));
                                        stmt.setLong(2,messageEvent.getSender().getId());
                                        stmt.executeUpdate();
                                        System.out.println("[SD]设置pin");
                                        stmt.close();
                                    }else {
                                        conn.close();
                                        stmt.close();
                                        messageChainBuilder.append("你无法使用这个 pin。\n在 https://interface.100oj.com/stat/pininfo.html 查看所有类型。");
                                        return messageChainBuilder.build();
                                    }
                                }else {
                                    conn.close();
                                    stmt.close();
                                    messageChainBuilder.append("你无法使用这个 pin。\n在 https://interface.100oj.com/stat/pininfo.html 查看所有类型。");
                                    return messageChainBuilder.build();
                                }
                            }else {
                                conn.close();
                                stmt.close();
                                messageChainBuilder.append("在数据库中查找你的信息失败。\n如果你还没有绑定过id，使用[#stat bind [steam64id]]来进行绑定。");
                                return messageChainBuilder.build();
                            }
                            conn.close();
                            stmt.close();
                        }catch (SQLException | ClassNotFoundException e){
                            messageChainBuilder.append("不存在的 pin。\n在 https://interface.100oj.com/stat/pininfo.html 查看所有类型。");
                            e.printStackTrace();
                            return messageChainBuilder.build();
                        }
                        messageChainBuilder.append("Done.");
                        return messageChainBuilder.build();
                    }
                default:
                    String steam64id = null;
                    String url = URL_HEADER;
                    int renderTyper = 0;
                    int sp1 = 0;
                    if (params[1].startsWith("@")){
                        try{
                            Class.forName(JDBC_DRIVER);
                            System.out.println("[SD]连接数据库...");
                            conn = DriverManager.getConnection(DB_URL, USER, PASS);
                            System.out.println("[SD]实例化Statement对象...");
                            String sql = "select steam64id,sp1 from steamInfo where qq = ?;";
                            stmt = conn.prepareStatement(sql);
                            stmt.setString(1,params[1].substring(1));
                            ResultSet rs = stmt.executeQuery();
                            if (rs.next()){
                                steam64id = rs.getString("steam64id");
                                sp1 = rs.getInt("sp1");
                            }else {
                                messageChainBuilder.append("在数据库中查找该玩家的信息失败。\n该玩家没有对 qq 和 steam64id 进行绑定。");
                                return messageChainBuilder.build();
                            }
                            rs.close();
                            conn.close();
                            stmt.close();
                        } catch (SQLException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    else if (params[1].equalsIgnoreCase("me")){
                        try {
                            Class.forName(JDBC_DRIVER);
                            System.out.println("[SD]连接数据库...");
                            conn = DriverManager.getConnection(DB_URL, USER, PASS);
                            System.out.println("[SD]实例化Statement对象...");
                            String sql = "select steam64id,renderType,sp1 from steamInfo where qq = ?;";
                            stmt = conn.prepareStatement(sql);
                            stmt.setLong(1,messageEvent.getSender().getId());
                            ResultSet rs = stmt.executeQuery();
                            if (rs.next()){
                                steam64id = rs.getString("steam64id");
                                renderTyper = rs.getInt("renderType");
                                sp1 = rs.getInt("sp1");
                            }else {
                                messageChainBuilder.append("在数据库中查找你的信息失败。\n如果你还没有绑定过id，使用[#stat bind [steam64id]]来进行绑定。");
                                return messageChainBuilder.build();
                            }
                            /** 保险 **/
                            if (steam64id.equalsIgnoreCase("null") || steam64id.isEmpty()){
                                messageChainBuilder.append("在数据库中查找你的信息失败。\n如果你还没有绑定过id，使用[#stat bind [steam64id]]来进行绑定。");
                                return messageChainBuilder.build();
                            }
                            rs.close();
                            conn.close();
                            stmt.close();
                        }catch (SQLException | ClassNotFoundException e){
                            messageChainBuilder.append("JDBC错误。\n在数据库中查找你的信息失败。\n如果你还没有绑定过id，使用[#stat bind [steam64id]]来进行绑定。");
                            e.printStackTrace();
                            return messageChainBuilder.build();
                        }
                    }else if (DataUtil.isSteam64id(params[1])){
                        steam64id = params[1];
                        try{
                            Class.forName(JDBC_DRIVER);
                            System.out.println("[SD]连接数据库...");
                            conn = DriverManager.getConnection(DB_URL, USER, PASS);
                            System.out.println("[SD]实例化Statement对象...");
                            String sql = "select sp1 from steamInfo where steam64id = ?;";
                            stmt = conn.prepareStatement(sql);
                            stmt.setString(1,steam64id);
                            ResultSet rs = stmt.executeQuery();
                            if (rs.next()){
                                sp1 = rs.getInt("sp1");
                            }
                            rs.close();
                            conn.close();
                            stmt.close();
                        } catch (SQLException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }else {
                        messageChainBuilder.append("这不是一个有效的steam64id。它应该是以7656开头、总长度为17位的纯数字。\n如果你不知道什么是steam64id，推荐在网络上了解更多的相关知识。\n");
                        return messageChainBuilder.build();
                    }
                    url = url + steam64id + "&render=" + renderTyper + "&sp1=" + sp1;
                    if (params.length==3){//limit
                        url = url + "&limit=" + params[2];
                    }
                    try {
                        ExternalResource ex = ExternalResource.Companion.create(HtmlTools.getUrlByByte(url));
                        System.out.println("[SD]尝试获取>" + url);
                        Image img = ExternalResource.uploadAsImage(ex,bot.getGroup(475379747));//上传图片
                        messageChainBuilder.append(img);
                        return messageChainBuilder.build();
                    }catch (Exception ignored){
                        messageChainBuilder.append("生成失败！可能原因>\n1>网络原因，本机无法连接至steam。\n2>请将steam->编辑个人资料->隐私设置->游戏详情，设置为公开|所有人可见。(该设置有一定的延迟。)");
                        return messageChainBuilder.build();
                    }
            }
        }else {
            messageChainBuilder.append("本命令用于生成百橙玩家的个人统计数据图片。\n" +
                    "使用前请确认自己steam的隐私设置。steam->编辑个人资料->隐私设置->游戏详情，设置为公开|所有人可见。(该设置有一定的延迟。)\n" +
                    "使用方法>\n" +
                    "#stat [steam64id] [行数。可不填，默认为5]\n" +
                    "#stat bind [steam64id]    用于将当前qq绑定到对应steam账户。重复使用会更新绑定。\n" +
                    "#stat unbind    删除自己的绑定。\n" +
                    "#stat me [行数。可不填，默认为5]   在qq绑定steam64id后，使用本命令来快速生成自己的资料。\n" +
                    "#stat type [类型编号]   更改出图类型。");
            return messageChainBuilder.build();
        }
    }
}

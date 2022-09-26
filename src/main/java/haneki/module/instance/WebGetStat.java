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

public class WebGetStat extends BasicModule implements MessageModule {
    static final String URL_HEADER = "http://interface.100oj.com/stat/render.php?steamid=";
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/data_ethene";
    static final String USER = "sora";
    static final String PASS = "***REMOVED***";
    Connection conn = null;
    Statement stmt = null;

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
                                stmt = conn.createStatement();
                                String sql = "insert into steamInfo(qq,steam64id) value('" + messageEvent.getSender().getId() + "','" + params[2] + "') on duplicate key update steam64id = '" + params[2] + "';";
                                stmt.execute(sql);
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
                        stmt = conn.createStatement();
                        String sql = "delete from steamInfo where qq = '" + messageEvent.getSender().getId() + "';";
                        stmt.execute(sql);
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
                default:
                    String steam64id = null;
                    String url = URL_HEADER;
                    if (params[1].equalsIgnoreCase("me")){
                        try {
                            Class.forName(JDBC_DRIVER);
                            System.out.println("[SD]连接数据库...");
                            conn = DriverManager.getConnection(DB_URL, USER, PASS);
                            System.out.println("[SD]实例化Statement对象...");
                            stmt = conn.createStatement();
                            String sql = "select steam64id from steamInfo where qq = '" + messageEvent.getSender().getId() + "';";
                            ResultSet rs = stmt.executeQuery(sql);
                            if (rs.next()){
                                steam64id = rs.getString("steam64id");
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
                    }else {
                        messageChainBuilder.append("这不是一个有效的steam64id。它应该是以7656开头、总长度为17位的纯数字。\n如果你不知道什么是steam64id，推荐在网络上了解更多的相关知识。\n");
                        return messageChainBuilder.build();
                    }
                    url = url + steam64id;
                    if (params.length==3){//limit
                        url = url + "&limit=" + params[2];
                    }
                    try {
                        ExternalResource ex = ExternalResource.Companion.create(HtmlTools.getUrlByByte(url));
                        System.out.println("[SD]尝试获取>" + url);
                        Image img = ExternalResource.uploadAsImage(ex,bot.getGroup(205312025));//上传图片
                        messageChainBuilder.append(img);
                        return messageChainBuilder.build();
                    }catch (Exception ignored){
                        messageChainBuilder.append("生成失败！可能原因>\n1>网络原因，本机无法连接至steam。\n2>请将你的steam->编辑个人资料->隐私设置->游戏详情，设置为公开|所有人可见。");
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
                    "#stat me [行数。可不填，默认为5]   在qq绑定steam64id后，使用本命令来快速生成自己的资料。");
            return messageChainBuilder.build();
        }
    }
}

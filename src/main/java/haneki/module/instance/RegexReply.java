package haneki.module.instance;

import haneki.module.BasicModule;
import haneki.module.NeedOperactor;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.ForwardMessageBuilder;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.PlainText;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexReply extends BasicModule implements NeedOperactor {
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/data_ethene";
    static final String USER = "sora";
    static final String PASS = "***REMOVED***";
    Connection conn = null;
    PreparedStatement stmt = null;
    ArrayList<Integer> replyID = new ArrayList<Integer>();
    ArrayList<String> replyRegex = new ArrayList<>();
    ArrayList<String> replyContent = new ArrayList<>();//序列化后的jsonStr

    public RegexReply(String module_name) {
        super(module_name);
        loadRC();
    }

    @Override
    public void moduleReact() {

    }

    @Override
    public String getTiggerRegex() {
        return "((?!#)(.|\\n))* (.|\\n)*|(#rr.*)";
    }

    @Override
    public MessageChain moduleReact(MessageChain message, MessageEvent messageEvent, Bot bot) {
        return null;
    }

    @Override
    public MessageChain moduleReact(MessageChain message, MessageEvent messageEvent, Bot bot, List<Long> operator, List<Long> superadmin) {
        MessageChainBuilder messageChainBuilder = new MessageChainBuilder();
        Long senderId = messageEvent.getSender().getId();
        if (message.contentToString().startsWith("#rr")){
            if (operator.contains(senderId) ||superadmin.contains(senderId)){//权限确认
                String[] params = message.contentToString().split(" ");
                switch(params[1]){
                    case "+":
                    case "create":
                    case "add"://#rr add regex
                        if (params.length==3){
                            messageChainBuilder.append("请在接下来的一条消息中，发送你要设置的内容。\n请尽量避免使用[半角分号、引号]。");
                            bot.getEventChannel().filter(ev ->
                                    ev instanceof MessageEvent &&
                                            ((MessageEvent) ev).getSender().getId()==messageEvent.getSender().getId()
                            ).subscribeOnce(MessageEvent.class,event -> {
                                if (addRule(params[2],MessageChain.serializeToJsonString(event.getMessage()))){
                                    loadRC();
                                    event.getSubject().sendMessage("添加成功！");
                                }else {
                                    event.getSubject().sendMessage("添加失败。");
                                }
                            });
                        }else if (params.length==2){
                            messageChainBuilder.append("参数不足，缺少正则。");
                        }else {
                            messageChainBuilder.append("参数长度错误。\n本命令用法:#rr create|add|+ regex");
                        }
                        break;
                    case "-":
                    case "delete":
                    case "remove"://#rr remove id
                        if (params.length==3){
                            if (removeRule(Integer.parseInt(params[2]))){
                                loadRC();
                                messageChainBuilder.append("删除成功！");
                            }else {
                                messageChainBuilder.append("删除失败。");
                            }
                        }else if (params.length==2){
                            messageChainBuilder.append("参数不足，缺少id。");
                        }else {
                            messageChainBuilder.append("参数长度错误。\n本命令用法:#rr delete|remove|- id");
                        }
                        break;
                    case "replace":
                        messageChainBuilder.append("懒得写了，直接先[#rr remove id]再[#rr add regex]吧。");
                        break;
                    case "reload":
                        messageChainBuilder.append("重置映射。");
                        loadRC();
                        break;
                    case "-l":
                    case "list":
                        Group group = bot.getGroup(205312025L);//SumikaSystem
                        ForwardMessageBuilder forwardMessageBuilder = new ForwardMessageBuilder(group);
                        forwardMessageBuilder.add(bot.getId(),"自定资料列表",new PlainText(checkRule()));
                        messageChainBuilder.append(forwardMessageBuilder.build());
                        break;
                }
            }else {
                messageChainBuilder.append("权限不足！");
            }
        }else {
            boolean tigger = false;
            Group group = bot.getGroup(205312025L);//SumikaSystem
            ForwardMessageBuilder forwardMessageBuilder = new ForwardMessageBuilder(group);
            String[] key2bCheck = message.contentToString().split(" ");
            for (int i = 0; i < key2bCheck.length; i++) {
                for (int j = 0; j < replyRegex.size(); j++) {
                    Pattern p = Pattern.compile(replyRegex.get(j));
                    Matcher m = p.matcher(key2bCheck[i]);
                    if (m.matches()){
                        tigger = true;
                        forwardMessageBuilder.add(bot.getId(),"自定资料  " + replyID.get(j),MessageChain.deserializeFromJsonString(replyContent.get(j)));//反序列化。
                    }
                }
            }
            if (tigger){
                messageChainBuilder.append(forwardMessageBuilder.build());
            }
        }
        return messageChainBuilder.build();
    }

    public void loadRC(){
        System.out.println("[RegexReply]重置RC映射。");
        replyID.clear();
        replyRegex.clear();
        replyContent.clear();
        try {
            Class.forName(JDBC_DRIVER);
            System.out.println("[RegexReply]连接数据库...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            System.out.println("[RegexReply]实例化Statement对象...");
            String sql = "select id,regex,content from regexReply order by id;";
            stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            int count = 0;
            while (rs.next()){
                replyID.add(rs.getInt("id"));
                replyRegex.add(rs.getString("regex"));
                replyContent.add(rs.getString("content"));
                count++;
            }
            System.out.println("[RegexReply]RC映射已重置，共[" + count + "]条规则。");
            rs.close();
            conn.close();
            stmt.close();
        }catch (SQLException | ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    public boolean addRule(String regex,String content){
        System.out.println("[RegexReply]添加规则。");
        try {
            Class.forName(JDBC_DRIVER);
            System.out.println("[RegexReply]连接数据库...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            System.out.println("[RegexReply]实例化Statement对象...");
            String sql = "insert into regexReply (regex,content) value(?,?);";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1,regex);
            stmt.setString(2,content);
            stmt.executeUpdate();
            stmt.close();
            conn.close();
            return true;
        }catch (SQLException | ClassNotFoundException e){
            e.printStackTrace();
            System.out.println(e.getMessage());
            return false;
        }
    }

    public boolean removeRule(int id){
        System.out.println("[RegexReply]移除规则。");
        try {
            Class.forName(JDBC_DRIVER);
            System.out.println("[RegexReply]连接数据库...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            System.out.println("[RegexReply]实例化Statement对象...");
            String sql = "delete from regexReply where id = ? ;";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1,id);
            stmt.executeUpdate();
            stmt.close();
            conn.close();
            return true;
        }catch (SQLException | ClassNotFoundException e){
            e.printStackTrace();
            System.out.println(e.getMessage());
            return false;
        }
    }

    public String checkRule(){
        System.out.println("[RegexReply]列举规则。");
        String result = "规则列举>|id|regex|";
        try {
            Class.forName(JDBC_DRIVER);
            System.out.println("[RegexReply]连接数据库...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            System.out.println("[RegexReply]实例化Statement对象...");
            String sql = "select id,regex from regexReply;";
            stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()){
                result += "\n" + rs.getInt("id") + "\t" + rs.getString("regex");
            }
            rs.close();
            stmt.close();
            conn.close();
            return result;
        }catch (SQLException | ClassNotFoundException e){
            e.printStackTrace();
            System.out.println(e.getMessage());
            return "查询失败。";
        }
    }
}

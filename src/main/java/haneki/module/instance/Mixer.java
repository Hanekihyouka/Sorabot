package haneki.module.instance;

import haneki.module.BasicModule;
import haneki.module.MessageModule;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.ForwardMessageBuilder;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.PlainText;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Mixer extends BasicModule implements MessageModule {

    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/data_oj";
    static final String USER = "guest";
    static final String PASS = "Sumika!System2";

    static final String MIXER_NAME[] = {"[空袭]","[增幅]","[返回]","[炸弹]","恩惠","{混乱}","[冰冻]","{布雷}","奇迹","{随机传送}",
            "恢复","[疾走]","宝箱","{BOSS HP+10}","追星者","{乞丐}","玩家ATK+1","玩家DEF+1","玩家EVD+1","[爆燃]",
            "[翻转]","{BOSS ATK+1}","{BOSS DEF+1}","{BOSS EVD+1}","鬼牌","{破产}","{粘液}","[孢子]"," - "};//算上none共29个
    //none的index为-1

    public Mixer(String module_name) {
        super(module_name);
    }

    @Override
    public void moduleReact() {

    }

    @Override
    public String getTiggerRegex() {
        return "#m\\d?\\d?\\d?";
    }

    @Override
    public MessageChain moduleReact(MessageChain message, MessageEvent messageEvent, Bot bot) {
        MessageChainBuilder messageChainBuilder = new MessageChainBuilder();
        Group group = bot.getGroup(205312025L);//SumikaSystem
        ForwardMessageBuilder forwardMessageBuilder = new ForwardMessageBuilder(group);
        String content = message.contentToString();
        int range = 2;
        if (content.length()>2){
            range = Integer.parseInt(content.substring(2));
        }
        if (range>400){
            return messageChainBuilder.append(new PlainText("请求数量过多！最大为400")).build();
        }

        Calendar calendar = Calendar.getInstance();
        long timeStampNow = calendar.getTimeInMillis();
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.HOUR_OF_DAY,16);//当天16点的时间戳
        calendar.set(Calendar.MILLISECOND,0);
        long timeStampStart = calendar.getTimeInMillis();

        int tagNow = 1;
        if (timeStampNow>timeStampStart){
            tagNow = 2;
        }

        String sql = "select * from mixer where ";
        for (int i = 0; i < range; i++) {
            sql += " timeinfo=";
            sql += "'" + Long.toHexString(timeStampStart/1000 + 86400*(i-1)) + "' or ";
        }
        sql += " false limit 500;";

        Connection conn = null;
        Statement stmt = null;
        try {
            Class.forName(JDBC_DRIVER);
            System.out.println("[Mixer]连接数据库...");
            conn = DriverManager.getConnection(DB_URL,USER,PASS);
            System.out.println("[Mixer]实例化Statement对象...");
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
            int textIndex = 1;
            StringBuilder pt = new StringBuilder();
            while (rs.next()){
                String sd = sdf.format(new Date((Long.parseLong(rs.getString("timeinfo"),16)+86400)*1000));//时间戳转换成时间
                String mixer1 = MIXER_NAME[rs.getInt("mixer1")];
                String mixer2 = MIXER_NAME[rs.getInt("mixer2")];
                String mixer3 = MIXER_NAME[rs.getInt("mixer3")];
                int modifier = 0;
                if (mixer1.contains("[")){ modifier += 10;} else if (mixer1.contains("{")) { modifier += 25;}
                if (mixer2.contains("[")){ modifier += 10;} else if (mixer2.contains("{")) { modifier += 25;}
                if (mixer3.contains("[")){ modifier += 10;} else if (mixer3.contains("{")) { modifier += 25;}
                pt.append(sd);
                pt.append("  +" + modifier + "%");
                if (textIndex==tagNow){pt.append("  (当前)\n");}else {pt.append("\n");}
                pt.append(mixer1).append("  ").append(mixer2).append("  ").append(mixer3).append("\n");
                if (textIndex%20==0){
                    forwardMessageBuilder.add(bot.getId(),"混合器",new PlainText(pt.toString()));
                    pt.delete(0,pt.length());//清空
                }
                textIndex++;
            }
            if ((textIndex%20)!=0){
                forwardMessageBuilder.add(bot.getId(),"混合器",new PlainText(pt.toString()));
            }
            conn.close();
            stmt.close();
            rs.close();
            return messageChainBuilder.append(forwardMessageBuilder.build()).build();
        }catch (Exception e){ e.printStackTrace(); }
        return null;
    }
}

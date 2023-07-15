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

public class SumikaSql extends BasicModule implements MessageModule {

    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/data_oj";
    static final String USER = "guest";
    static final String PASS = "Sumika!System2";
    Connection conn = null;
    Statement stmt = null;

    public SumikaSql(String module_name) {
        super(module_name);
    }

    @Override
    public void moduleReact() {

    }

    @Override
    public String getTiggerRegex() {
        return "##sql(.|\r|\n)*";
    }

    @Override
    public MessageChain moduleReact(MessageChain message, MessageEvent messageEvent, Bot bot) {
        MessageChainBuilder messageChainBuilder = new MessageChainBuilder();
        Group group = bot.getGroup(475379747L);//SumikaSystem
        ForwardMessageBuilder forwardMessageBuilder = new ForwardMessageBuilder(group);

        String sql = message.contentToString().substring(5);
        try {
            Class.forName(JDBC_DRIVER);
            System.out.println("[SumikaSQL]连接数据库...");
            conn = DriverManager.getConnection(DB_URL,USER,PASS);
            System.out.println("[SumikaSQL]实例化Statement对象...");
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            String columnText = "列>| ";
            for (int i = 0; i < columnCount; i++) {
                columnText +=  rsmd.getColumnName(i+1) + " | ";
            }
            forwardMessageBuilder.add(bot.getId(),"INDEX",new PlainText(columnText));

            while (rs.next()){
                String thisLine = "| ";
                for (int i = 0; i < columnCount; i++) {
                    thisLine += rs.getString(i+1) + " | ";
                }
                forwardMessageBuilder.add(bot.getId(),"查询",new PlainText(thisLine));
            }

            rs.close();
            conn.close();
            stmt.close();
            return messageChainBuilder.append(forwardMessageBuilder.build()).build();
        }catch (SQLException | ClassNotFoundException e){
            e.printStackTrace();
            messageChainBuilder.append("JDBC警告>\n");
            messageChainBuilder.append(e.getMessage());
            return messageChainBuilder.build();
        }
    }
}

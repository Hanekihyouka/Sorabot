package haneki.module.instance;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import haneki.module.BasicModule;
import haneki.module.MessageModule;
import haneki.util.DataUtil;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.ForwardMessageBuilder;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.PlainText;


import java.io.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class SumikaSqlLite extends BasicModule implements MessageModule {

    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/data_oj";
    static final String USER = "quest";
    static final String PASS = "SumikaSystem";
    Connection conn = null;
    Statement stmt = null;

    /**
     * 映射  触发正则  ->  数据 ->   i18nkey
     *                       ->   tableName
     * */
    HashMap<String, HashMap<String,String>> tiggerMap = new HashMap<>();

    public SumikaSqlLite(String module_name) {
        super(module_name);
        /** 用于测试
        HashMap<String,String> liteTest = new HashMap<>();
        liteTest.put("CARD_UNIT_SUGURI_V2","unitCharacter");
        liteTest.put("CARD_UNIT_SUGURI46","unitCharacter");
        liteTest.put("CARD_BATTLE_ACCELHYPER","cardDeck");
        liteTest.put("CARD_HYPER_SUGURI","cardHyper");
        tiggerMap.put("(?i)LiteTest", liteTest);
        * 结束 */
        LoadTiggerMap();

        try {
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void moduleReact() {

    }

    @Override
    public String getTiggerRegex() {
        return "((?!#)(.|\\n))* (.|\\n)*|(##sumika reload)";
    }

    @Override
    public MessageChain moduleReact(MessageChain message, MessageEvent messageEvent, Bot bot) {
        MessageChainBuilder messageChainBuilder = new MessageChainBuilder();
        switch (message.contentToString()){
            case "##sumika reload":
                messageChainBuilder.append("重置映射。");
                LoadTiggerMap();
                return messageChainBuilder.build();
            default:
                Group group = bot.getGroup(205312025L);//SumikaSystem
                ForwardMessageBuilder forwardMessageBuilder = new ForwardMessageBuilder(group);
                String[] key2bCheck = message.contentToString().split(" ");
                boolean messageNotNull = false;
                for (int i = 0; i < key2bCheck.length; i++) {
                    Iterator iterator = tiggerMap.entrySet().iterator();
                    while (iterator.hasNext()){
                        Map.Entry<String,HashMap<String,String>> entry = (Map.Entry<String, HashMap<String, String>>) iterator.next();
                        String regex = entry.getKey();
                        HashMap<String,String> data = entry.getValue();
                        if (key2bCheck[i].matches(regex)){//正则触发了
                            messageNotNull = true;
                            data.forEach((i18nkey,tableName)->{
                                forwardMessageBuilder.add(bot.getId(),i18nkey,new PlainText(cardMessageBuilder(tableName,i18nkey)));
                            });
                        }
                    }
                }
                if (messageNotNull){
                    return messageChainBuilder.append(forwardMessageBuilder.build()).build();
                }
        }
        return null;
    }

    public void LoadTiggerMap(){
        //使用json格式
        GsonBuilder gsonBuilder = new  GsonBuilder();
        gsonBuilder.serializeNulls();
        gsonBuilder.setPrettyPrinting();
        Gson gson = gsonBuilder.create();
        File config_folder = new File("./config/SumikaSqlLite");
        if(!config_folder.exists()&&!config_folder.isDirectory()){
            try {
                config_folder.mkdirs();
                System.out.println("[SumikaSqlLite]映射文件夹[./config/SumikaSqlLite]不存在。创建空文件夹。");
                System.out.println("[SumikaSqlLite]创建空的配置文件。");
                File voidConfig = new File("./config/SumikaSqlLite/default.json");
                voidConfig.createNewFile();
                FileWriter fileWriter = new FileWriter(voidConfig);
                fileWriter.write(gson.toJson(tiggerMap));
                fileWriter.flush();
                fileWriter.close();
            }catch (Exception e){  }
        }

        File[] config_list = config_folder.listFiles();
        for (File f:config_list) {
            System.out.println("[SumikaSqlLite]载人新的映射集[" + f.getName() + "]");
            HashMap<String,HashMap<String,String>> thisConfig = gson.fromJson(DataUtil.readToString(f), new TypeToken<HashMap<String,HashMap<String,String>>>(){}.getType());
            thisConfig.forEach((key,value) -> tiggerMap.put(key,value));
        }
        System.out.println("[SumikaSqlLite]映射集载人结束，共[" + tiggerMap.size() + "]条规则。");
    }

    public String cardMessageBuilder(String typeForTable,String i18nkey) {
        String cardMessage = "";
        String sql = "select";

        try {
            Class.forName(JDBC_DRIVER);
            System.out.println("[SumikaSQLLite]连接数据库...");
            conn = DriverManager.getConnection(DB_URL,USER,PASS);
            System.out.println("[SumikaSQLLite]实例化Statement对象...");
            stmt = conn.createStatement();
        }catch (Exception ignored){}



        switch (typeForTable){
            case "cardDeck":
                sql += " groupData,name_,type_,level_,cost,limit_,point_,descr,flavor ";   //其中 flavor和point可以为null
                sql += " from cardDeck ";
                sql += " where i18nkey='"+ i18nkey + "' ;";
                try {
                    ResultSet rs = stmt.executeQuery(sql);
                    rs.next();
                    String _groupData = "";
                    String _name = "";
                    String _type = "";
                    String _level = "";
                    String _cost = "";
                    String _limit = "";
                    String _point;
                    String _descr;
                    String _flavor;

                    try { _groupData = rs.getString("groupData"); }catch (NullPointerException e){ }
                    try { _name = rs.getString("name_"); }catch (NullPointerException e){ }
                    try { _type = rs.getString("type_"); }catch (NullPointerException e){ }
                    try { _level = rs.getString("level_"); }catch (NullPointerException e){ }
                    try { _cost = rs.getString("cost"); }catch (NullPointerException e){ }
                    try { _limit = rs.getString("limit_"); }catch (NullPointerException e){ }
                    try { _point = rs.getString("point_"); if (_point.equals("null")){_point="合作不可携带";}}catch (NullPointerException e){ _point="合作不可携带"; }
                    try { _descr = rs.getString("descr"); if (_descr.equals("null")){_descr="\n";}}catch (NullPointerException e){ _descr="\n"; }
                    try { _flavor = rs.getString("flavor"); if (_flavor.equals("null")){_flavor="\n";}}catch (NullPointerException e){ _flavor="\n"; }

                    cardMessage += " -- " + _groupData + " -- \n" +
                            _name + "\n" +
                            "(" + _type + "  " + _level + "/" + _cost + ")\n" +
                            "(Max:" + _limit + "  point:" + _point + ")\n" +
                            _descr + "\n" +
                            _flavor;

                    rs.close();
                    stmt.close();
                    conn.close();
                }catch (SQLException e){ cardMessage = "sql>" + sql + "\n查询数据库时出错>" + e.getMessage(); }
                break;
            case "cardHyper":
                sql += " groupData,chara,name_,type_,level_,cost,descr,flavor ";//其中 flavor和chara可以为null
                sql += " from cardHyper ";
                sql += " where i18nkey='"+ i18nkey + "' ;";
                try {
                    ResultSet rs = stmt.executeQuery(sql);
                    rs.next();
                    String _groupData = "";
                    String _chara;
                    String _name = "";
                    String _type = "";
                    String _level = "";
                    String _cost = "";
                    String _descr;
                    String _flavor;

                    String _charaName;

                    try { _groupData =rs.getString("groupData"); }catch (NullPointerException e){ }
                    try { _chara = rs.getString("chara"); _charaName = getCharaName(_chara);}catch (NullPointerException e){ _charaName =" FBF "; }
                    try { _name = rs.getString("name_"); }catch (NullPointerException e){ }
                    try { _type =rs.getString("type_"); }catch (NullPointerException e){ }
                    try { _level = rs.getString("level_"); }catch (NullPointerException e){ }
                    try { _cost =rs.getString("cost"); }catch (NullPointerException e){ }
                    try { _descr = rs.getString("descr"); if (_descr.equals("null")){_descr="\n";}}catch (NullPointerException e){ _descr="\n"; }
                    try { _flavor = rs.getString("flavor"); if (_flavor.equals("null")){_flavor="\n";}}catch (NullPointerException e){ _flavor="\n"; }

                    cardMessage += " -- " + _groupData + " -- \n" +
                            _name + "\n" +
                            "([H]" + _type + "  " + _level + "/" + _cost + ")\n" +
                            "(Owner:" + _charaName + ")\n" +
                            _descr + "\n" +
                            _flavor;
                    rs.close();
                    stmt.close();
                    conn.close();
                }catch (SQLException e){ cardMessage = "sql>" + sql + "\n查询数据库时出错>" + e.getMessage(); }
                break;
            case "cardOther":
                sql += " groupData,name_,type_,level_,cost,descr,flavor ";   //其中 flavor可以为null
                sql += " from cardOther ";
                sql += " where i18nkey='"+ i18nkey + "' ;";
                try {
                    ResultSet rs = stmt.executeQuery(sql);
                    rs.next();
                    String _groupData = "";
                    String _name = "";
                    String _type = "";
                    String _level = "";
                    String _cost = "";
                    String _descr;
                    String _flavor;

                    try { _groupData = rs.getString("groupData"); }catch (NullPointerException e){ }
                    try { _name = rs.getString("name_"); }catch (NullPointerException e){ }
                    try { _type = rs.getString("type_"); }catch (NullPointerException e){ }
                    try { _level = rs.getString("level_"); }catch (NullPointerException e){ }
                    try { _cost = rs.getString("cost"); }catch (NullPointerException e){ }
                    try { _descr = rs.getString("descr"); if (_descr.equals("null")){_descr="\n";}}catch (NullPointerException e){ _descr="\n"; }
                    try { _flavor = rs.getString("flavor"); if (_flavor.equals("null")){_flavor="\n";}}catch (NullPointerException e){ _flavor="\n"; }

                    cardMessage += " -- " + _groupData + " -- \n" +
                            _name + "\n" +
                            "(" + _type + "  " + _level + "/" + _cost + ")\n" +
                            _descr + "\n" +
                            _flavor;

                    rs.close();
                    stmt.close();
                    conn.close();
                }catch (SQLException e){ cardMessage = "sql>" + sql + "\n查询数据库时出错>" + e.getMessage(); }
                break;
            case "cardBossSkill":
                sql += " name_,type_,level_,cost,descr,chara,isHyper,isHyperOnly";   //
                sql += " from cardBossSkill ";
                sql += " where i18nkey='"+ i18nkey + "' ;";
                try {
                    ResultSet rs = stmt.executeQuery(sql);
                    rs.next();
                    String _name = "";
                    String _type = "";
                    String _level = "";
                    String _cost = "";
                    String _descr;
                    String _chara;
                    String _charaName;
                    boolean isHyper = false;//是否为boss的Hyper卡
                    boolean isHyperOnly = false;//是否仅限Hyper难度


                    try { _name = rs.getString("name_"); }catch (NullPointerException e){ }
                    try { _type = rs.getString("type_"); }catch (NullPointerException e){ }
                    try { _level = rs.getString("level_"); }catch (NullPointerException e){ }
                    try { _cost = rs.getString("cost"); }catch (NullPointerException e){ }
                    try { _descr = rs.getString("descr"); if (_descr.equals("null")){_descr="\n";}}catch (NullPointerException e){ _descr="\n"; }
                    try { _chara = rs.getString("chara"); _charaName = getCharaName(_chara);}catch (NullPointerException e){ _charaName =" FBF "; }
                    try { isHyper = rs.getBoolean("isHyper"); }catch (NullPointerException e){ }
                    try { isHyperOnly = rs.getBoolean("isHyperOnly"); }catch (NullPointerException e){ }

                    if (isHyper){
                        _type = "[H]" + _type;
                    }
                    if (isHyperOnly){
                        _name += "(HYPER难度)";
                    }

                    cardMessage += " -- 合作 -- " + _charaName + " -- \n" +
                            _name + "\n" +
                            "(" + _type + "  " + _level + "/" + _cost + ")\n" +
                            _descr;

                    rs.close();
                    stmt.close();
                    conn.close();
                }catch (SQLException e){ cardMessage = "sql>" + sql + "\n查询数据库时出错>" + e.getMessage(); }
                break;
            case "unitNPC":
            case "unitCharacter":
                sql += " groupData,hyper,name_,_hp,_atk,_def,_evd,_rec,descr ";//其中 _rec和descr可以为null
                sql += " from " + typeForTable;//对于npc,hyper可以为null
                sql += " where i18nkey='"+ i18nkey + "' ;";
                try {
                    ResultSet rs = stmt.executeQuery(sql);
                    rs.next();
                    String _groupData = "";
                    String _hyper;
                    String _name = "";
                    String _hp = "";
                    String _atk = "";
                    String _def = "";
                    String _evd = "";
                    String _rec;
                    String _descr;
                    String _hyperName;

                    try { _groupData = rs.getString("groupData"); }catch (NullPointerException e){ }
                    try { _hyper = rs.getString("hyper"); _hyperName = getHyperName(_hyper);}catch (NullPointerException e){_hyperName = " ";}
                    try { _name = rs.getString("name_"); }catch (NullPointerException e){ }
                    try { _hp = rs.getString("_hp"); }catch (NullPointerException e){ }
                    try { _atk = rs.getString("_atk"); }catch (NullPointerException e){ }
                    try { _def = rs.getString("_def"); }catch (NullPointerException e){ }
                    try { _evd = rs.getString("_evd"); }catch (NullPointerException e){ }
                    try { _rec = rs.getString("_rec");if (_rec.equals("null")||_rec.equals("-1")){_rec="-";} }catch (NullPointerException e){ _rec = "-"; }
                    try { _descr = rs.getString("descr");if (_descr.equals("null")){_descr="";} }catch (NullPointerException e){ _descr = "\n"; }
                    //System.out.println(_descr);
                    cardMessage += " -- " + _groupData + " -- \n" +
                            _name + "\n" +
                            "(" + _hp + "HP " + _atk + " " + _def +
                            " " + _evd + " " + _rec + ")\n" +
                            "(HYPER: 「" + _hyperName + "」)\n" +
                            _descr + "\n";
                    rs.close();
                    stmt.close();
                    conn.close();
                }catch (SQLException e){ cardMessage = "sql>" + sql + "\n查询数据库时出错>" + e.getMessage(); }
                break;
        }
        return cardMessage;
    }

    public String getCharaName(String i18nkey){
        if (i18nkey.equals("null")){
            return " FBF ";
        }
        String sql = " select name_ from unitCharacter where i18nkey='" + i18nkey + "' " +
                "union" +
                " select name_ from unitNPC where i18nkey='" + i18nkey + "' ;";
        try {
            Connection tempCnn = DriverManager.getConnection(DB_URL,USER,PASS);
            Statement tempStmt = tempCnn.createStatement();
            ResultSet rs = tempStmt.executeQuery(sql);
            rs.next();
            String charaName = rs.getString("name_");
            rs.close();
            tempStmt.close();
            tempCnn.close();
            return charaName;
        }catch (SQLException e){System.out.println("[SumikaSqlLite]查询角色名时出错。");
            return "ERROR";
        }catch (NullPointerException e){
            return " FBF ";
        }
    }

    public String getHyperName(String i18nkey){
        if (i18nkey.equals("null")){
            return "没有HYPER";
        }
        String sql = " select name_ from cardHyper where i18nkey='" + i18nkey + "' " +
                "union" +
                " select name_ from cardBossSkill where i18nkey='" + i18nkey + "' ;";
        try {
            Connection tempCnn = DriverManager.getConnection(DB_URL,USER,PASS);
            Statement tempStmt = tempCnn.createStatement();
            ResultSet rs = tempStmt.executeQuery(sql);
            rs.next();
            String hyperName = rs.getString("name_");
            rs.close();
            tempStmt.close();
            tempCnn.close();
            return hyperName;
        }catch (SQLException e){System.out.println("[SumikaSqlLite]查询角色名时出错。");
            return "ERROR";
        }catch (NullPointerException e){
            return "没有HYPER";
        }
    }
}

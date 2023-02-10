package haneki.module.instance;

import haneki.module.BasicModule;
import haneki.module.MessageModule;
import haneki.util.DataUtil;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.ForwardMessageBuilder;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.utils.ExternalResource;

import java.io.File;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;


public class Le extends BasicModule implements MessageModule {
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/data_ethene";
    static final String USER = "sora";
    static final String PASS = "***REMOVED***";
    Connection conn = null;
    Statement stmt = null;

    String mwList[] = {"平安的旅途","风信鸡的指引","烹饪时间","极速装置","束发魔女","主角的特权","甜点守护者","愤怒狂暴","湛蓝乌鸦·再临","给你的礼物",
            "反射外装","决杀手术","奇迹红豆冰淇淋","星之再生","金蛋","三角力场","变化无常的风车","亚空间通道","阴谋机器人出动！","恶魔之手",
            "阴谋的间谍行动～预备","坚实魔女","空间的跳跃(标记)","特别舞台","飞艇轰炸","束缚之锁","情报官","魔法狱火","记录重现","荒唐的性能",
            "阴谋的操纵者","兔子模玩店","扩散光子步枪","永恒的观测者","浮游炮展开","融化的记忆","圣诞小姐的工作","海贼在天上飞？","店铺扩张战略","天使之手",
            "超能模式！","永久流放","修罗场模式","玩偶使","白色圣诞大粉碎","赌博！","涡轮满载","×16大火箭","大爆炸铃","脱衣",
            "隐身启动","劲敌","杀戮魔法","神出鬼没","兽之魔女","不动之物体","艾莉的奇迹","爆燃！","自爆","火箭炮",
            "大火箭炮","乔纳桑·速袭","另一个最终兵器","露露的幸运蛋","星球的导火线","才气觉醒","水晶障壁","统治者","急速的亚莉希安罗妮","圣露眼",
            "拜托了厨师长！","升档","炽热的商人之魂","月夜之舞","社交界亮相","黄昏色的梦","小麦格农炮","梦寐以求的世界","星星收集者","甜点天堂","甜点制作者的魔法","甜点成堆大作战"};
    String deckList[] = {"冲刺！","纱季的曲奇","迁怒于人","远距离射击","美妙的叮铃铃","美妙的礼物","坚固的结晶","布丁","残机奖励","大小姐的特权",
            "孤独的暴走车","偷袭","探究心","学生会长的权益","模仿","成功报酬","绅士的决斗","宝物小偷","拦路者","甜点的破坏者",
            "幕后交易","加班","燃起来吧！","兔子浮游炮","虹色之环","大麦格农炮","护盾","最终决战","性格反转力场","敌前逃亡",
            "护盾反转","极速超能","部件扩张","瞬间再生","阴谋的资金筹集","自暴自弃的改造","赌命一搏","殊死对决","便携布丁","虚假除械",
            "糟糕的布丁","米缪冲击锤","危险布丁","存钱罐","袭来","飞一边去","热度300%","突击","深夜的惨剧","互相交换",
            "喷射火焰","空中餐厅「纯洁」","为了玩具店的未来","小鸡小鸡大游行","记忆封印","无情的恶作剧","礼物小偷","通缉令","啵噗化","想要见到你",
            "安可","香蕉奈奈子","这里那里","大群的海鸥","神圣之夜","弹药用尽","礼物交换","我们是阴谋组","晚餐","超绝认真模式",
            "强制复活","小小的战争","我的朋友呀","被封印的守护者","混成化现象","恐怖的推销","众神的嬉戏","大争夺·圣诞夜","烧毁星球之光","派对时间",
            "加速空域","宁静","劳而无薪","无差别火力支援","宠物零食","家居翻修","不幸护符","疾风附魔","迷路的孩子","力量的代价",
            "掠夺者啵噗","鲜血渴望","幸运护符","全金属单体结构","幸运7"
            ,"露露的幸运蛋","露露的幸运蛋","露露的幸运蛋","露露的幸运蛋","露露的幸运蛋","露露的幸运蛋","露露的幸运蛋","露露的幸运蛋","露露的幸运蛋","露露的幸运蛋",
            "露露的幸运蛋","露露的幸运蛋","露露的幸运蛋","露露的幸运蛋"};

    public Le(String module_name) {
        super(module_name);
    }

    @Override
    public void moduleReact() {

    }

    @Override
    public String getTiggerRegex() {
        return ".*";
    }

    @Override
    public MessageChain moduleReact(MessageChain message, MessageEvent messageEvent, Bot bot) {
        String lower = message.contentToString().toLowerCase();
        switch (lower){
            case "#塔罗牌":
            case "#塔罗":
            case "#tarot":
            case "#tarots":
                return leTarot(bot.getId(),messageEvent.getSender().getId(),messageEvent.getSubject(),bot.getGroup(205312025L));//SumikaSystem
            case "#le":
            case "#乐":
                return leLuLu(messageEvent);
            case "#mw":
            case "#奇迹漫步":
                return leNico();
            case "#7":
            case "#浮游炮":
                return leNanako();
            case "##le_say":
                return leSay();
            default:
                if ((!lower.startsWith("#"))
                        &&(!lower.startsWith("."))
                        &&(!lower.startsWith("。"))){
                    if (Math.random()>0.995){
                        return leSay();
                    }
                }
        }
        return null;
    }

    public MessageChain leTarot(Long botid,Long senderid,Contact subject, Group group){
        MessageChainBuilder messageChainBuilder = new MessageChainBuilder();
        ForwardMessageBuilder forwardMessageBuilder = new ForwardMessageBuilder(group);
        //校验上次抽卡时间
        String sql = "select lastdate from tarotInfo where qq='" + senderid + "';";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Date date = new Date();
        String today = simpleDateFormat.format(date);
        try {
            Class.forName(JDBC_DRIVER);
            System.out.println("[Tarot]连接数据库...");
            conn = DriverManager.getConnection(DB_URL,USER,PASS);
            System.out.println("[Tarot]实例化Statement对象...");
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()){
                if (rs.getString("lastdate").equals(today)){
                    rs.close();
                    stmt.close();
                    conn.close();
                    return messageChainBuilder.append("你今天已经抽过卡了，请明天再来吧。").build();
                }else {
                    rs.close();
                    sql = "update tarotInfo set lastdate = '" + today + "' where qq ='" + senderid + "';";
                    stmt.execute(sql);
                    stmt.close();
                    conn.close();
                }
            }else {
                rs.close();
                sql = "insert into tarotInfo(qq,lastdate) value('" + senderid + "','" + today + "');";
                stmt.execute(sql);
                stmt.close();
                conn.close();
            }
        }catch (SQLException | ClassNotFoundException e){
            e.printStackTrace();
        }
        int tarotIndex = (int) Math.floor(Math.random()*22);
        boolean isReversed = new Random().nextBoolean();//逆
        String path = "./data/le/tarot/" + tarotIndex;
        try {
            ExternalResource resource = ExternalResource.create(new File(path + (isReversed?"_.jpg":".jpg")));
            forwardMessageBuilder.add(botid,"塔罗牌",subject.uploadImage(resource));
        }catch (Exception e){e.printStackTrace();}
        /** 文本  **/
        File file = new File(path + (isReversed?"_.txt":".txt"));
        forwardMessageBuilder.add(botid,"塔罗牌", new PlainText(DataUtil.readToString(file)));
        return messageChainBuilder.append(forwardMessageBuilder.build()).build();
    }

    public MessageChain leLuLu(MessageEvent messageEvent){
        MessageChainBuilder messageChainBuilder = new MessageChainBuilder();
        messageChainBuilder.append("[露露的幸运蛋]结果:\n");
        String image_path = "./data/le/lulu/";
        if (Math.random()<0.99){
            switch ((int) Math.floor(Math.random()*3)){
                case 0:
                    image_path += "le0.png";
                    int dice = (int) Math.ceil(Math.random()*6);
                    messageChainBuilder.append("·投掷骰子，获得骰子点数×20的STARS。\n" +
                            "1d6=[" + dice + "]，净获得 " + (dice*20-40) + "STARS。");
                    break;
                case 1:
                    image_path += "le1.png";
                    messageChainBuilder.append("·抽取5张卡。\n" +
                            "「" + deckList[(int) Math.floor(Math.random()*deckList.length)] +
                            "」「" + deckList[(int) Math.floor(Math.random()*deckList.length)] +
                            "」「" + deckList[(int) Math.floor(Math.random()*deckList.length)] +
                            "」「" + deckList[(int) Math.floor(Math.random()*deckList.length)] +
                            "」「" + deckList[(int) Math.floor(Math.random()*deckList.length)] +
                            "」");
                    break;
                case 2:
                    image_path += "le2.png";
                    messageChainBuilder.append("·恢复所有HP，获得永久ATK、DEF、EVD+1。");
                    break;
            }
        }else {
            switch ((int) Math.floor(Math.random()*3)){
                case 0:
                    image_path += "le0b.png";
                    messageChainBuilder.append("『脸伸过来！』");
                    break;
                case 1:
                    image_path += "le1b.png";
                    messageChainBuilder.append("『蓝碟！』");
                    break;
                case 2:
                    image_path += "le2b.png";
                    messageChainBuilder.append("『露露的幸运蛋壳！』");
                    break;
            }
        }
        try {
            ExternalResource resource = ExternalResource.create(new File(image_path));
            messageChainBuilder.append(messageEvent.getSubject().uploadImage(resource));
        }catch (Exception e){e.printStackTrace();}
        return messageChainBuilder.build();
    }

    public MessageChain leNico(){
        MessageChainBuilder messageChainBuilder = new MessageChainBuilder();
        messageChainBuilder.append("[奇迹漫步]结果:\n" +
                "「" + mwList[(int) Math.floor(Math.random()*mwList.length)] +
                "」「" + mwList[(int) Math.floor(Math.random()*mwList.length)] +
                "」「" + mwList[(int) Math.floor(Math.random()*mwList.length)] +
                "」「" + mwList[(int) Math.floor(Math.random()*mwList.length)] +
                "」");
        return messageChainBuilder.build();
    }

    public MessageChain leNanako(){
        MessageChainBuilder messageChainBuilder = new MessageChainBuilder();
        int bit7[] = {0,0,0};
        for (int i = 0; i < 7; i++) {
            bit7[(int) Math.floor(Math.random()*3)]++;
        }
        messageChainBuilder.append("[浮游炮]分配结果:\n" +
                "+(" + bit7[0] + " " + bit7[1] + " " + bit7[2] + ")");
        return messageChainBuilder.build();
    }

    public MessageChain leSay(){
        MessageChainBuilder messageChainBuilder = new MessageChainBuilder();
        File cvDir = new File("./data/le/cv/");
        File[] cvFileList = cvDir.listFiles();
        int cvFileIndex = (int) (Math.random()*cvFileList.length);
        String cvContent = "";
        String cvFileName = cvFileList[cvFileIndex].getName();
        while (!cvContent.startsWith("<")){
            cvContent = DataUtil.readRandomLine(cvFileList[cvFileIndex]);
        }
        cvContent = cvContent.replaceAll("<[0-9]{3,4}> ","");
        cvContent = cvContent.replace("\\n","\n");
        messageChainBuilder.append("「" + cvContent + "」\n— " + cvFileName.substring(11,cvFileName.length()-4));
        return messageChainBuilder.build();
    }
}

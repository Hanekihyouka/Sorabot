package haneki.module.instance;

import haneki.module.BasicModule;
import haneki.module.MessageModule;
import haneki.util.DataUtil;
import haneki.util.HtmlTools;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.ForwardMessageBuilder;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.PlainText;

public class LhwBattle extends BasicModule implements MessageModule {

    public LhwBattle(String module_name) {
        super(module_name);
    }

    @Override
    public void moduleReact() {

    }

    @Override
    public String getTiggerRegex() {
        return "(?i)((-?\\d){4}( (Sherry|Repa|Msk|Tql|Iru))? vs (-?\\d){4}( (Sherry|Repa|Msk|Tql|Iru))?)|(#?vs help)";
    }

    @Override
    public MessageChain moduleReact(MessageChain message, MessageEvent messageEvent, Bot bot) {
        MessageChainBuilder messageChainBuilder = new MessageChainBuilder();
        Group group = bot.getGroup(205312025L);//SumikaSystem
        ForwardMessageBuilder forwardMessageBuilder = new ForwardMessageBuilder(group);
        if (message.contentToString().contains("vs help")){
            forwardMessageBuilder.add(bot.getId(),"vs help",new PlainText(
                    "vs 使用方法\n" +
                            "\n" +
                            "基础：\n" +
                            "血攻防闪 vs 血攻防闪\n" +
                            "41-12 vs 411-1\n" +
                            "\n" +
                            "增加被动：\n" +
                            "4001 Repa vs 41-20 Msk\n" +
                            "\n" +
                            "被动列表：\n" +
                            "雪莉：Sherry\n" +
                            "蕾帕：Repa\n" +
                            "美羽咲：Msk\n" +
                            "船长：Tql\n" +
                            "依琉：Iru"
            ));
            return messageChainBuilder.append(forwardMessageBuilder.build()).build();
        }
        String[] players = message.contentToString().split(" vs ");
        String[] params = new String[2];
        params[0] = players[0].split(" ")[0];
        params[1] = players[1].split(" ")[0];
        String[] p = new String[8];
        int pi = 0;
        for (int pj = 0; pj < 2; pj++) {
            char[] chara = params[pj].toCharArray();
            for (int i = 0; i < chara.length; i++) {
                if (chara[i]!='-'){
                    p[pi] = String.valueOf(chara[i]);
                }else {
                    p[pi] = String.valueOf(chara[i]) + chara[i + 1];
                    i++;
                }
                pi++;
            }
        }
        String get_url = "http://47.242.108.196:1570/battle" +
                "?hp=" + p[0] +
                "&atk=" + p[1] +
                "&def=" + p[2] +
                "&evd=" + p[3] +
                "&hpt=" + p[4] +
                "&atkt=" + p[5] +
                "&deft=" + p[6] +
                "&evdt=" + p[7];
        if (players[0].split(" ").length==2){
            get_url += "&psv=" + DataUtil.caotureName(players[0].split(" ")[1]);
        }
        if (players[1].split(" ").length==2){
            get_url += "&psvt=" + DataUtil.caotureName(players[1].split(" ")[1]);
        }
        System.out.println("[OJBattle]" + get_url);
        String result = HtmlTools.readStringFromURL(get_url);
        forwardMessageBuilder.add(bot.getId(),"oj battle",new PlainText(result));
        return messageChainBuilder.append(forwardMessageBuilder.build()).build();
    }
}

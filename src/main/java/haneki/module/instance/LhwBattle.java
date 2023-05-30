package haneki.module.instance;

import haneki.module.BasicModule;
import haneki.module.MessageModule;
import haneki.util.HtmlTools;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.ForwardMessageBuilder;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;

public class LhwBattle extends BasicModule implements MessageModule {

    public LhwBattle(String module_name) {
        super(module_name);
    }

    @Override
    public void moduleReact() {

    }

    @Override
    public String getTiggerRegex() {
        return "#ojbk? .*";
    }

    @Override
    public MessageChain moduleReact(MessageChain message, MessageEvent messageEvent, Bot bot) {
        MessageChainBuilder messageChainBuilder = new MessageChainBuilder();

        Group group = bot.getGroup(205312025L);//SumikaSystem
        ForwardMessageBuilder forwardMessageBuilder = new ForwardMessageBuilder(group);
        String[] params = message.contentToString().split(" ");
        switch (params.length){
            case 9:
                messageChainBuilder.append(HtmlTools.readStringFromURL("http://47.242.108.196:1570/battle" +
                        "?hp=" + params[1] +
                        "&atk=" + params[2] +
                        "&def=" + params[3] +
                        "&evd=" + params[4] +
                        "&hpt=" + params[5] +
                        "&atkt=" + params[6] +
                        "&deft=" + params[7] +
                        "&evdt=" + params[8]
                ));
                break;
            case 3:
                String[] p = new String[8];
                int pi = 0;
                for (int pj = 1; pj < 3; pj++) {
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
                messageChainBuilder.append(HtmlTools.readStringFromURL("http://47.242.108.196:1570/battle" +
                        "?hp=" + p[0] +
                        "&atk=" + p[1] +
                        "&def=" + p[2] +
                        "&evd=" + p[3] +
                        "&hpt=" + p[4] +
                        "&atkt=" + p[5] +
                        "&deft=" + p[6] +
                        "&evdt=" + p[7]
                ));
                break;
            default:
                messageChainBuilder.append("参数数量错误。");
                break;
        }
        return messageChainBuilder.build();
    }
}

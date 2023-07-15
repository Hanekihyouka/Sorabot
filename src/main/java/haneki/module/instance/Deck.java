package haneki.module.instance;

import haneki.module.BasicModule;
import haneki.module.MessageModule;
import haneki.util.HtmlTools;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.utils.ExternalResource;

public class Deck extends BasicModule implements MessageModule {
    static final String URL_HEADER = "http://interface.100oj.com/deck/render.php?deck=";//P___________

    public Deck(String module_name) {
        super(module_name);
    }

    @Override
    public void moduleReact() {

    }

    @Override
    public String getTiggerRegex() {
        return "(?i)#deck=.*";
    }

    @Override
    public MessageChain moduleReact(MessageChain message, MessageEvent messageEvent, Bot bot) {
        MessageChainBuilder messageChainBuilder = new MessageChainBuilder();
        String content = message.contentToString().substring(6,18);
        System.out.println("[Deck]获取卡组>" + content);
        try {
            switch (content.charAt(0)){
                case 'Q':
                case 'R':
                case 'S':
                case 'T':
                case 'U':
                case 'V':
                case 'W':
                case 'X':
                case 'Y':
                case 'Z':
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                    messageChainBuilder.append("合作卡组>");
                    break;
                default:
                    messageChainBuilder.append("非合作卡组>");
            }

            ExternalResource ex = ExternalResource.Companion.create(HtmlTools.getUrlByByte(URL_HEADER + content));
            Image img = ExternalResource.uploadAsImage(ex,bot.getGroup(475379747));//上传图片
            messageChainBuilder.append(img);
        }catch (Exception e){
            System.out.println("[Deck]请求/上传图片时出错。");
            e.printStackTrace();
        }

        return messageChainBuilder.build();
    }
}

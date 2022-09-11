package haneki.module.instance;

import haneki.module.BasicModule;
import haneki.module.MessageModule;
import haneki.util.HtmlSoup;
import haneki.util.HtmlTools;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.*;
import net.mamoe.mirai.utils.ExternalResource;

public class Gelbooru extends BasicModule implements MessageModule {

    public Gelbooru(String module_name) {
        super(module_name);
    }

    @Override
    public void moduleReact() {

    }

    @Override
    public String getTiggerRegex() {
        return "#(gallery|gel|gelbooru|booru) random";
    }

    @Override
    public MessageChain moduleReact(MessageChain message, MessageEvent messageEvent, Bot bot) {
        // https://100oj.booru.org/index.php?page=post&s=random
        MessageChainBuilder messageChainBuilder = new MessageChainBuilder();
        Group group = bot.getGroup(205312025L);//SumikaSystem
        ForwardMessageBuilder forwardMessageBuilder = new ForwardMessageBuilder(group);
        String url = HtmlSoup.getRandomImageUrl();
        try {
            ExternalResource ex = ExternalResource.Companion.create(HtmlTools.getUrlByByte(url));
            Image img = ExternalResource.uploadAsImage(ex,group);//上传图片
            forwardMessageBuilder.add(bot.getId(),"仅用作测试",img);
            forwardMessageBuilder.add(bot.getId(),"仅用作测试",new PlainText("！！在本条提示被移除之前，不要启用本模块！！"));
        }catch (Exception e){
            System.out.println("[WebGet]请求/上传图片时出错。");
            e.printStackTrace();
        }

        return messageChainBuilder.append(forwardMessageBuilder.build()).build();
    }
}

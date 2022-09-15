package haneki.module.instance;

import haneki.module.BasicModule;
import haneki.module.MessageModule;
import haneki.util.BooruImageInfo;
import haneki.util.DataUtil;
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
        return "#(gallery|gel|gelbooru|booru).*";
    }

    @Override
    public MessageChain moduleReact(MessageChain message, MessageEvent messageEvent, Bot bot) {
        // https://100oj.booru.org/index.php?page=post&s=random
        String[] params = message.contentToString().split(" ");
        MessageChainBuilder messageChainBuilder = new MessageChainBuilder();
        switch (params.length){
            case 2:
                BooruImageInfo booruImageInfo = null;
                switch (params[1]){
                    case "random":
                    case "-r":
                        booruImageInfo = HtmlSoup.getRandomBooruImage();
                        break;
                    default:
                        if (DataUtil.isNumericZidai(params[1])){
                            booruImageInfo = HtmlSoup.getBooruImagebyUrl("https://100oj.booru.org/index.php?page=post&s=view&id=" + params[1]);
                        }else {
                            booruImageInfo.setId(params[1]);
                            booruImageInfo.setImage_url("http://interface.oranges.wang/common/data/emotes/fr_mpoppo00_large.png");
                            booruImageInfo.setRating("R 18");
                        }
                        break;
                }

                Group group = bot.getGroup(205312025L);//SumikaSystem
                ForwardMessageBuilder forwardMessageBuilder = new ForwardMessageBuilder(group);
                try {
                    ExternalResource ex = ExternalResource.Companion.create(HtmlTools.getUrlByByte(booruImageInfo.getImage_url()));
                    Image img = ExternalResource.uploadAsImage(ex,group);//上传图片
                    forwardMessageBuilder.add(bot.getId(),"booru",img);
                    StringBuilder tags = new StringBuilder();
                    for (String tag:booruImageInfo.getTags()) {
                        tags.append("\n  " + tag);
                    }
                    forwardMessageBuilder.add(bot.getId(),"info",new PlainText(
                            "id:  " + booruImageInfo.getId() + "\n" +
                                    "rating:  " + booruImageInfo.getRating() + "\n" +
                                    "tags:  " + tags));
                }catch (Exception e){
                    System.out.println("[WebGet]请求/上传图片时出错。");
                    e.printStackTrace();
                }
                messageChainBuilder.append(forwardMessageBuilder.build());

                break;
            case 1:
            default:
                messageChainBuilder.append("本模块用于从[图库](https://100oj.booru.org/)中获取图片。\n#booru random  获取随机图片。" +
                        "\n#booru [id]  来获取对应[id]的图片。" +
                        "\n本模块未完成。");
                break;
        }



        return messageChainBuilder.build();
    }
}

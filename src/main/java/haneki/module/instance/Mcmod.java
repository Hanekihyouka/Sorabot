package haneki.module.instance;

import haneki.module.BasicModule;
import haneki.module.MessageModule;
import haneki.util.HtmlSoup;
import haneki.util.HtmlTools;
import haneki.util.McmodDataInfo;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.*;
import net.mamoe.mirai.utils.ExternalResource;


public class Mcmod extends BasicModule implements MessageModule {

    public Mcmod(String module_name) {
        super(module_name);
    }

    @Override
    public void moduleReact() {

    }

    @Override
    public String getTiggerRegex() {
        return "(?i)#(mcmod).*";
    }

    @Override
    public MessageChain moduleReact(MessageChain message, MessageEvent messageEvent, Bot bot) {
        String[] params = message.contentToString().split(" ",3);
        MessageChainBuilder messageChainBuilder = new MessageChainBuilder();
        int filter = 0;
        String searchKey;
        //ser filter and searchKey
        switch (params[1].toLowerCase()){
            case "mod":
            case "1":
                filter = 1;
                searchKey = params[2];
                break;
            case "pack":
            case "modpack":
            case "2":
                filter = 2;
                searchKey = params[2];
                break;
            case "data":
            case "item":
            case "3":
                filter = 3;
                searchKey = params[2];
                break;
            case "tutorial":
            case "article":
            case "4":
                filter = 4;
                searchKey = params[2];
                break;
            default:
                searchKey = message.contentToString().split(" ",2)[1];
                break;
        }
        String[][] dataList = HtmlSoup.getMcmodDataList(searchKey,filter);
        if (dataList.length>0){
            Group group = bot.getGroup(475379747L);//SumikaSystem
            ForwardMessageBuilder forwardMessageBuilder = new ForwardMessageBuilder(group);
            forwardMessageBuilder.add(bot.getId(),"请发送编号",new PlainText("请发送编号查看。"));
            for (int i = 0; i < dataList.length; i++) {
                forwardMessageBuilder.add(bot.getId(),i + " : " + dataList[i][0],new PlainText(i + "\n" + dataList[i][0]));
            }
            messageChainBuilder.append(forwardMessageBuilder.build());
            bot.getEventChannel().filter(ev ->
                    ev instanceof MessageEvent &&
                            ((MessageEvent) ev).getSender().getId()==messageEvent.getSender().getId()
            ).subscribeOnce(MessageEvent.class,event -> {
                String content = event.getMessage().contentToString();
                    if (content.matches("[0-7]")){
                        String dataURL = dataList[Integer.parseInt(content)][1];
                        McmodDataInfo mcData = HtmlSoup.getMcmodDataInfobyUrl(dataURL);
                        ForwardMessageBuilder mcforwardMessageBuilder = new ForwardMessageBuilder(group);
                        try {
                            ExternalResource ex = ExternalResource.Companion.create(HtmlTools.getUrlByByte(mcData.getIconURL()));
                            Image img = ExternalResource.uploadAsImage(ex,bot.getGroup(475379747));//上传图片
                            mcforwardMessageBuilder.add(bot.getId(),"资料",img);
                        }catch (Exception e){
                            System.out.println("[Mcmod]请求/上传图片时出错。");
                            e.printStackTrace();
                        }
                        mcforwardMessageBuilder.add(bot.getId(),"资料",new PlainText(mcData.getTitle()));
                        mcforwardMessageBuilder.add(bot.getId(),"资料",new PlainText(mcData.getDataURL()));
                        mcforwardMessageBuilder.add(bot.getId(),"资料",new PlainText(mcData.getContent()));

                        event.getSubject().sendMessage(mcforwardMessageBuilder.build());
                    }else {
                        event.getSubject().sendMessage("查询已取消。");
                    }
            });
        }else {
            messageChainBuilder.append("没有相关结果。");
        }

        return messageChainBuilder.build();
    }
}

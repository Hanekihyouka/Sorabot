package haneki.module.instance;

import haneki.module.BasicModule;
import haneki.module.MessageModule;
import haneki.util.EmoteManager;
import haneki.util.HtmlTools;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.*;
import net.mamoe.mirai.utils.ExternalResource;


import java.io.File;


public class SumikaSqlEmote extends BasicModule implements MessageModule {
    public SumikaSqlEmote(String module_name) {
        super(module_name);
    }

    @Override
    public void moduleReact() {

    }

    @Override
    public String getTiggerRegex() {
        return "((?!#)(.|\\r|\\n)*:(([0-9A-Za-z_]*)|(#[0-9A-Fa-f]{1,8})):(.|\\r|\\n)*)|(#emote.*)";
    }

    @Override
    public MessageChain moduleReact(MessageChain message, MessageEvent messageEvent, Bot bot) {
        MessageChainBuilder messageChainBuilder = new MessageChainBuilder();
        if (message.contentToString().startsWith("#emote")){
            String content = message.contentToString().replaceAll("[^a-zA-Z0-9_ -]","");
            String[] params = content.split(" ");
            switch (params.length){
                case 1:
                    messageChainBuilder.append(new PlainText("使用:emote:来发送游戏中的表情，支持拼接和换行。拼接有大小限制。\n" +
                            "使用[#emote add|replace [emoteName] [图片]]来添加/覆盖自定的表情。仅支持字母、数字和下划线。\n" +
                            "使用[#emote remove [emoteName]]来删除对应的表情。\n" +
                            "使用[#emote list|-l]来查看所有自定表情。"));
                    break;
                case 2:
                    switch (params[1]){
                        case "add":
                        case "replace":
                            messageChainBuilder.append(new PlainText("#emote add|replace [emoteName] [图片]"));
                            break;
                        case "remove":
                            messageChainBuilder.append(new PlainText("#emote remove [emoteName]"));
                            break;
                        case "list":
                        case "-l":
                            EmoteManager emoteManager = new EmoteManager();
                            String emotesList = "表情列表>";
                            String[] emoteFiles = new File("./data/.emote").list();
                            for (String s:emoteFiles) {
                                emotesList += "    " + s;
                            }
                            messageChainBuilder.append(new PlainText(emotesList));
                            break;
                    }
                    break;
                default:
                    EmoteManager emoteManager = new EmoteManager();
                    switch (params[1]){
                        case "add":
                        case "replace":
                            Image image = (Image) message.stream().filter(Image.class::isInstance).findFirst().orElse(null);
                            // 0-#emote 1-add 2-key0 3-key1
                            for (int i = 2; i < params.length; i++) {
                                HtmlTools.downLoadFromUrl(Image.queryUrl(image),params[i],"./data/.emote/");
                            }
                            messageChainBuilder.append(new PlainText("Done!"));
                            break;
                        case "remove":
                            String response = "移除表情>";
                            for (int i = 2; i < params.length; i++) {
                                File file = new File("./data/.emote/" + params[i]);
                                if (file.exists()){
                                    if (file.delete()){
                                        response += "\n已删除 " + params[i];
                                    }else {
                                        response += "\n无法删除 " + params[i];
                                    }
                                }else {
                                    response += "\n没有 " + params[i];
                                }
                            }
                            messageChainBuilder.append(new PlainText(response));
                            break;
                        default:
                            messageChainBuilder.append(new PlainText("不正确的参数。"));
                            break;
                    }
            }
        }else {
            //only   nums\chars\_:   left
            String[] content = message.contentToString().replaceAll("[^a-zA-Z0-9_ :\\r\\n#]", "").split(":",256);
            EmoteManager emoteManager = new EmoteManager();
            File emoteFile = new File(emoteManager.generator(content));
            ExternalResource resource = ExternalResource.create(emoteFile);
            messageChainBuilder.append(messageEvent.getSubject().uploadImage(resource));
            try {emoteFile.delete();}catch (Exception ignored){}
        }
        return messageChainBuilder.build();
    }
}

package haneki.module.instance;

import haneki.module.BasicModule;
import haneki.module.MessageModule;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.*;

public class EssentialsX extends BasicModule implements MessageModule {
    public EssentialsX(String module_name) {
        super(module_name);
    }

    @Override
    public String getTiggerRegex() {
        return "(?i)##Ess(ential(s(X)?)?)?.*";
    }

    @Override
    public MessageChain moduleReact(MessageChain message, MessageEvent messageEvent, Bot bot) {
        String[] params = message.contentToString().split(" ");
        MessageChainBuilder messageChainBuilder = new MessageChainBuilder();
        if (params.length==1){
            messageChainBuilder.append("输入 '##ess help' 来查看帮助");
            return messageChainBuilder.build();
        }
        switch (params[1]){
            case "help":
            case "-h":
                Group group = bot.getGroup(205312025L);//SumikaSystem
                ForwardMessageBuilder forwardMessageBuilder = new ForwardMessageBuilder(group);
                forwardMessageBuilder.add(bot.getId(),"INDEX",new PlainText("##EssentialsX模块，可用指令头>help、module、reload、operator"));
                forwardMessageBuilder.add(bot.getId(),"help",new PlainText("##ess help"));
                forwardMessageBuilder.add(bot.getId(),"module",new PlainText(
                        "需要对应权限。\n本群群主可以!!以不带群号的形式，在本群聊天!!开关本群模块。\n机器人操作员(暨次级管理 暨op)可以操作所有群的模块，在任何聊天。\n" +
                                "开启> ##ess module|-m enable|-on MODULE_NAME 可选[GROUP_ID]\n" +
                                "关闭> ##ess module|-m disable|-off MODULE_NAME 可选[GROUP_ID]\n" +
                                "列出> ##ess list|ls|-l 可选[group|-g]\n"));
                forwardMessageBuilder.add(bot.getId(),"reload",new PlainText(
                        "需要操作员权限。\n" +
                                "保存当前配置到文件!!会覆盖文件!!\n如果你是在聊天中修改了配置，想要保存(!!大部分修改会自动保存，而无需再调用本条!!)，请使用这个" +
                                "> ##ess save|-s\n\n" +
                                "从文件读取配置!!会覆盖当前配置!!\n如果你是在网页端修改了文件，想要应用更新，请使用这个" +
                                "> ##ess load|-ld\n"));
                forwardMessageBuilder.add(bot.getId(),"operator",new PlainText(
                        "需要操作员权限。\n!!操作员之间可以互相添加和移除!!\n" +
                                "添加> ##ess operator add|+ QQUSER_ID\n" +
                                "移除> ##ess operator remove|- QQUSER_ID\n" +
                                "列出> ##ess operator list|ls|-l\n" +
                                "添加(只是写法不同)> ##ess op|+op| QQUSER_ID\n" +
                                "移除(只是写法不同)> ##ess deop|-op QQUSER_ID\n"));
                messageChainBuilder.append(forwardMessageBuilder.build());
                return messageChainBuilder.build();
            case "test":
            case "-t":
                if (params.length==2){
                    messageChainBuilder.append("SoraBot正飞速运转！");
                    return messageChainBuilder.build();
                }else {
                    messageChainBuilder.append("当前指令>test\n参数长度>" + (params.length-1));
                    return messageChainBuilder.build();
                }
        }
        return null;
    }


    @Override
    public void moduleReact() {

    }
}

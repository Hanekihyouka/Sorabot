package haneki.module.instance;

import haneki.module.BasicModule;
import haneki.module.MessageModule;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.ForwardMessageBuilder;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.PlainText;

import java.io.*;

public class VanillaBattle extends BasicModule implements MessageModule {

    private boolean isRunning = false;

    public VanillaBattle(String module_name) {
        super(module_name);
    }

    @Override
    public void moduleReact() {

    }

    @Override
    public String getTiggerRegex() {
        return "#wrc? .*";
    }

    @Override
    public MessageChain moduleReact(MessageChain message, MessageEvent messageEvent, Bot bot) {
        MessageChainBuilder messageChainBuilder = new MessageChainBuilder();

        Group group = bot.getGroup(205312025L);//SumikaSystem
        ForwardMessageBuilder forwardMessageBuilder = new ForwardMessageBuilder(group);

        if (isRunning){
            messageChainBuilder.append("请等待上一个计算结束。");
        }else {
            try {
                StringBuilder result = new StringBuilder();

                isRunning = true;
                String[] content = message.contentToString().substring(1).split(" ");
                String[] execCommand = new String[content.length+1];
                execCommand[0] = "/home/haneki/Projects/mirai/data/vanilla/vanilla";
                for (int i = 0; i < content.length; i++) {
                    execCommand[i+1] = content[i];
                }

                Process process = Runtime.getRuntime().exec(execCommand,null,new File("./data/vanilla"));
                System.out.println("[vanilla]->计算中...");

                BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while (((line) = br.readLine()) != null){
                    result.append(line).append("\n");
                }
                br.close();
                forwardMessageBuilder.add(bot.getId(),"胜率计算",new PlainText(result));
                messageChainBuilder.append(forwardMessageBuilder.build());
                //结束
                process.destroy();
                isRunning = false;
                System.out.println("[vanilla]->计算结束");
            }catch (IOException e){
                messageChainBuilder.append("Exception>").append(e.getMessage());
                e.printStackTrace();
            }
        }

        return messageChainBuilder.build();
    }
}

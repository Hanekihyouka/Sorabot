package haneki.module.instance;

import haneki.module.BasicModule;
import haneki.module.MessageModule;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.util.NoSuchElementException;

public class BigRedButton extends BasicModule implements MessageModule {
    public BigRedButton(String module_name) {
        super(module_name);
    }

    @Override
    public void moduleReact() {

    }

    @Override
    public String getTiggerRegex() {
        return "#大红按钮";
    }

    @Override
    public MessageChain moduleReact(MessageChain message, MessageEvent messageEvent, Bot bot) {
        bot.getGroups().forEach((e) -> {
            MessageChainBuilder messageChainBuilder = new MessageChainBuilder();
            boolean isABhere1 = false;
            boolean isABhere2 = false;
            try {
                e.getOrFail(1679875310L);
                isABhere1 = true;
            }catch (NoSuchElementException ignored){
                isABhere1 = false;
            }
            try {
                e.getOrFail(2134641032L);
                isABhere2 = true;
            }catch (NoSuchElementException ignored){
                isABhere2 = false;
            }
            if (isABhere1){
                messageChainBuilder.append(new At(1679875310L));
            }
            if (isABhere2){
                messageChainBuilder.append(new At(2134641032L));
            }
            if (isABhere1||isABhere2){
                e.sendMessage(messageChainBuilder.build());
            }
        });
        return null;
    }
}

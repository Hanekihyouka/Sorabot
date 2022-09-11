package haneki.module;


import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChain;

public interface MessageModule{

    String getTiggerRegex();
    /**
     * 触发后，本模块的回复内容
     * */
    MessageChain moduleReact(MessageChain message, MessageEvent messageEvent, Bot bot);


}

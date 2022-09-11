package haneki.module;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChain;

import java.util.List;

public interface NeedOperactor extends MessageModule{
    MessageChain moduleReact(MessageChain message, MessageEvent messageEvent, Bot bot, List<Long> operator, List<Long> superadmin);
}

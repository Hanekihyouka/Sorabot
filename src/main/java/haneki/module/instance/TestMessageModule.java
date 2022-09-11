package haneki.module.instance;

import haneki.module.BasicModule;
import haneki.module.MessageModule;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;

public class TestMessageModule extends BasicModule implements MessageModule {

    public TestMessageModule(String module_name) {
        super(module_name);
    }

    @Override
    public void moduleReact() {

    }

    @Override
    public String getTiggerRegex() {
        return "(?i)##T(est)?";
    }

    @Override
    public MessageChain moduleReact(MessageChain message, MessageEvent messageEvent, Bot bot) {
        MessageChainBuilder messageChainBuilder = new MessageChainBuilder()
                .append("喵呜！");
        return messageChainBuilder.build();
    }
}

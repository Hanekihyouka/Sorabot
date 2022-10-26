package haneki.module.instance;

import haneki.module.BasicModule;
import haneki.module.MessageModule;
import haneki.util.CombineEmote;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
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
        return "(.|\\r|\\n)*:[0-9A-Za-z_]*:(.|\\r|\\n)*";
    }

    @Override
    public MessageChain moduleReact(MessageChain message, MessageEvent messageEvent, Bot bot) {
        //only   nums\chars\_:   left
        String[] content = message.contentToString().replaceAll("[^a-zA-Z0-9_ :\\r\\n]", "").split(":",65);
        MessageChainBuilder messageChainBuilder = new MessageChainBuilder();
        CombineEmote combineEmote = new CombineEmote();
        ExternalResource resource = ExternalResource.create(new File(combineEmote.generator(content)));
        messageChainBuilder.append(messageEvent.getSubject().uploadImage(resource));
        return messageChainBuilder.build();
    }
}

package haneki.module.instance;

import haneki.module.BasicModule;
import haneki.module.MessageModule;
import haneki.util.HtmlTools;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.utils.ExternalResource;



import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class WebGet extends BasicModule implements MessageModule {
    public WebGet(String module_name) {
        super(module_name);
    }

    @Override
    public void moduleReact() {

    }

    @Override
    public String getTiggerRegex() {
        return "(.|\\n)*!\\[.*?\\]\\((.+?)\\)(.|\\n)*";
    }

    @Override
    public MessageChain moduleReact(MessageChain message, MessageEvent messageEvent, Bot bot) {
        MessageChainBuilder messageChainBuilder = new MessageChainBuilder();
        String content = message.contentToString();
        String pattern = "!\\[.*?\\]\\((.+?)\\)";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(content);
        while (m.find()){
            String url = m.group(1);
            if (url.startsWith("http")){
                //HtmlTools.downLoadFromUrl(url,System.currentTimeMillis() + ".webp","./data/web_get_image");
                try {
                    ExternalResource ex = ExternalResource.Companion.create(HtmlTools.getUrlByByte(url));
                    Image img = ExternalResource.uploadAsImage(ex,bot.getGroup(205312025));//上传图片
                    messageChainBuilder.append(img);
                }catch (Exception e){
                    System.out.println("[WebGet]请求/上传图片时出错。");
                    e.printStackTrace();
                }
            } else if (url.startsWith("!http")) {
                String out_path = System.getProperty("user.dir")
                        + "/data/.web_get_image/" + Long.toHexString(System.currentTimeMillis()) + "_" + Integer.toHexString(url.hashCode()) + ".jpg";
                try {
                    //xvfb-run --server-args="-screen 0, 1920x1080x24" cutycapt --url="https://interface.100oj.com/stat/global.php" --out=out.jpg
                    Process process = Runtime.getRuntime().exec(new String[]{
                            "sh",
                            System.getProperty("user.dir") + "/data/.web_get_image/gen.sh",
                            url.substring(1),
                            out_path
                    });
                    BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String s;
                    while ((s = br.readLine()) != null){
                        System.out.println("[XC]" + s);
                    }
                    process.waitFor();

                    ExternalResource ex = ExternalResource.Companion.create(new File(out_path));
                    Image img = ExternalResource.uploadAsImage(ex,bot.getGroup(205312025));//上传图片
                    messageChainBuilder.append(img);

                    br.close();
                    process.destroy();
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }
        return messageChainBuilder.build();
    }
}

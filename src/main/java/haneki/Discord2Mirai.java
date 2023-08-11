package haneki;


import haneki.util.HtmlTools;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.utils.ExternalResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Discord2Mirai extends ListenerAdapter {
    String tmd_dirPath = "/home/haneki/Voxel/twitter_media_downloader";
    String galleryPath = "/home/haneki/Pixel/oj/interface/carton/gallery";
    String discordImgSource = "https://cdn.discordapp.com/emojis/";
    Bot bot;
    public Discord2Mirai(Bot bot) {
        System.out.println("[D2M]Discord bot loading...");
        this.bot = bot;
        System.out.println("[D2M]Discord bot done!");
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String tmd_command = "python3 " + tmd_dirPath + "/twitter-media-downloader.py url";
        if (event.getAuthor().isBot()){
            System.out.println("来自Discord的消息>[Furitbat Factory Community Server #announcements]\n");
        }
        Message message = event.getMessage();
        String content = message.getContentRaw();


        if (content.equals("!ping")){
            MessageChannel channel = event.getChannel();
            channel.sendMessage("pong!").queue();
        }
        Long channel_id = event.getMessage().getChannel().getIdLong();
        boolean toOJGroup = false;
        boolean hasTwitter = false;
        boolean hasPixiv = false;
        //转发到qq

        if(channel_id==819881875660603402L| channel_id==960682453209587712L | channel_id==464075692657606659L ){
            MessageChainBuilder chain = new MessageChainBuilder();
            chain.append("Discord  ");
            //头部
            switch (String.valueOf(channel_id)){
                case "819881875660603402"://bot-cmd
                    chain.append("#bot-cmd>\n");
                    break;
                case "960682453209587712"://box-大转发池子
                    toOJGroup = true;
                    break;
                case "464075692657606659"://77-画廊
                    chain.append("#橙汁秘密基地-画廊>" + event.getAuthor().getName() + "->\n");
                    toOJGroup = true;
                    break;
            }

            /**
             * emote 表情
             * 以及 discrod cdn 的图片 <- 也就是上传图片后的链接。
             * 表情 <a:emj:xxxx>
             * 图片 https://cdn.discordapp.com/attachments/888759138308481044/1004250399143903273/Plushie-Photos-14.jpg
             * **/

            String pattern = "<(a|b)?:\\w*:(\\d{15,20})>|(https?://.*?\\.(jpg|jpeg|png|gif))";

            /**
             * 0
             * 1  (a|b)
             * 2  (\d{15,20})
             * 3  (https?://.*?\.(jpg|jpeg|png|gif))
             * 4  (jpg|jpeg|png|gif)
             * **/
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(content);

            int start = 0;
            int end = content.length();

            for (int i = 0;m.find(); i++) {
                //添加本段前文本
                chain.append(content.substring(start,m.start()));
                ExternalResource ex = null;
                try {
                    if (m.group(2) != null){
                        ex = ExternalResource.Companion.create(HtmlTools.getUrlByByte(discordImgSource + m.group(2) + "?size=64"));
                    }else if (m.group(3) != null){
                        ex = ExternalResource.Companion.create(HtmlTools.getUrlByByte(m.group(3)));
                    }
                    Image img = ExternalResource.uploadAsImage(ex,bot.getGroup(475379747));//上传图片
                    ex.close();
                    //添加本段图片
                    chain.append(img);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                start = m.end();
            }
            //补充最后一段后文本
            chain.append(content.substring(start,end));

            /**
             * 自带的图片
             *
             * **/

            List<Message.Attachment> attachments = message.getAttachments();
            for (Message.Attachment attachment:attachments) {
                if (attachment.isImage()){
                    ExternalResource ex = null;
                    try {
                        ex = ExternalResource.Companion.create(HtmlTools.getUrlByByte(attachment.getUrl()));
                        Image img = ExternalResource.uploadAsImage(ex,bot.getGroup(475379747));//上传图片
                        ex.close();
                        //添加图片
                        chain.append(img);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }


            /**
             * twitter 图片
             * 放在最后
             *
             * **/

            pattern = "https://(vx)?twitter\\.com/\\w+?/status/[0-9]{18,22}";
            r = Pattern.compile(pattern);
            m = r.matcher(content);

            for (int i = 0;m.find(); i++) {
                hasTwitter = true;
                tmd_command += " \"" + m.group(0).replaceFirst("https://vx","https://") + "\"";
            }
            if (hasTwitter){
                try {
                    Process pro = Runtime.getRuntime().exec(tmd_command);//利用tmd.py抓取图片到本地
                    System.out.println("[tmd抓取]->" + tmd_command);
                    pro.waitFor();
                    System.out.println("[tmd抓取结束]");
                    File dir = new File(tmd_dirPath + "/twitter_media_download");
                    File[] files = dir.listFiles();
                    for (File img:files) {
                        String img_name = img.getName();
                        if (img_name.endsWith(".jpg")|img_name.endsWith(".png")|img_name.endsWith(".jpeg")|img_name.endsWith(".gif")){
                            Image sendimg = net.mamoe.mirai.contact.Contact.uploadImage(bot.getGroup(475379747), new FileInputStream(tmd_dirPath + "/twitter_media_download/" + img_name));//上传
                            chain.append(sendimg);
                        }else if (img_name.endsWith(".mp4")){
                            // twitter gif
                            ExternalResource gif = ExternalResource.create(img);
                            bot.getGroup(475379747).getFiles().getRoot().resolveFolder("临时文件").uploadNewFile(img_name,gif);//纸箱
                            if (toOJGroup){
                                bot.getGroup(223667456).getFiles().getRoot().resolveFolder("临时文件").uploadNewFile(img_name,gif);//佳
                                bot.getGroup(572808546).getFiles().getRoot().resolveFolder("临时文件").uploadNewFile(img_name,gif);//小店
                            }
                            gif.close();
                        }
                        img.renameTo(new File(galleryPath + "/" + img_name));
                    }

                    pro.destroy();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }

            /**
             * pixiv 图片
             * 放在最后
             *
             * **/

            pattern = "https://www.pixiv.net/\\w?\\w?/?artworks/([0-9]{6,10})";
            r = Pattern.compile(pattern);
            m = r.matcher(content);
            for (int i = 0;m.find(); i++) {
                ExternalResource ex = null;
                try {
                    ex = ExternalResource.Companion.create(HtmlTools.getUrlByByte("https://px2.rainchan.win/img/regular/" + m.group(1)));
                    Image img = ExternalResource.uploadAsImage(ex,bot.getGroup(475379747));//上传图片
                    ex.close();
                    //添加图片
                    chain.append(img);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            Objects.requireNonNull(bot.getGroup(475379747)).sendMessage(chain.build());//纸箱
            if (toOJGroup){
                Objects.requireNonNull(bot.getGroup(223667456)).sendMessage(chain.build());//佳
                Objects.requireNonNull(bot.getGroup(572808546)).sendMessage(chain.build());//小店
            }
        }else if(channel_id==612854891114790920L){
            //转发到 背着xx
            event.getGuild().getTextChannelById(1003977859876016259L).sendMessage(message).queue();

            //转发到 qq
            MessageChainBuilder chain = new MessageChainBuilder();
            chain.append("Discord  #橙汁秘密基地-画廊(nsfw)>" + event.getAuthor().getName() + "发表了新的内容。\n频道链接>\nhttps://discord.gg/gqzs2BA\n镜像链接>\n");

            /**
             * twitter 图片
             * 发布镜像链接
             *
             * **/
            String pattern = "https://(vx)?twitter\\.com/\\w+?/status/[0-9]{18,22}";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(content);

            for (int i = 0;m.find(); i++) {
                hasTwitter = true;
                tmd_command += " \"" + m.group(0).replaceFirst("https://vx","https://") + "\"";
            }
            if (hasTwitter){
                try {
                    Process pro = Runtime.getRuntime().exec(tmd_command);//利用tmd.py抓取图片到本地
                    System.out.println("[tmd抓取]->" + tmd_command);
                    pro.waitFor();
                    System.out.println("[tmd抓取结束]");
                    File dir = new File(tmd_dirPath + "/twitter_media_download");
                    File[] files = dir.listFiles();
                    for (File img:files) {
                        String img_name = img.getName();
                        if (img_name.endsWith("jpg")|img_name.endsWith("jpeg")|img_name.endsWith("png")|img_name.endsWith("gif")|img_name.endsWith("mp4")){
                            chain.append("https://interface.100oj.com/carton/gallery/" + img_name + "\n");
                        }
                        img.renameTo(new File(galleryPath + "/" + img_name));

                    }
                    pro.destroy();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }

            /**
             * pixiv 图片
             * 发布镜像链接
             *
             * **/

            pattern = "https://www.pixiv.net/\\w?\\w?/?artworks/([0-9]{6,10})";
            r = Pattern.compile(pattern);
            m = r.matcher(content);
            for (int i = 0;m.find(); i++) {
                hasPixiv = true;
                chain.append("https://px2.rainchan.win/img/regular/" + m.group(1) + "\n" + "pid:" + m.group(1));
            }

            if (hasPixiv | hasTwitter){
                Objects.requireNonNull(bot.getGroup(475379747)).sendMessage(chain.build());//纸箱
                Objects.requireNonNull(bot.getGroup(223667456)).sendMessage(chain.build());//佳
                Objects.requireNonNull(bot.getGroup(572808546)).sendMessage(chain.build());//小店
            }
        }

        System.out.println( "[Discord]Channel:[" + event.getMessage().getChannel() + "] -> " + event.getMessage().getContentDisplay());
    }
}

package haneki.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlSoup {
    public static void main(String[] args) {

        //McmodDataInfo mcmodDataInfo = getMcmodDataInfobyUrl("https://www.mcmod.cn/class/332.html");
        McmodDataInfo mcmodDataInfo = getMcmodDataInfobyUrl("https://www.mcmod.cn/item/10184.html");

        System.out.println(mcmodDataInfo.getIconURL());
        System.out.println(mcmodDataInfo.getTitle());
        System.out.println(mcmodDataInfo.getDataURL());
        System.out.println(mcmodDataInfo.getContent());
    }
    static final String randomUrl = "https://100oj.booru.org/index.php?page=post&s=random";

    public static BooruImageInfo getRandomBooruImage(){
        return getBooruImagebyUrl(randomUrl);
    }
    public static BooruImageInfo getBooruImagebyUrl(String pageUrl){
        BooruImageInfo booruImageInfo = new BooruImageInfo();
        try{
            Document document = Jsoup.connect(pageUrl).get();
            // 图源 url
            booruImageInfo.setImage_url(document.body().getElementById("image").attr("src"));
            // tag 列表
            Element tag_list = document.body().getElementById("tag_list");
            Elements tags = tag_list.getElementsByTag("a");
            booruImageInfo.setTags(tags.eachText());
            // rating 分级
            Element edit_form = document.body().getElementById("edit_form");
            booruImageInfo.setRating(edit_form.select("input[name='rating'][checked='checked']").attr("value"));
            // id
            booruImageInfo.setId(edit_form.select("input[name='id']").attr("value"));

        }catch (IOException e){
            e.printStackTrace();
        }

        return booruImageInfo;
    }

    public static String[][] getMcmodDataList(String searchKey,int filter){
        String searchURL = "https://search.mcmod.cn/s?filter=0&key=" + searchKey + "&filter=" + filter;
        try {
            Document document = Jsoup.connect(searchURL).get();
            Elements dataElements = document.body().getElementsByClass("result-item");
            int limit = 8;
            if (dataElements.size() < 8){
                limit = dataElements.size();
            }
            String[][] results = new String[limit][2];
            for (int i = 0; i < limit; i++) {
                Element dataElement = dataElements.get(i).getElementsByClass("head").first();
                System.out.println("======>>");
                System.out.println(dataElement.toString());
                System.out.println("<<======");
                Element result = dataElement.selectFirst("a[href^=http]");
                String resultName = result.text();
                String resultURL = result.attr("href");
                results[i][0] = resultName;
                results[i][1] = resultURL;
            }
            return results;
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    public static McmodDataInfo getMcmodDataInfobyUrl(String pageUrl){
        McmodDataInfo mcmodDataInfo = new McmodDataInfo();
        mcmodDataInfo.setDataURL(pageUrl);
        String pattern = "https://www\\.mcmod\\.cn/(\\w+?)/[0-9]{1,9}.html";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(pageUrl);
        m.matches();
        String dataType = m.group(1);
        switch (dataType){
            case "class":
                try {
                    Document document = Jsoup.connect(pageUrl).get();
                    mcmodDataInfo.setTitle(
                        document.body().getElementsByClass("short-name").text() + "\n"
                        + document.body().getElementsByTag("h3").text() + "\n"
                        + document.body().getElementsByTag("h4").text()
                    );
                    mcmodDataInfo.setIconURL("https:" + document.getElementsByClass("class-cover-image").first().getElementsByTag("img").attr("src"));
                    List<String> contents = document.body().select("div[data-frame='2']").get(1).selectFirst("li[data-id='1']").getElementsByTag("p").eachText();
                    String dataContent = "简介:";for (String s:contents) {dataContent += "\n" + s;}
                    mcmodDataInfo.setContent(dataContent);
                }catch (IOException e){
                    e.printStackTrace();
                }
                break;
            case "item":
                try {
                    Document document = Jsoup.connect(pageUrl).get();
                    mcmodDataInfo.setTitle(document.body().getElementsByClass("name").text());
                    mcmodDataInfo.setIconURL("https:" + document.body().getElementsByClass("item-info-table").first().select("img[src*='i.mcmod.cn/item/icon/128x128/']").attr("src"));
                    List<String> contents = document.body().getElementsByClass("item-content").first().getElementsByTag("p").eachText();
                    String dataContent = "简介:";for (String s:contents) {dataContent += "\n" + s;}
                    mcmodDataInfo.setContent(dataContent);
                }catch (IOException e){
                    e.printStackTrace();
                }
                break;
            default:
                mcmodDataInfo.setTitle("还没写好。");
                mcmodDataInfo.setContent("还没写好。");
                mcmodDataInfo.setIconURL("https://www.mcmod.cn/images/logo.gif");
                break;
        }
        return mcmodDataInfo;
    }
}

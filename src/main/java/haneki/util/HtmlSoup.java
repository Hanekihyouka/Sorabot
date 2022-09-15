package haneki.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class HtmlSoup {
    public static void main(String[] args) {
        String testurl = "https://100oj.booru.org/index.php?page=post&s=view&id=5270";
        BooruImageInfo testInfo = getBooruImagebyUrl(randomUrl);
        System.out.println(testInfo.getId());
        System.out.println(testInfo.getRating());
        System.out.println(testInfo.getImage_url());
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

}

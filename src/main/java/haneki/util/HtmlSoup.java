package haneki.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class HtmlSoup {
    public static void main(String[] args) {
        System.out.println(getRandomImageUrl());
    }
    static final String randomUrl = "https://100oj.booru.org/index.php?page=post&s=random";

    public static String getRandomImageUrl(){
        return getImageUrlbyUrl(randomUrl);
    }

    public static String getImageUrlbyUrl(String pageUrl){
        try {
            Document document = Jsoup.connect(pageUrl).get();
            return document.body().getElementById("image").attr("src");
        } catch (IOException e) {
            e.printStackTrace();
            return "https://interface.oranges.wang/common/data/emotes/fr_mpoppo00_large.png";
        }
    }

}

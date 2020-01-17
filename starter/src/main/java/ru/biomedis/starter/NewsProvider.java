package ru.biomedis.starter;


import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class NewsProvider {
    private  static String host = "biomedis.life";
    private static int port = 80;
    private static String ru_news="/doc/b_mair/apinews.php";
    private static String en_news="/doc/b_mair/apinewseng.php";
    private static int timeout = 25;

    public static CompletableFuture<List<News>> getRusNews(int maxResult){
       return getNews( host, ru_news,  port , timeout, maxResult);
    }

    public static CompletableFuture<List<News>> getEngNews(int maxResult){
        return getNews( host, en_news,  port , timeout, maxResult);
    }

    private static CompletableFuture<List<News>> getNews(String host,String path, int port ,int timeout, int maxResult){
        CompletableFuture<List<News>> future = new CompletableFuture<>();
        WebHelper.getWebHelper().getJsonData(host, path, port,timeout)
                 .thenAccept(n->{
                     try {
                         future.complete(buildFromJson(n,maxResult));
                     } catch (Exception e) {
                         future.completeExceptionally(e);
                     }
                 })
                 .exceptionally(e->{future.completeExceptionally(e);return null;});
        return  future;
    }



    private static List<News> buildFromJson(JsonObject o,int maxResult) throws Exception {
        if(!o.getString("result").equals("ok")) throw new Exception("Ошибка обработки данных на сервере");
        JsonArray news = o.getJsonArray("news",new JsonArray());
        List<News> res = new ArrayList<>();
        for(int i=0;i<news.size();i++)
        {
            if(i > maxResult) break;
            JsonObject newsObject = news.getJsonObject(i);
            res.add(new News(
                    Integer.valueOf(newsObject.getString("id","0")),
                    newsObject.getString("header",""),
                    newsObject.getString("url",""),
                    newsObject.getString("date","")));
        }
       return res;
    }

    @Data
    @AllArgsConstructor
    public static class News {
        private int id;
        private String title;
        private String url;
        private String date;

    }

}

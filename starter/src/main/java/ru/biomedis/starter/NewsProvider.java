package ru.biomedis.starter;


import io.vertx.core.json.JsonObject;

import java.util.concurrent.CompletableFuture;

public class NewsProvider {
    private  static String host = "biomedis.ru";
    private static int port = 80;
    private static String ru_news="/apinews.php";
    private static String en_news="/apinewseng.php";
    private static int timeout = 5;

    public static CompletableFuture<JsonObject> getRusNews(){
        return WebHelper.getWebHelper().getJsonData(host, ru_news,port,timeout);
    }

    public static CompletableFuture<JsonObject> getEngNews(){
        return WebHelper.getWebHelper().getJsonData(host, en_news,port,timeout);
    }


}

package ru.biomedis.starter;


import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.codec.BodyCodec;

import java.util.concurrent.CompletableFuture;

public class WebHelper {
   private WebClient client;
   private Vertx vertx =Vertx.vertx();
   private static WebHelper helper = new WebHelper();

    private WebHelper(){
        WebClientOptions options = new WebClientOptions()
                .setUserAgent("BiomedisMAir4")
                .setKeepAlive(false);

         client = WebClient.create(vertx, options);
    }

    public static WebHelper getWebHelper(){
        return helper;
    }


    /**
     * Делает запрос на получение JSON объекта
     * @param host хост
     * @param path путь от корня сайта
     * @param timeOutSec таймаут операции в секундах
     * @return
     */
    public CompletableFuture<JsonObject> getJsonData(String host, String path, int port, int timeOutSec){
        HttpRequest<JsonObject> request = client.get(port, host, path)
                                           .timeout(timeOutSec * 1000)
                                           .as(BodyCodec.jsonObject());

        request.headers().set("content-type", "application/json");
        CompletableFuture<JsonObject> future= new CompletableFuture();
        request.send(ar -> {
                    if (ar.succeeded()) {
                        HttpResponse<JsonObject> response = ar.result();
                        future.complete(response.body());
                    } else {
                        future.completeExceptionally(ar.cause());
                    }
                });

        return future;
    }

    /**
     * Закрывает клиент и освобождает ресурсы
     */
    public void close(){vertx.close();}

    public static void main(String[] args) {
        getWebHelper().getJsonData("biomedis.ru", "/doc/b_mair/apinewseng.php", 80,3)
                .thenAccept(System.out::println)
                .exceptionally(e->{e.printStackTrace(); return null;});

    }

}

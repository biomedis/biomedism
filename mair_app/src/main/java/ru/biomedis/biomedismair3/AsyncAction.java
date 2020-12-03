package ru.biomedis.biomedismair3;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.concurrent.Task;
import javafx.stage.Stage;
import ru.biomedis.biomedismair3.utils.Other.Result;


/**
 * Позволяет выполнять задачи неблокируя
 */
public class AsyncAction {

  private static final ExecutorService executor = Executors.newFixedThreadPool(3);

  @FunctionalInterface
  public interface Action1<T>{
    T get() throws Exception;
  }

  @FunctionalInterface
  public interface Action0{
    void run() throws Exception;
  }


  public static  <T> CompletableFuture<Result<T>> actionResult(Action1<T> action){

    return CompletableFuture.supplyAsync(()->{
      try {
        return action.get();
      }catch (Throwable e){
        throw new RuntimeException(e);
      }
    }, executor)
        .thenApplyAsync(Result::ok)
        .exceptionally(e ->  Result.error(e.getCause()));
  }


  public static CompletableFuture<Result<Void>> actionNoResult( Action0 action){

   return CompletableFuture.runAsync(()->{
     try {
       action.run();
     }catch (Throwable e){
       throw new RuntimeException(e);
     }
    }, executor)
        .thenApplyAsync(unused -> Result.ok((Void)null))
        .exceptionally(e ->  Result.error(e.getCause()));
  }


}

package ru.biomedis.biomedismair3;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import javafx.concurrent.Task;
import javafx.stage.Stage;
import ru.biomedis.biomedismair3.utils.Other.Result;


/**
 * Позволяет выполнять задачи блокирующи интерфейс с прогресс индикатором.
 */
public class BlockingAction {

  private static ExecutorService executor = Executors.newSingleThreadExecutor();

  @FunctionalInterface
  public interface Action1<T>{
    T get() throws Exception;
  }

  @FunctionalInterface
  public interface Action0{
    void run() throws Exception;
  }


  public static  <T> Result<T> actionResult(Stage context, Action1<T> action){

    Task<T> task = new Task<T>() {
      @Override
      protected T call() throws Exception {
        return action.get();
      }
    };
    task.setOnFailed(event -> Waiter.closeLayer());
    task.setOnSucceeded(event -> Waiter.closeLayer());

    executor.execute(task);
    Waiter.openLayer(context,true);
    if(task.getException() == null){
      T value = task.getValue();
      return Result.ok(value);
    }else {
      return Result.error(task.getException());
    }

  }

  public static Result<Void> actionNoResult(Stage context, Action0 action){

    Task<Void> task = new Task<Void>() {
      @Override
      protected Void call() throws Exception {
         action.run();
         return null;
      }
    };
    task.setOnFailed(event -> Waiter.closeLayer());
    task.setOnSucceeded(event -> Waiter.closeLayer());

    Waiter.openLayer(context,false);
    executor.execute(task);
    Waiter.show();
    if(task.getException()!=null) {

      return Result.error(task.getException());
    }
    else {

      return Result.ok(null);
    }

  }


}

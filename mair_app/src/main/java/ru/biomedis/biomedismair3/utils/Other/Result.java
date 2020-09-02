package ru.biomedis.biomedismair3.utils.Other;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;


public class Result<T> {
  private final Optional<T> value;
  private final Optional<Throwable> error;

  private Result(T value, Throwable error) {
    this.value = Optional.ofNullable(value);
    this.error = Optional.ofNullable(error);
  }


  public static <U> Result<U> ok(U value) {
    return new Result<>(value, null);
  }

  public static <U> Result<U> error(Throwable error) {
    return new Result<>(null, error);
  }


  public<U> Result<U> flatMap(Function<T, Result<U>> mapper) {

    if(this.isError()) {
      return Result.error(this.getError());
    }

    try{
      return mapper.apply(value.get());
    }catch (Exception e){
      return Result.error(e);
    }

  }

  public<U> Result<U> map(Function<T, U> mapper) {

    if(this.isError()) {
      return Result.error(this.getError());
    }

     try {
       return  ok(mapper.apply(value.get()));
     }catch (Exception e){
       return error(e);
     }
  }


  public T orElseGet(T defaultValue){
    if(error.isPresent()){
      return defaultValue;
    }else return value.get();
  }

  public T orElseThrow(Function<Throwable, Exception> action) throws Exception {
    if(error.isPresent()){
      throw action.apply(error.get());
    }else return value.get();
  }

  public T orElseGet(Function<Throwable, T> action) throws Exception {
    if(error.isPresent()){
      return action.apply(error.get());
    }else return value.get();
  }

  public void ifError(Consumer<Throwable> action){
    error.ifPresent(action);
  }

  public void ifPresent(Consumer<T> action){
    value.ifPresent(action);
  }

  public boolean isError() {
    return error.isPresent();
  }

  public T getValue() {
    return value.get();
  }

  public Throwable getError() {
    return error.get();
  }

  public void action(Consumer<Throwable> error, Consumer<T> action){
      if(this.error.isPresent()) error.accept(this.error.get());
      else action.accept(value.get());
  }
}

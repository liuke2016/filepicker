package com.lynn.filepicker;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;


public class RxBus {

  private final PublishSubject<Object> bus = PublishSubject.create();

  private static final RxBus BUS = new RxBus();

  public static RxBus getDefault() {
    return BUS;
  }

  public boolean hasObservers() {
    return bus.hasObservers();
  }

  public void post(Object o) {
    bus.onNext(o);
  }

  public <T> Observable<T> toObservable(Class<T> eventType) {
    return bus.ofType(eventType);
  }
}

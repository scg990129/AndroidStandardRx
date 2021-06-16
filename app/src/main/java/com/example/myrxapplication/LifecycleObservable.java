package com.example.myrxapplication;


import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;

import static androidx.lifecycle.Lifecycle.Event.ON_ANY;

public class LifecycleObservable implements LifecycleObserver, ObservableSource<Lifecycle.Event> {

    protected LifecycleOwner lifecycleOwner;
    protected Observer<? super Lifecycle.Event> observer;
    protected Lifecycle.Event currentEvent = null;//Lifecycle.Event.;

    public LifecycleObservable(@NonNull LifecycleOwner lifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner;
        this.lifecycleOwner.getLifecycle().addObserver(this);
    }

    @OnLifecycleEvent(ON_ANY)
    protected synchronized void onAny(LifecycleOwner source, Lifecycle.Event event) {
        Log.i(LifecycleObservable.class.getSimpleName(), String.format("onAny(%s): %s", this.observer, event));
        if (this.observer != null) {
            this.observer.onNext(this.currentEvent = event);
        }
        if (event.equals(Lifecycle.Event.ON_DESTROY)){
            source.getLifecycle().removeObserver(this);
        }
    }

    @Override
    public synchronized void subscribe(@NonNull Observer<? super Lifecycle.Event> observer) {
        this.observer = observer;
        this.currentEvent = Lifecycle.Event.upTo(this.lifecycleOwner.getLifecycle().getCurrentState());
        Log.i(LifecycleObservable.class.getSimpleName(), String.format("subscribing: currentEvent: %s: %s", this.lifecycleOwner.getLifecycle().getCurrentState(), this.currentEvent));
        if (this.currentEvent != null)
            this.observer.onNext(this.currentEvent);
    }
}

//    LifecycleProvider<Lifecycle.Event> provider
//            = AndroidLifecycle.createLifecycleProvider(this);

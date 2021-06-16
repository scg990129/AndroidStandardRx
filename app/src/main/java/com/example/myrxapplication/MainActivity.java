package com.example.myrxapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;

import android.net.ConnectivityManager;
import android.os.Bundle;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import java.lang.reflect.Type;

public class MainActivity extends AppCompatActivity {

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    protected NetworkHelper.ObservableNetworkCallback networkConnectivityCallback;
    protected ConnectivityManager connectivityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getLifecycle().addObserver(networkConnectivityCallback = new NetworkHelper.ObservableNetworkCallback(connectivityManager = this.getApplicationContext().getSystemService(ConnectivityManager.class)));
    }

    private void test(){
                Type typeMyType = new TypeToken<ArrayList<Object>>() {
        }.getType();
//        io.reactivex.functions.Function<JSValue, List<CMSYuuOffer>> temp = i -> gson.fromJson(i.toString().trim(), typeMyType);
//        this.compositeDisposable.add(
        mPresenter.apply(this.yuuService.key)
                .retryWhen(errors -> errors.filter(error -> error instanceof IOException || error instanceof TimeoutException).wrap(this.networkConnectivityCallback).filter(isConnected -> isConnected))
                .subscribeOn(Schedulers.io())
                .distinctUntilChanged()
                // .takeLast(1)
                // .doAfterNext() // database
                .doOnError(Throwable::printStackTrace)
                .map(jsvalue-> yuuOffer = gson.fromJson(String.valueOf(jsvalue).trim(), CMSYuuOffer.class))
                .observeOn(AndroidSchedulers.mainThread())
                .onTerminateDetach()
//                .doOnTerminate(this.compositeDisposable::dispose)
                .takeUntil(Observable.wrap(new LifecycleObservable(this)).distinctUntilChanged().filter(event -> event == Lifecycle.Event.ON_DESTROY))
                .delaySubscription(Observable.wrap(new LifecycleObservable(this)).filter(event -> event == Lifecycle.Event.ON_RESUME || event == Lifecycle.Event.ON_START))
                .subscribe(new CMSYuuOfferDisposableObserver());
//                .subscribeWith(new CMSYuuOfferDisposableObserver()));
    }

}
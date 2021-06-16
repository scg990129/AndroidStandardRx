package com.example.myrxapplication;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import io.reactivex.ObservableSource;
import io.reactivex.Observer;

public class NetworkHelper {

    public static final NetworkRequest request = new NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            .build();

    @Deprecated
    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = null;
        if (cm != null) {
            info = cm.getActiveNetworkInfo();
        }
        return (info != null && info.isConnected());
    }

    public synchronized static boolean isConnected(@NonNull ConnectivityManager connectivityManager) {
        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        return networkCapabilities != null && isConnected(networkCapabilities);
    }

    public synchronized static boolean isConnected(@NonNull ConnectivityManager connectivityManager, @androidx.annotation.Nullable Network network) {
        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
        return networkCapabilities != null && isConnected(networkCapabilities);
    }

    public synchronized static boolean isConnected(@NonNull NetworkCapabilities networkCapabilities) {
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }


    public static class ObservableNetworkCallback extends ConnectivityManager.NetworkCallback implements ObservableSource<Boolean>, LifecycleObserver {

        protected ConnectivityManager connectivityManager;
        protected Observer<? super Boolean> observer;

        public ObservableNetworkCallback(@NonNull Context applicationContext) {
            super();
            this.connectivityManager = applicationContext.getSystemService(ConnectivityManager.class);
        }

        public ObservableNetworkCallback(@NonNull ConnectivityManager connectivityManager) {
            super();
            this.connectivityManager = connectivityManager;
        }

        @Override
        public synchronized void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities);

            boolean isInternetContactable0 = isConnected(this.connectivityManager, network);
            boolean isInternetContactable1 = isConnected(networkCapabilities);
            boolean isInternetContactable2 = isConnected(this.connectivityManager);

            Log.i(NetworkHelper.class.getSimpleName(), String.format("onCapabilitiesChanged(%s: %b, networkCapabilities: %b): %b", network, isInternetContactable0, isInternetContactable1, isInternetContactable2));
            // lastStatus = isInternetContactable0 || isInternetContactable1 || isInternetContactable2;
            if (this.observer != null)
                this.observer.onNext(isInternetContactable0 || isInternetContactable1 || isInternetContactable2);
        }

        @Override
        public void onAvailable(@NonNull Network network) {
            super.onAvailable(network);

            boolean isInternetContactable2 = isConnected(connectivityManager);
            boolean isInternetContactable1 = isConnected(connectivityManager, network);

            Log.i(NetworkHelper.class.getSimpleName(), String.format("onAvailable(%s: %b): %b", network, isInternetContactable1, isInternetContactable2));
            //  lastStatus = isInternetContactable1 || isInternetContactable2;
            if (this.observer != null)
                this.observer.onNext(isInternetContactable1 || isInternetContactable2);
        }

        @Override
        public void onLost(@NonNull Network network) {
            super.onLost(network);
            boolean isInternetContactable = isConnected(connectivityManager);
            boolean isInternetContactable2 = isConnected(connectivityManager, network);

            Log.i(NetworkHelper.class.getSimpleName(), String.format("onLost(%s: %b): %b", network, isInternetContactable2, isInternetContactable));
//            lastStatus = isInternetContactable || isInternetContactable2;
            if (this.observer != null)
                this.observer.onNext(isInternetContactable || isInternetContactable2);
        }

        @Override
        public void onUnavailable() {
            super.onUnavailable();
            boolean isInternetContactable = isConnected(connectivityManager);

            Log.i(NetworkHelper.class.getSimpleName(), String.format("onUnavailable(): %b", isInternetContactable));
            if (this.observer != null) this.observer.onNext(isInternetContactable);
        }

        @Override
        public synchronized void subscribe(@NonNull Observer<? super Boolean> observer) {
            this.observer = observer;

            boolean lastStatus;
            Log.i(NetworkHelper.class.getSimpleName(), String.format("subscribe: %b", lastStatus = isConnected(connectivityManager)));
            this.observer.onNext(lastStatus);
        }


        @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
        public synchronized void onCreate() {
//            this.connectivityManager.unregisterNetworkCallback(this);
            this.connectivityManager.registerNetworkCallback(request, this);
            Log.i(NetworkHelper.class.getSimpleName(), "OnLifecycleEvent: ON_CREATE");
        }

//        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
//        public synchronized void onPause() {
//            this.connectivityManager.unregisterNetworkCallback(this);
//            Log.i(NetworkHelper.class.getSimpleName(),  "OnLifecycleEvent: ON_PAUSE");
//        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        public synchronized void dispose() {
            this.connectivityManager.unregisterNetworkCallback(this);
            if (this.observer != null) {
                this.observer = null;
            }

            Log.i(NetworkHelper.class.getSimpleName(), "OnLifecycleEvent: ON_DESTROY");
        }

    }
}


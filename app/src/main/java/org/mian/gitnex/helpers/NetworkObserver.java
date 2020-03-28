package org.mian.gitnex.helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

/**
 * Author M M Arif
 */

public class NetworkObserver implements LifecycleObserver {

    private ConnectivityManager mConnectivityMgr;
    private Context mContext;
    private NetworkStateReceiver mNetworkStateReceiver;

    public interface ConnectionStateListener {
        void onAvailable(boolean isAvailable);
    }

    public NetworkObserver(Context context) {
        mContext = context;
        mConnectivityMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        ((AppCompatActivity) mContext).getLifecycle().addObserver(this);
    }


    public void onInternetStateListener(ConnectionStateListener listener) {

        mNetworkStateReceiver = new NetworkStateReceiver(listener);
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        mContext.registerReceiver(mNetworkStateReceiver, intentFilter);

    }


    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {

        ((AppCompatActivity) mContext).getLifecycle().removeObserver(this);

        if (mNetworkStateReceiver != null) {
            mContext.unregisterReceiver(mNetworkStateReceiver);
        }

    }


    public class NetworkStateReceiver extends BroadcastReceiver {

        ConnectionStateListener mListener;

        public NetworkStateReceiver(ConnectionStateListener listener) {
            mListener = listener;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras() != null) {
                NetworkInfo activeNetworkInfo = mConnectivityMgr.getActiveNetworkInfo();

                if (activeNetworkInfo != null && activeNetworkInfo.getState() == NetworkInfo.State.CONNECTED) {

                    mListener.onAvailable(true); // connected

                } else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {

                    mListener.onAvailable(false); // disconnected

                }
            }
        }
    }

}

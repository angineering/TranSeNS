package uk.co.computicake.angela.thesis;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import uk.co.computicake.angela.thesis.MainActivity;


// OBS! eventually get buffer overflows when it runs in the background!!
// Actually. this might no longer be needed. depending on how in-class definition works in background... go with the other thing for now. Contemplate initialising a ConnMgr in BroadcastReceiver to listen for network changes instead. 

public class ConnectivityReceiver extends BroadcastReceiver {
	public ConnectivityReceiver() {
		super();
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
		// ignore intents that have been mistakenly picked up
		//picks up WIFI_STATE_CHANGED (this made it crash previously) and CONNECTIVITY_CHANGE
		// keep getting WIFI_STATE_CHANGED twice, same for CONNECTIVITY_CHANGE. is connected on 2nd CONNECTIVITY_CHANGE.
    	Log.i("onReceive", "picked up "+action);
	}

}

package com.example.timerdemo;


import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class TimerService extends Service {

	// private NotificationManager mNM;

	// Unique Identification Number for the Notification.
	// We use it on Notification start, and to cancel it.
	private int NOTIFICATION = R.string.local_service_started;
	private long mStart = 0;
	private long mTime = 0;

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			long current = System.currentTimeMillis();
			mTime += current - mStart;
			mStart = current;

			updateTime(mTime);

			mHandler.sendEmptyMessageDelayed(0, 250);
		};
	};

	/**
	 * Class for clients to access. Because we know this service always runs in
	 * the same process as its clients, we don't need to deal with IPC.
	 */

	public class LocalBinder extends Binder {

		TimerService getService() {
			return TimerService.this;
		}
	}

	@Override
	public void onCreate() {
		// mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

		// Display a notification about us starting. We put an icon in the
		// status bar.
		// showNotification();
		// updateTime(1000);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("LocalService", "Received start id " + startId + ": " + intent);
       
        
        if (isTimerRunning()) {
            stopTimer();
            return START_STICKY;
        }
        
		mStart = System.currentTimeMillis();
		updateTime(0);
		mHandler.removeMessages(0);
		mHandler.sendEmptyMessage(0);

		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}

	public void stopTimer() {
        mHandler.removeMessages(0);
        stopSelf();
        // Broadcast timer stopped
        Intent intent = new Intent(TimerDemoActivity.ACTION_TIMER_STOPPED);
        intent.putExtra("time", mTime);
        sendBroadcast(intent);
	}

	public boolean isTimerRunning() {
		return mHandler.hasMessages(0);
	}

	public void resetTimer() {
		stopTimer();
		timerStopped(mTime);
		mTime = 0;
	}

	private void timerStopped(long time) {
		// Broadcast timer stopped
		Intent intent = new Intent(TimerDemoActivity.ACTION_TIMER_FINISHED);
		intent.putExtra("time", time);
		sendBroadcast(intent);

		// Stop the notification
		// stopForeground(true);
	}

	@Override
	public void onDestroy() {
		// Cancel the persistent notification.
		// mNM.cancel(NOTIFICATION);

		// Tell the user we stopped.
		Toast.makeText(this, R.string.local_service_stopped, Toast.LENGTH_SHORT)
				.show();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return mIBinder;
	}

	IBinder mIBinder = new LocalBinder();

	private void updateTime(long time) {
		// Broadcast the new time
		Intent intent = new Intent(TimerDemoActivity.ACTION_TIME_UPDATE);
		intent.putExtra("time", time);
		sendBroadcast(intent);

		// Now update the notification
		// updateNotification(time);
	}

}

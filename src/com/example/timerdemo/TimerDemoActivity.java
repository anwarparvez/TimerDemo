package com.example.timerdemo;

import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class TimerDemoActivity extends Activity implements OnClickListener {
	public static final String ACTION_TIME_UPDATE = "com.example.ActionTimeUpdate";
	public static final String ACTION_TIMER_FINISHED = "com.example.ActionTimerFinished";
	public static final String ACTION_TIMER_STOPPED = "com.example.ActionTimerStopped";
	private static final String TAG = "TimeTrackerActivity";

	public static int TIMER_NOTIFICATION = 0;

	private BroadcastReceiver mTimeReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			long time = intent.getLongExtra("time", 0);

			if (ACTION_TIME_UPDATE.equals(action)) {
				TextView counter = (TextView) TimerDemoActivity.this
						.findViewById(R.id.counter);
				counter.setText(DateUtils.formatElapsedTime(time / 1000));
				TextView ssButton = (TextView) TimerDemoActivity.this
						.findViewById(R.id.start_stop);
				ssButton.setText(R.string.stop);
			} else if (ACTION_TIMER_FINISHED.equals(action)) {
				/*
				 * if (mTimeListAdapter != null && time > 0)
				 * mTimeListAdapter.add(time/1000);
				 */
			}
		}
	};

	private void registerTimerReceiver() {
		// Register the TimeReceiver
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_TIME_UPDATE);
		filter.addAction(ACTION_TIMER_FINISHED);
		registerReceiver(mTimeReceiver, filter);
	}

	private void unregisterTimerReceiver() {
		if (mTimeReceiver != null) {
			unregisterReceiver(mTimeReceiver);
		}
	}

	TimerService mBoundTimerService;
	private ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			// Because it is running in our same process, we should never
			// see this happen.
			mBoundTimerService = null;
			Toast.makeText(TimerDemoActivity.this,
					R.string.local_service_disconnected, Toast.LENGTH_SHORT)
					.show();

		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			// This is called when the connection with the service has been
			// established, giving us the service object we can use to
			// interact with the service. Because we have bound to a explicit
			// service that we know is running in our own process, we can
			// cast its IBinder to a concrete class and directly access it.
			mBoundTimerService = ((TimerService.LocalBinder) service)
					.getService();

			// Tell the user about this for our demo.
			Toast.makeText(TimerDemoActivity.this,
					R.string.local_service_connected, Toast.LENGTH_SHORT)
					.show();

		}
	};
	boolean mIsBound = false;

	void doBindService() {
		// Establish a connection with the service. We use an explicit
		// class name because we want a specific service implementation that
		// we know will be running in our own process (and thus won't be
		// supporting component replacement by other applications).
		bindService(new Intent(TimerDemoActivity.this, TimerService.class),
				mServiceConnection, Context.BIND_AUTO_CREATE);
		mIsBound = true;
	}

	void doUnbindService() {
		if (mIsBound) {
			// Detach our existing connection.
			unbindService(mServiceConnection);
			mIsBound = false;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		doUnbindService();
		unregisterTimerReceiver();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timer_demo);

		// Initialize the Timer
		TextView counter = (TextView) findViewById(R.id.counter);
		counter.setText(DateUtils.formatElapsedTime(0));

		Button startButton = (Button) findViewById(R.id.start_stop);
		startButton.setOnClickListener(this);

		Button finishButton = (Button) findViewById(R.id.finish);
		finishButton.setOnClickListener(this);

		registerTimerReceiver();
		doBindService();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.timer_demo, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		TextView ssButton = (TextView) findViewById(R.id.start_stop);

		if (v.getId() == R.id.start_stop) {
			if (mBoundTimerService == null) {
				ssButton.setText(R.string.stop);
				startService(new Intent(this, TimerService.class));
			} else if (!mBoundTimerService.isTimerRunning()) {
				ssButton.setText(R.string.stop);
				mBoundTimerService.startService(new Intent(this,
						TimerService.class));
			} else {
				ssButton.setText(R.string.start);
				mBoundTimerService.stopTimer();
			}
		} else if (v.getId() == R.id.finish) {
			if (mBoundTimerService != null) {
				mBoundTimerService.resetTimer();
			}
			TextView counter = (TextView) findViewById(R.id.counter);
			counter.setText(DateUtils.formatElapsedTime(0));
			ssButton.setText(R.string.start);
		}

	}

}

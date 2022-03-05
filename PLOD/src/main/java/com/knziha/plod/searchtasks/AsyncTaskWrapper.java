package com.knziha.plod.searchtasks;

import android.os.AsyncTask;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AsyncTaskWrapper<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
	protected final AtomicBoolean running = new AtomicBoolean(true);

	public void stop(boolean mayInterruptIfRunning) {
		running.set(false);
		cancel(mayInterruptIfRunning);
	}
}
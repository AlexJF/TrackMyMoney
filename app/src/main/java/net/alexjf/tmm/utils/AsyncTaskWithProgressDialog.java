/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class AsyncTaskWithProgressDialog<Params>
		extends AsyncTask<Params, Void, Bundle> {

	private static Map<Context, ProgressDialogInfo> progressDialogs =
			new HashMap<Context, ProgressDialogInfo>();

	private Context context;

	private String taskId;
	private String progressMessage;
	private Throwable throwable;
	private AsyncTaskResultListener resultListener;
	private boolean running;

	public AsyncTaskWithProgressDialog(String taskId, String progressMessage) {
		this(null, taskId, progressMessage);
	}

	public AsyncTaskWithProgressDialog(Context context, String taskId,
			String progressMessage) {
		this.context = context;
		this.taskId = taskId;
		this.progressMessage = progressMessage;
		running = false;
	}

	@Override
	protected void onPreExecute() {
		running = true;
		showDialog();
	}

	@Override
	protected void onPostExecute(Bundle result) {
		running = false;
		dismissDialog();

		if (throwable == null) {
			resultListener.onAsyncTaskResultSuccess(taskId, result);
		} else {
			resultListener.onAsyncTaskResultFailure(taskId, throwable);
		}
	}

	/**
	 * @param context the context to set
	 */
	public void setContext(Context context) {
		if (context == null) {
			dismissDialog();
			this.context = context;
		} else {
			dismissDialog();
			this.context = context;
			showDialog();
		}
	}

	private void showDialog() {
		if (context == null || !running) {
			return;
		}

		ProgressDialogInfo progInfo = progressDialogs.get(context);

		if (progInfo == null) {
			progInfo = new ProgressDialogInfo();
			progressDialogs.put(context, progInfo);
		}

		progInfo.progressMessages.add(progressMessage);

		if (progInfo.progressDialog == null) {
			progInfo.progressDialog = new ProgressDialog(context);
			progInfo.progressDialog.setIndeterminate(true);
			progInfo.progressDialog.setCancelable(false);
			progInfo.progressDialog.setCanceledOnTouchOutside(false);
			progInfo.progressDialog.setMessage(progressMessage);
			progInfo.progressDialog.show();
		}
	}

	private void dismissDialog() {
		if (context == null) {
			return;
		}

		ProgressDialogInfo progInfo = progressDialogs.get(context);

		progInfo.progressMessages.remove(progressMessage);

		if (progInfo.progressDialog != null) {
			if (progInfo.progressMessages.size() == 0) {
				progInfo.progressDialog.dismiss();
				progressDialogs.remove(context);
			} else {
				progInfo.progressDialog.setTitle(
						progInfo.progressMessages.get(0));
			}
		}
	}

	/**
	 * @param throwable the throwable to set
	 */
	public void setThrowable(Throwable throwable) {
		this.throwable = throwable;
	}

	/**
	 * @param resultListener the resultListener to set
	 */
	public void setResultListener(AsyncTaskResultListener resultListener) {
		this.resultListener = resultListener;
	}

	private static class ProgressDialogInfo {
		public List<String> progressMessages;
		public ProgressDialog progressDialog;

		public ProgressDialogInfo() {
			progressMessages = new LinkedList<String>();
		}
	}

	public interface AsyncTaskResultListener {
		public void onAsyncTaskResultSuccess(String taskId, Bundle resultData);

		public void onAsyncTaskResultCanceled(String taskId);

		public void onAsyncTaskResultFailure(String taskId, Throwable exception);
	}
}

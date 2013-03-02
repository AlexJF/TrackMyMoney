/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public abstract class AsyncTaskWithProgressDialog<Params, Progress, Result> 
    extends AsyncTask<Params, Progress, Result> {
    private ProgressDialog progressDialog;
    private Context context;
    private String progressMessage;
    private Throwable throwable;

    public AsyncTaskWithProgressDialog(Context context, 
            String progressMessage) {
        this.context = context;
        this.progressMessage = progressMessage;
    }

    @Override
    protected void onPreExecute() {
        progressDialog = ProgressDialog.show(context, "",
                progressMessage);
    }

    @Override
    protected void onPostExecute(Result result) {
        progressDialog.dismiss();
        if (throwable == null) {
            onPostExecuteSuccess(result);
        } else {
            onPostExecuteFail(throwable);
        }
    }

    protected void onPostExecuteSuccess(Result result) {
    }

    protected void onPostExecuteFail(Throwable throwable) {
    }

    /**
     * @return the context
     */
    public Context getContext() {
        return context;
    }

    /**
     * @return the progressMessage
     */
    public String getProgressMessage() {
        return progressMessage;
    }

    /**
     * @param throwable the throwable to set
     */
    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
}

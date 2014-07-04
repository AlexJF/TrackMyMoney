/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.preferences;

import net.alexjf.tmm.activities.PreferencesActivity;
import net.alexjf.tmm.activities.PreferencesActivity.OnDestroyListener;
import net.alexjf.tmm.activities.PreferencesActivity.OnRestoreInstanceListener;
import net.alexjf.tmm.activities.PreferencesActivity.OnSaveInstanceListener;
import net.alexjf.tmm.utils.AsyncTaskWithProgressDialog;
import net.alexjf.tmm.utils.AsyncTaskWithProgressDialog.AsyncTaskResultListener;

import android.content.Context;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;

/**
 * @param <P> Describes the type of inputs received by the underlying AsyncTask.
 */
public abstract class DialogWithAsyncTaskProgressPreference <P>
    extends DialogPreference 
    implements AsyncTaskResultListener, OnDestroyListener, 
               OnRestoreInstanceListener, OnSaveInstanceListener {

    private static AsyncTaskWithProgressDialog<?> task;

    private PreferencesActivity activity;

    public DialogWithAsyncTaskProgressPreference(Context context, 
            AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context);
    }

    public DialogWithAsyncTaskProgressPreference(Context context, 
            AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    protected void initialize(Context context) {
        activity = (PreferencesActivity) context;

        activity.registerOnDestroyListener(this);
        activity.registerOnRestoreInstanceListener(this);
        activity.registerOnSaveInstanceListener(this);
    }

    protected abstract AsyncTaskWithProgressDialog<P> createTask();
    protected abstract void runTask(AsyncTaskWithProgressDialog<P> task);

    @Override
    @SuppressWarnings("unchecked")
    protected void onDialogClosed(boolean positiveResult) {
        if (!positiveResult) {
            return;
        }

        if (task != null) {
            return;
        }

        task = createTask();
        task.setContext(activity);
        task.setResultListener(this);
        runTask((AsyncTaskWithProgressDialog<P>) task);
    }

    @Override
    public void onAsyncTaskResultSuccess(String taskId, Bundle resultData) {
        task = null;
    }

    @Override
    public void onAsyncTaskResultCanceled(String taskId) {
        task = null;
    }

    @Override
    public void onAsyncTaskResultFailure(String taskId, Throwable e) {
        task = null;
    }

    @Override
    public void onDestroy() {
        if (task != null) {
            task.setContext(null);
        }
    }

    @Override
    public void onRestoreInstance(Bundle savedInstanceState) {
        if (task != null) {
            task.setContext(activity);
            task.setResultListener(this);
        }
    }

    /**
     * @return the activity
     */
    public PreferencesActivity getActivity() {
        return activity;
    }

    @Override
    public void onSaveInstance(Bundle bundle) {
    }
}

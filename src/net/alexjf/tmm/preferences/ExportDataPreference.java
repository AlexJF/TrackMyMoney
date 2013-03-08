/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.preferences;

import java.io.File;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import net.alexjf.tmm.R;
import net.alexjf.tmm.activities.PreferencesActivity;
import net.alexjf.tmm.importexport.CSVImportExport;
import net.alexjf.tmm.utils.AsyncTaskWithProgressDialog;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.Toast;

public class ExportDataPreference 
    extends DialogWithAsyncTaskProgressPreference<Date>
    implements OnDateSetListener {
    private static final int RES_DIALOGLAYOUT = R.layout.prefdiag_export_data;

    private static final String KEY_CURRENTSTARTDATE = "startDate";
    private static final String KEY_CURRENTENDDATE = "endDate";
    private static final String KEY_CURRENTPICKERDATE = "pickerDate";
    private static final String KEY_STARTDATEEDIT = "startTimeEdit";
    private static final String KEY_FILE = "file";
    private static final String KEY_SENDTO = "sendTo";

    private static final String TASK_EXPORT = "export";

    private Calendar startDate;
    private Calendar endDate;
    private static DateFormat dateTimeFormat = DateFormat.getDateTimeInstance();
    private static DateFormat dateFormat = DateFormat.getDateInstance();
    private boolean startDateBeingEdited;

    private CheckBox shareCheckBox;
    private CheckBox dateIntervalCheckBox;
    private Button startDateButton;
    private Button endDateButton;

    private DatePickerDialog datePicker;

    public ExportDataPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ExportDataPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void initialize(Context context) {
        super.initialize(context);

        setPositiveButtonText(R.string.export_text);
        startDate = Calendar.getInstance();
        endDate = Calendar.getInstance();
    }

    @Override
    protected View onCreateDialogView() {
        LayoutInflater vi = (LayoutInflater) getActivity().
            getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = vi.inflate(RES_DIALOGLAYOUT, null);

        shareCheckBox = (CheckBox) view.findViewById(R.id.share_check);
        dateIntervalCheckBox = (CheckBox) view.findViewById(R.id.dateinterval_check);
        final View dateSelectorLayout = view.findViewById(R.id.dateinterval_dates);
        startDateButton = (Button) view.findViewById(R.id.dateinterval_start);
        endDateButton = (Button) view.findViewById(R.id.dateinterval_end);

        dateIntervalCheckBox.setOnCheckedChangeListener(
            new OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton view, 
                    boolean checked) {
                    if (checked) {
                        dateSelectorLayout.setVisibility(View.VISIBLE);
                    } else {
                        dateSelectorLayout.setVisibility(View.GONE);
                    }
                };
            });

        startDateButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                startDateBeingEdited = true;
                showDatePicker(startDate);
            }
        });

        endDateButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                startDateBeingEdited = false;
                showDatePicker(endDate);
            }
        });

        updateDateButtons();

        return view;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        Calendar calendar;

        if (startDateBeingEdited) {
            calendar = startDate;
        } else {
            calendar = endDate;
        }

        calendar.set(year, month, day);
        datePicker = null;
        updateDateButtons();
    }

    @Override
    protected AsyncTaskWithProgressDialog<Date> createTask() {
        String strLoading = getActivity().getResources().getString(
                R.string.exporting);
        return new AsyncTaskWithProgressDialog<Date> 
        (TASK_EXPORT, strLoading) {
            private boolean sendToOnSuccess = shareCheckBox.isChecked();

            @Override
            protected Bundle doInBackground(Date... args) {
                CSVImportExport exporter = new CSVImportExport();
                DateFormat dateFormat = new SimpleDateFormat("'TMM_'yyyy_MM_dd_HH_mm_ss'.cvs'");
                File extDir = Environment.getExternalStorageDirectory();
                File tmmDir = new File(extDir, "TMM");
                File exportPath = new File(tmmDir, dateFormat.format(new Date()));

                try {
                    tmmDir.mkdirs();
                    if (args.length == 2) {
                        exporter.exportData(exportPath.getPath(), args[0], args[1]);
                    } else {
                        exporter.exportData(exportPath.getPath());
                    }

                    Bundle bundle = new Bundle();
                    bundle.putSerializable(KEY_FILE, exportPath);
                    bundle.putBoolean(KEY_SENDTO, sendToOnSuccess);
                    return bundle;
                } catch (Exception e) {
                    setThrowable(e);
                    return null;
                }
            }
        };
    }

    @Override
    protected void runTask(AsyncTaskWithProgressDialog<Date> task) {
        task.ensureDatabaseOpen(true);
        if (dateIntervalCheckBox.isChecked()) {
            task.execute(startDate.getTime(), endDate.getTime());
        } else {
            task.execute();
        }
    }

    @Override
    public void onAsyncTaskResultSuccess(String taskId, Bundle resultData) {
        super.onAsyncTaskResultSuccess(taskId, resultData);
        PreferencesActivity activity = getActivity();
        if (resultData.getBoolean(KEY_SENDTO)) {
            File file = (File) resultData.getSerializable(KEY_FILE);
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_STREAM, Uri.parse(file.getPath()));
            String title = activity.getResources().getString(
                    R.string.export_share_title);
            activity.startActivity(Intent.createChooser(i, title));
        }
        Toast.makeText(activity, 
            activity.getResources().getString(R.string.export_success),
            3).show();
    }

    @Override
    public void onAsyncTaskResultFailure(String taskId, Throwable e) {
        super.onAsyncTaskResultFailure(taskId, e);
        String strError = getActivity().getResources().getString(
                R.string.error_export);
        Toast.makeText(getActivity(), 
            String.format(strError, e.getMessage()), 3).show();
        Log.e("TMM", e.getMessage(), e);
    }

    private void showDatePicker(Calendar date) {
        int year = date.get(Calendar.YEAR);
        int month = date.get(Calendar.MONTH);
        int day = date.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and show it
        datePicker = new DatePickerDialog(getActivity(), this, year, month, day);
        datePicker.show();
    }

    private void restoreDatePicker(Bundle inState) {
        String curDatePickerDateString = 
            inState.getString(KEY_CURRENTPICKERDATE);

        if (curDatePickerDateString != null) {
            Log.d("TMM", "Restoring picker state");
            Date date = new Date();
            try {
                date = dateFormat.parse(curDatePickerDateString);
            } catch (Exception e) {
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            showDatePicker(calendar);
        }
    }

    private void saveDatePicker(Bundle outState) {
        if (datePicker == null) {
            return;
        }

        Log.d("TMM", "Saving picker state");

        Calendar currentDatePickerDate = Calendar.getInstance();
        DatePicker picker = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            picker = datePicker.getDatePicker();
        } else {
            try {
                Field mDatePickerField = datePicker.getClass().getDeclaredField("mDatePicker");
                mDatePickerField.setAccessible(true);
                picker = (DatePicker) mDatePickerField.get(datePicker);
            } catch (Exception e) {
                Log.e("TMM", e.getMessage(), e);
                return;
            }
        }
        currentDatePickerDate.set(Calendar.DAY_OF_MONTH, picker.getDayOfMonth());
        currentDatePickerDate.set(Calendar.MONTH, picker.getMonth());
        currentDatePickerDate.set(Calendar.YEAR, picker.getYear());

        outState.putString(KEY_CURRENTPICKERDATE, 
                dateFormat.format(currentDatePickerDate.getTime()));
    }

    private void updateDateButtons() {
        String startDateString = dateFormat.format(startDate.getTime());
        String endDateString = dateFormat.format(endDate.getTime());

        startDateButton.setText(startDateString);
        endDateButton.setText(endDateString);
    }

    @Override
    public void onDestroy() {
        if (datePicker != null && datePicker.isShowing()) {
            Log.d("TMM", "Dismissing date picker");
            datePicker.dismiss();
        }

        super.onDestroy();
    }

    @Override
    public void onSaveInstance(Bundle outState) {
        saveDatePicker(outState);

        outState.putString(KEY_CURRENTSTARTDATE, 
                dateTimeFormat.format(startDate.getTime()));
        outState.putString(KEY_CURRENTENDDATE, 
                dateTimeFormat.format(endDate.getTime()));
        outState.putByte(KEY_STARTDATEEDIT,
                (byte) (startDateBeingEdited ? 1 : 0));
    }

    @Override
    public void onRestoreInstance(Bundle savedInstanceState) {
        super.onRestoreInstance(savedInstanceState);

        if (savedInstanceState != null) {
            Log.d("TMM", "Restoring instance state");
            try {
                startDate.setTime(dateTimeFormat.parse(
                    savedInstanceState.getString(KEY_CURRENTSTARTDATE)));
            } catch (ParseException e) {
                Log.e("TMM", "Error parsing saved start date", e);
            }
            try {
                endDate.setTime(dateTimeFormat.parse(
                    savedInstanceState.getString(KEY_CURRENTENDDATE)));
            } catch (ParseException e) {
                Log.e("TMM", "Error parsing saved end date", e);
            }

            startDateBeingEdited = 
                savedInstanceState.getByte(KEY_STARTDATEEDIT) == 1;
            restoreDatePicker(savedInstanceState);
        }
    }
}

package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class ActivityLogActivity extends AppCompatActivity {

    private DataWrapper dataWrapper;
    private ListView listView;
    private ActivityLogAdapter activityLogAdapter;

    @SuppressLint("InlinedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalGUIRoutines.setTheme(this, false, false/*, false*/, false, false, false); // must by called before super.onCreate()
        //GlobalGUIRoutines.setLanguage(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_activity_log);
        setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.ppp_app_name)));

        /*
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            //w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // create our manager instance after the content view is set
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            // enable status bar tint
            tintManager.setStatusBarTintEnabled(true);
            // set a custom tint color for status bar
            switch (ApplicationPreferences.applicationTheme(getApplicationContext(), true)) {
                case "color":
                    tintManager.setStatusBarTintColor(ContextCompat.getColor(getBaseContext(), R.color.primary));
                    break;
                case "white":
                    tintManager.setStatusBarTintColor(ContextCompat.getColor(getBaseContext(), R.color.primaryDark19_white));
                    break;
                default:
                    tintManager.setStatusBarTintColor(ContextCompat.getColor(getBaseContext(), R.color.primary_dark));
                    break;
            }
        }
        */

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_activity_activity_log);
            getSupportActionBar().setElevation(0/*GlobalGUIRoutines.dpToPx(1)*/);
        }

        dataWrapper = new DataWrapper(getApplicationContext(), false, 0, false, DataWrapper.IT_FOR_EDITOR, 0, 0f);

        listView = findViewById(R.id.activity_log_list);

        // Setup cursor adapter using cursor from last step
        Cursor activityLogCursor =  DatabaseHandler.getInstance(getApplicationContext()).getActivityLogCursor();
        if (activityLogCursor != null) {
            activityLogAdapter = new ActivityLogAdapter(getBaseContext(), activityLogCursor);

            // Attach cursor adapter to the ListView
            listView.setAdapter(activityLogAdapter);
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_log, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        /*MenuItem menuItem = menu.findItem(R.id.menu_settingsX);
        menuItem.setTitle(getString(R.string.menu_settings) + "  >");*/
        MenuItem menuItem = menu.findItem(R.id.menu_activity_log_play_pause);

        int theme = GlobalGUIRoutines.getTheme(false, false, /*false,*/ false, false, false, getApplicationContext());
        if (theme != 0) {
            TypedArray a = getTheme().obtainStyledAttributes(theme, new int[]{R.attr.actionActivityLogPauseIcon});
            int attributeResourceId = a.getResourceId(0, 0);
            a.recycle();
            menuItem.setIcon(attributeResourceId);
        }

        if (PPApplication.prefActivityLogEnabled) {
            menuItem.setTitle(R.string.menu_activity_log_pause);
        }
        else {
            menuItem.setTitle(R.string.menu_activity_log_play);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        else
        if (itemId == R.id.menu_activity_log_reload) {
            activityLogAdapter.reload(dataWrapper);
            listView.setSelection(0);
            return true;
        }
        else
        if (itemId == R.id.menu_activity_log_clear) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setTitle(R.string.activity_log_clear_alert_title);
            dialogBuilder.setMessage(R.string.activity_log_clear_alert_message);
            dialogBuilder.setPositiveButton(R.string.alert_button_yes, (dialog, which) -> {
                DatabaseHandler.getInstance(getApplicationContext()).clearActivityLog();
                activityLogAdapter.reload(dataWrapper);
            });
            dialogBuilder.setNegativeButton(R.string.alert_button_no, null);
            AlertDialog dialog = dialogBuilder.create();

//                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                    @Override
//                    public void onShow(DialogInterface dialog) {
//                        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                        if (positive != null) positive.setAllCaps(false);
//                        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                        if (negative != null) negative.setAllCaps(false);
//                    }
//                });

            if (!isFinishing())
                dialog.show();
            return true;
        }
        else
        if (itemId == R.id.menu_activity_log_play_pause) {
            boolean enabled = PPApplication.prefActivityLogEnabled;
            if (enabled)
                PPApplication.addActivityLog(getApplicationContext(), PPApplication.ALTYPE_PAUSED_LOGGING, null, null, "");
            PPApplication.setActivityLogEnabled(getApplicationContext(), !enabled);
            if (!enabled)
                PPApplication.addActivityLog(getApplicationContext(), PPApplication.ALTYPE_STARTED_LOGGING, null, null, "");
            activityLogAdapter.reload(dataWrapper);
            listView.setSelection(0);
            invalidateOptionsMenu();
            return true;
        }
        else
        if (itemId == R.id.menu_activity_log_help) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setTitle(R.string.activity_log_help_title);
            dialogBuilder.setCancelable(true);
            //dialogBuilder.setNegativeButton(android.R.string.cancel, null);

            LayoutInflater inflater = getLayoutInflater();
            @SuppressLint("InflateParams")
            View layout = inflater.inflate(R.layout.dialog_info_preference, null);
            dialogBuilder.setView(layout);

            TextView infoTextView = layout.findViewById(R.id.info_pref_dialog_info_text);

            String message = "";

            message = message + "<b>" + getString(R.string.activity_log_help_message_colors) + ":</b><br>";

            message = message + getString(R.string.activity_log_help_message_colors_profile_activation) + ": ";
            int color = ContextCompat.getColor(this, R.color.altype_profile);
            String colorString = String.format("%X", color).substring(2); // !!strip alpha value!!
            message = message + String.format("<font color=\"#%s\">%s</font>", colorString, "&#x25a0;") + "<br>";

            message = message + getString(R.string.activity_log_help_message_colors_event_start) + ": ";
            color = ContextCompat.getColor(this, R.color.altype_eventStart);
            colorString = String.format("%X", color).substring(2); // !!strip alpha value!!
            message = message + String.format("<font color=\"#%s\">%s</font>", colorString, "&#x25a0;") + "<br>";

            message = message + getString(R.string.activity_log_help_message_colors_event_end) + ": ";
            color = ContextCompat.getColor(this, R.color.altype_eventEnd);
            colorString = String.format("%X", color).substring(2); // !!strip alpha value!!
            message = message + String.format("<font color=\"#%s\">%s</font>", colorString, "&#x25a0;") + "<br>";

            message = message + getString(R.string.activity_log_help_message_colors_restart_events) + ": ";
            color = ContextCompat.getColor(this, R.color.altype_restartEvents);
            colorString = String.format("%X", color).substring(2); // !!strip alpha value!!
            message = message + String.format("<font color=\"#%s\">%s</font>", colorString, "&#x25a0;") + "<br>";

            message = message + getString(R.string.activity_log_help_message_colors_event_delay_start_end) + ": ";
            color = ContextCompat.getColor(this, R.color.altype_eventDelayStartEnd);
            colorString = String.format("%X", color).substring(2); // !!strip alpha value!!
            message = message + String.format("<font color=\"#%s\">%s</font>", colorString, "&#x25a0;") + "<br>";

            message = message + getString(R.string.activity_log_help_message_colors_error) + ": ";
            color = ContextCompat.getColor(this, R.color.altype_error);
            colorString = String.format("%X", color).substring(2); // !!strip alpha value!!
            message = message + String.format("<font color=\"#%s\">%s</font>", colorString, "&#x25a0;") + "<br>";

            message = message + getString(R.string.activity_log_help_message_colors_others) + ": ";
            color = ContextCompat.getColor(this, R.color.altype_other);
            colorString = String.format("%X", color).substring(2); // !!strip alpha value!!
            message = message + String.format("<font color=\"#%s\">%s</font>", colorString, "&#x25a0;") + "<br>";

            message = message + "<br>";
            message = message + "<b>" + getString(R.string.activity_log_help_message) + ":</b><br><br>";

            message = message + "•<b> " + "\"" + getString(R.string.activity_log_header_data_type) + "\"=";
            message = message + "\"" + getString(R.string.altype_mergedProfileActivation) + ": X [Y]\":</b><br>";
            message = message + getString(R.string.activity_log_help_message_mergedProfileActivation);

            message = message + "<br><br>";
            message = message + "•<b> " + "\"" + getString(R.string.activity_log_header_data) + "\" ";
            message = message + getString(R.string.activity_log_help_message_data_for) + " ";
            message = message + "\"" + getString(R.string.activity_log_header_data_type) + "\"=";
            message = message + "\"" + getString(R.string.altype_profileActivation) + "\":</b><br>";
            message = message + getString(R.string.activity_log_help_message_data_profileName) + "<br>";
            message = message + getString(R.string.activity_log_help_message_data_displayedInGUI);

            message = message + "<br><br>";
            message = message + "•<b> " + "\"" + getString(R.string.activity_log_header_data) + "\" ";
            message = message + getString(R.string.activity_log_help_message_data_for) + " ";
            message = message + "\"" + getString(R.string.activity_log_header_data_type) + "\"=";
            message = message + "\"" + getString(R.string.altype_mergedProfileActivation) + "\":</b><br>";
            message = message + getString(R.string.activity_log_help_message_data_profileNameEventName) + "<br>";
            message = message + getString(R.string.activity_log_help_message_data_displayedInGUI);

            message = message + "<br><br>";
            message = message + "•<b> " + "\"" + getString(R.string.activity_log_header_data) + "\" ";
            message = message + getString(R.string.activity_log_help_message_data_for) + " ";
            message = message + "\"" + getString(R.string.activity_log_header_data_type) + "\"=";
            message = message + getString(R.string.activity_log_help_message_data_otherProfileDataTypes) + ":</b><br>";
            message = message + getString(R.string.activity_log_help_message_data_profileName_otherDataTypes);

            message = message + "<br><br>";
            message = message + "•<b> " + "\"" + getString(R.string.activity_log_header_data) + "\" ";
            message = message + getString(R.string.activity_log_help_message_data_for) + " ";
            message = message + "\"" + getString(R.string.activity_log_header_data_type) + "\"=";
            message = message + getString(R.string.activity_log_help_message_data_otherEventDataTypes) + ":</b><br>";
            message = message + getString(R.string.activity_log_help_message_data_eventName_otherDataTypes);

            infoTextView.setText(GlobalGUIRoutines.fromHtml(message, true, false, 0, 0));
            infoTextView.setClickable(true);
            infoTextView.setMovementMethod(LinkMovementMethod.getInstance());

            dialogBuilder.setPositiveButton(R.string.activity_log_help_close, null);
            AlertDialog dialog = dialogBuilder.create();

//                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                    @Override
//                    public void onShow(DialogInterface dialog) {
//                        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                        if (positive != null) positive.setAllCaps(false);
//                        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                        if (negative != null) negative.setAllCaps(false);
//                    }
//                });

            if (!isFinishing())
                dialog.show();
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Cursor cursor = activityLogAdapter.getCursor();
        if (cursor != null)
            cursor.close();
    }

}

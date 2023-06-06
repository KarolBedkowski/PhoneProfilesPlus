package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class ExitApplicationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

//        PPApplicationStatic.logE("[BACKGROUND_ACTIVITY] ExitApplicationActivity.onCreate", "xxx");
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        // set theme and language for dialog alert ;-)
        GlobalGUIRoutines.setTheme(this, true, false, false, false, false, false);
        //GlobalGUIRoutines.setLanguage(this);

        PPAlertDialog dialog = new PPAlertDialog(
                getString(R.string.exit_application_alert_title),
                getString(R.string.exit_application_alert_message),
                getString(R.string.alert_button_yes),
                getString(R.string.alert_button_no),
                null, null,
                (dialog1, which) -> {

                    Context appContext = getApplicationContext();

                    //IgnoreBatteryOptimizationNotification.setShowIgnoreBatteryOptimizationNotificationOnStart(appContext, true);
                    SharedPreferences settings = ApplicationPreferences.getSharedPreferences(appContext);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_NEVER_ASK_FOR_ENABLE_RUN, false);
                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_NEVER_ASK_FOR_GRANT_ROOT, false);
                    editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_NEVER_ASK_FOR_GRANT_G1_PERMISSION, false);
                    editor.apply();
                    ApplicationPreferences.applicationEventNeverAskForEnableRun(appContext);
                    ApplicationPreferences.applicationNeverAskForGrantRoot(appContext);
                    ApplicationPreferences.applicationNeverAskForGrantG1Permission(appContext);

                    DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false, 0, 0, 0f);
                    PPApplicationStatic.exitApp(true, appContext, dataWrapper, this, false, true);

                    // close activities
                    Intent intent = new Intent(PPApplication.ACTION_FINISH_ACTIVITY);
                    intent.putExtra(PPApplication.EXTRA_WHAT_FINISH, "activator");
                    appContext.sendBroadcast(intent);
                    intent = new Intent(PPApplication.ACTION_FINISH_ACTIVITY);
                    intent.putExtra(PPApplication.EXTRA_WHAT_FINISH, "editor");
                    appContext.sendBroadcast(intent);

                    finish();
                },
                (dialogInterface, i) -> finish(),
                null,
                null,
                null,
                true, true,
                false, false,
                true,
                this
        );

        if (!isFinishing())
            dialog.show();
    }

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(0, 0);
    }

}

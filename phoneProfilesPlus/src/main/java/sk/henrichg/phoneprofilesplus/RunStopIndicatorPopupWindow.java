package sk.henrichg.phoneprofilesplus;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;

class RunStopIndicatorPopupWindow extends GuiInfoPopupWindow {

    @SuppressLint("SetTextI18n")
    RunStopIndicatorPopupWindow(final DataWrapper dataWrapper, final Activity activity) {
        super(R.layout.popup_window_run_stop_indicator, R.string.editor_activity_targetHelps_trafficLightIcon_title, activity);

        // Disable default animation
        //setAnimationStyle(0);

        final TextView textView = popupView.findViewById(R.id.run_stop_indicator_popup_window_important_info);
        textView.setText(activity.getString(R.string.popup_window_events_status_show_info) + "\u00A0\u21D2");
        textView.setClickable(true);
        textView.setOnClickListener(v -> {
            Intent intentLaunch = new Intent(activity, ImportantInfoActivityForceScroll.class);
            intentLaunch.putExtra(ImportantInfoActivity.EXTRA_SHOW_QUICK_GUIDE, false);
            //if (activity instanceof ActivatorActivity) {
            //    intentLaunch.putExtra(ImportantInfoActivityForceScroll.EXTRA_SHOW_FRAGMENT, 1);
            //    intentLaunch.putExtra(ImportantInfoActivityForceScroll.EXTRA_SCROLL_TO, R.id.activity_info_profile_activation8);
            //}
            //else {
                //EditorActivity editorProfilesActivity = (EditorActivity) activity;
                //if (editorProfilesActivity.editorSelectedView == 0) {
                //    intentLaunch.putExtra(ImportantInfoActivityForceScroll.EXTRA_SHOW_FRAGMENT, 1);
                //    intentLaunch.putExtra(ImportantInfoActivityForceScroll.EXTRA_SCROLL_TO, R.id.activity_info_profile_activation8);
                //}
                //else {
                    intentLaunch.putExtra(ImportantInfoActivityForceScroll.EXTRA_SHOW_FRAGMENT, 2);
                    intentLaunch.putExtra(ImportantInfoActivityForceScroll.EXTRA_SCROLL_TO, R.id.activity_info_event_activation8);
                //}
            //}
            activity.startActivity(intentLaunch);

            dismiss();
        });

        final SwitchCompat checkBox = popupView.findViewById(R.id.run_stop_indicator_popup_window_checkbox);
        checkBox.setChecked(Event.getGlobalEventsRunning());
        checkBox.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (dataWrapper != null)
                dataWrapper.runStopEventsWithAlert(activity, checkBox, isChecked);
        });
    }

}

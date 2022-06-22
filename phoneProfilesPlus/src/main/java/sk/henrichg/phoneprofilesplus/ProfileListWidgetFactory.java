package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.Comparator;
import java.util.List;

class ProfileListWidgetFactory implements RemoteViewsService.RemoteViewsFactory {

    private DataWrapper dataWrapper;

    private final Context context;
    //private int appWidgetId;
    //private List<Profile> profileList = new ArrayList<>();

    ProfileListWidgetFactory(Context context, @SuppressWarnings("unused") Intent intent) {
        this.context = context;
        /*appWidgetId=intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                                       AppWidgetManager.INVALID_APPWIDGET_ID);*/
    }

    public void onCreate() {
    }

    public void onDestroy() {
        /*if (dataWrapper != null)
            dataWrapper.invalidateDataWrapper();
        dataWrapper = null;*/
    }

    public int getCount() {
        int count = 0;
        if (dataWrapper != null) {
            //if (dataWrapper.profileList != null) {
                for (Profile profile : dataWrapper.profileList) {
                    if (profile._showInActivator)
                        ++count;
                }
            //}
        }
        return count;
    }

    private Profile getItem(int position)
    {
        if (getCount() == 0)
            return null;
        else
        {
            Profile _profile = null;
            if (dataWrapper != null) {
                int pos = -1;
                for (Profile profile : dataWrapper.profileList) {
                    if (profile._showInActivator)
                        ++pos;

                    if (pos == position) {
                        _profile = profile;
                        break;
                    }
                }
            }
            return _profile;
        }
    }

    public RemoteViews getViewAt(int position) {
        RemoteViews row;

        boolean applicationWidgetListGridLayout;
        String applicationWidgetListLightnessT;
        boolean applicationWidgetListHeader;
        boolean applicationWidgetListPrefIndicator;
        boolean applicationWidgetListChangeColorsByNightMode;
        String applicationWidgetListIconColor;
        boolean applicationWidgetListUseDynamicColors;

        synchronized (PPApplication.applicationPreferencesMutex) {
            applicationWidgetListGridLayout = ApplicationPreferences.applicationWidgetListGridLayout;
            applicationWidgetListLightnessT = ApplicationPreferences.applicationWidgetListLightnessT;
            applicationWidgetListHeader = ApplicationPreferences.applicationWidgetListHeader;
            applicationWidgetListPrefIndicator = ApplicationPreferences.applicationWidgetListPrefIndicator;
            applicationWidgetListChangeColorsByNightMode = ApplicationPreferences.applicationWidgetListChangeColorsByNightMode;
            applicationWidgetListIconColor = ApplicationPreferences.applicationWidgetListIconColor;
            applicationWidgetListUseDynamicColors = ApplicationPreferences.applicationWidgetListUseDynamicColors;

            if (Build.VERSION.SDK_INT >= 31) {
                if (//PPApplication.isPixelLauncherDefault(context) ||
                        applicationWidgetListChangeColorsByNightMode) {
                    int nightModeFlags =
                            context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                    switch (nightModeFlags) {
                        case Configuration.UI_MODE_NIGHT_YES:
                            applicationWidgetListLightnessT = "100"; // lightness of text = white
                            break;
                        case Configuration.UI_MODE_NIGHT_NO:
                        case Configuration.UI_MODE_NIGHT_UNDEFINED:
                            applicationWidgetListLightnessT = "0"; // lightness of text = black
                            break;
                    }
                }
            }
        }

        if (!((Build.VERSION.SDK_INT >= 31) && applicationWidgetListChangeColorsByNightMode &&
                applicationWidgetListIconColor.equals("0") && applicationWidgetListUseDynamicColors)) {
            if (!applicationWidgetListGridLayout)
                row = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.profile_list_widget_item);
            else
                row = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.profile_grid_widget_item);
        } else {
            if (!applicationWidgetListGridLayout)
                row = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.profile_list_widget_item_dn);
            else
                row = new RemoteViews(PPApplication.PACKAGE_NAME, R.layout.profile_grid_widget_item_dn);
        }
    
        Profile profile = getItem(position);

        if (profile != null) {
            if (profile.getIsIconResourceID()) {
                if (profile._iconBitmap != null)
                    row.setImageViewBitmap(R.id.widget_profile_list_item_profile_icon, profile._iconBitmap);
                else {
                    row.setImageViewResource(R.id.widget_profile_list_item_profile_icon,
                            /*context.getResources().getIdentifier(profile.getIconIdentifier(), "drawable", context.PPApplication.PACKAGE_NAME)*/
                            Profile.getIconResource(profile.getIconIdentifier()));
                }
            } else {
                row.setImageViewBitmap(R.id.widget_profile_list_item_profile_icon, profile._iconBitmap);
            }
            int red = 0xFF;
            int green;
            int blue;
            switch (applicationWidgetListLightnessT) {
                case "0":
                    red = 0x00;
                    break;
                case "12":
                    red = 0x20;
                    break;
                case "25":
                    red = 0x40;
                    break;
                case "37":
                    red = 0x60;
                    break;
                case "50":
                    red = 0x80;
                    break;
                case "62":
                    red = 0xA0;
                    break;
                case "75":
                    red = 0xC0;
                    break;
                case "87":
                    red = 0xE0;
                    break;
                case "100":
                    //noinspection ConstantConditions
                    red = 0xFF;
                    break;
            }
            green = red;
            blue = red;
            if (!applicationWidgetListHeader) {
                if (profile._checked) {
                    row.setTextViewTextSize(R.id.widget_profile_list_item_profile_name, TypedValue.COMPLEX_UNIT_DIP, 16);

                    if (!((Build.VERSION.SDK_INT >= 31) && applicationWidgetListChangeColorsByNightMode &&
                            applicationWidgetListIconColor.equals("0") && applicationWidgetListUseDynamicColors))
                        row.setTextColor(R.id.widget_profile_list_item_profile_name, Color.argb(0xFF, red, green, blue));
                } else {
                    row.setTextViewTextSize(R.id.widget_profile_list_item_profile_name, TypedValue.COMPLEX_UNIT_DIP, 15);

                    if (!((Build.VERSION.SDK_INT >= 31) && applicationWidgetListChangeColorsByNightMode &&
                            applicationWidgetListIconColor.equals("0") && applicationWidgetListUseDynamicColors))
                        row.setTextColor(R.id.widget_profile_list_item_profile_name, Color.argb(0xCC, red, green, blue));
                }
            } else {
                if (!((Build.VERSION.SDK_INT >= 31) && applicationWidgetListChangeColorsByNightMode &&
                        applicationWidgetListIconColor.equals("0") && applicationWidgetListUseDynamicColors))
                    row.setTextColor(R.id.widget_profile_list_item_profile_name, Color.argb(0xFF, red, green, blue));
            }
            if ((!applicationWidgetListHeader) && (profile._checked)) {
                // hm, interesting, how to set bold style for RemoteView text ;-)
                Spannable profileName = DataWrapper.getProfileNameWithManualIndicator(profile, !applicationWidgetListGridLayout,
                                            "", true, true, applicationWidgetListGridLayout, dataWrapper);
                Spannable sb = new SpannableString(profileName);
                sb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, profileName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                row.setTextViewText(R.id.widget_profile_list_item_profile_name, sb);
            } else {
                Spannable profileName = profile.getProfileNameWithDuration("", "",
                        true/*applicationWidgetListGridLayout*/, applicationWidgetListGridLayout, context.getApplicationContext());
                row.setTextViewText(R.id.widget_profile_list_item_profile_name, profileName);
            }
            if (!applicationWidgetListGridLayout) {
                if (applicationWidgetListPrefIndicator) {
                    if (profile._preferencesIndicator != null)
                        row.setImageViewBitmap(R.id.widget_profile_list_profile_pref_indicator, profile._preferencesIndicator);
                    else
                        row.setImageViewResource(R.id.widget_profile_list_profile_pref_indicator, R.drawable.ic_empty);
                    row.setViewVisibility(R.id.widget_profile_list_profile_pref_indicator, View.VISIBLE);
                }
                else
                    //row.setImageViewResource(R.id.widget_profile_list_profile_pref_indicator, R.drawable.ic_empty);
                    row.setViewVisibility(R.id.widget_profile_list_profile_pref_indicator, View.GONE);
            }

            Intent i = new Intent();
            Bundle extras = new Bundle();

            if ((!applicationWidgetListHeader) &&
                Event.getGlobalEventsRunning() && (position == 0))
                extras.putLong(PPApplication.EXTRA_PROFILE_ID, Profile.RESTART_EVENTS_PROFILE_ID);
            else
                extras.putLong(PPApplication.EXTRA_PROFILE_ID, profile._id);
            extras.putInt(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_SHORTCUT);
            i.putExtras(extras);
            row.setOnClickFillInIntent(R.id.widget_profile_list_item, i);

        }

        return(row);
    }

    public RemoteViews getLoadingView() {
        return(null);
    }
  
    public int getViewTypeCount() {
        return 1;
    }

    public long getItemId(int position) {
        return position;
    }

    public boolean hasStableIds() {
        return false;
    }

    private DataWrapper createProfilesDataWrapper(boolean local,
                                                  String applicationWidgetListIconLightness,
                                                  String applicationWidgetListIconColor,
                                                  boolean applicationWidgetListCustomIconLightness,
                                                  String applicationWidgetListPrefIndicatorLightness,
                                                  boolean applicationWidgetListChangeColorsByNightMode,
                                                  boolean applicationWidgetListUseDynamicColors)
    {
        int monochromeValue = 0xFF;
        switch (applicationWidgetListIconLightness) {
            case "0":
                monochromeValue = 0x00;
                break;
            case "12":
                monochromeValue = 0x20;
                break;
            case "25":
                monochromeValue = 0x40;
                break;
            case "37":
                monochromeValue = 0x60;
                break;
            case "50":
                monochromeValue = 0x80;
                break;
            case "62":
                monochromeValue = 0xA0;
                break;
            case "75":
                monochromeValue = 0xC0;
                break;
            case "87":
                monochromeValue = 0xE0;
                break;
            case "100":
                //noinspection ConstantConditions
                monochromeValue = 0xFF;
                break;
        }

        float prefIndicatorLightnessValue = 0f;
        int prefIndicatorMonochromeValue = 0x00;
        switch (applicationWidgetListPrefIndicatorLightness) {
            case "0":
                prefIndicatorLightnessValue = -128f;
                //noinspection ConstantConditions
                prefIndicatorMonochromeValue = 0x00;
                break;
            case "12":
                prefIndicatorLightnessValue = -96f;
                prefIndicatorMonochromeValue = 0x20;
                break;
            case "25":
                prefIndicatorLightnessValue = -64f;
                prefIndicatorMonochromeValue = 0x40;
                break;
            case "37":
                prefIndicatorLightnessValue = -32f;
                prefIndicatorMonochromeValue = 0x60;
                break;
            case "50":
                prefIndicatorLightnessValue = 0f;
                prefIndicatorMonochromeValue = 0x80;
                break;
            case "62":
                prefIndicatorLightnessValue = 32f;
                prefIndicatorMonochromeValue = 0xA0;
                break;
            case "75":
                prefIndicatorLightnessValue = 64f;
                prefIndicatorMonochromeValue = 0xC0;
                break;
            case "87":
                prefIndicatorLightnessValue = 96f;
                prefIndicatorMonochromeValue = 0xE0;
                break;
            case "100":
                prefIndicatorLightnessValue = 128f;
                prefIndicatorMonochromeValue = 0xFF;
                break;
        }

        int indicatorType = DataWrapper.IT_FOR_WIDGET;
        if ((Build.VERSION.SDK_INT >= 31) && applicationWidgetListChangeColorsByNightMode &&
                applicationWidgetListIconColor.equals("0") && applicationWidgetListUseDynamicColors)
            indicatorType = DataWrapper.IT_FOR_WIDGET_MONOCHROME_INDICATORS;

        if (local) {
            return new DataWrapper(context.getApplicationContext(), applicationWidgetListIconColor.equals("1"),
                    monochromeValue, applicationWidgetListCustomIconLightness,
                    indicatorType, prefIndicatorMonochromeValue, prefIndicatorLightnessValue);
        }
        else {
            if (dataWrapper == null) {
                dataWrapper = new DataWrapper(context.getApplicationContext(), applicationWidgetListIconColor.equals("1"),
                        monochromeValue, applicationWidgetListCustomIconLightness,
                        indicatorType, prefIndicatorMonochromeValue, prefIndicatorLightnessValue);
            } else {
                dataWrapper.setParameters(applicationWidgetListIconColor.equals("1"),
                        monochromeValue, applicationWidgetListCustomIconLightness,
                        indicatorType, prefIndicatorMonochromeValue, prefIndicatorLightnessValue);
            }
            return dataWrapper;
        }
    }

    /*
        Called when notifyDataSetChanged() is triggered on the remote adapter. This allows a RemoteViewsFactory to
        respond to data changes by updating any internal references.

        Note: expensive tasks can be safely performed synchronously within this method. In the interim,
        the old data will be displayed within the widget.
    */
    public void onDataSetChanged() {
        String applicationWidgetListIconColor;
        String applicationWidgetListIconLightness;
        boolean applicationWidgetListCustomIconLightness;
        boolean applicationWidgetListPrefIndicator;
        String applicationWidgetListPrefIndicatorLightness;
        boolean applicationWidgetListHeader;
        boolean applicationWidgetListChangeColorsByNightMode;
        boolean applicationWidgetListUseDynamicColors;
        synchronized (PPApplication.applicationPreferencesMutex) {
            applicationWidgetListIconLightness = ApplicationPreferences.applicationWidgetListIconLightness;
            applicationWidgetListIconColor = ApplicationPreferences.applicationWidgetListIconColor;
            applicationWidgetListCustomIconLightness = ApplicationPreferences.applicationWidgetListCustomIconLightness;
            applicationWidgetListPrefIndicator = ApplicationPreferences.applicationWidgetListPrefIndicator;
            applicationWidgetListPrefIndicatorLightness = ApplicationPreferences.applicationWidgetListPrefIndicatorLightness;
            applicationWidgetListHeader = ApplicationPreferences.applicationWidgetListHeader;
            applicationWidgetListChangeColorsByNightMode = ApplicationPreferences.applicationWidgetListChangeColorsByNightMode;
            applicationWidgetListUseDynamicColors = ApplicationPreferences.applicationWidgetListUseDynamicColors;

            if (Build.VERSION.SDK_INT >= 31) {
                if (applicationWidgetListChangeColorsByNightMode) {
                    int nightModeFlags =
                            context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                    switch (nightModeFlags) {
                        case Configuration.UI_MODE_NIGHT_YES:
                            //applicationWidgetListIconColor = "0"; // icon type = colorful
                            applicationWidgetListIconLightness = "75";
                            applicationWidgetListPrefIndicatorLightness = "62"; // lightness of preference indicators
                            break;
                        case Configuration.UI_MODE_NIGHT_NO:
                        case Configuration.UI_MODE_NIGHT_UNDEFINED:
                            //applicationWidgetListIconColor = "0"; // icon type = colorful
                            applicationWidgetListIconLightness = "62";
                            applicationWidgetListPrefIndicatorLightness = "50"; // lightness of preference indicators
                            break;
                    }
                }
            }
        }

        DataWrapper _dataWrapper = createProfilesDataWrapper(true,
                                                applicationWidgetListIconLightness,
                                                applicationWidgetListIconColor,
                                                applicationWidgetListCustomIconLightness,
                                                applicationWidgetListPrefIndicatorLightness,
                                                applicationWidgetListChangeColorsByNightMode,
                                                applicationWidgetListUseDynamicColors);

        List<Profile> newProfileList = _dataWrapper.getNewProfileList(true,
                                                        applicationWidgetListPrefIndicator);
        _dataWrapper.getEventTimelineList(true);

        if (!applicationWidgetListHeader)
        {
            // show activated profile in list if is not showed in activator
            Profile profile = _dataWrapper.getActivatedProfile(newProfileList);
            if ((profile != null) && (!profile._showInActivator))
            {
                profile._showInActivator = true;
                profile._porder = -1;
            }
        }
        newProfileList.sort(new ProfileComparator());

        Profile restartEvents = null;
        if ((!applicationWidgetListHeader) &&
                Event.getGlobalEventsRunning()) {
            //restartEvents = DataWrapper.getNonInitializedProfile(context.getString(R.string.menu_restart_events), "ic_profile_restart_events|1|0|0", 0);
            restartEvents = DataWrapper.getNonInitializedProfile(context.getString(R.string.menu_restart_events),
                    "ic_profile_restart_events|1|1|"+ApplicationPreferences.applicationRestartEventsIconColor, 0);
            restartEvents._showInActivator = true;
            newProfileList.add(0, restartEvents);
        }

        createProfilesDataWrapper(false,
                                    applicationWidgetListIconLightness,
                                    applicationWidgetListIconColor,
                                    applicationWidgetListCustomIconLightness,
                                    applicationWidgetListPrefIndicatorLightness,
                                    applicationWidgetListChangeColorsByNightMode,
                                    applicationWidgetListUseDynamicColors);
        //if (dataWrapper != null) {
            //dataWrapper.invalidateProfileList();
            if (restartEvents != null)
                dataWrapper.generateProfileIcon(restartEvents, true, false);
            dataWrapper.setProfileList(newProfileList);
            //profileList = newProfileList;
        //}
    }

    private static class ProfileComparator implements Comparator<Profile> {

        public int compare(Profile lhs, Profile rhs) {
            int res = 0;
            if ((lhs != null) && (rhs != null))
                res = lhs._porder - rhs._porder;
            return res;
        }
    }

}

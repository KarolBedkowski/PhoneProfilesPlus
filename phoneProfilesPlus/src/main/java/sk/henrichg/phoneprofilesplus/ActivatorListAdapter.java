package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.Spannable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

class ActivatorListAdapter extends BaseAdapter
{
    private ActivatorListFragment fragment;
    private final DataWrapper activityDataWrapper;

    //public boolean targetHelpsSequenceStarted;
    static final String PREF_START_TARGET_HELPS = "activate_profile_list_adapter_start_target_helps";
    static final String PREF_START_TARGET_HELPS_FINISHED = "activate_profile_list_adapter_start_target_helps_finished";

    ActivatorListAdapter(ActivatorListFragment f, /*List<Profile> pl, */DataWrapper dataWrapper)
    {
        fragment = f;
        this.activityDataWrapper = dataWrapper;
    }

    public void release()
    {
        fragment = null;
    }

    public int getCount()
    {
        synchronized (activityDataWrapper.profileList) {
            boolean someData = activityDataWrapper.profileListFilled &&
                    (activityDataWrapper.profileList.size() > 0);
            fragment.textViewNoData.setVisibility(someData ? View.GONE : View.VISIBLE);
            /*if (fragment.gridViewDivider != null)
                fragment.gridViewDivider.setBackgroundResource(
                        GlobalGUIRoutines.getThemeActivatorGridDividerColor(someData, fragment.getActivity()));*/

            int count = 0;
            if (activityDataWrapper.profileListFilled) {
                for (Profile profile : activityDataWrapper.profileList) {
                    if (profile._showInActivator)
                        ++count;
                }
            }
            return count;
        }
    }

    public Object getItem(int position)
    {
        if (getCount() == 0)
            return null;
        else
        {
            synchronized (activityDataWrapper.profileList) {
                Profile _profile = null;

                int pos = -1;
                for (Profile profile : activityDataWrapper.profileList) {
                    if (profile._showInActivator)
                        ++pos;

                    if (pos == position) {
                        _profile = profile;
                        break;
                    }
                }

                return _profile;
            }
        }
    }

    public long getItemId(int position)
    {
        return position;
    }

    /*
    public int getItemId(Profile profile)
    {
        for (int i = 0; i < profileList.size(); i++)
        {
            if (profileList.get(i)._id == profile._id)
                return i;
        }
        return -1;
    }
    */
    /*
    int getItemPosition(Profile profile)
    {
        if (profile == null)
            return -1;

        if (!activityDataWrapper.profileListFilled)
            return -1;

        int pos = -1;

        for (int i = 0; i < activityDataWrapper.profileList.size(); i++)
        {
            ++pos;
            if (activityDataWrapper.profileList.get(i)._id == profile._id)
                return pos;
        }
        return -1;
    }
    */
    public Profile getActivatedProfile()
    {
        synchronized (activityDataWrapper.profileList) {
            for (Profile p : activityDataWrapper.profileList) {
                if (p._checked) {
                    return p;
                }
            }

            return null;
        }
    }

    public void notifyDataSetChanged(boolean refreshIcons) {
        if (refreshIcons) {
            synchronized (activityDataWrapper.profileList) {
                for (Profile profile : activityDataWrapper.profileList) {
                    activityDataWrapper.refreshProfileIcon(profile, true,
                            //ApplicationPreferences.applicationActivatorPrefIndicator(activityDataWrapper.context));
                            ApplicationPreferences.applicationEditorPrefIndicator);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class ViewHolder {
          //ViewGroup listItemRoot;
          ImageView profileIcon;
          TextView profileName;
          ImageView profileIndicator;
          //int position;
        }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;

        View vi = convertView;

        boolean applicationActivatorGridLayout = ApplicationPreferences.applicationActivatorGridLayout;

        if (convertView == null)
        {
            holder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(fragment.getActivity());
            if (!applicationActivatorGridLayout)
            {
                //boolean applicationActivatorPrefIndicator = ApplicationPreferences.applicationActivatorPrefIndicator;
                boolean applicationActivatorPrefIndicator = ApplicationPreferences.applicationEditorPrefIndicator;
                if (applicationActivatorPrefIndicator)
                    vi = inflater.inflate(R.layout.activator_list_item, parent, false);
                else
                    vi = inflater.inflate(R.layout.activator_list_item_no_indicator, parent, false);
                //holder.listItemRoot = vi.findViewById(R.id.act_prof_list_item_root);
                holder.profileName = vi.findViewById(R.id.act_prof_list_item_profile_name);
                holder.profileIcon = vi.findViewById(R.id.act_prof_list_item_profile_icon);
                if (applicationActivatorPrefIndicator)
                    holder.profileIndicator = vi.findViewById(R.id.act_prof_list_profile_pref_indicator);
            }
            else
            {
                vi = inflater.inflate(R.layout.activator_grid_item, parent, false);
                //holder.listItemRoot = vi.findViewById(R.id.act_prof_list_item_root);
                holder.profileName = vi.findViewById(R.id.act_prof_list_item_profile_name);
                holder.profileIcon = vi.findViewById(R.id.act_prof_list_item_profile_icon);
            }
            vi.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)vi.getTag();
        }

        final Profile profile = (Profile)getItem(position);

        if ((applicationActivatorGridLayout) &&
                (profile._porder == ActivatorListFragment.PORDER_FOR_EMPTY_SPACE)) {
            holder.profileName.setText(R.string.empty_string);
            holder.profileIcon.setImageResource(R.drawable.ic_empty);
        }
        else {
            //boolean applicationActivatorHeader = ApplicationPreferences.applicationActivatorHeader(fragment.getActivity());
            /*if (profile._checked && (!applicationActivatorHeader)) {
                holder.profileName.setTypeface(//Typeface.create("sans-serif-condensed", Typeface.BOLD)
                    null, Typeface.BOLD);
                if (applicationActivatorGridLayout)
                    holder.profileName.setTextSize(14);
                else
                    holder.profileName.setTextSize(16);
                holder.profileName.setTextColor(GlobalGUIRoutines.getThemeAccentColor(fragment.getActivity()));
            } else*/ {
                holder.profileName.setTypeface(null, Typeface.NORMAL);
                if (applicationActivatorGridLayout)
                    holder.profileName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
                else
                    holder.profileName.setTextSize(15);
                //noinspection ConstantConditions
                holder.profileName.setTextColor(GlobalGUIRoutines.getThemeWhiteTextColor(fragment.getActivity()));
            }

            Spannable profileName = DataWrapper.getProfileNameWithManualIndicator(profile,
                    false, "", true, false, applicationActivatorGridLayout,
                    activityDataWrapper);
            holder.profileName.setText(profileName);


            if (profile.getIsIconResourceID()) {
                Bitmap bitmap = profile.increaseProfileIconBrightnessForActivity(fragment.getActivity(), profile._iconBitmap);
                //double luminance = ColorUtils.calculateLuminance(iconColor);
                //Log.e("ActivatorListAdapter.getView", "profile="+profile._name+" - luminance="+luminance);
                if (bitmap != null)
                    holder.profileIcon.setImageBitmap(bitmap);
                else {
                    if (profile._iconBitmap != null)
                        holder.profileIcon.setImageBitmap(profile._iconBitmap);
                    else {
                        //holder.profileIcon.setImageBitmap(null);
                        //int res = vi.getResources().getIdentifier(profile.getIconIdentifier(), "drawable",
                        //        vi.getContext().PPApplication.PACKAGE_NAME);
                        int res = Profile.getIconResource(profile.getIconIdentifier());
                        holder.profileIcon.setImageResource(res); // icon resource
                    }
                }
            } else {
                Bitmap bitmap = profile.increaseProfileIconBrightnessForActivity(fragment.getActivity(), profile._iconBitmap);
                if (bitmap != null)
                    holder.profileIcon.setImageBitmap(bitmap);
                else
                    holder.profileIcon.setImageBitmap(profile._iconBitmap);
            }

            if (ApplicationPreferences.applicationActivatorAddRestartEventsIntoProfileList
                    && (position == 0)) {
                if (ApplicationPreferences.applicationEditorPrefIndicator && (!applicationActivatorGridLayout)) {
                    holder.profileIndicator.setVisibility(View.GONE);
                }
            }
            else
            if (holder.profileIndicator != null) {
                holder.profileIndicator.setVisibility(View.VISIBLE);
                //if ((ApplicationPreferences.applicationActivatorPrefIndicator(fragment.getActivity())) && (!applicationActivatorGridLayout)) {
                if (ApplicationPreferences.applicationEditorPrefIndicator && (!applicationActivatorGridLayout)) {
                    if (profile._preferencesIndicator != null) {
                        //profilePrefIndicatorImageView.setImageBitmap(null);
                        //Bitmap bitmap = ProfilePreferencesIndicator.paint(profile, vi.getContext());
                        //profilePrefIndicatorImageView.setImageBitmap(bitmap);
                        holder.profileIndicator.setImageBitmap(profile._preferencesIndicator);
                    } else
                        holder.profileIndicator.setImageResource(R.drawable.ic_empty);
                }
            }
        }

        return vi;
    }

    void showTargetHelps(final Activity activity, /*final ActivatorListFragment fragment,*/ final View listItemView) {
        /*if (Build.VERSION.SDK_INT <= 19)
            // TapTarget.forToolbarMenuItem FC :-(
            // Toolbar.findViewById() returns null
            return;*/

        if (ActivatorTargetHelpsActivity.activity == null)
            return;

        //if (fragment.targetHelpsSequenceStarted)
        //    return;

        boolean startTargetHelpsFinished = ApplicationPreferences.prefActivatorActivityStartTargetHelpsFinished &&
                                            ApplicationPreferences.prefActivatorFragmentStartTargetHelpsFinished;
        if (!startTargetHelpsFinished) {
            final Handler handler = new Handler(activity.getMainLooper());
            handler.postDelayed(() -> {
//                    PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=ActivatorListAdapter.showTargetHelps (3)");

                if (ActivatorTargetHelpsActivity.activity != null) {
                    //Log.d("ActivatorListAdapter.showTargetHelps", "finish activity");
                    try {
                        ActivatorTargetHelpsActivity.activity.finish();
                    } catch (Exception e) {
                        PPApplication.recordException(e);
                    }
                    ActivatorTargetHelpsActivity.activity = null;
                    //ActivatorTargetHelpsActivity.activatorActivity = null;
                }
            }, 500);

            return;
        }

        if (ApplicationPreferences.prefActivatorAdapterStartTargetHelps) {

            //Log.e("ActivatorListAdapter.showTargetHelps", "PREF_START_TARGET_HELPS=true");

            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(activityDataWrapper.context);
            editor.putBoolean(PREF_START_TARGET_HELPS, false);
            editor.apply();
            ApplicationPreferences.prefActivatorAdapterStartTargetHelps = false;

            Rect profileItemTarget = new Rect(0, 0, listItemView.getHeight(), listItemView.getHeight());
            int[] screenLocation = new int[2];
            listItemView.getLocationOnScreen(screenLocation);
            //listItemView.getLocationInWindow(screenLocation);
            if (ApplicationPreferences.applicationActivatorGridLayout)
                profileItemTarget.offset(screenLocation[0] + listItemView.getWidth() / 2 - listItemView.getHeight() / 2, screenLocation[1]);
            else
                profileItemTarget.offset(screenLocation[0] + 100, screenLocation[1]);

            final TapTargetSequence sequence = new TapTargetSequence(ActivatorTargetHelpsActivity.activity);

            //String appTheme = ApplicationPreferences.applicationTheme(activity, true);
            int outerCircleColor = R.color.tabTargetHelpOuterCircleColor;
//                if (appTheme.equals("dark"))
//                    outerCircleColor = R.color.tabTargetHelpOuterCircleColor_dark;
            int targetCircleColor = R.color.tabTargetHelpTargetCircleColor;
//                if (appTheme.equals("dark"))
//                    targetCircleColor = R.color.tabTargetHelpTargetCircleColor_dark;
            int textColor = R.color.tabTargetHelpTextColor;
//                if (appTheme.equals("dark"))
//                    textColor = R.color.tabTargetHelpTextColor_dark;
            //boolean tintTarget = !appTheme.equals("white");

            sequence.targets(
                    TapTarget.forBounds(profileItemTarget, activity.getString(R.string.activator_activity_targetHelps_activateProfile_title), activity.getString(R.string.activator_activity_targetHelps_activateProfile_description))
                            .transparentTarget(true)
                            .outerCircleColor(outerCircleColor)
                            .targetCircleColor(targetCircleColor)
                            .textColor(textColor)
                            .textTypeface(Typeface.DEFAULT_BOLD)
                            .tintTarget(true)
                            .drawShadow(true)
                            .id(1)
            );
            sequence.listener(new TapTargetSequence.Listener() {
                // This listener will tell us when interesting(tm) events happen in regards
                // to the sequence
                @Override
                public void onSequenceFinish() {
                    //targetHelpsSequenceStarted = false;

                    SharedPreferences.Editor editor = ApplicationPreferences.getEditor(activity.getApplicationContext());
                    editor.putBoolean(ActivatorListFragment.PREF_START_TARGET_HELPS_FINISHED, true);
                    editor.putBoolean(ActivatorListAdapter.PREF_START_TARGET_HELPS_FINISHED, true);
                    editor.apply();
                    ApplicationPreferences.prefActivatorFragmentStartTargetHelpsFinished = true;
                    ApplicationPreferences.prefActivatorAdapterStartTargetHelpsFinished = true;

                    final Handler handler = new Handler(activity.getMainLooper());
                    handler.postDelayed(() -> {
//                            PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=ActivatorListAdapter.showTargetHelps (1)");

                        if (ActivatorTargetHelpsActivity.activity != null) {
                            //Log.d("ActivatorListAdapter.showTargetHelps", "finish activity");
                            try {
                                ActivatorTargetHelpsActivity.activity.finish();
                            } catch (Exception e) {
                                PPApplication.recordException(e);
                            }
                            ActivatorTargetHelpsActivity.activity = null;
                            //ActivatorTargetHelpsActivity.activatorActivity = null;
                        }
                    }, 500);
                }

                @Override
                public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                    //Log.d("TapTargetView", "Clicked on " + lastTarget.id());
                }

                @Override
                public void onSequenceCanceled(TapTarget lastTarget) {
                    //targetHelpsSequenceStarted = false;

                    SharedPreferences.Editor editor = ApplicationPreferences.getEditor(activity.getApplicationContext());
                    editor.putBoolean(ActivatorActivity.PREF_START_TARGET_HELPS, false);
                    editor.putBoolean(ActivatorListFragment.PREF_START_TARGET_HELPS, false);
                    editor.putBoolean(ActivatorListAdapter.PREF_START_TARGET_HELPS, false);

                    editor.putBoolean(ActivatorActivity.PREF_START_TARGET_HELPS_FINISHED, true);
                    editor.putBoolean(ActivatorListFragment.PREF_START_TARGET_HELPS_FINISHED, true);
                    editor.putBoolean(ActivatorListAdapter.PREF_START_TARGET_HELPS_FINISHED, true);

                    editor.apply();

                    ApplicationPreferences.prefActivatorActivityStartTargetHelps = false;
                    ApplicationPreferences.prefActivatorFragmentStartTargetHelps = false;
                    ApplicationPreferences.prefActivatorAdapterStartTargetHelps = false;

                    ApplicationPreferences.prefActivatorActivityStartTargetHelpsFinished = true;
                    ApplicationPreferences.prefActivatorFragmentStartTargetHelpsFinished = true;
                    ApplicationPreferences.prefActivatorAdapterStartTargetHelpsFinished = true;

                    final Handler handler = new Handler(activity.getMainLooper());
                    handler.postDelayed(() -> {
//                            PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=ActivatorListAdapter.showTargetHelps (2)");

                        if (ActivatorTargetHelpsActivity.activity != null) {
                            //Log.d("ActivatorListAdapter.showTargetHelps", "finish activity");
                            try {
                                ActivatorTargetHelpsActivity.activity.finish();
                            } catch (Exception e) {
                                PPApplication.recordException(e);
                            }
                            ActivatorTargetHelpsActivity.activity = null;
                            //ActivatorTargetHelpsActivity.activatorActivity = null;
                        }
                    }, 500);
                }
            });
            sequence.continueOnCancel(true)
                    .considerOuterCircleCanceled(true);
            //targetHelpsSequenceStarted = true;

            editor = ApplicationPreferences.getEditor(activity.getApplicationContext());
            editor.putBoolean(ActivatorListAdapter.PREF_START_TARGET_HELPS_FINISHED, false);
            editor.apply();
            ApplicationPreferences.prefActivatorAdapterStartTargetHelpsFinished = false;

            sequence.start();
        }
        else {
            final Handler handler = new Handler(activity.getMainLooper());
            handler.postDelayed(() -> {
//                    PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=ActivatorListAdapter.showTargetHelps (3)");

                if (ActivatorTargetHelpsActivity.activity != null) {
                    //Log.d("ActivatorListAdapter.showTargetHelps", "finish activity");
                    try {
                        ActivatorTargetHelpsActivity.activity.finish();
                    } catch (Exception e) {
                        PPApplication.recordException(e);
                    }
                    ActivatorTargetHelpsActivity.activity = null;
                    //ActivatorTargetHelpsActivity.activatorActivity = null;
                }
            }, 500);
        }
    }

}

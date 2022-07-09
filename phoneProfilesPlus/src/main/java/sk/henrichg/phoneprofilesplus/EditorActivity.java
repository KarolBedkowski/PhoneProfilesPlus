package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
//import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import me.ibrahimsn.lib.SmoothBottomBar;
import sk.henrichg.phoneprofiles.PPIntentForExport;
import sk.henrichg.phoneprofiles.PPProfileForExport;
import sk.henrichg.phoneprofiles.PPShortcutForExport;
import sk.henrichg.phoneprofilesplus.EditorEventListFragment.OnStartEventPreferences;
import sk.henrichg.phoneprofilesplus.EditorProfileListFragment.OnStartProfilePreferences;

public class EditorActivity extends AppCompatActivity
                                    implements OnStartProfilePreferences,
                                               OnStartEventPreferences
{

    //private static volatile EditorActivity instance;
    private boolean activityStarted = false;

    private ImageView eventsRunStopIndicator;

    private static boolean savedInstanceStateChanged;

    private static ApplicationsCache applicationsCache;

    @SuppressWarnings("rawtypes")
    private AsyncTask importAsyncTask = null;
    @SuppressWarnings("rawtypes")
    private AsyncTask importFromPPAsyncTask = null;
    @SuppressWarnings("rawtypes")
    private AsyncTask exportAsyncTask = null;
    @SuppressWarnings("rawtypes")
    private AsyncTask backupAsyncTask = null;
    @SuppressWarnings("rawtypes")
    private AsyncTask restoreAsyncTask = null;

    static boolean doImport = false;
    private AlertDialog importProgressDialog = null;
    private AlertDialog exportProgressDialog = null;
    private AlertDialog backupProgressDialog = null;
    private AlertDialog restoreProgressDialog = null;

    static boolean importFromPPStopped = false;

    private static final int DSI_PROFILES_ALL = 0;
    private static final int DSI_PROFILES_SHOW_IN_ACTIVATOR = 1;
    private static final int DSI_PROFILES_NO_SHOW_IN_ACTIVATOR = 2;
    private static final int DSI_EVENTS_START_ORDER = 0;
    private static final int DSI_EVENTS_ALL = 1;
    private static final int DSI_EVENTS_NOT_STOPPED = 2;
    private static final int DSI_EVENTS_RUNNING = 3;
    private static final int DSI_EVENTS_PAUSED = 4;
    private static final int DSI_EVENTS_STOPPED = 5;

    // request code for startActivityForResult with intent BackgroundActivateProfileActivity
    static final int REQUEST_CODE_ACTIVATE_PROFILE = 6220;
    // request code for startActivityForResult with intent ProfilesPrefsActivity
    private static final int REQUEST_CODE_PROFILE_PREFERENCES = 6221;
    // request code for startActivityForResult with intent EventPrefsActivity
    private static final int REQUEST_CODE_EVENT_PREFERENCES = 6222;
    // request code for startActivityForResult with intent PhoneProfilesActivity
    private static final int REQUEST_CODE_APPLICATION_PREFERENCES = 6229;
    // request code for startActivityForResult with intent "phoneprofiles.intent.action.EXPORTDATA"
    //private static final int REQUEST_CODE_REMOTE_EXPORT = 6250;
    // request code for startActivityForResult with intent ACTION_OPEN_DOCUMENT_TREE
    private static final int REQUEST_CODE_BACKUP_SETTINGS = 6230;
    private static final int REQUEST_CODE_BACKUP_SETTINGS_2 = 6231;
    private static final int REQUEST_CODE_RESTORE_SETTINGS = 6232;
    private static final int REQUEST_CODE_SHARE_SETTINGS = 6233;
    private static final int REQUEST_CODE_RESTORE_SHARED_SETTINGS = 6234;

    public static final String PREF_START_TARGET_HELPS = "editor_profiles_activity_start_target_helps";
    public static final String PREF_START_TARGET_HELPS_DEFAULT_PROFILE = "editor_profile_activity_start_target_helps_default_profile";

    public static final String PREF_START_TARGET_HELPS_RUN_STOP_INDICATOR = "editor_profile_activity_start_target_helps_run_stop_indicator";
    public static final String PREF_START_TARGET_HELPS_BOTTOM_NAVIGATION = "editor_profile_activity_start_target_helps_bottom_navigation";

    public static final String PREF_START_TARGET_HELPS_FINISHED = "editor_profiles_activity_start_target_helps_finished";

    private static final String PREF_BACKUP_CREATE_PPP_SUBFOLDER = "backup_create_ppp_subfolder";

    static final String EXTRA_NEW_PROFILE_MODE = "new_profile_mode";
    static final String EXTRA_PREDEFINED_PROFILE_INDEX = "predefined_profile_index";
    static final String EXTRA_NEW_EVENT_MODE = "new_event_mode";
    static final String EXTRA_PREDEFINED_EVENT_INDEX = "predefined_event_index";
    //static final String EXTRA_SELECTED_FILTER = "selected_filter";

    private static final int IMPORTEXPORT_IMPORT = 1;
    private static final int IMPORTEXPORT_EXPORT = 2;
    private static final int IMPORTEXPORT_IMPORTFROMPP = 3;

    private Toolbar editorToolbar;
    //Toolbar bottomToolbar;
    //private DrawerLayout drawerLayout;
    //private PPScrimInsetsFrameLayout drawerRoot;
    //private ListView drawerListView;
    //private ActionBarDrawerToggle drawerToggle;
    //private BottomNavigationView bottomNavigationView;
    AppCompatSpinner filterSpinner;
    //private AppCompatSpinner orderSpinner;
    //private View headerView;
    //private ImageView drawerHeaderFilterImage;
    //private TextView drawerHeaderFilterTitle;
    //private TextView drawerHeaderFilterSubtitle;
    //private BottomNavigationView bottomNavigationView;
    private SmoothBottomBar bottomNavigationView;

    //private String[] drawerItemsTitle;
    //private String[] drawerItemsSubtitle;
    //private Integer[] drawerItemsIcon;

    boolean filterInitialized;

    int editorSelectedView = 0;
    private int filterProfilesSelectedItem = 0;
    private int filterEventsSelectedItem = 0;

    //private boolean startTargetHelps;
    //public boolean targetHelpsSequenceStarted;

    int selectedLanguage = 0;
    String defaultLanguage = "";
    String defaultCountry = "";
    String defaultScript = "";
    final Collator languagesCollator = Collator.getInstance();

    AddProfileDialog addProfileDialog;
    AddEventDialog addEventDialog;

    private final BroadcastReceiver refreshGUIBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive( Context context, Intent intent ) {
//            PPApplication.logE("[IN_BROADCAST] EditorActivity.refreshGUIBroadcastReceiver", "xxx");
            //boolean refresh = intent.getBooleanExtra(RefreshActivitiesBroadcastReceiver.EXTRA_REFRESH, true);
            boolean refreshIcons = intent.getBooleanExtra(RefreshActivitiesBroadcastReceiver.EXTRA_REFRESH_ICONS, false);
            long profileId = intent.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
            long eventId = intent.getLongExtra(PPApplication.EXTRA_EVENT_ID, 0);
            // not change selection in editor if refresh is outside editor
            EditorActivity.this.refreshGUI(/*refresh,*//* true,*/  refreshIcons, false, profileId, eventId);
        }
    };

    private final BroadcastReceiver showTargetHelpsBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive( Context context, Intent intent ) {
//            PPApplication.logE("[IN_BROADCAST] EditorActivity.showTargetHelpsBroadcastReceiver", "xxx");
            Fragment fragment = EditorActivity.this.getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
            if (fragment != null) {
                if (fragment instanceof EditorProfileListFragment)
                    ((EditorProfileListFragment) fragment).showTargetHelps();
                else
                    ((EditorEventListFragment) fragment).showTargetHelps();
            }
        }
    };

    private final BroadcastReceiver finishBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive( Context context, Intent intent ) {
//            PPApplication.logE("[IN_BROADCAST] EditorActivity.finishBroadcastReceiver", "xxx");
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(PPApplication.ACTION_FINISH_ACTIVITY)) {
                    String what = intent.getStringExtra(PPApplication.EXTRA_WHAT_FINISH);
                    if (what.equals("editor")) {
                        try {
                            EditorActivity.this.setResult(Activity.RESULT_CANCELED);
                            EditorActivity.this.finishAffinity();
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }
                    }
                }
            }
        }
    };

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //PPApplication.logE("EditorActivity.onCreate", "xxx");

        GlobalGUIRoutines.setTheme(this, false, true/*, true*/, false, false, false);
        //GlobalGUIRoutines.setLanguage(this);

        savedInstanceStateChanged = (savedInstanceState != null);

        createApplicationsCache();

        /*if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            setContentView(R.layout.activity_editor_list_onepane_19);
        else*/
            setContentView(R.layout.activity_editor);
        setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.ppp_app_name)));

        boolean doServiceStart = startPPServiceWhenNotStarted();
        if (doServiceStart) {
            finish();
            return;
        }
        else
        if (showNotStartedToast()) {
            finish();
            return;
        }

        activityStarted = true;

        //drawerLayout = findViewById(R.id.editor_list_drawer_layout);

        /*
        if (Build.VERSION.SDK_INT >= 21) {
            drawerLayout.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                        int statusBarHeight = insets.getSystemWindowInsetTop();
                        PPApplication.logE("EditorActivity.onApplyWindowInsets", "statusBarHeight="+statusBarHeight);
                        Rect rect = insets.getSystemWindowInsets();
                        PPApplication.logE("EditorActivity.onApplyWindowInsets", "rect.top="+rect.top);
                        rect.top = rect.top + statusBarHeight;
                        return insets.replaceSystemWindowInsets(rect);
                    }
                }
            );
        }
        */

        //overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        //String appTheme = ApplicationPreferences.applicationTheme(getApplicationContext(), true);

        /*
        if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            //w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // create our manager instance after the content view is set
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            // enable status bar tint
            tintManager.setStatusBarTintEnabled(true);
            // set a custom tint color for status bar
            switch (appTheme) {
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

        //if (android.os.Build.VERSION.SDK_INT >= 21)
        //	getWindow().setNavigationBarColor(R.attr.colorPrimary);

        //setWindowContentOverlayCompat();

    /*	// add profile list into list container
        EditorProfileListFragment fragment = new EditorProfileListFragment();
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.editor_list_container, fragment, "EditorProfileListFragment").commit(); */

        /*
        drawerRoot = findViewById(R.id.editor_drawer_root);

        // set status bar background for Activity body layout
        switch (appTheme) {
            case "color":
                drawerLayout.setStatusBarBackground(R.color.primaryDark);
                break;
            case "white":
                drawerLayout.setStatusBarBackground(R.color.primaryDark_white);
                break;
            case "dark":
                drawerLayout.setStatusBarBackground(R.color.primaryDark_dark);
                break;
            case "dlight":
                drawerLayout.setStatusBarBackground(R.color.primaryDark_dark);
                break;
        }

        drawerListView = findViewById(R.id.editor_drawer_list);
        headerView =  getLayoutInflater().inflate(R.layout.editor_drawer_list_header, drawerListView, false);
        drawerListView.addHeaderView(headerView, null, false);
        drawerHeaderFilterImage = findViewById(R.id.editor_drawer_list_header_icon);
        drawerHeaderFilterTitle = findViewById(R.id.editor_drawer_list_header_title);
        drawerHeaderFilterSubtitle = findViewById(R.id.editor_drawer_list_header_subtitle);

        // set header padding for notches
        //if (Build.VERSION.SDK_INT >= 21) {
            drawerRoot.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                @Override
                public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                    headerView.setPadding(
                            headerView.getPaddingLeft(),
                            headerView.getPaddingTop() + insets.getSystemWindowInsetTop(),
                            headerView.getPaddingRight(),
                            headerView.getPaddingBottom());
                    insets.consumeSystemWindowInsets();
                    drawerRoot.setOnApplyWindowInsetsListener(null);
                    return insets;
                }
            });
        //}

        //if (Build.VERSION.SDK_INT < 21)
        //    drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        // actionbar titles
        drawerItemsTitle = new String[] {
                getString(R.string.editor_drawer_title_profiles),
                getString(R.string.editor_drawer_title_profiles),
                getString(R.string.editor_drawer_title_profiles),
                getString(R.string.editor_drawer_title_events),
                getString(R.string.editor_drawer_title_events),
                getString(R.string.editor_drawer_title_events),
                getString(R.string.editor_drawer_title_events),
                getString(R.string.editor_drawer_title_events)
              };

        // drawer item titles
        drawerItemsSubtitle = new String[] {
                getString(R.string.editor_drawer_list_item_profiles_all),
                getString(R.string.editor_drawer_list_item_profiles_show_in_activator),
                getString(R.string.editor_drawer_list_item_profiles_no_show_in_activator),
                getString(R.string.editor_drawer_list_item_events_start_order),
                getString(R.string.editor_drawer_list_item_events_all),
                getString(R.string.editor_drawer_list_item_events_running),
                getString(R.string.editor_drawer_list_item_events_paused),
                getString(R.string.editor_drawer_list_item_events_stopped)
              };

        drawerItemsIcon = new Integer[] {
                R.drawable.ic_events_drawer_profile_filter_2,
                R.drawable.ic_events_drawer_profile_filter_0,
                R.drawable.ic_events_drawer_profile_filter_1,
                R.drawable.ic_events_drawer_event_filter_2,
                R.drawable.ic_events_drawer_event_filter_2,
                R.drawable.ic_events_drawer_event_filter_0,
                R.drawable.ic_events_drawer_event_filter_1,
                R.drawable.ic_events_drawer_event_filter_3,
              };


        // Pass string arrays to EditorDrawerListAdapter
        // use action bar themed context
        //drawerAdapter = new EditorDrawerListAdapter(drawerListView, getSupportActionBar().getThemedContext(), drawerItemsTitle, drawerItemsSubtitle, drawerItemsIcon);
        EditorDrawerListAdapter drawerAdapter = new EditorDrawerListAdapter(getBaseContext(), drawerItemsTitle, drawerItemsSubtitle, drawerItemsIcon);
        
        // Set the MenuListAdapter to the ListView
        drawerListView.setAdapter(drawerAdapter);
 
        // Capture listview menu item click
        drawerListView.setOnItemClickListener(new DrawerItemClickListener());
        */

        editorToolbar = findViewById(R.id.editor_toolbar);
        setSupportActionBar(editorToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_activity_editor);
        }

        int startupSource = getIntent().getIntExtra(PPApplication.EXTRA_STARTUP_SOURCE, 0);

        //bottomToolbar = findViewById(R.id.editor_list_bottom_bar);

        /*
        // Enable ActionBar app icon to behave as action to toggle nav drawer
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        */

        /*
        // is required. This adds hamburger icon in toolbar
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.editor_drawer_open, R.string.editor_drawer_open)
        {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }
 
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
            // this disable animation
            //@Override
            //public void onDrawerSlide(View drawerView, float slideOffset)
            //{
            //      if(drawerView!=null && drawerView == drawerRoot){
            //            super.onDrawerSlide(drawerView, 0);
            //      }else{
            //            super.onDrawerSlide(drawerView, slideOffset);
            //      }
            //}
        };
        drawerLayout.addDrawerListener(drawerToggle);
        */

        bottomNavigationView = findViewById(R.id.editor_list_bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(
                (me.ibrahimsn.lib.OnItemSelectedListener) item -> {
                    //PPApplication.logE("EditorActivity.bottomNavigationView.OnItemSelectedListener", "xxx");
                    //noinspection Convert2MethodRef
                    return EditorActivity.this.selectViewItem(item);
                }
        );
        // set size of icons of BottomNavigationView
        /*BottomNavigationMenuView menuView = (BottomNavigationMenuView) bottomNavigationView.getChildAt(0);
        for (int i = 0; i < menuView.getChildCount(); i++) {
            final View iconView = menuView.getChildAt(i).findViewById(com.google.android.material.R.id.icon);
            final ViewGroup.LayoutParams layoutParams = iconView.getLayoutParams();
            final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, displayMetrics);
            layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, displayMetrics);
            iconView.setLayoutParams(layoutParams);
        }*/

        filterSpinner = findViewById(R.id.editor_filter_spinner);
        String[] filterItems = new String[] {
                getString(R.string.editor_drawer_title_profiles) + " - " + getString(R.string.editor_drawer_list_item_profiles_all),
                getString(R.string.editor_drawer_title_profiles) + " - " + getString(R.string.editor_drawer_list_item_profiles_show_in_activator),
                getString(R.string.editor_drawer_title_profiles) + " - " + getString(R.string.editor_drawer_list_item_profiles_no_show_in_activator)
        };
        GlobalGUIRoutines.HighlightedSpinnerAdapter filterSpinnerAdapter = new GlobalGUIRoutines.HighlightedSpinnerAdapter(
                this,
                R.layout.highlighted_filter_spinner,
                filterItems);
        filterSpinnerAdapter.setDropDownViewResource(R.layout.highlighted_spinner_dropdown);
        filterSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background);
        filterSpinner.setSupportBackgroundTintList(ContextCompat.getColorStateList(this/*getBaseContext()*/, R.color.highlighted_spinner_all_editor));
/*        switch (appTheme) {
            case "dark":
                filterSpinner.setSupportBackgroundTintList(ContextCompat.getColorStateList(getBaseContext(), R.color.editorFilterTitleColor_dark));
                //filterSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background_dark);
                break;
            case "white":
                filterSpinner.setSupportBackgroundTintList(ContextCompat.getColorStateList(getBaseContext(), R.color.editorFilterTitleColor_white));
                //filterSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background_white);
                break;
//            case "dlight":
//                filterSpinner.setSupportBackgroundTintList(ContextCompat.getColorStateList(getBaseContext(), R.color.editorFilterTitleColor));
//                filterSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background_dlight);
//                break;
            default:
                filterSpinner.setSupportBackgroundTintList(ContextCompat.getColorStateList(getBaseContext(), R.color.editorFilterTitleColor));
                //filterSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background_white);
                break;
        }*/
        filterInitialized = false;
        filterSpinner.setAdapter(filterSpinnerAdapter);
        filterSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!filterInitialized) {
                    filterInitialized = true;
                    return;
                }
                if (filterSpinner.getAdapter() != null) {
                    //if (filterSpinner.getAdapter().getCount() <= position)
                    //    position = 0;
                    ((GlobalGUIRoutines.HighlightedSpinnerAdapter) filterSpinner.getAdapter()).setSelection(position);
                }

                //Log.e("EditorActivity.filterSpinner.onItemSelected", "position=" + position);
                selectFilterItem(editorSelectedView, position, true/*, true*/);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        eventsRunStopIndicator = findViewById(R.id.editor_list_run_stop_indicator);
        TooltipCompat.setTooltipText(eventsRunStopIndicator, getString(R.string.editor_activity_targetHelps_trafficLightIcon_title));
        eventsRunStopIndicator.setOnClickListener(view -> {
            if (!isFinishing()) {
                RunStopIndicatorPopupWindow popup = new RunStopIndicatorPopupWindow(getDataWrapper(), EditorActivity.this);

                View contentView = popup.getContentView();
                contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                int popupWidth = contentView.getMeasuredWidth();
                //int popupHeight = contentView.getMeasuredHeight();
                //Log.d("ActivatorActivity.eventsRunStopIndicator.onClick","popupWidth="+popupWidth);
                //Log.d("ActivatorActivity.eventsRunStopIndicator.onClick","popupHeight="+popupHeight);

                int[] runStopIndicatorLocation = new int[2];
                //eventsRunStopIndicator.getLocationOnScreen(runStopIndicatorLocation);
                eventsRunStopIndicator.getLocationInWindow(runStopIndicatorLocation);

                int x = 0;
                int y = 0;

                if (runStopIndicatorLocation[0] + eventsRunStopIndicator.getWidth() - popupWidth < 0)
                    x = -(runStopIndicatorLocation[0] + eventsRunStopIndicator.getWidth() - popupWidth);

                popup.setClippingEnabled(false); // disabled for draw outside activity
                popup.showOnAnchor(eventsRunStopIndicator, RelativePopupWindow.VerticalPosition.ALIGN_TOP,
                        RelativePopupWindow.HorizontalPosition.ALIGN_RIGHT, x, y, false);
            }
        });
        
        // set drawer item and order
        //if ((savedInstanceState != null) || (ApplicationPreferences.applicationEditorSaveEditorState(getApplicationContext())))
        //{
            //filterSelectedItem = ApplicationPreferences.preferences.getInt(SP_EDITOR_DRAWER_SELECTED_ITEM, 1);

            if (startupSource == PPApplication.STARTUP_SOURCE_EDITOR_SHOW_IN_ACTIVATOR_FILTER) {
                editorSelectedView = 0;
                filterProfilesSelectedItem = DSI_PROFILES_ALL;
            }
            else if (startupSource == PPApplication.STARTUP_SOURCE_EDITOR_SHOW_IN_EDITOR_FILTER) {
                editorSelectedView = 0;
                filterProfilesSelectedItem = DSI_EVENTS_ALL;
            }
            else {
//                ApplicationPreferences.editorSelectedView(getApplicationContext());
//                ApplicationPreferences.editorProfilesViewSelectedItem(getApplicationContext());
                editorSelectedView = ApplicationPreferences.editorSelectedView;
                filterProfilesSelectedItem = ApplicationPreferences.editorProfilesViewSelectedItem;
            }
//            ApplicationPreferences.editorEventsViewSelectedItem(getApplicationContext());
            filterEventsSelectedItem = ApplicationPreferences.editorEventsViewSelectedItem;
//            Log.e("EditorActivity.onCreate", "editorSelectedView="+editorSelectedView);
//            Log.e("EditorActivity.onCreate", "filterProfilesSelectedItem="+filterProfilesSelectedItem);
//            Log.e("EditorActivity.onCreate", "filterEventsSelectedItem="+filterEventsSelectedItem);
        //}

        //startTargetHelps = false;
        /*if (editorSelectedView == 0)
            bottomNavigationView.setSelectedItemId(R.id.menu_profiles_view);
        else
            bottomNavigationView.setSelectedItemId(R.id.menu_events_view);*/
        bottomNavigationView.setItemActiveIndex(editorSelectedView);
        selectViewItem(editorSelectedView);

        /*
        if (editorSelectedView == 0)
            selectFilterItem(filterProfilesSelectedItem, false, false, false);
        else
            selectFilterItem(filterEventsSelectedItem, false, false, false);
        */

        /*
        // not working good, all activity is under status bar
        ViewCompat.setOnApplyWindowInsetsListener(drawerLayout, new OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                int statusBarSize = insets.getSystemWindowInsetTop();
                PPApplication.logE("EditorActivity.onApplyWindowInsets", "statusBarSize="+statusBarSize);
                return insets;
            }
        });
        */

        getApplicationContext().registerReceiver(finishBroadcastReceiver, new IntentFilter(PPApplication.ACTION_FINISH_ACTIVITY));
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        boolean doServiceStart = startPPServiceWhenNotStarted();
        if (doServiceStart) {
            if (!isFinishing())
                finish();
            return;
        }
        else
        if (showNotStartedToast()) {
            if (!isFinishing())
                finish();
            return;
        }

        //PPApplication.logE("EditorActivity.onStart", "xxx");

        if (activityStarted) {
            Intent intent = new Intent(PPApplication.ACTION_FINISH_ACTIVITY);
            intent.putExtra(PPApplication.EXTRA_WHAT_FINISH, "activator");
            getApplicationContext().sendBroadcast(intent);

            LocalBroadcastManager.getInstance(this).registerReceiver(refreshGUIBroadcastReceiver,
                    new IntentFilter(PPApplication.PACKAGE_NAME + ".RefreshEditorGUIBroadcastReceiver"));
            LocalBroadcastManager.getInstance(this).registerReceiver(showTargetHelpsBroadcastReceiver,
                    new IntentFilter(PPApplication.PACKAGE_NAME + ".ShowEditorTargetHelpsBroadcastReceiver"));

            refreshGUI(/*true,*/ false, true, 0, 0);

            Permissions.grantNotificationsPermission(this);
        }
        else {
            if (!isFinishing())
                finish();
        }
    }

    @SuppressWarnings("SameReturnValue")
    private boolean showNotStartedToast() {
//        PPApplication.logE("[APP_START] EditorActivity.showNotStartedToast", "setApplicationFullyStarted");
        PPApplication.setApplicationFullyStarted(getApplicationContext());
        return false;
/*        boolean applicationStarted = PPApplication.getApplicationStarted(true);
        boolean fullyStarted = PPApplication.applicationFullyStarted;
        if (!applicationStarted) {
            String text = getString(R.string.ppp_app_name) + " " + getString(R.string.application_is_not_started);
            PPApplication.showToast(getApplicationContext(), text, Toast.LENGTH_SHORT);
            return true;
        }
        if (!fullyStarted) {
            if ((PPApplication.startTimeOfApplicationStart > 0) &&
                    ((Calendar.getInstance().getTimeInMillis() - PPApplication.startTimeOfApplicationStart) > PPApplication.APPLICATION_START_DELAY)) {
                Intent activityIntent = new Intent(this, WorkManagerNotWorkingActivity.class);
                // clear all opened activities
                activityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(activityIntent);
            }
            else {
                String text = getString(R.string.ppp_app_name) + " " + getString(R.string.application_is_starting_toast);
                PPApplication.showToast(getApplicationContext(), text, Toast.LENGTH_SHORT);
            }
            return true;
        }
        return false;*/
    }

    private boolean startPPServiceWhenNotStarted() {
        // this is for list widget header
        boolean serviceStarted = PhoneProfilesService.isServiceRunning(getApplicationContext(), PhoneProfilesService.class, false);
        if (!serviceStarted) {
            /*if (PPApplication.logEnabled()) {
                PPApplication.logE("EditorActivity.onStart", "application is not started");
                PPApplication.logE("EditorActivity.onStart", "service instance=" + PhoneProfilesService.getInstance());
                if (PhoneProfilesService.getInstance() != null)
                    PPApplication.logE("EditorActivity.onStart", "service hasFirstStart=" + PhoneProfilesService.getInstance().getServiceHasFirstStart());
            }*/

            AutostartPermissionNotification.showNotification(getApplicationContext(), true);

            // start PhoneProfilesService
            //PPApplication.firstStartServiceStarted = false;
            PPApplication.setApplicationStarted(getApplicationContext(), true);
            Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
            //serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
            //serviceIntent.putExtra(PhoneProfilesService.EXTRA_DEACTIVATE_PROFILE, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, true);
            serviceIntent.putExtra(PPApplication.EXTRA_APPLICATION_START, true);
            serviceIntent.putExtra(PPApplication.EXTRA_DEVICE_BOOT, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_PACKAGE_REPLACE, false);
//            PPApplication.logE("[START_PP_SERVICE] EditorProfileActivity.startPPServiceWhenNotStarted", "(1)");
            PPApplication.startPPService(this, serviceIntent);
            return true;
        } else {
            if ((PhoneProfilesService.getInstance() == null) || (!PhoneProfilesService.getInstance().getServiceHasFirstStart())) {
                /*if (PPApplication.logEnabled()) {
                    PPApplication.logE("EditorActivity.onStart", "application is started");
                    PPApplication.logE("EditorActivity.onStart", "service instance=" + PhoneProfilesService.getInstance());
                    if (PhoneProfilesService.getInstance() != null)
                        PPApplication.logE("EditorActivity.onStart", "service hasFirstStart=" + PhoneProfilesService.getInstance().getServiceHasFirstStart());
                }*/

                // start PhoneProfilesService
                //PPApplication.firstStartServiceStarted = false;

                /*
                Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
                //serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
                //serviceIntent.putExtra(PhoneProfilesService.EXTRA_DEACTIVATE_PROFILE, true);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, false);
                serviceIntent.putExtra(PPApplication.EXTRA_APPLICATION_START, true);
                serviceIntent.putExtra(PPApplication.EXTRA_DEVICE_BOOT, false);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_PACKAGE_REPLACE, false);
                PPApplication.logE("[START_PP_SERVICE] EditorProfileActivity.startPPServiceWhenNotStarted", "(2)");
                PPApplication.startPPService(this, serviceIntent);
                */

                return true;
            }
            //else {
            //    PPApplication.logE("EditorActivity.onStart", "application and service is started");
            //}
        }

        return false;
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        //PPApplication.logE("EditorActivity.onStop", "xxx");

        LocalBroadcastManager.getInstance(this).unregisterReceiver(refreshGUIBroadcastReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(showTargetHelpsBroadcastReceiver);

        if ((addProfileDialog != null) && (addProfileDialog.mDialog != null) && addProfileDialog.mDialog.isShowing())
            addProfileDialog.mDialog.dismiss();
        if ((addEventDialog != null) && (addEventDialog.mDialog != null) && addEventDialog.mDialog.isShowing())
            addEventDialog.mDialog.dismiss();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if ((importProgressDialog != null) && importProgressDialog.isShowing()) {
            importProgressDialog.dismiss();
            importProgressDialog = null;
        }
        if ((exportProgressDialog != null) && exportProgressDialog.isShowing()) {
            exportProgressDialog.dismiss();
            exportProgressDialog = null;
        }
        if ((backupProgressDialog != null) && backupProgressDialog.isShowing()) {
            backupProgressDialog.dismiss();
            backupProgressDialog = null;
        }
        if ((restoreProgressDialog != null) && restoreProgressDialog.isShowing()) {
            restoreProgressDialog.dismiss();
            restoreProgressDialog = null;
        }
        if ((importAsyncTask != null) && !importAsyncTask.getStatus().equals(AsyncTask.Status.FINISHED)){
            importAsyncTask.cancel(true);
            doImport = false;
        }
        if ((importFromPPAsyncTask != null) && !importFromPPAsyncTask.getStatus().equals(AsyncTask.Status.FINISHED)){
            importFromPPAsyncTask.cancel(true);
            doImport = false;
        }
        if ((exportAsyncTask != null) && !exportAsyncTask.getStatus().equals(AsyncTask.Status.FINISHED)){
            exportAsyncTask.cancel(true);
        }
        if ((backupAsyncTask != null) && !backupAsyncTask.getStatus().equals(AsyncTask.Status.FINISHED)){
            backupAsyncTask.cancel(true);
        }
        if ((restoreAsyncTask != null) && !restoreAsyncTask.getStatus().equals(AsyncTask.Status.FINISHED)){
            restoreAsyncTask.cancel(true);
        }

        if (!savedInstanceStateChanged)
        {
            // no destroy caches on orientation change
            if (applicationsCache != null)
                applicationsCache.clearCache(true);
            applicationsCache = null;
        }

        try {
            getApplicationContext().unregisterReceiver(finishBroadcastReceiver);
        } catch (Exception e) {
            //PPApplication.recordException(e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        editorToolbar.inflateMenu(R.menu.editor_top_bar);

        if (Build.VERSION.SDK_INT >= 28) {
            menu.setGroupDividerEnabled(true);
        }

        return true;
    }

    private static void onNextLayout(final View view, final Runnable runnable) {
        final ViewTreeObserver observer = view.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                final ViewTreeObserver trueObserver;

                if (observer.isAlive()) {
                    trueObserver = observer;
                } else {
                    trueObserver = view.getViewTreeObserver();
                }

                trueObserver.removeOnGlobalLayoutListener(this);

                runnable.run();
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean ret = super.onPrepareOptionsMenu(menu);

        MenuItem menuItem;

        //menuItem = menu.findItem(R.id.menu_import_export);
        //menuItem.setTitle(getResources().getString(R.string.menu_import_export) + "  >");

        // change global events run/stop menu item title
        menuItem = menu.findItem(R.id.menu_run_stop_events);
        if (menuItem != null)
        {
            if (Event.getGlobalEventsRunning())
            {
                menuItem.setTitle(R.string.menu_stop_events);
                menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            }
            else
            {
                menuItem.setTitle(R.string.menu_run_events);
                menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
            }
        }

        menuItem = menu.findItem(R.id.menu_restart_events);
        if (menuItem != null)
        {
            menuItem.setVisible(Event.getGlobalEventsRunning());
            menuItem.setEnabled(PPApplication.getApplicationStarted(true));
        }

        menuItem = menu.findItem(R.id.menu_dark_theme);
        if (menuItem != null)
        {
            String appTheme = ApplicationPreferences.applicationTheme(getApplicationContext(), false);
            if (!appTheme.equals("night_mode")) {
                menuItem.setVisible(true);
                menuItem.setEnabled(true);
                if (appTheme.equals("dark"))
                    menuItem.setTitle(R.string.menu_dark_theme_off);
                else
                    menuItem.setTitle(R.string.menu_dark_theme_on);
            }
            else {
                menuItem.setVisible(false);
                menuItem.setEnabled(false);
            }
        }

        menuItem = menu.findItem(R.id.menu_email_debug_logs_to_author);
        if (menuItem != null)
        {
            //noinspection ConstantConditions
            menuItem.setVisible(PPApplication.logIntoFile || PPApplication.crashIntoFile);
            //noinspection ConstantConditions
            menuItem.setEnabled(PPApplication.logIntoFile || PPApplication.crashIntoFile);
        }

        menuItem = menu.findItem(R.id.menu_debug);
        if (menuItem != null) {
            menuItem.setVisible(DebugVersion.enabled);
            menuItem.setEnabled(DebugVersion.enabled);
        }

        /*
        boolean activityExists = GlobalGUIRoutines.activityActionExists(Intent.ACTION_OPEN_DOCUMENT_TREE, getApplicationContext());
        Log.e("EditorActivity.onPrepareOptionsMenu", "ACTION_OPEN_DOCUMENT_TREE activityExists="+activityExists);
        menuItem = menu.findItem(R.id.menu_import);
        if (menuItem != null) {
            menuItem.setVisible(activityExists);
            menuItem.setEnabled(activityExists);
        }
        menuItem = menu.findItem(R.id.menu_export);
        if (menuItem != null) {
            menuItem.setVisible(activityExists);
            menuItem.setEnabled(activityExists);
        }
        */

        /*activityExists = GlobalGUIRoutines.activityActionExists(Intent.ACTION_OPEN_DOCUMENT_TREE, getApplicationContext());
        menuItem = menu.findItem(R.id.menu_share_settings);
        if (menuItem != null) {
            menuItem.setVisible(activityExists);
            menuItem.setEnabled(activityExists);
        }*/
        /*activityExists = GlobalGUIRoutines.activityActionExists(Intent.ACTION_OPEN_DOCUMENT, getApplicationContext());
        Log.e("EditorActivity.onPrepareOptionsMenu", "ACTION_OPEN_DOCUMENT activityExists="+activityExists);
        menuItem = menu.findItem(R.id.menu_restore_shared_settings);
        if (menuItem != null) {
            menuItem.setVisible(activityExists);
            menuItem.setEnabled(activityExists);
        }*/

        menuItem = menu.findItem(R.id.menu_import_from_pp);
        if (menuItem != null) {
            try {
                PackageManager packageManager = getPackageManager();
                PackageInfo pInfo = packageManager.getPackageInfo(PPApplication.PACKAGE_NAME_PP, 0);
                int packageVersionCode = PPApplication.getVersionCode(pInfo);

                /*ActivityManager manager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
                List<ActivityManager.RunningAppProcessInfo> processes = manager.getRunningAppProcesses();
                boolean isRunnning = false;
                for (ActivityManager.RunningAppProcessInfo process : processes)
                {
                    if (process.processName.equals(PPApplication.PACKAGE_NAME_PP)) {
                        isRunnning = true;
                        break;
                    }
                }*/
                menuItem.setVisible((packageVersionCode >= 3601) /*&& isRunnning*/);
                menuItem.setEnabled((packageVersionCode >= 3601) /*&& isRunnning*/);
            } catch (Exception e) {
                menuItem.setVisible(false);
                menuItem.setEnabled(false);
            }
        }

        PackageManager packageManager = getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage("com.sec.android.app.samsungapps");
        boolean galaxyStoreInstalled = (intent != null);
        //intent = packageManager.getLaunchIntentForPackage("com.huawei.appmarket");
        //boolean appGalleryInstalled = (intent != null);
        menuItem = menu.findItem(R.id.menu_check_in_galaxy_store);
        if (menuItem != null) {
            menuItem.setVisible(PPApplication.deviceIsSamsung && galaxyStoreInstalled);
        }
        //menuItem = menu.findItem(R.id.menu_check_in_appgallery);
        //if (menuItem != null) {
        //    menuItem.setVisible(PPApplication.deviceIsHuawei && PPApplication.romIsEMUI && appGalleryInstalled);
        //}

        //noinspection Convert2MethodRef
        onNextLayout(editorToolbar, () -> showTargetHelps());

        return ret;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent intent;

        DataWrapper dataWrapper = getDataWrapper();

        int itemId = item.getItemId();
/*        if (itemId == android.R.id.home) {
//                if (drawerLayout.isDrawerOpen(drawerRoot)) {
//                    drawerLayout.closeDrawer(drawerRoot);
//                } else {
//                    drawerLayout.openDrawer(drawerRoot);
//                }
                return super.onOptionsItemSelected(item);
          }
 */
        if (itemId == R.id.menu_restart_events) {
            //getDataWrapper().addActivityLog(DatabaseHandler.ALTYPE_RESTARTEVENTS, null, null, null, 0);

            // ignore manual profile activation
            // and unblock forceRun events
            //PPApplication.logE("$$$ restartEvents","from EditorActivity.onOptionsItemSelected menu_restart_events");
            if (dataWrapper != null)
                dataWrapper.restartEventsWithAlert(this);
            return true;
        }
        else
        if (itemId == R.id.menu_run_stop_events) {
            if (dataWrapper != null)
                dataWrapper.runStopEventsWithAlert(this, null, false);
            return true;
        }
        else
        if (itemId == R.id.menu_activity_log) {
            intent = new Intent(getBaseContext(), ActivityLogActivity.class);
            startActivity(intent);
            return true;
        }
        else
        if (itemId == R.id.important_info) {
            intent = new Intent(getBaseContext(), ImportantInfoActivity.class);
            startActivity(intent);
            return true;
        }
        else
        if (itemId == R.id.menu_settings) {
            intent = new Intent(getBaseContext(), PhoneProfilesPrefsActivity.class);
            //noinspection deprecation
            startActivityForResult(intent, REQUEST_CODE_APPLICATION_PREFERENCES);
            return true;
        }
        else
        if (itemId == R.id.menu_dark_theme) {
            String theme = ApplicationPreferences.applicationTheme(getApplicationContext(), false);
            if (!theme.equals("night_mode")) {
                SharedPreferences preferences = ApplicationPreferences.getSharedPreferences(getApplicationContext());
                Editor editor = preferences.edit();
                if (theme.equals("dark")) {
                    //theme = preferences.getString(ApplicationPreferences.PREF_APPLICATION_NOT_DARK_THEME, "white");
                    //theme = ApplicationPreferences.applicationNightModeOffTheme(getApplicationContext());
                    editor.putString(ApplicationPreferences.PREF_APPLICATION_THEME, "white"/*theme*/);
                    editor.apply();
                    ApplicationPreferences.applicationTheme = "white";
                } else {
                    //editor.putString(ApplicationPreferences.PREF_APPLICATION_NOT_DARK_THEME, theme);
                    editor.putString(ApplicationPreferences.PREF_APPLICATION_THEME, "dark");
                    editor.apply();
                    ApplicationPreferences.applicationTheme = "dark";
                }
                GlobalGUIRoutines.switchNightMode(getApplicationContext(), false);
                GlobalGUIRoutines.reloadActivity(this, true);
            }
            return true;
        }
        else
        if (itemId == R.id.menu_export) {
            exportData(R.string.menu_export, false, false, false);
            return true;
        }
        else
        if (itemId == R.id.menu_import) {
            importData(R.string.menu_import, false);
            return true;
        }

        else
        if (itemId == R.id.menu_share_settings) {
            exportData(R.string.menu_share_settings, false, false, true);
            return true;
        }
        else
        if (itemId == R.id.menu_restore_shared_settings) {
            importData(R.string.menu_restore_shared_settings, true);
            return true;
        }

        else
        if (itemId == R.id.menu_export_and_email) {
            exportData(R.string.menu_export_and_email, true, false, false);
            return true;
        }
        else
        if (itemId == R.id.menu_email_to_author) {
            intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:")); // only email apps should handle this
            String[] email = {"henrich.gron@gmail.com"};
            intent.putExtra(Intent.EXTRA_EMAIL, email);
            String packageVersion = "";
            try {
                PackageInfo pInfo = getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0);
                packageVersion = " - v" + pInfo.versionName + " (" + PPApplication.getVersionCode(pInfo) + ")";
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
            intent.putExtra(Intent.EXTRA_SUBJECT, "PhoneProfilesPlus" + packageVersion + " - " + getString(R.string.about_application_support_subject));
            intent.putExtra(Intent.EXTRA_TEXT, getEmailBodyText());
            try {
                startActivity(Intent.createChooser(intent, getString(R.string.email_chooser)));
            } catch (Exception e) {
                PPApplication.recordException(e);
            }

            return true;
        }
        else
        if (itemId == R.id.menu_export_and_email_to_author) {
            exportData(R.string.menu_export_and_email_to_author, true, true, false);
            return true;
        }
        else
        if (itemId == R.id.menu_email_debug_logs_to_author) {
            ArrayList<Uri> uris = new ArrayList<>();

            File sd = getApplicationContext().getExternalFilesDir(null);

            File logFile = new File(sd, PPApplication.LOG_FILENAME);
            if (logFile.exists()) {
                Uri fileUri = FileProvider.getUriForFile(this, PPApplication.PACKAGE_NAME + ".provider", logFile);
                uris.add(fileUri);
            }

            File crashFile = new File(sd, TopExceptionHandler.CRASH_FILENAME);
            if (crashFile.exists()) {
                Uri fileUri = FileProvider.getUriForFile(this, PPApplication.PACKAGE_NAME + ".provider", crashFile);
                uris.add(fileUri);
            }

            if (uris.size() != 0) {
                String emailAddress = "henrich.gron@gmail.com";
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", emailAddress, null));

                String packageVersion = "";
                try {
                    PackageInfo pInfo = getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0);
                    packageVersion = " - v" + pInfo.versionName + " (" + PPApplication.getVersionCode(pInfo) + ")";
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "PhoneProfilesPlus" + packageVersion + " - " + getString(R.string.email_debug_log_files_subject));
                emailIntent.putExtra(Intent.EXTRA_TEXT, getEmailBodyText());
                emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                List<ResolveInfo> resolveInfo = getPackageManager().queryIntentActivities(emailIntent, 0);
                List<LabeledIntent> intents = new ArrayList<>();
                for (ResolveInfo info : resolveInfo) {
                    intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                    intent.setComponent(new ComponentName(info.activityInfo.packageName, info.activityInfo.name));
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddress});
                    intent.putExtra(Intent.EXTRA_SUBJECT, "PhoneProfilesPlus" + packageVersion + " - " + getString(R.string.email_debug_log_files_subject));
                    intent.putExtra(Intent.EXTRA_TEXT, getEmailBodyText());
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris); //ArrayList<Uri> of attachment Uri's
                    intents.add(new LabeledIntent(intent, info.activityInfo.packageName, info.loadLabel(getPackageManager()), info.icon));
                }
                try {
                    Intent chooser = Intent.createChooser(intents.remove(intents.size() - 1), getString(R.string.email_chooser));
                    //noinspection ToArrayCallWithZeroLengthArrayArgument
                    chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toArray(new LabeledIntent[intents.size()]));
                    startActivity(chooser);
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
            } else {
                // toast notification
                PPApplication.showToast(getApplicationContext(), getString(R.string.toast_debug_log_files_not_exists),
                        Toast.LENGTH_SHORT);
            }

            return true;
        }
        else
        if (itemId == R.id.menu_about) {
            intent = new Intent(getBaseContext(), AboutApplicationActivity.class);
            startActivity(intent);
            return true;
        }
        else
        if (itemId == R.id.menu_exit) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setTitle(R.string.exit_application_alert_title);
            dialogBuilder.setMessage(R.string.exit_application_alert_message);
            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
            dialogBuilder.setPositiveButton(R.string.alert_button_yes, (dialog, which) -> {
                //PPApplication.logE("PPApplication.exitApp", "from EditorProfileActivity.onOptionsItemSelected shutdown=false");

                //IgnoreBatteryOptimizationNotification.setShowIgnoreBatteryOptimizationNotificationOnStart(getApplicationContext(), true);
                SharedPreferences settings = ApplicationPreferences.getSharedPreferences(getApplicationContext());
                Editor editor = settings.edit();
                editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_NEVER_ASK_FOR_ENABLE_RUN, false);
                editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_NEVER_ASK_FOR_GRANT_ROOT, false);
                editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_NEVER_ASK_FOR_GRANT_G1_PERMISSION, false);
                editor.apply();
                ApplicationPreferences.applicationEventNeverAskForEnableRun(getApplicationContext());
                ApplicationPreferences.applicationNeverAskForGrantRoot(getApplicationContext());
                ApplicationPreferences.applicationNeverAskForGrantG1Permission(getApplicationContext());

                PPApplication.exitApp(true, getApplicationContext(), EditorActivity.this.getDataWrapper(), EditorActivity.this, false, true);
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
        if (itemId == R.id.gui_items_help) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setTitle(R.string.gui_items_help_alert_title);
            dialogBuilder.setMessage(R.string.gui_items_help_alert_message);
            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
            dialogBuilder.setPositiveButton(R.string.alert_button_yes, (dialog, which) -> {
                //PPApplication.logE("PPApplication.exitApp", "from EditorProfileActivity.onOptionsItemSelected shutdown=false");
                ApplicationPreferences.startStopTargetHelps(getApplicationContext(), true);
                GlobalGUIRoutines.reloadActivity(this, true);
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
        if (itemId == R.id.menu_import_from_pp) {
            importDataFromPP();
            return true;
        }
        else
        if (itemId == R.id.menu_show_sound_mode) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setTitle("Sound mode in system");

            String soundModeString = "Ringer mode=";

            final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null) {
                switch (audioManager.getRingerMode()) {
                    case AudioManager.RINGER_MODE_NORMAL:
                        soundModeString = soundModeString + "RINGER_MODE_NORMAL";
                        break;
                    case AudioManager.RINGER_MODE_VIBRATE:
                        soundModeString = soundModeString + "RINGER_MODE_VIBRATE";
                        break;
                    case AudioManager.RINGER_MODE_SILENT:
                        soundModeString = soundModeString + "RINGER_MODE_SILENT";
                        break;
                }
            }

            soundModeString = soundModeString + "\nZen mode=";
            switch (ActivateProfileHelper.getSystemZenMode(getApplicationContext())) {
                case ActivateProfileHelper.ZENMODE_ALL:
                    soundModeString = soundModeString + "ZENMODE_ALL";
                    break;
                case ActivateProfileHelper.ZENMODE_PRIORITY:
                    soundModeString = soundModeString + "ZENMODE_PRIORITY";
                    break;
                case ActivateProfileHelper.ZENMODE_ALARMS:
                    soundModeString = soundModeString + "ZENMODE_ALARMS";
                    break;
                case ActivateProfileHelper.ZENMODE_NONE:
                    soundModeString = soundModeString + "ZENMODE_NONE";
                    break;
                case ActivateProfileHelper.ZENMODE_SILENT:
                    soundModeString = soundModeString + "ZENMODE_SILENT";
                    break;
            }

            dialogBuilder.setMessage(soundModeString);

            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
            dialogBuilder.setPositiveButton(android.R.string.ok, null);
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
        if ((itemId == R.id.menu_check_in_github) ||
                (itemId == R.id.menu_check_in_fdroid) ||
                (itemId == R.id.menu_check_in_galaxy_store) ||
//                (itemId == R.id.menu_check_in_amazon_appstore) ||
                (itemId == R.id.menu_check_in_appgallery) ||
                (itemId == R.id.menu_check_in_apkpure)) {

            Intent _intent;
            _intent = new Intent(this, CheckPPPReleasesActivity.class);
            _intent.putExtra(CheckPPPReleasesActivity.EXTRA_MENU_ITEM_ID, itemId);
            _intent.putExtra(CheckPPPReleasesActivity.EXTRA_CRITICAL_CHECK, false);
            _intent.putExtra(CheckPPPReleasesActivity.EXTRA_NEW_VERSION_NAME, "");
            _intent.putExtra(CheckPPPReleasesActivity.EXTRA_NEW_VERSION_CODE, 0);
            startActivity(_intent);

            return true;
        }
        else
        if (itemId == R.id.menu_test_crash) {
            throw new RuntimeException("Test Crash");
            //return true;
        }
        else
        if (itemId == R.id.menu_test_nonFatal) {
            try {
                throw new RuntimeException("Test non-fatal exception");
            } catch (Exception e) {
                // You must relaunch PPP to get this exception in Firebase console:
                //
                // Crashlytics processes exceptions on a dedicated background thread, so the performance
                // impact to your app is minimal. To reduce your users’ network traffic, Crashlytics batches
                // logged exceptions together and sends them the next time the app launches.
                //
                // Crashlytics only stores the most recent 8 exceptions in a given app session. If your app
                // throws more than 8 exceptions in a session, older exceptions are lost.
                PPApplication.recordException(e);
            }
            return true;
        }
        else
        if (itemId == R.id.menu_donation) {
            intent = new Intent(getBaseContext(), DonationPayPalActivity.class);
            startActivity(intent);
            return true;
        }
        else
        if (itemId == R.id.menu_choose_language) {
            String storedLanguage = LocaleHelper.getLanguage(getApplicationContext());
            String storedCountry = LocaleHelper.getCountry(getApplicationContext());
            String storedScript = LocaleHelper.getScript(getApplicationContext());
//            Log.e("EditorActivity.onOptionsItemSelected", "storedLanguage="+storedLanguage);
//            Log.e("EditorActivity.onOptionsItemSelected", "storedCountry="+storedCountry);
//            Log.e("EditorActivity.onOptionsItemSelected", "storedScript="+storedScript);

            final String[] languageValues = getResources().getStringArray(R.array.chooseLanguageValues);
            ArrayList<Language> languages = new ArrayList<>();

            for (String languageValue : languageValues) {
                Language language = new Language();
                if (languageValue.equals("[sys]")) {
                    language.language = languageValue;
                    language.country = "";
                    language.script = "";
                    language.name = getString(R.string.menu_choose_language_system_language);
                } else {
                    String[] splits = languageValue.split("-");
                    String sLanguage = splits[0];
                    String country = "";
                    if (splits.length >= 2)
                        country = splits[1];
                    String script = "";
                    if (splits.length >= 3)
                        script = splits[2];

                    Locale loc = null;
                    if (country.isEmpty() && script.isEmpty())
                        loc = new Locale.Builder().setLanguage(sLanguage).build();
                    if (!country.isEmpty() && script.isEmpty())
                        loc = new Locale.Builder().setLanguage(sLanguage).setRegion(country).build();
                    if (country.isEmpty() && !script.isEmpty())
                        loc = new Locale.Builder().setLanguage(sLanguage).setScript(script).build();
                    if (!country.isEmpty() && !script.isEmpty())
                        loc = new Locale.Builder().setLanguage(sLanguage).setRegion(country).setScript(script).build();

//                    Log.e("EditorActivity.onOptionsItemSelected", "sLanguage="+sLanguage);
//                    Log.e("EditorActivity.onOptionsItemSelected", "country="+country);
//                    Log.e("EditorActivity.onOptionsItemSelected", "script="+script);

                    language.language = sLanguage;
                    language.country = country;
                    language.script = script;
                    language.name = loc.getDisplayName(loc);
                    language.name = language.name.substring(0, 1).toUpperCase(loc) + language.name.substring(1);
                }
                languages.add(language);
            }

            languages.sort(new LanguagesComparator());

            final String[] languageNameChoices = new String[languages.size()];
            for(int i = 0; i < languages.size(); i++) languageNameChoices[i] = languages.get(i).name;

            for (int i = 0; i < languages.size(); i++) {
                Language language = languages.get(i);
                String sLanguage = language.language;
                String country = language.country;
                String script = language.script;

                if (sLanguage.equals(storedLanguage) &&
                        storedCountry.isEmpty() &&
                        storedScript.isEmpty()) {
                    selectedLanguage = i;
                    break;
                }
                if (sLanguage.equals(storedLanguage) &&
                        country.equals(storedCountry) &&
                        storedScript.isEmpty()) {
                    selectedLanguage = i;
                    break;
                }
                if (sLanguage.equals(storedLanguage) &&
                        storedCountry.isEmpty() &&
                        script.equals(storedScript)) {
                    selectedLanguage = i;
                    break;
                }
                if (sLanguage.equals(storedLanguage) &&
                        country.equals(storedCountry) &&
                        script.equals(storedScript)) {
                    selectedLanguage = i;
                    break;
                }
            }

            //Log.e("MainActivity.onOptionsItemSelected", "defualt language="+Locale.getDefault().getDisplayLanguage());
            // this is list of locales by order in system settings. Index 0 = default locale in system
            //LocaleListCompat locales = ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration());
            //for (int i = 0; i < locales.size(); i++) {
            //    Log.e("MainActivity.onOptionsItemSelected", "language="+locales.get(i).getDisplayLanguage());
            //}

            AlertDialog chooseLanguageDialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.menu_choose_language)
                    .setCancelable(true)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setSingleChoiceItems(languageNameChoices, selectedLanguage, (dialog, which) -> {
                        selectedLanguage = which;

                        Language language = languages.get(selectedLanguage);
                        defaultLanguage = language.language;
                        defaultCountry = language.country;
                        defaultScript = language.script;

//                        Log.e("EditorActivity.onOptionsItemSelected", "defaultLanguage="+defaultLanguage);
//                        Log.e("EditorActivity.onOptionsItemSelected", "defaultCountry="+defaultCountry);
//                        Log.e("EditorActivity.onOptionsItemSelected", "defaultScript="+defaultScript);

                        LocaleHelper.setLocale(getApplicationContext(),
                                defaultLanguage, defaultCountry, defaultScript, true);

                        GlobalGUIRoutines.reloadActivity(this, false);
                        dialog.dismiss();
                    })
                    .create();

//                    dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                        @Override
//                        public void onShow(DialogInterface dialog) {
//                            Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                            if (positive != null) positive.setAllCaps(false);
//                            Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                            if (negative != null) negative.setAllCaps(false);
//                        }
//                    });

            chooseLanguageDialog.show();

            return true;
        }


        else {
            return super.onOptionsItemSelected(item);
        }
    }

    /*
    // fix for bug in LG stock ROM Android <= 4.1
    // https://code.google.com/p/android/issues/detail?id=78154
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        String manufacturer = PPApplication.getROMManufacturer();
        if ((keyCode == KeyEvent.KEYCODE_MENU) &&
            (Build.VERSION.SDK_INT <= 16) &&
            (manufacturer != null) && (manufacturer.compareTo("lge") == 0)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        String manufacturer = PPApplication.getROMManufacturer();
        if ((keyCode == KeyEvent.KEYCODE_MENU) &&
            (Build.VERSION.SDK_INT <= 16) &&
            (manufacturer != null) && (manufacturer.compareTo("lge") == 0)) {
            openOptionsMenu();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }
    */

    /////

    /*
    // ListView click listener in the navigation drawer
    private class DrawerItemClickListener implements
            ListView.OnItemClickListener {

        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // header is position=0
            if (position > 0)
                selectFilterItem(position, true, false, true);
        }
    }
    */

    private boolean selectViewItem(int item) {
//        PPApplication.logE("EditorActivity.selectViewItem", "xxx");

        //int itemId = item.getItemId();
        //if (itemId == R.id.menu_profiles_view) {
        if (item == 0) {
//            PPApplication.logE("EditorActivity.selectViewItem", "menu_profiles_view");
            final Handler handler = new Handler(getMainLooper());
            handler.postDelayed(() -> {
//                PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=EditorActivity.selectViewItem (0)");
                String[] filterItems = new String[]{
                        EditorActivity.this.getString(R.string.editor_drawer_title_profiles) + " - " + EditorActivity.this.getString(R.string.editor_drawer_list_item_profiles_all),
                        EditorActivity.this.getString(R.string.editor_drawer_title_profiles) + " - " + EditorActivity.this.getString(R.string.editor_drawer_list_item_profiles_show_in_activator),
                        EditorActivity.this.getString(R.string.editor_drawer_title_profiles) + " - " + EditorActivity.this.getString(R.string.editor_drawer_list_item_profiles_no_show_in_activator),
                };
                GlobalGUIRoutines.HighlightedSpinnerAdapter filterSpinnerAdapter = new GlobalGUIRoutines.HighlightedSpinnerAdapter(
                        EditorActivity.this,
                        R.layout.highlighted_filter_spinner,
                        filterItems);
                filterSpinnerAdapter.setDropDownViewResource(R.layout.highlighted_spinner_dropdown);
                filterSpinner.setAdapter(filterSpinnerAdapter);
//                Log.e("EditorActivity.selectViewItem (0)", "filterProfilesSelectedItem="+filterProfilesSelectedItem);
                EditorActivity.this.selectFilterItem(0, filterProfilesSelectedItem, false/*, startTargetHelps*/);
                Fragment fragment = EditorActivity.this.getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
                if (fragment instanceof EditorProfileListFragment)
                    ((EditorProfileListFragment) fragment).showHeaderAndBottomToolbar();
            }, 200);
            return true;
            //} else if (itemId == R.id.menu_events_view) {
        } else if (item == 1) {
//            PPApplication.logE("EditorActivity.selectViewItem", "menu_events_view");

            final Handler handler = new Handler(getMainLooper());
            handler.postDelayed(() -> {
//                PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=EditorActivity.selectViewItem (1)");
                String[] filterItems = new String[]{
                        EditorActivity.this.getString(R.string.editor_drawer_title_events) + " - " + EditorActivity.this.getString(R.string.editor_drawer_list_item_events_start_order),
                        EditorActivity.this.getString(R.string.editor_drawer_title_events) + " - " + EditorActivity.this.getString(R.string.editor_drawer_list_item_events_all),
                        EditorActivity.this.getString(R.string.editor_drawer_title_events) + " - " + EditorActivity.this.getString(R.string.editor_drawer_list_item_events_not_stopped),
                        EditorActivity.this.getString(R.string.editor_drawer_title_events) + " - " + EditorActivity.this.getString(R.string.editor_drawer_list_item_events_running),
                        EditorActivity.this.getString(R.string.editor_drawer_title_events) + " - " + EditorActivity.this.getString(R.string.editor_drawer_list_item_events_paused),
                        EditorActivity.this.getString(R.string.editor_drawer_title_events) + " - " + EditorActivity.this.getString(R.string.editor_drawer_list_item_events_stopped)
                };
                GlobalGUIRoutines.HighlightedSpinnerAdapter filterSpinnerAdapter = new GlobalGUIRoutines.HighlightedSpinnerAdapter(
                        EditorActivity.this,
                        R.layout.highlighted_filter_spinner,
                        filterItems);
                filterSpinnerAdapter.setDropDownViewResource(R.layout.highlighted_spinner_dropdown);
                filterSpinner.setAdapter(filterSpinnerAdapter);
//                Log.e("EditorActivity.selectViewItem (1)", "filterProfilesSelectedItem="+filterProfilesSelectedItem);
                EditorActivity.this.selectFilterItem(1, filterEventsSelectedItem, false/*, startTargetHelps*/);
                Fragment fragment = EditorActivity.this.getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
                if (fragment instanceof EditorEventListFragment) {
                    ((EditorEventListFragment) fragment).showHeaderAndBottomToolbar();
                }
            }, 200);
            return true;
        } else
            return false;
    }

    private void selectFilterItem(int selectedView, int position, boolean fromClickListener/*, boolean startTargetHelps*/) {
//        if (PPApplication.logEnabled()) {
//            PPApplication.logE("EditorActivity.selectFilterItem", "editorSelectedView=" + editorSelectedView);
//            PPApplication.logE("EditorActivity.selectFilterItem", "selectedView=" + selectedView);
//            PPApplication.logE("EditorActivity.selectFilterItem", "position=" + position);
//        }

        boolean viewChanged = false;
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
        if (fragment instanceof EditorProfileListFragment) {
            if (selectedView != 0)
                viewChanged = true;
        } else
        if (fragment instanceof EditorEventListFragment) {
            if (selectedView != 1)
                viewChanged = true;
        }
        else
            viewChanged = true;

        int filterSelectedItem;
        if (selectedView == 0) {
//            PPApplication.logE("EditorActivity.selectFilterItem", "filterProfilesSelectedItem=" + filterProfilesSelectedItem);
            if ((filterSpinner.getAdapter() == null) || (filterSpinner.getAdapter().getCount() <= filterProfilesSelectedItem))
                filterSelectedItem = 0;
            else
                filterSelectedItem = filterProfilesSelectedItem;
        }
        else {
//            PPApplication.logE("EditorActivity.selectFilterItem", "filterEventsSelectedItem=" + filterEventsSelectedItem);
            if ((filterSpinner.getAdapter() == null) || (filterSpinner.getAdapter().getCount() <= filterEventsSelectedItem))
                filterSelectedItem = 0;
            else
            filterSelectedItem = filterEventsSelectedItem;
        }

        if (viewChanged || (position != filterSelectedItem))
        {
//            PPApplication.logE("EditorActivity.selectFilterItem", "items changed");

            if (viewChanged) {
                // stop running AsyncTask
                if (fragment instanceof EditorProfileListFragment) {
                    if (((EditorProfileListFragment) fragment).isAsyncTaskPendingOrRunning()) {
                        ((EditorProfileListFragment) fragment).stopRunningAsyncTask();
                    }
                } else if (fragment instanceof EditorEventListFragment) {
                    if (((EditorEventListFragment) fragment).isAsyncTaskPendingOrRunning()) {
                        ((EditorEventListFragment) fragment).stopRunningAsyncTask();
                    }
                }
            }

            editorSelectedView = selectedView;
            if (editorSelectedView == 0) {
                if ((filterSpinner.getAdapter() == null) || (filterSpinner.getAdapter().getCount() <= position)) {
                    filterProfilesSelectedItem = 0;
                    filterSelectedItem = ApplicationPreferences.EDITOR_PROFILES_VIEW_SELECTED_ITEM_DEFAULT_VALUE;
                }
                else {
                    filterProfilesSelectedItem = position;
                    filterSelectedItem = position;
                }
            }
            else {
                if ((filterSpinner.getAdapter() == null) || (filterSpinner.getAdapter().getCount() <= position)) {
                    filterEventsSelectedItem = 0;
                    filterSelectedItem = ApplicationPreferences.EDITOR_EVENTS_VIEW_SELECTED_ITEM_DEFAULT_VALUE;
                }
                else {
                    filterEventsSelectedItem = position;
                    filterSelectedItem = position;
                }
            }
            //PPApplication.logE("EditorActivity.selectFilterItem", "filterEventsSelectedItem=" + filterEventsSelectedItem);

            // save into shared preferences
            Editor editor = ApplicationPreferences.getEditor(getApplicationContext());
            editor.putInt(ApplicationPreferences.EDITOR_SELECTED_VIEW, editorSelectedView);
            editor.putInt(ApplicationPreferences.EDITOR_PROFILES_VIEW_SELECTED_ITEM, filterProfilesSelectedItem);
            editor.putInt(ApplicationPreferences.EDITOR_EVENTS_VIEW_SELECTED_ITEM, filterEventsSelectedItem);
            editor.apply();
            ApplicationPreferences.editorSelectedView(getApplicationContext());
            ApplicationPreferences.editorProfilesViewSelectedItem(getApplicationContext());
            ApplicationPreferences.editorEventsViewSelectedItem(getApplicationContext());

            Bundle arguments;

            int profilesFilterType;
            int eventsFilterType;
            //int eventsOrderType = getEventsOrderType();

            switch (editorSelectedView) {
                case 0:
                    switch (filterProfilesSelectedItem) {
                        case DSI_PROFILES_ALL:
                            profilesFilterType = EditorProfileListFragment.FILTER_TYPE_ALL;
                            //PPApplication.logE("EditorActivity.selectFilterItem", "profilesFilterType=FILTER_TYPE_ALL");
                            if (viewChanged) {
                                fragment = new EditorProfileListFragment();
                                arguments = new Bundle();
                                arguments.putInt(EditorProfileListFragment.FILTER_TYPE_ARGUMENT, profilesFilterType);
                                //arguments.putBoolean(EditorProfileListFragment.START_TARGET_HELPS_ARGUMENT, startTargetHelps);
                                fragment.setArguments(arguments);
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.editor_list_container, fragment, "EditorProfileListFragment")
                                        .commitAllowingStateLoss();
                            }
                            else {
                                //noinspection ConstantConditions
                                EditorProfileListFragment displayedFragment = (EditorProfileListFragment)fragment;
                                displayedFragment.changeFragmentFilter(profilesFilterType/*, startTargetHelps*/);
                            }
                            break;
                        case DSI_PROFILES_SHOW_IN_ACTIVATOR:
                            profilesFilterType = EditorProfileListFragment.FILTER_TYPE_SHOW_IN_ACTIVATOR;
                            //PPApplication.logE("EditorActivity.selectFilterItem", "profilesFilterType=FILTER_TYPE_SHOW_IN_ACTIVATOR");
                            if (viewChanged) {
                                fragment = new EditorProfileListFragment();
                                arguments = new Bundle();
                                arguments.putInt(EditorProfileListFragment.FILTER_TYPE_ARGUMENT, profilesFilterType);
                                //arguments.putBoolean(EditorProfileListFragment.START_TARGET_HELPS_ARGUMENT, startTargetHelps);
                                fragment.setArguments(arguments);
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.editor_list_container, fragment, "EditorProfileListFragment")
                                        .commitAllowingStateLoss();
                            }
                            else {
                                //noinspection ConstantConditions
                                EditorProfileListFragment displayedFragment = (EditorProfileListFragment)fragment;
                                displayedFragment.changeFragmentFilter(profilesFilterType/*, startTargetHelps*/);
                            }
                            break;
                        case DSI_PROFILES_NO_SHOW_IN_ACTIVATOR:
                            profilesFilterType = EditorProfileListFragment.FILTER_TYPE_NO_SHOW_IN_ACTIVATOR;
                            //PPApplication.logE("EditorActivity.selectFilterItem", "profilesFilterType=FILTER_TYPE_NO_SHOW_IN_ACTIVATOR");
                            if (viewChanged) {
                                fragment = new EditorProfileListFragment();
                                arguments = new Bundle();
                                arguments.putInt(EditorProfileListFragment.FILTER_TYPE_ARGUMENT, profilesFilterType);
                                //arguments.putBoolean(EditorProfileListFragment.START_TARGET_HELPS_ARGUMENT, startTargetHelps);
                                fragment.setArguments(arguments);
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.editor_list_container, fragment, "EditorProfileListFragment")
                                        .commitAllowingStateLoss();
                            }
                            else {
                                //noinspection ConstantConditions
                                EditorProfileListFragment displayedFragment = (EditorProfileListFragment)fragment;
                                displayedFragment.changeFragmentFilter(profilesFilterType/*, startTargetHelps*/);
                            }
                            break;
                    }
                    break;
                case 1:
                    switch (filterEventsSelectedItem) {
                        case DSI_EVENTS_START_ORDER:
                            eventsFilterType = EditorEventListFragment.FILTER_TYPE_START_ORDER;
                            //PPApplication.logE("EditorActivity.selectFilterItem", "eventsFilterType=FILTER_TYPE_START_ORDER");
                            if (viewChanged) {
                                fragment = new EditorEventListFragment();
                                arguments = new Bundle();
                                arguments.putInt(EditorEventListFragment.FILTER_TYPE_ARGUMENT, eventsFilterType);
                                //arguments.putBoolean(EditorEventListFragment.START_TARGET_HELPS_ARGUMENT, startTargetHelps);
                                fragment.setArguments(arguments);
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.editor_list_container, fragment, "EditorEventListFragment")
                                        .commitAllowingStateLoss();
                            }
                            else {
                                //noinspection ConstantConditions
                                EditorEventListFragment displayedFragment = (EditorEventListFragment)fragment;
                                displayedFragment.changeFragmentFilter(eventsFilterType/*, startTargetHelps*/);
                            }
                            break;
                        case DSI_EVENTS_ALL:
                            eventsFilterType = EditorEventListFragment.FILTER_TYPE_ALL;
                            //PPApplication.logE("EditorActivity.selectFilterItem", "eventsFilterType=FILTER_TYPE_ALL");
                            if (viewChanged) {
                                fragment = new EditorEventListFragment();
                                arguments = new Bundle();
                                arguments.putInt(EditorEventListFragment.FILTER_TYPE_ARGUMENT, eventsFilterType);
                                //arguments.putBoolean(EditorEventListFragment.START_TARGET_HELPS_ARGUMENT, startTargetHelps);
                                fragment.setArguments(arguments);
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.editor_list_container, fragment, "EditorEventListFragment")
                                        .commitAllowingStateLoss();
                            }
                            else {
                                //noinspection ConstantConditions
                                EditorEventListFragment displayedFragment = (EditorEventListFragment)fragment;
                                displayedFragment.changeFragmentFilter(eventsFilterType/*, startTargetHelps*/);
                            }
                            break;
                        case DSI_EVENTS_NOT_STOPPED:
                            eventsFilterType = EditorEventListFragment.FILTER_TYPE_NOT_STOPPED;
                            //PPApplication.logE("EditorActivity.selectFilterItem", "eventsFilterType=FILTER_TYPE_NOT_STOPPED");
                            if (viewChanged) {
                                fragment = new EditorEventListFragment();
                                arguments = new Bundle();
                                arguments.putInt(EditorEventListFragment.FILTER_TYPE_ARGUMENT, eventsFilterType);
                                //arguments.putBoolean(EditorEventListFragment.START_TARGET_HELPS_ARGUMENT, startTargetHelps);
                                fragment.setArguments(arguments);
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.editor_list_container, fragment, "EditorEventListFragment")
                                        .commitAllowingStateLoss();
                            }
                            else {
                                //noinspection ConstantConditions
                                EditorEventListFragment displayedFragment = (EditorEventListFragment)fragment;
                                displayedFragment.changeFragmentFilter(eventsFilterType/*, startTargetHelps*/);
                            }
                            break;
                        case DSI_EVENTS_RUNNING:
                            eventsFilterType = EditorEventListFragment.FILTER_TYPE_RUNNING;
                            //PPApplication.logE("EditorActivity.selectFilterItem", "eventsFilterType=FILTER_TYPE_RUNNING");
                            if (viewChanged) {
                                fragment = new EditorEventListFragment();
                                arguments = new Bundle();
                                arguments.putInt(EditorEventListFragment.FILTER_TYPE_ARGUMENT, eventsFilterType);
                                //arguments.putBoolean(EditorEventListFragment.START_TARGET_HELPS_ARGUMENT, startTargetHelps);
                                fragment.setArguments(arguments);
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.editor_list_container, fragment, "EditorEventListFragment")
                                        .commitAllowingStateLoss();
                            }
                            else {
                                //noinspection ConstantConditions
                                EditorEventListFragment displayedFragment = (EditorEventListFragment)fragment;
                                displayedFragment.changeFragmentFilter(eventsFilterType/*, startTargetHelps*/);
                            }
                            break;
                        case DSI_EVENTS_PAUSED:
                            eventsFilterType = EditorEventListFragment.FILTER_TYPE_PAUSED;
                            //PPApplication.logE("EditorActivity.selectFilterItem", "eventsFilterType=FILTER_TYPE_PAUSED");
                            if (viewChanged) {
                                fragment = new EditorEventListFragment();
                                arguments = new Bundle();
                                arguments.putInt(EditorEventListFragment.FILTER_TYPE_ARGUMENT, eventsFilterType);
                                //arguments.putBoolean(EditorEventListFragment.START_TARGET_HELPS_ARGUMENT, startTargetHelps);
                                fragment.setArguments(arguments);
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.editor_list_container, fragment, "EditorEventListFragment")
                                        .commitAllowingStateLoss();
                            }
                            else {
                                //noinspection ConstantConditions
                                EditorEventListFragment displayedFragment = (EditorEventListFragment)fragment;
                                displayedFragment.changeFragmentFilter(eventsFilterType/*, startTargetHelps*/);
                            }
                            break;
                        case DSI_EVENTS_STOPPED:
                            eventsFilterType = EditorEventListFragment.FILTER_TYPE_STOPPED;
                            //PPApplication.logE("EditorActivity.selectFilterItem", "eventsFilterType=FILTER_TYPE_STOPPED");
                            if (viewChanged) {
                                fragment = new EditorEventListFragment();
                                arguments = new Bundle();
                                arguments.putInt(EditorEventListFragment.FILTER_TYPE_ARGUMENT, eventsFilterType);
                                //arguments.putBoolean(EditorEventListFragment.START_TARGET_HELPS_ARGUMENT, startTargetHelps);
                                fragment.setArguments(arguments);
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.editor_list_container, fragment, "EditorEventListFragment")
                                        .commitAllowingStateLoss();
                            }
                            else {
                                //noinspection ConstantConditions
                                EditorEventListFragment displayedFragment = (EditorEventListFragment)fragment;
                                displayedFragment.changeFragmentFilter(eventsFilterType/*, startTargetHelps*/);
                            }
                            break;
                    }
                    break;
            }
        }

        /*
        // header is position=0
        drawerListView.setItemChecked(drawerSelectedItem, true);
        // Get the title and icon followed by the position
        //editorToolbar.setSubtitle(drawerItemsTitle[drawerSelectedItem - 1]);
        //setIcon(drawerItemsIcon[drawerSelectedItem-1]);
        drawerHeaderFilterImage.setImageResource(drawerItemsIcon[drawerSelectedItem -1]);
        drawerHeaderFilterTitle.setText(drawerItemsTitle[drawerSelectedItem - 1]);
        */
        if (!fromClickListener)
            filterSpinner.setSelection(filterSelectedItem);

        //bottomToolbar.setVisibility(View.VISIBLE);

        // set filter status bar title
        //setStatusBarTitle();
        
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_ACTIVATE_PROFILE)
        {
            Fragment _fragment = getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
            if (_fragment instanceof EditorProfileListFragment) {
                EditorProfileListFragment fragment = (EditorProfileListFragment) getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
                if (fragment != null)
                    fragment.doOnActivityResult(requestCode, resultCode, data);
            }
        }
        else
        if (requestCode == REQUEST_CODE_PROFILE_PREFERENCES)
        {
            if ((resultCode == RESULT_OK) && (data != null))
            {
                long profile_id = data.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
                int newProfileMode = data.getIntExtra(EXTRA_NEW_PROFILE_MODE, EditorProfileListFragment.EDIT_MODE_UNDEFINED);
                //int predefinedProfileIndex = data.getIntExtra(EXTRA_PREDEFINED_PROFILE_INDEX, 0);

                if (profile_id > 0)
                {
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                    notificationManager.cancel(
                            PPApplication.DISPLAY_PREFERENCES_PROFILE_ERROR_NOTIFICATION_TAG+"_"+profile_id,
                            PPApplication.PROFILE_ID_NOTIFICATION_ID + (int) profile_id);
                    notificationManager.cancel(
                            PPApplication.GENERATED_BY_PROFILE_NOTIFICATION_TAG,
                            PPApplication.GENERATED_BY_PROFILE_NOTIFICATION_ID + (int) profile_id);
                    ActivateProfileHelper.cancelNotificationsForInteractiveParameters(getApplicationContext());

                    Profile profile = DatabaseHandler.getInstance(getApplicationContext()).getProfile(profile_id, false);
                    if (profile != null) {
                        // generate bitmaps
                        profile.generateIconBitmap(getApplicationContext(), false, 0, false);
                        profile.generatePreferencesIndicator(getApplicationContext(), false, 0, DataWrapper.IT_FOR_EDITOR, 0f);

                        // redraw list fragment , notifications, widgets after finish ProfilesPrefsActivity
                        redrawProfileListFragment(profile, newProfileMode);

                        //Profile mappedProfile = profile; //Profile.getMappedProfile(profile, getApplicationContext());
                        //Permissions.grantProfilePermissions(getApplicationContext(), profile, false, true,
                        //        /*true, false, 0,*/ PPApplication.STARTUP_SOURCE_EDITOR, false, true, false);
                        PhoneProfilesService.displayPreferencesErrorNotification(profile, null, false, getApplicationContext());
                    }
                }

                /*Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_WORKERS, true);
                PPApplication.startPPService(this, serviceIntent);*/
                Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
                //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
                commandIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_WORKERS, true);
                PPApplication.runCommand(this, commandIntent);
            }
            else
            if (data != null) {
                boolean restart = data.getBooleanExtra(PhoneProfilesPrefsActivity.EXTRA_RESET_EDITOR, false);
                if (restart) {
                    // refresh activity for special changes
                    GlobalGUIRoutines.reloadActivity(this, true);
                }
            }
        }
        else
        if (requestCode == REQUEST_CODE_EVENT_PREFERENCES)
        {
            if ((resultCode == RESULT_OK) && (data != null))
            {
                // redraw list fragment after finish EventPreferencesActivity
                long event_id = data.getLongExtra(PPApplication.EXTRA_EVENT_ID, 0L);
                int newEventMode = data.getIntExtra(EXTRA_NEW_EVENT_MODE, EditorEventListFragment.EDIT_MODE_UNDEFINED);
                //int predefinedEventIndex = data.getIntExtra(EXTRA_PREDEFINED_EVENT_INDEX, 0);

                if (event_id > 0)
                {
                    Event event = DatabaseHandler.getInstance(getApplicationContext()).getEvent(event_id);

                    // redraw list fragment , notifications, widgets after finish EventPreferencesActivity
                    redrawEventListFragment(event, newEventMode);

                    //Permissions.grantEventPermissions(getApplicationContext(), event, true, false);
                    PhoneProfilesService.displayPreferencesErrorNotification(null, event, false, getApplicationContext());
                }

                /*Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_WORKERS, true);
                PPApplication.startPPService(this, serviceIntent);*/
                Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
                //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
                commandIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_WORKERS, true);
                PPApplication.runCommand(this, commandIntent);

                //IgnoreBatteryOptimizationNotification.showNotification(getApplicationContext());
            }
            else
            if (data != null) {
                boolean restart = data.getBooleanExtra(PhoneProfilesPrefsActivity.EXTRA_RESET_EDITOR, false);
                if (restart) {
                    // refresh activity for special changes
                    GlobalGUIRoutines.reloadActivity(this, true);
                }
            }
        }
        else
        if (requestCode == REQUEST_CODE_APPLICATION_PREFERENCES)
        {
            if (resultCode == RESULT_OK)
            {
//                Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
//                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
//                serviceIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_WORKERS, true);
//                PPApplication.startPPService(this, serviceIntent);
//                Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
//                //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
//                commandIntent.putExtra(PhoneProfilesService.EXTRA_REREGISTER_RECEIVERS_AND_WORKERS, true);
//                PPApplication.runCommand(this, commandIntent);

//                if (PhoneProfilesService.getInstance() != null) {
//
//                    boolean powerSaveMode = PPApplication.isPowerSaveMode;
//                    if ((PhoneProfilesService.isLocationScannerStarted())) {
//                        PhoneProfilesService.getLocationScanner().resetLocationUpdates(powerSaveMode, true);
//                    }
//                    PhoneProfilesService.getInstance().resetListeningOrientationSensors(powerSaveMode, true);
//                    if (PhoneProfilesService.isMobileCellsScannerStarted())
//                        PhoneProfilesService.mobileCellsScanner.resetListening(powerSaveMode, true);
//
//                }

                boolean restart = data.getBooleanExtra(PhoneProfilesPrefsActivity.EXTRA_RESET_EDITOR, false);
                //PPApplication.logE("EditorActivity.onActivityResult", "restart="+restart);

                if (restart)
                {
                    // refresh activity for special changes
                    GlobalGUIRoutines.reloadActivity(this, true);
                }
            }
        }
        /*else
        if (requestCode == REQUEST_CODE_REMOTE_EXPORT)
        {
            if (resultCode == RESULT_OK)
            {
                doImportData(GlobalGUIRoutines.REMOTE_EXPORT_PATH);
            }
        }*/
        /*else
        if (requestCode == (Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_PROFILE)) {
            if (data != null) {
                long profileId = data.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
                int startupSource = data.getIntExtra(PPApplication.EXTRA_STARTUP_SOURCE, 0);
                boolean mergedProfile = data.getBooleanExtra(Permissions.EXTRA_MERGED_PROFILE, false);
                boolean activateProfile = data.getBooleanExtra(Permissions.EXTRA_ACTIVATE_PROFILE, false);

                if (activateProfile && (getDataWrapper() != null)) {
                    Profile profile = getDataWrapper().getProfileById(profileId, false, false, mergedProfile);
                    getDataWrapper().activateProfileFromMainThread(profile, mergedProfile, startupSource, this);
                }
            }
        }*/
        else
        if (requestCode == (Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_EXPORT)) {
            if (resultCode == RESULT_OK) {
                doExportData(false, false, false);
            }
        }
        else
        if (requestCode == (Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_EXPORT_AND_EMAIL)) {
            if (resultCode == RESULT_OK) {
                doExportData(true, false, false);
            }
        }
        else
        if (requestCode == (Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_IMPORT)) {
            if ((resultCode == RESULT_OK) && (data != null)) {
                boolean ok = false;
                try {
                    Intent intent;
                    if (Build.VERSION.SDK_INT >= 29) {
                        StorageManager sm = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
                        intent = sm.getPrimaryStorageVolume().createOpenDocumentTreeIntent();
                    }
                    else {
                        intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    }
                    //intent.putExtra("android.content.extra.SHOW_ADVANCED",true);
                    //intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, PPApplication.backupFolderUri);
                    //noinspection deprecation
                    startActivityForResult(intent, REQUEST_CODE_RESTORE_SETTINGS);
                    ok = true;
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
                if (!ok) {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                    dialogBuilder.setMessage(R.string.directory_tree_activity_not_found_alert);
                    //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                    dialogBuilder.setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = dialogBuilder.create();

//                            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                                @Override
//                                public void onShow(DialogInterface dialog) {
//                                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                                    if (positive != null) positive.setAllCaps(false);
//                                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                                    if (negative != null) negative.setAllCaps(false);
//                                }
//                            });

                    if (!isFinishing())
                        dialog.show();
                }
            }
        }
        else
        if (requestCode == (Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_SHARED_IMPORT)) {
            if ((resultCode == RESULT_OK) && (data != null)) {
                boolean ok = false;
                try {
                    Intent intent;
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                    intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                    intent.putExtra(Intent.EXTRA_LOCAL_ONLY, false);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setType("application/zip");
                    //noinspection deprecation
                    startActivityForResult(intent, REQUEST_CODE_RESTORE_SHARED_SETTINGS);
                    ok = true;
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
                if (!ok) {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                    dialogBuilder.setMessage(R.string.open_document_activity_not_found_alert);
                    //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                    dialogBuilder.setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = dialogBuilder.create();

//                            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                                @Override
//                                public void onShow(DialogInterface dialog) {
//                                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                                    if (positive != null) positive.setAllCaps(false);
//                                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                                    if (negative != null) negative.setAllCaps(false);
//                                }
//                            });

                    if (!isFinishing())
                        dialog.show();
                }
            }
        }
        else
        if (requestCode == (Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_EXPORT_AND_EMAIL_TO_AUTHOR)) {
            if (resultCode == RESULT_OK) {
                doExportData(true, true, false);
            }
        }
        else
        if ((requestCode == REQUEST_CODE_BACKUP_SETTINGS) || (requestCode == REQUEST_CODE_BACKUP_SETTINGS_2)) {
            if (resultCode == RESULT_OK) {
                // uri of folder
                Uri treeUri = data.getData();
                if (treeUri != null) {
                    getApplicationContext().grantUriPermission(PPApplication.PACKAGE_NAME, treeUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION/* | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION*/);
                    // persistent permissions
                    final int takeFlags = //data.getFlags() &
                            (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    getApplicationContext().getContentResolver().takePersistableUriPermission(treeUri, takeFlags);

/*                    class BackupAsyncTask extends AsyncTask<Void, Integer, Integer> {
                        DocumentFile pickedDir;
                        final Uri treeUri;
                        final Activity activity;

                        final int requestCode;
                        int ok = 1;

                        private BackupAsyncTask(int requestCode, Uri treeUri, Activity activity) {
                            this.treeUri = treeUri;
                            this.requestCode = requestCode;
                            this.activity = activity;

                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                            dialogBuilder.setMessage(R.string.backup_settings_alert_title);

                            LayoutInflater inflater = (activity.getLayoutInflater());
                            View layout = inflater.inflate(R.layout.dialog_progress_bar, null);
                            dialogBuilder.setView(layout);

                            backupProgressDialog = dialogBuilder.create();

                        }

                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();

                            pickedDir = DocumentFile.fromTreeUri(getApplicationContext(), treeUri);

                            GlobalGUIRoutines.lockScreenOrientation(activity, false);
                            backupProgressDialog.setCancelable(false);
                            backupProgressDialog.setCanceledOnTouchOutside(false);
                            if (!activity.isFinishing())
                                backupProgressDialog.show();
                        }

                        @Override
                        protected Integer doInBackground(Void... params) {
                            if (pickedDir != null) {
                                if (pickedDir.canWrite()) {
                                    if (requestCode == REQUEST_CODE_BACKUP_SETTINGS_2) {
                                        // if directory exists, create new = "PhoneProfilesPlus (x)"
                                        // create subdirectory
                                        pickedDir = pickedDir.createDirectory("PhoneProfilesPlus");
                                        if (pickedDir == null) {
                                            // error for create directory
                                            //PPApplication.logE("--------- EditorActivity.onActivityResult", "REQUEST_CODE_BACKUP_SETTINGS - error for create directory");
                                            ok = 0;
                                        }
                                    }
                                }
                                else {
                                    // pickedDir is not writable
                                    //PPApplication.logE("--------- EditorActivity.onActivityResult", "REQUEST_CODE_BACKUP_SETTINGS - pickedDir is not writable");
                                    ok = 0;
                                }

                                if (ok == 1) {
                                    if (pickedDir.canWrite()) {
                                        File applicationDir = getApplicationContext().getExternalFilesDir(null);

                                        ok = copyToBackupDirectory(pickedDir, applicationDir, PPApplication.EXPORT_APP_PREF_FILENAME, getApplicationContext());
                                        if (ok == 1)
                                            ok = copyToBackupDirectory(pickedDir, applicationDir, DatabaseHandler.EXPORT_DBFILENAME, getApplicationContext());
                                    }
                                    else {
                                        // cannot copy backup files, pickedDir is not writable
                                        //PPApplication.logE("--------- EditorActivity.onActivityResult", "REQUEST_CODE_BACKUP_SETTINGS - cannot copy backup files, pickedDir is not writable");
                                        ok = 0;
                                    }
                                }

                            }
                            else {
                                // pickedDir is null
                                //PPApplication.logE("--------- EditorActivity.onActivityResult", "REQUEST_CODE_BACKUP_SETTINGS - pickedDir is null");
                                ok = 0;
                            }

                            return ok;
                        }

                        @Override
                        protected void onPostExecute(Integer result) {
                            super.onPostExecute(result);

                            if (!isFinishing()) {
                                if ((backupProgressDialog != null) && backupProgressDialog.isShowing()) {
                                    if (!isDestroyed())
                                        backupProgressDialog.dismiss();
                                    backupProgressDialog = null;
                                }
                                GlobalGUIRoutines.unlockScreenOrientation(activity);
                            }

                            if (result == 0) {
                                //PPApplication.logE("--------- EditorActivity.onActivityResult", "REQUEST_CODE_BACKUP_SETTINGS - Error backup files");

                                if (!activity.isFinishing()) {
                                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                                    dialogBuilder.setTitle(R.string.backup_settings_alert_title);
                                    dialogBuilder.setMessage(R.string.backup_settings_error_on_backup);
                                    //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                                    dialogBuilder.setPositiveButton(android.R.string.ok, null);
                                    AlertDialog dialog = dialogBuilder.create();

                                    //        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                    //            @Override
                                    //            public void onShow(DialogInterface dialog) {
                                    //                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                                    //                if (positive != null) positive.setAllCaps(false);
                                    //                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                                    //                if (negative != null) negative.setAllCaps(false);
                                    //            }
                                    //        });

                                    dialog.show();
                                }
                            }
                            else {
                                PPApplication.showToast(getApplicationContext(), getString(R.string.backup_settings_ok_backed_up), Toast.LENGTH_SHORT);
                            }
                        }
                    }
 */

                    backupAsyncTask = new BackupAsyncTask(requestCode, treeUri, this).execute();
                }
            }
        }
        else
        if (requestCode == REQUEST_CODE_RESTORE_SETTINGS) {
            if (resultCode == RESULT_OK) {
                // uri of folder
                Uri treeUri = data.getData();
                if (treeUri != null) {
                    getApplicationContext().grantUriPermission(PPApplication.PACKAGE_NAME, treeUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION/* | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION*/);
                    // persistent permissions
                    final int takeFlags = //data.getFlags() &
                            (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    getApplicationContext().getContentResolver().takePersistableUriPermission(treeUri, takeFlags);
                    restoreAsyncTask = new RestoreAsyncTask(treeUri, false, this).execute();
                }
            }
        }
        else
        if (requestCode == REQUEST_CODE_SHARE_SETTINGS) {
            if (resultCode == RESULT_OK) {
                PPApplication.showToast(getApplicationContext(), getString(R.string.share_settings_ok_shared), Toast.LENGTH_SHORT);
            } else {
                if (!isFinishing()) {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                    dialogBuilder.setTitle(R.string.share_settings_alert_title);
                    dialogBuilder.setMessage(R.string.share_settings_error_on_share);
                    //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                    dialogBuilder.setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = dialogBuilder.create();

                    //        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    //            @Override
                    //            public void onShow(DialogInterface dialog) {
                    //                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                    //                if (positive != null) positive.setAllCaps(false);
                    //                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                    //                if (negative != null) negative.setAllCaps(false);
                    //            }
                    //        });

                    dialog.show();
                }
            }
        }
        else
        if (requestCode == REQUEST_CODE_RESTORE_SHARED_SETTINGS) {
            if (resultCode == RESULT_OK) {
                // uri of folder
                Uri fileUri = data.getData();
                if (fileUri != null) {
                    getApplicationContext().grantUriPermission(PPApplication.PACKAGE_NAME, fileUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION/* | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION*/);
                    // persistent permissions
                    final int takeFlags = //data.getFlags() &
                            (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    getApplicationContext().getContentResolver().takePersistableUriPermission(fileUri, takeFlags);

                    restoreAsyncTask = new RestoreAsyncTask(fileUri, true, this).execute();

                }
            }
        }
        else
        if (requestCode == Permissions.NOTIFICATIONS_PERMISSION_REQUEST_CODE)
        {
            PhoneProfilesService.drawProfileNotification(true, getApplicationContext());
            DrawOverAppsPermissionNotification.showNotification(getApplicationContext(), true);
            IgnoreBatteryOptimizationNotification.showNotification(getApplicationContext(), true);
        }

    }

    /*
    @Override
    public void onBackPressed()
    {
        if (drawerLayout.isDrawerOpen(drawerRoot))
            drawerLayout.closeDrawer(drawerRoot);
        else
            super.onBackPressed();
    }
    */

    /*
    @Override
    public void openOptionsMenu() {
        Configuration config = getResources().getConfiguration();
        if ((config.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) > Configuration.SCREENLAYOUT_SIZE_LARGE) {
            int originalScreenLayout = config.screenLayout;
            config.screenLayout = Configuration.SCREENLAYOUT_SIZE_LARGE;
            super.openOptionsMenu();
            config.screenLayout = originalScreenLayout;
        } else {
            super.openOptionsMenu();
        }
    }
    */

    private void importExportErrorDialog(int importExport, int dbResult, int appSettingsResult/*, int sharedProfileResult*/)
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        String title;
        if (importExport == IMPORTEXPORT_IMPORT)
            title = getString(R.string.import_profiles_alert_title);
        else
        if (importExport == IMPORTEXPORT_IMPORTFROMPP)
            title = getString(R.string.import_profiles_from_pp_alert_title);
        else
            title = getString(R.string.export_profiles_alert_title);
        dialogBuilder.setTitle(title);
        String message;
        if (importExport == IMPORTEXPORT_IMPORT) {
            message = getString(R.string.import_profiles_alert_error) + ":";
            if (dbResult != DatabaseHandler.IMPORT_OK) {
                if (dbResult == DatabaseHandler.IMPORT_ERROR_NEVER_VERSION)
                    message = message + "\n• " + getString(R.string.import_profiles_alert_error_database_newer_version);
                else
                    message = message + "\n• " + getString(R.string.import_profiles_alert_error_database_bug);
            }
            if (appSettingsResult == 0)
                message = message + "\n• " + getString(R.string.import_profiles_alert_error_appSettings_bug);
            //if (sharedProfileResult == 0)
            //    message = message + "\n• " + getString(R.string.import_profiles_alert_error_sharedProfile_bug);
        }
        else
        if (importExport == IMPORTEXPORT_IMPORTFROMPP) {
            message = getString(R.string.import_profiles_from_pp_alert_error) + ":";
            if (dbResult != DatabaseHandler.IMPORT_OK) {
                message = message + "\n• " + getString(R.string.import_profiles_from_pp_alert_error_database_bug);
            }
            if (appSettingsResult == 0)
                message = message + "\n• " + getString(R.string.import_profiles_from_pp_alert_error_appSettings_bug);
            /*if (sharedProfileResult == 0)
                message = message + "\n• " + getString(R.string.import_profiles_from_pp_alert_error_sharedProfile_bug);*/
        }
        else
            message = getString(R.string.export_profiles_alert_error);
        dialogBuilder.setMessage(message);
        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        dialogBuilder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            // refresh activity
            GlobalGUIRoutines.reloadActivity(EditorActivity.this, true);
        });
        dialogBuilder.setOnCancelListener(dialog -> {
            // refresh activity
            GlobalGUIRoutines.reloadActivity(EditorActivity.this, true);
        });
        AlertDialog dialog = dialogBuilder.create();

//        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//            @Override
//            public void onShow(DialogInterface dialog) {
//                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                if (positive != null) positive.setAllCaps(false);
//                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                if (negative != null) negative.setAllCaps(false);
//            }
//        });

        if (!isFinishing())
            dialog.show();
    }

    @SuppressLint({"SetWorldReadable", "SetWorldWritable"})
    private boolean importApplicationPreferences(File src/*, int what*/) {
        boolean res = true;
        ObjectInputStream input = null;
        try {
            try {
                if (src.exists()) {
                    try {
                        //noinspection ResultOfMethodCallIgnored
                        src.setReadable(true, false);
                    } catch (Exception ee) {
                        PPApplication.recordException(ee);
                    }
                    try {
                        //noinspection ResultOfMethodCallIgnored
                        src.setWritable(true, false);
                    } catch (Exception ee) {
                        PPApplication.recordException(ee);
                    }

                    input = new ObjectInputStream(new FileInputStream(src));
                    Editor prefEdit;
                    //if (what == 1) {
                        prefEdit = getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE).edit();
                        prefEdit.clear();
                    //}
                    //else {
                    //    prefEdit = getSharedPreferences("profile_preferences_default_profile", Activity.MODE_PRIVATE).edit();
                    //    prefEdit.clear();
                    //}
                    //noinspection unchecked
                    Map<String, ?> entries = (Map<String, ?>) input.readObject();
                    for (Entry<String, ?> entry : entries.entrySet()) {
                        Object v = entry.getValue();
                        String key = entry.getKey();

                        if (v instanceof Boolean)
                            prefEdit.putBoolean(key, (Boolean) v);
                        else if (v instanceof Float)
                            prefEdit.putFloat(key, (Float) v);
                        else if (v instanceof Integer)
                            prefEdit.putInt(key, (Integer) v);
                        else if (v instanceof Long)
                            prefEdit.putLong(key, (Long) v);
                        else if (v instanceof String)
                            prefEdit.putString(key, ((String) v));

                        //if (what == 1) {
                            if (key.equals(ApplicationPreferences.PREF_APPLICATION_THEME)) {
                                if (v.equals("light") || v.equals("material") || v.equals("color") || v.equals("dlight")) {
                                    String defaultValue = "white";
                                    if (Build.VERSION.SDK_INT >= 28)
                                        defaultValue = "night_mode";
                                    prefEdit.putString(key, defaultValue);
                                }
                            }
                            if (key.equals(ActivateProfileHelper.PREF_MERGED_RING_NOTIFICATION_VOLUMES))
                                ActivateProfileHelper.setMergedRingNotificationVolumes(getApplicationContext(), /*true,*/ prefEdit);
                            if (key.equals(ApplicationPreferences.PREF_APPLICATION_FIRST_START))
                                prefEdit.putBoolean(ApplicationPreferences.PREF_APPLICATION_FIRST_START, false);
                        //}

                    /*if (what == 2) {
                        if (key.equals(Profile.PREF_PROFILE_LOCK_DEVICE)) {
                            if (v.equals("3"))
                            prefEdit.putString(Profile.PREF_PROFILE_LOCK_DEVICE, "1");
                        }
                    }*/
                    }
                    prefEdit.apply();
                    //if (what == 1) {
                        // save version code
                        try {
                            Context appContext = getApplicationContext();
                            PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0);
                            int actualVersionCode = PPApplication.getVersionCode(pInfo);
                            PPApplication.setSavedVersionCode(appContext, actualVersionCode);
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }
                    //}

                    for (int i = 0; i < PPApplication.quickTileProfileId.length; i++)
                        ApplicationPreferences.setQuickTileProfileId(getApplicationContext(),i, 0);


                    // set application parameters to "Not used" for non-granted Uri premissions
                    ContentResolver contentResolver = getApplicationContext().getContentResolver();
                    String tone = ApplicationPreferences.getSharedPreferences(getApplicationContext()).getString(
                            ApplicationPreferences.PREF_APPLICATION_APPLICATION_INTERFACE_NOTIFICATION_SOUND,
                            ApplicationPreferences.PREF_APPLICATION_APPLICATION_INTERFACE_NOTIFICATION_SOUND_DEFAULT_VALUE);
                    if (!tone.isEmpty()) {
                        if (tone.contains("content://media/external")) {
                            boolean isGranted = false;
                            Uri uri = Uri.parse(tone);
                            if (uri != null) {
                                try {
                                    getApplicationContext().grantUriPermission(PPApplication.PACKAGE_NAME, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    isGranted = true;
                                } catch (Exception e) {
                                    //isGranted = false;
                                }
//                                Log.e("*********** EditorActivity.importApplicationPreferences", "isGranted=" + isGranted);
                            }
                            if (!isGranted) {
                                SharedPreferences.Editor editor = ApplicationPreferences.getEditor(getApplicationContext());
                                editor.putString(ApplicationPreferences.PREF_APPLICATION_APPLICATION_INTERFACE_NOTIFICATION_SOUND,
                                        ApplicationPreferences.PREF_APPLICATION_APPLICATION_INTERFACE_NOTIFICATION_SOUND_DEFAULT_VALUE);
                                editor.apply();
                            }
                        }
                    }
                    tone = ApplicationPreferences.getSharedPreferences(getApplicationContext()).getString(
                            ApplicationPreferences.PREF_APPLICATION_DEFAULT_PROFILE_NOTIFICATION_SOUND,
                            ApplicationPreferences.PREF_APPLICATION_DEFAULT_PROFILE_NOTIFICATION_SOUND_DEFAULT_VALUE);
                    if (!tone.isEmpty()) {
                        if (tone.contains("content://media/external")) {
                            boolean isGranted = false;
                            Uri uri = Uri.parse(tone);
                            if (uri != null) {
                                try {
                                    getApplicationContext().grantUriPermission(PPApplication.PACKAGE_NAME, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    isGranted = true;
                                } catch (Exception e) {
                                    //isGranted = false;
                                }
//                                Log.e("*********** EditorActivity.importApplicationPreferences", "isGranted=" + isGranted);
                            }
                            if (!isGranted) {
                                SharedPreferences.Editor editor = ApplicationPreferences.getEditor(getApplicationContext());
                                editor.putString(ApplicationPreferences.PREF_APPLICATION_DEFAULT_PROFILE_NOTIFICATION_SOUND,
                                        ApplicationPreferences.PREF_APPLICATION_DEFAULT_PROFILE_NOTIFICATION_SOUND_DEFAULT_VALUE);
                                editor.apply();
                            }
                        }
                    }

                    PPApplication.loadGlobalApplicationData(getApplicationContext());
                    PPApplication.loadApplicationPreferences(getApplicationContext());
                    PPApplication.loadProfileActivationData(getApplicationContext());
                }
                else
                    res = false;
            }/* catch (FileNotFoundException ignored) {
                // no error, this is OK
            }*/ catch (Exception e) {
                //Log.e("EditorActivity.importApplicationPreferences", Log.getStackTraceString(e));
                PPApplication.recordException(e);
                res = false;
            }
        }finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                PPApplication.recordException(e);
            }

            WifiScanWorker.setScanRequest(getApplicationContext(), false);
            WifiScanWorker.setWaitForResults(getApplicationContext(), false);
            WifiScanWorker.setWifiEnabledForScan(getApplicationContext(), false);

            BluetoothScanWorker.setScanRequest(getApplicationContext(), false);
            BluetoothScanWorker.setLEScanRequest(getApplicationContext(), false);
            BluetoothScanWorker.setWaitForResults(getApplicationContext(), false);
            BluetoothScanWorker.setWaitForLEResults(getApplicationContext(), false);
            BluetoothScanWorker.setBluetoothEnabledForScan(getApplicationContext(), false);
            BluetoothScanWorker.setScanKilled(getApplicationContext(), false);

        }
        return res;
    }

    private void doImportData(/*String applicationDataPath*/)
    {
        //final EditorActivity activity = this;
        //final String _applicationDataPath = applicationDataPath;

        importAsyncTask = new ImportAsyncTask(this).execute();
    }

    private void importData(final int titleRes, final boolean share)
    {
        AlertDialog.Builder dialogBuilder2 = new AlertDialog.Builder(this);
        dialogBuilder2.setTitle(titleRes);
        dialogBuilder2.setMessage(R.string.import_profiles_alert_message);

        dialogBuilder2.setPositiveButton(R.string.alert_button_yes, (dialog, which) -> {
            if (share) {
                if (Permissions.grantImportPermissions(true, getApplicationContext(), EditorActivity.this)) {
                    boolean ok = false;
                    try {
                        Intent intent;
                        intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, false);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        intent.setType("application/zip");
                        //noinspection deprecation
                        startActivityForResult(intent, REQUEST_CODE_RESTORE_SHARED_SETTINGS);
                        ok = true;
                    } catch (Exception e) {
                        PPApplication.recordException(e);
                    }
                    if (!ok) {
                        AlertDialog.Builder _dialogBuilder = new AlertDialog.Builder(EditorActivity.this);
                        _dialogBuilder.setMessage(R.string.open_document_activity_not_found_alert);
                        //_dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                        _dialogBuilder.setPositiveButton(android.R.string.ok, null);
                        AlertDialog _dialog = _dialogBuilder.create();

//                            _dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                                @Override
//                                public void onShow(DialogInterface dialog) {
//                                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                                    if (positive != null) positive.setAllCaps(false);
//                                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                                    if (negative != null) negative.setAllCaps(false);
//                                }
//                            });

                        if (!isFinishing())
                            _dialog.show();
                    }
                }

            } else {
                if (Permissions.grantImportPermissions(false, getApplicationContext(), EditorActivity.this)) {
                    boolean ok = false;
                    try {
                        Intent intent;
                        if (Build.VERSION.SDK_INT >= 29) {
                            StorageManager sm = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
                            intent = sm.getPrimaryStorageVolume().createOpenDocumentTreeIntent();
                        } else {
                            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        }
                        // not supported by ACTION_OPEN_DOCUMENT_TREE
                        //intent.putExtra(Intent.EXTRA_LOCAL_ONLY, false);

                        //intent.putExtra("android.content.extra.SHOW_ADVANCED",true);
                        //intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, PPApplication.backupFolderUri);
                        //noinspection deprecation
                        startActivityForResult(intent, REQUEST_CODE_RESTORE_SETTINGS);
                        ok = true;
                    } catch (Exception e) {
                        PPApplication.recordException(e);
                    }
                    if (!ok) {
                        AlertDialog.Builder _dialogBuilder = new AlertDialog.Builder(EditorActivity.this);
                        _dialogBuilder.setMessage(R.string.directory_tree_activity_not_found_alert);
                        //_dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                        _dialogBuilder.setPositiveButton(android.R.string.ok, null);
                        AlertDialog _dialog = _dialogBuilder.create();

//                            _dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                                @Override
//                                public void onShow(DialogInterface dialog) {
//                                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                                    if (positive != null) positive.setAllCaps(false);
//                                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                                    if (negative != null) negative.setAllCaps(false);
//                                }
//                            });

                        if (!isFinishing())
                            _dialog.show();
                    }
                }
            }
        });
        dialogBuilder2.setNegativeButton(R.string.alert_button_no, null);
        AlertDialog dialog = dialogBuilder2.create();

//        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//            @Override
//            public void onShow(DialogInterface dialog) {
//                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                if (positive != null) positive.setAllCaps(false);
//                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                if (negative != null) negative.setAllCaps(false);
//            }
//        });

        if (!isFinishing())
            dialog.show();
    }

    private void doImportDataFromPP(final boolean importProfiles,
                                    final boolean deleteConfiguredProfiles,
                                    final boolean importApplicationPreferences) {
        importFromPPAsyncTask = new ImportFromPPAsyncTask(
                        importProfiles,
                        deleteConfiguredProfiles,
                        importApplicationPreferences,
                        this).execute();
    }

    private void importDataFromPP() {
        AlertDialog.Builder dialogBuilder2 = new AlertDialog.Builder(this);
        dialogBuilder2.setTitle(R.string.import_profiles_from_pp_alert_title);

        LayoutInflater inflater = (getLayoutInflater());
        final View layout = inflater.inflate(R.layout.dialog_import_pp_data_alert, null);
        dialogBuilder2.setView(layout);

        dialogBuilder2.setPositiveButton(R.string.alert_button_yes, (dialog, which) -> {
            boolean importProfiles = true;
            boolean deleteConfiguredProfiles = false;
            boolean importApplicationPreferences = false;
            CheckBox checkbox = layout.findViewById(R.id.exportPPDataDataProfiles);
            if (checkbox != null) importProfiles = checkbox.isChecked();
            checkbox = layout.findViewById(R.id.exportPPDataDataDeleteConfiguredProfiles);
            if (checkbox != null) deleteConfiguredProfiles = checkbox.isChecked();
            checkbox = layout.findViewById(R.id.exportPPDataDataApplicationPreferences);
            if (checkbox != null) importApplicationPreferences = checkbox.isChecked();
            doImportDataFromPP(importProfiles, deleteConfiguredProfiles, importApplicationPreferences);
        });
        dialogBuilder2.setNegativeButton(R.string.alert_button_no, null);

        AlertDialog dialog = dialogBuilder2.create();
        dialog.setOnShowListener(dialog1 -> {
            CheckBox checkbox = layout.findViewById(R.id.exportPPDataDataProfiles);
            if (checkbox != null) checkbox.setChecked(true);
            checkbox = layout.findViewById(R.id.exportPPDataDataDeleteConfiguredProfiles);
            if (checkbox != null) checkbox.setChecked(false);
            checkbox = layout.findViewById(R.id.exportPPDataDataApplicationPreferences);
            if (checkbox != null) checkbox.setChecked(false);
        });

//        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//            @Override
//            public void onShow(DialogInterface dialog) {
//                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                if (positive != null) positive.setAllCaps(false);
//                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                if (negative != null) negative.setAllCaps(false);
//            }
//        });

        if (!isFinishing())
            dialog.show();
    }

    @SuppressLint({"ApplySharedPref", "SetWorldReadable", "SetWorldWritable"})
    private boolean exportApplicationPreferences(File dst, boolean runStopEvents/*, int what*/) {
        boolean res = true;
        ObjectOutputStream output = null;
        try {
            try {
                output = new ObjectOutputStream(new FileOutputStream(dst));
                SharedPreferences pref;
                //if (what == 1)
                    pref = getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
                //else
                //    pref = getSharedPreferences(PPApplication.SHARED_PROFILE_PREFS_NAME, Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();

                editor.putBoolean(Event.PREF_GLOBAL_EVENTS_RUN_STOP, runStopEvents);

                AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                if (audioManager != null) {
                    try {
                        editor.putInt("maximumVolume_ring", audioManager.getStreamMaxVolume(AudioManager.STREAM_RING));
                    } catch (Exception ignored) {}
                    try {
                        editor.putInt("maximumVolume_notification", audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION));
                    } catch (Exception ignored) {}
                    try {
                        editor.putInt("maximumVolume_music", audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
                    } catch (Exception ignored) {}
                    try {
                        editor.putInt("maximumVolume_alarm", audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM));
                    } catch (Exception ignored) {}
                    try {
                        editor.putInt("maximumVolume_system", audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM));
                    } catch (Exception ignored) {}
                    try {
                        editor.putInt("maximumVolume_voiceCall", audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL));
                    } catch (Exception ignored) {}
                    try {
                        editor.putInt("maximumVolume_dtmf", audioManager.getStreamMaxVolume(AudioManager.STREAM_DTMF));
                    } catch (Exception ignored) {}
                    if (Build.VERSION.SDK_INT >= 26) {
                        try {
                            editor.putInt("maximumVolume_accessibility", audioManager.getStreamMaxVolume(AudioManager.STREAM_ACCESSIBILITY));
                        } catch (Exception ignored) {}
                    }
                    try {
                        editor.putInt("maximumVolume_bluetoothSCO", audioManager.getStreamMaxVolume(ActivateProfileHelper.STREAM_BLUETOOTH_SCO));
                    } catch (Exception ignored) {}
                }

                editor.commit();
                output.writeObject(pref.getAll());

                output.flush();

                try {
                    //noinspection ResultOfMethodCallIgnored
                    dst.setReadable(true, false);
                } catch (Exception ee) {
                    PPApplication.recordException(ee);
                }
                try {
                    //noinspection ResultOfMethodCallIgnored
                    dst.setWritable(true, false);
                } catch (Exception ee) {
                    PPApplication.recordException(ee);
                }

            } catch (FileNotFoundException e) {
                PPApplication.recordException(e);
                // this is OK
            } catch (IOException e) {
                PPApplication.recordException(e);
                res = false;
            }
        } finally {
            try {
                if (output != null) {
                    output.flush();
                    output.close();
                }
            } catch (IOException e) {
                PPApplication.recordException(e);
            }
        }
        return res;
    }

    private void exportData(final int titleRes, final boolean email, final boolean toAuthor, final boolean share)
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(titleRes);

        if (email)
            dialogBuilder.setMessage(getString(R.string.export_profiles_alert_message_note));
        else
        if (share) {
            String message = getString(R.string.share_settings_alert_message) + "\n\n" +
                    getString(R.string.export_profiles_alert_message_note);
            dialogBuilder.setMessage(message);
        }
        else
            dialogBuilder.setMessage(getString(R.string.export_profiles_alert_message) + "\n\n" +
                                        getString(R.string.export_profiles_alert_message_note));
        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);

        // for share is not needed grant Storage permission
        dialogBuilder.setPositiveButton(R.string.alert_button_backup, (dialog, which) -> {
            if (email || share)
                doExportData(email, toAuthor, share);
            else
            if (Permissions.grantExportPermissions(getApplicationContext(), EditorActivity.this))
                doExportData(false, false, false);
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, null);
        AlertDialog dialog = dialogBuilder.create();

//        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//            @Override
//            public void onShow(DialogInterface dialog) {
//                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                if (positive != null) positive.setAllCaps(false);
//                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                if (negative != null) negative.setAllCaps(false);
//            }
//        });

        if (!isFinishing())
            dialog.show();
    }

    private void doExportData(final boolean email, final boolean toAuthor, final boolean share)
    {
        if (email || share || Permissions.checkExport(getApplicationContext())) {

            exportAsyncTask = new ExportAsyncTask(email, toAuthor, share, this).execute();
        }

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        //drawerToggle.syncState();
    }
 
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        savedInstanceStateChanged = true;
    }

     @Override
     public void setTitle(CharSequence title) {
         if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(title);
     }

     /*
     public void setIcon(int iconRes) {
         getSupportActionBar().setIcon(iconRes);
     }
     */

     /*
     private void setStatusBarTitle()
     {
        // set filter status bar title
        String text = drawerItemsSubtitle[drawerSelectedItem-1];
        //filterStatusBarTitle.setText(drawerItemsTitle[drawerSelectedItem - 1] + " - " + text);
        drawerHeaderFilterSubtitle.setText(text);
     }
     */

    private void startProfilePreferenceActivity(Profile profile, int editMode, int predefinedProfileIndex) {
        /*
        if (profile != null)
            PPApplication.logE("EditorActivity.startProfilePreferenceActivity", "profile._name="+profile._name);
        PPApplication.logE("EditorActivity.startProfilePreferenceActivity", "editMode="+editMode);
        PPApplication.logE("EditorActivity.startProfilePreferenceActivity", "predefinedProfileIndex="+predefinedProfileIndex);
        */

        Intent intent = new Intent(getBaseContext(), ProfilesPrefsActivity.class);
        if ((profile == null) || (editMode == EditorProfileListFragment.EDIT_MODE_INSERT))
            intent.putExtra(PPApplication.EXTRA_PROFILE_ID, 0L);
        else
            intent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
        intent.putExtra(EXTRA_NEW_PROFILE_MODE, editMode);
        intent.putExtra(EXTRA_PREDEFINED_PROFILE_INDEX, predefinedProfileIndex);
        //noinspection deprecation
        startActivityForResult(intent, REQUEST_CODE_PROFILE_PREFERENCES);
        //PPApplication.logE("EditorActivity.startProfilePreferenceActivity", "call of ProfilesPrefsActivity");
    }

    public void onStartProfilePreferences(Profile profile, int editMode, int predefinedProfileIndex/*, boolean startTargetHelps*/) {
        // In single-pane mode, simply start the profile preferences activity
        // for the profile position.
        if (((profile != null) ||
            (editMode == EditorProfileListFragment.EDIT_MODE_INSERT) ||
            (editMode == EditorProfileListFragment.EDIT_MODE_DUPLICATE))
            && (editMode != EditorProfileListFragment.EDIT_MODE_DELETE))
            startProfilePreferenceActivity(profile, editMode, predefinedProfileIndex);
    }

    void redrawProfileListFragment(final Profile profile, int newProfileMode /*int predefinedProfileIndex, boolean startTargetHelps*/) {
        // redraw list fragment, notification a widgets

        Fragment _fragment = getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
        if (_fragment instanceof EditorProfileListFragment) {
            final EditorProfileListFragment fragment = (EditorProfileListFragment) getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
            if (fragment != null) {
                // update profile, this rewrite profile in profileList
                fragment.activityDataWrapper.updateProfile(profile);

                boolean newProfile = ((newProfileMode == EditorProfileListFragment.EDIT_MODE_INSERT) ||
                        (newProfileMode == EditorProfileListFragment.EDIT_MODE_DUPLICATE));
                fragment.updateListView(profile, newProfile, false, false/*, 0*/);

                Profile activeProfile = fragment.activityDataWrapper.getActivatedProfile(true,
                        ApplicationPreferences.applicationEditorPrefIndicator);
                fragment.updateHeader(activeProfile);
//                PPApplication.logE("###### PPApplication.updateGUI", "from=EditorActivity.redrawProfileListFragment");
                PPApplication.updateGUI(true, false, fragment.activityDataWrapper.context);

                fragment.activityDataWrapper.setDynamicLauncherShortcutsFromMainThread();

                if (filterProfilesSelectedItem != 0) {
                    final EditorActivity editorActivity = this;
                    Handler handler = new Handler(getMainLooper());
                    handler.postDelayed(() -> {
//                            PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=EditorActivity.redrawProfileListFragment");
                        if (!editorActivity.isFinishing()) {
                            boolean changeFilter = false;
                            switch (filterProfilesSelectedItem) {
                                case EditorActivity.DSI_PROFILES_NO_SHOW_IN_ACTIVATOR:
                                    changeFilter = profile._showInActivator;
                                    break;
                                case EditorActivity.DSI_PROFILES_SHOW_IN_ACTIVATOR:
                                    changeFilter = !profile._showInActivator;
                                    break;
                            }
                            if (changeFilter) {
                                fragment.scrollToProfile = profile;
                                ((GlobalGUIRoutines.HighlightedSpinnerAdapter) editorActivity.filterSpinner.getAdapter())
                                        .setSelection(ApplicationPreferences.EDITOR_PROFILES_VIEW_SELECTED_ITEM_DEFAULT_VALUE);
//                                Log.e("EditorActivity.redrawProfileListFragment", "position=0");
                                editorActivity.selectFilterItem(0, ApplicationPreferences.EDITOR_PROFILES_VIEW_SELECTED_ITEM_DEFAULT_VALUE, false/*, true*/);
                            }
                            else
                                fragment.scrollToProfile = null;
                        }
                    }, 200);
                }
            }
        }
    }

    private void startEventPreferenceActivity(Event event, final int editMode, final int predefinedEventIndex) {
        /*
        if (event != null)
            PPApplication.logE("EditorActivity.startEventPreferenceActivity", "event._name="+event._name);
        PPApplication.logE("EditorActivity.startEventPreferenceActivity", "editMode="+editMode);
        PPApplication.logE("EditorActivity.startEventPreferenceActivity", "predefinedEventIndex="+predefinedEventIndex);
        */

        boolean profileExists = true;
        long startProfileId = 0;
        long endProfileId = -1;
        if ((editMode == EditorEventListFragment.EDIT_MODE_INSERT) && (predefinedEventIndex > 0)) {
            if (getDataWrapper() != null) {
                // search names of start and end profiles
                String[] profileStartNamesArray = getResources().getStringArray(R.array.addEventPredefinedStartProfilesArray);
                String[] profileEndNamesArray = getResources().getStringArray(R.array.addEventPredefinedEndProfilesArray);

                startProfileId = getDataWrapper().getProfileIdByName(profileStartNamesArray[predefinedEventIndex], true);
                if (startProfileId == 0)
                    profileExists = false;

                if (!profileEndNamesArray[predefinedEventIndex].isEmpty()) {
                    endProfileId = getDataWrapper().getProfileIdByName(profileEndNamesArray[predefinedEventIndex], true);
                    if (endProfileId == 0)
                        profileExists = false;
                }
            }
        }

        //PPApplication.logE("EditorActivity.startEventPreferenceActivity", "profileExists="+profileExists);
        if (profileExists) {
            Intent intent = new Intent(getBaseContext(), EventsPrefsActivity.class);
            if ((event == null) || (editMode == EditorEventListFragment.EDIT_MODE_INSERT))
                intent.putExtra(PPApplication.EXTRA_EVENT_ID, 0L);
            else {
                intent.putExtra(PPApplication.EXTRA_EVENT_ID, event._id);
                intent.putExtra(PPApplication.EXTRA_EVENT_STATUS, event.getStatus());
            }
            intent.putExtra(EXTRA_NEW_EVENT_MODE, editMode);
            intent.putExtra(EXTRA_PREDEFINED_EVENT_INDEX, predefinedEventIndex);
            //noinspection deprecation
            startActivityForResult(intent, REQUEST_CODE_EVENT_PREFERENCES);
            //PPApplication.logE("EditorActivity.startEventPreferenceActivity", "call of EventsPrefsActivity");
        } else {
            //PPApplication.logE("EditorActivity.startEventPreferenceActivity", "add new event");

            final long _startProfileId = startProfileId;
            final long _endProfileId = endProfileId;

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setTitle(R.string.menu_new_event);

            String startProfileName = "";
            String endProfileName = "";
            if (_startProfileId == 0) {
                // create profile
                int[] profileStartIndex = {0, 0, 0, 2, 4, 0, 5};
                startProfileName = getDataWrapper().getPredefinedProfile(profileStartIndex[predefinedEventIndex], false, getBaseContext())._name;
            }
            if (_endProfileId == 0) {
                // create profile
                int[] profileEndIndex = {0, 0, 0, 0, 0, 0, 6};
                endProfileName = getDataWrapper().getPredefinedProfile(profileEndIndex[predefinedEventIndex], false, getBaseContext())._name;
            }

            String message = "";
            if (!startProfileName.isEmpty())
                message = message + " \"" + startProfileName + "\"";
            if (!endProfileName.isEmpty()) {
                if (!message.isEmpty())
                    message = message + ",";
                message = message + " \"" + endProfileName + "\"";
            }
            message = getString(R.string.new_event_profiles_not_exists_alert_message1) + message + " " +
                    getString(R.string.new_event_profiles_not_exists_alert_message2);

            dialogBuilder.setMessage(message);

            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
            dialogBuilder.setPositiveButton(R.string.alert_button_yes, (dialog, which) -> {
                if (_startProfileId == 0) {
                    // create profile
                    int[] profileStartIndex = {0, 0, 0, 2, 4, 0, 5};
                    getDataWrapper().getPredefinedProfile(profileStartIndex[predefinedEventIndex], true, getBaseContext());
                }
                if (_endProfileId == 0) {
                    // create profile
                    int[] profileEndIndex = {0, 0, 0, 0, 0, 0, 6};
                    getDataWrapper().getPredefinedProfile(profileEndIndex[predefinedEventIndex], true, getBaseContext());
                }

                Intent intent = new Intent(getBaseContext(), EventsPrefsActivity.class);
                intent.putExtra(PPApplication.EXTRA_EVENT_ID, 0L);
                intent.putExtra(EXTRA_NEW_EVENT_MODE, editMode);
                intent.putExtra(EXTRA_PREDEFINED_EVENT_INDEX, predefinedEventIndex);
                //noinspection deprecation
                startActivityForResult(intent, REQUEST_CODE_EVENT_PREFERENCES);
            });
            dialogBuilder.setNegativeButton(R.string.alert_button_no, (dialog, which) -> {
                Intent intent = new Intent(getBaseContext(), EventsPrefsActivity.class);
                intent.putExtra(PPApplication.EXTRA_EVENT_ID, 0L);
                intent.putExtra(EXTRA_NEW_EVENT_MODE, editMode);
                intent.putExtra(EXTRA_PREDEFINED_EVENT_INDEX, predefinedEventIndex);
                //noinspection deprecation
                startActivityForResult(intent, REQUEST_CODE_EVENT_PREFERENCES);
            });
            AlertDialog dialog = dialogBuilder.create();

//            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                @Override
//                public void onShow(DialogInterface dialog) {
//                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                    if (positive != null) positive.setAllCaps(false);
//                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                    if (negative != null) negative.setAllCaps(false);
//                }
//            });

            if (!isFinishing())
                dialog.show();
        }
    }

    public void onStartEventPreferences(Event event, int editMode, int predefinedEventIndex/*, boolean startTargetHelps*/) {
        if (((event != null) ||
            (editMode == EditorEventListFragment.EDIT_MODE_INSERT) ||
            (editMode == EditorEventListFragment.EDIT_MODE_DUPLICATE))
            && (editMode != EditorEventListFragment.EDIT_MODE_DELETE))
            startEventPreferenceActivity(event, editMode, predefinedEventIndex);
    }

    void redrawEventListFragment(final Event event, int newEventMode /*int predefinedEventIndex, boolean startTargetHelps*/) {
        // redraw list fragment, notification and widgets
        Fragment _fragment = getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
        if (_fragment instanceof EditorEventListFragment) {
            final EditorEventListFragment fragment = (EditorEventListFragment) getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
            if (fragment != null) {
                // update event, this rewrite event in eventList
                fragment.activityDataWrapper.updateEvent(event);

                boolean newEvent = ((newEventMode == EditorEventListFragment.EDIT_MODE_INSERT) ||
                        (newEventMode == EditorEventListFragment.EDIT_MODE_DUPLICATE));
                fragment.updateListView(event, newEvent, false, false/*, 0*/);

                Profile activeProfile = fragment.activityDataWrapper.getActivatedProfileFromDB(true,
                        ApplicationPreferences.applicationEditorPrefIndicator);
                fragment.updateHeader(activeProfile);

                if (filterEventsSelectedItem != 0) {
                    final EditorActivity editorActivity = this;
                    Handler handler = new Handler(getMainLooper());
                    handler.postDelayed(() -> {
//                            PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=EditorActivity.redrawEventListFragment");
                        if (!editorActivity.isFinishing()) {
                            boolean changeFilter = false;
                            switch (filterEventsSelectedItem) {
                                case EditorActivity.DSI_EVENTS_NOT_STOPPED:
                                    changeFilter = event.getStatus() == Event.ESTATUS_STOP;
                                    break;
                                case EditorActivity.DSI_EVENTS_RUNNING:
                                    changeFilter = event.getStatus() != Event.ESTATUS_RUNNING;
                                    break;
                                case EditorActivity.DSI_EVENTS_PAUSED:
                                    changeFilter = event.getStatus() != Event.ESTATUS_PAUSE;
                                    break;
                                case EditorActivity.DSI_EVENTS_STOPPED:
                                    changeFilter = event.getStatus() != Event.ESTATUS_STOP;
                                    break;
                            }
                            if (changeFilter) {
                                fragment.scrollToEvent = event;
                                ((GlobalGUIRoutines.HighlightedSpinnerAdapter) editorActivity.filterSpinner.getAdapter())
                                        .setSelection(ApplicationPreferences.EDITOR_EVENTS_VIEW_SELECTED_ITEM_DEFAULT_VALUE);
//                                Log.e("EditorActivity.redrawEventListFragment", "position=0");
                                editorActivity.selectFilterItem(1, ApplicationPreferences.EDITOR_EVENTS_VIEW_SELECTED_ITEM_DEFAULT_VALUE, false/*, true*/);
                            }
                            else
                                fragment.scrollToEvent = null;
                        }
                    }, 200);
                }
            }
        }
    }

    public static ApplicationsCache getApplicationsCache()
    {
        return applicationsCache;
    }

    public static void createApplicationsCache()
    {
        if ((!savedInstanceStateChanged) || (applicationsCache == null))
        {
            if (applicationsCache != null)
                applicationsCache.clearCache(true);
            applicationsCache =  new ApplicationsCache();
        }
    }

    private DataWrapper getDataWrapper()
    {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
        if (fragment != null)
        {
            if (fragment instanceof EditorProfileListFragment)
                return ((EditorProfileListFragment)fragment).activityDataWrapper;
            else
                return ((EditorEventListFragment)fragment).activityDataWrapper;
        }
        else
            return null;
    }

    private void setEventsRunStopIndicator()
    {
        //boolean whiteTheme = ApplicationPreferences.applicationTheme(getApplicationContext(), true).equals("white");
        if (Event.getGlobalEventsRunning())
        {
            //if (ApplicationPreferences.prefEventsBlocked) {
            if (Event.getEventsBlocked(getApplicationContext())) {
                eventsRunStopIndicator.setImageResource(R.drawable.ic_traffic_light_manual_activation);
            }
            else {
                eventsRunStopIndicator.setImageResource(R.drawable.ic_traffic_light_running);
            }
        }
        else {
            eventsRunStopIndicator.setImageResource(R.drawable.ic_traffic_light_stopped);
        }
    }

    private void refreshGUI(/*final boolean refresh,*/ final boolean refreshIcons, final boolean setPosition, final long profileId, final long eventId)
    {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
                if (doImport)
                    return;

                setEventsRunStopIndicator();
                invalidateOptionsMenu();

                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
                if (fragment != null) {
                    if (fragment instanceof EditorProfileListFragment)
                        ((EditorProfileListFragment) fragment).refreshGUI(/*refresh,*/ refreshIcons, setPosition, profileId);
                    else
                        ((EditorEventListFragment) fragment).refreshGUI(/*refresh,*/ refreshIcons, setPosition, eventId);
                }
//            }
//        });
    }

    /*
    private void setWindowContentOverlayCompat() {
        if (android.os.Build.VERSION.SDK_INT >= 20) {
            // Get the content view
            View contentView = findViewById(android.R.id.content);

            // Make sure it's a valid instance of a FrameLayout
            if (contentView instanceof FrameLayout) {
                TypedValue tv = new TypedValue();

                // Get the windowContentOverlay value of the current theme
                if (getTheme().resolveAttribute(
                        android.R.attr.windowContentOverlay, tv, true)) {

                    // If it's a valid resource, set it as the foreground drawable
                    // for the content view
                    if (tv.resourceId != 0) {
                        ((FrameLayout) contentView).setForeground(
                                getResources().getDrawable(tv.resourceId));
                    }
                }
            }
        }
    }
    */

    private void showTargetHelps() {
        /*if (Build.VERSION.SDK_INT <= 19)
            // TapTarget.forToolbarMenuItem FC :-(
            // Toolbar.findViewById() returns null
            return;*/

        //startTargetHelps = true;

        boolean startTargetHelps = ApplicationPreferences.prefEditorActivityStartTargetHelps;
        //boolean startTargetHelpsProfilesFilterSpinner = ApplicationPreferences.prefEditorActivityStartTargetHelpsProfilesFilterSpinner;
        //boolean startTargetHelpsEventsFilterSpinner = ApplicationPreferences.prefEditorActivityStartTargetHelpsEventsFilterSpinner;
        boolean startTargetHelpsRunStopIndicator = ApplicationPreferences.prefEditorActivityStartTargetHelpsRunStopIndicator;
        boolean startTargetHelpsBottomNavigation = ApplicationPreferences.prefEditorActivityStartTargetHelpsBottomNavigation;

        if (startTargetHelps || //startTargetHelpsProfilesFilterSpinner || startTargetHelpsEventsFilterSpinner ||
                startTargetHelpsRunStopIndicator || startTargetHelpsBottomNavigation ||
                ApplicationPreferences.prefEditorFragmentStartTargetHelpsDefaultProfile ||
                ApplicationPreferences.prefEditorProfilesFragmentStartTargetHelps ||
                ApplicationPreferences.prefEditorProfilesAdapterStartTargetHelps ||
                ApplicationPreferences.prefEditorProfilesAdapterStartTargetHelpsOrder ||
                ApplicationPreferences.prefEditorProfilesAdapterStartTargetHelpsShowInActivator ||
                ApplicationPreferences.prefEditorEventsFragmentStartTargetHelps ||
                ApplicationPreferences.prefEditorEventsFragmentStartTargetHelpsOrderSpinner ||
                ApplicationPreferences.prefEditorEventsAdapterStartTargetHelps ||
                ApplicationPreferences.prefEditorEventsAdapterStartTargetHelpsOrder ||
                ApplicationPreferences.prefEditorEventsAdapterStartTargetHelpsStatus) {

            if (startTargetHelps || //startTargetHelpsProfilesFilterSpinner || startTargetHelpsEventsFilterSpinner ||
                    startTargetHelpsRunStopIndicator || startTargetHelpsBottomNavigation) {
                //Log.d("EditorActivity.showTargetHelps", "PREF_START_TARGET_HELPS=true");

                Editor editor = ApplicationPreferences.getEditor(getApplicationContext());
                editor.putBoolean(PREF_START_TARGET_HELPS, false);

                //if (editorSelectedView == 0)
                //    editor.putBoolean(EditorActivity.PREF_START_TARGET_HELPS_PROFILES_FILTER_SPINNER, false);
                //else
                //    editor.putBoolean(EditorActivity.PREF_START_TARGET_HELPS_EVENTS_FILTER_SPINNER, false);

                editor.putBoolean(EditorActivity.PREF_START_TARGET_HELPS_RUN_STOP_INDICATOR, false);
                editor.putBoolean(EditorActivity.PREF_START_TARGET_HELPS_BOTTOM_NAVIGATION, false);
                editor.apply();
                ApplicationPreferences.prefEditorActivityStartTargetHelps = false;

                //if (editorSelectedView == 0)
                //    ApplicationPreferences.prefEditorActivityStartTargetHelpsProfilesFilterSpinner = false;
                //else
                //    ApplicationPreferences.prefEditorActivityStartTargetHelpsEventsFilterSpinner = false;

                ApplicationPreferences.prefEditorActivityStartTargetHelpsRunStopIndicator = false;
                ApplicationPreferences.prefEditorActivityStartTargetHelpsBottomNavigation = false;

                //TypedValue tv = new TypedValue();
                //getTheme().resolveAttribute(R.attr.colorAccent, tv, true);

                //final Display display = getWindowManager().getDefaultDisplay();

                //String appTheme = ApplicationPreferences.applicationTheme(getApplicationContext(), true);
                int outerCircleColor = R.color.tabTargetHelpOuterCircleColor;
//                if (appTheme.equals("dark"))
//                    outerCircleColor = R.color.tabTargetHelpOuterCircleColor_dark;
                int targetCircleColor = R.color.tabTargetHelpTargetCircleColor;
//                if (appTheme.equals("dark"))
//                    targetCircleColor = R.color.tabTargetHelpTargetCircleColor_dark;
                int textColor = R.color.tabTargetHelpTextColor;
//                if (appTheme.equals("dark"))
//                    textColor = R.color.tabTargetHelpTextColor_dark;

                //int[] screenLocation = new int[2];
                //filterSpinner.getLocationOnScreen(screenLocation);
                //filterSpinner.getLocationInWindow(screenLocation);
                //Rect filterSpinnerTarget = new Rect(0, 0, filterSpinner.getHeight(), filterSpinner.getHeight());
                //filterSpinnerTarget.offset(screenLocation[0] + 100, screenLocation[1]);

                /*
                eventsRunStopIndicator.getLocationOnScreen(screenLocation);
                //eventsRunStopIndicator.getLocationInWindow(screenLocation);
                Rect eventRunStopIndicatorTarget = new Rect(0, 0, eventsRunStopIndicator.getHeight(), eventsRunStopIndicator.getHeight());
                eventRunStopIndicatorTarget.offset(screenLocation[0], screenLocation[1]);
                */

                final TapTargetSequence sequence = new TapTargetSequence(this);
                List<TapTarget> targets = new ArrayList<>();
                if (startTargetHelps) {

                    // do not add it again
                    //if (editorSelectedView == 0)
                    //    startTargetHelpsProfilesFilterSpinner = false;
                    //else
                    //    startTargetHelpsEventsFilterSpinner = false;

                    startTargetHelpsRunStopIndicator = false;
                    startTargetHelpsBottomNavigation = false;

                    if (Event.getGlobalEventsRunning()) {
                        /*targets.add(
                            TapTarget.forToolbarNavigationIcon(editorToolbar, getString(R.string.editor_activity_targetHelps_navigationIcon_title), getString(R.string.editor_activity_targetHelps_navigationIcon_description))
                                    .outerCircleColor(outerCircleColor)
                                    .targetCircleColor(targetCircleColor)
                                    .textColor(textColor)
                                    .tintTarget(true)
                                    .drawShadow(true)
                                    .id(1)
                        );*/
                        /*if (editorSelectedView == 0)
                            targets.add(
                                    //TapTarget.forBounds(filterSpinnerTarget, getString(R.string.editor_activity_targetHelps_filterSpinner_title), getString(R.string.editor_activity_targetHelps_filterSpinner_description))
                                    TapTarget.forView(filterSpinner, getString(R.string.editor_activity_targetHelps_filterSpinner_title), getString(R.string.editor_activity_targetHelps_filterSpinner_description))
                                            .transparentTarget(true)
                                            .outerCircleColor(outerCircleColor)
                                            .targetCircleColor(targetCircleColor)
                                            .textColor(textColor)
                                            .tintTarget(true)
                                            .drawShadow(true)
                                            .id(1)
                            );
                        else
                            targets.add(
                                    //TapTarget.forBounds(filterSpinnerTarget, getString(R.string.editor_activity_targetHelps_filterSpinner_title), getString(R.string.editor_activity_targetHelps_filterSpinner_description))
                                    TapTarget.forView(filterSpinner, getString(R.string.editor_activity_targetHelps_filterSpinner_title), getString(R.string.editor_activity_targetHelps_filterSpinner_description))
                                            .transparentTarget(true)
                                            .outerCircleColor(outerCircleColor)
                                            .targetCircleColor(targetCircleColor)
                                            .textColor(textColor)
                                            .tintTarget(true)
                                            .drawShadow(true)
                                            .id(1)
                            );
                        */
                        targets.add(
                                TapTarget.forToolbarOverflow(editorToolbar, getString(R.string.editor_activity_targetHelps_applicationMenu_title), getString(R.string.editor_activity_targetHelps_applicationMenu_description))
                                        .outerCircleColor(outerCircleColor)
                                        .targetCircleColor(targetCircleColor)
                                        .textColor(textColor)
                                        .textTypeface(Typeface.DEFAULT_BOLD)
                                        .tintTarget(true)
                                        .drawShadow(true)
                                        .id(1)
                        );

                        int id = 2;
                        try {
                            targets.add(
                                    TapTarget.forToolbarMenuItem(editorToolbar, R.id.menu_restart_events, getString(R.string.editor_activity_targetHelps_restartEvents_title), getString(R.string.editor_activity_targetHelps_restartEvents_description))
                                            .outerCircleColor(outerCircleColor)
                                            .targetCircleColor(targetCircleColor)
                                            .textColor(textColor)
                                            .textTypeface(Typeface.DEFAULT_BOLD)
                                            .tintTarget(true)
                                            .drawShadow(true)
                                            .id(id)
                            );
                            ++id;
                        } catch (Exception e) {
                            //PPApplication.recordException(e);
                        }
                        try {
                            targets.add(
                                    TapTarget.forToolbarMenuItem(editorToolbar, R.id.menu_activity_log, getString(R.string.editor_activity_targetHelps_activityLog_title), getString(R.string.editor_activity_targetHelps_activityLog_description))
                                            .outerCircleColor(outerCircleColor)
                                            .targetCircleColor(targetCircleColor)
                                            .textColor(textColor)
                                            .textTypeface(Typeface.DEFAULT_BOLD)
                                            .tintTarget(true)
                                            .drawShadow(true)
                                            .id(id)
                            );
                            ++id;
                        } catch (Exception e) {
                            //PPApplication.recordException(e);
                        }
                        try {
                            targets.add(
                                    TapTarget.forToolbarMenuItem(editorToolbar, R.id.important_info_menu, getString(R.string.editor_activity_targetHelps_importantInfoButton_title), getString(R.string.editor_activity_targetHelps_importantInfoButton_description))
                                            .outerCircleColor(outerCircleColor)
                                            .targetCircleColor(targetCircleColor)
                                            .textColor(textColor)
                                            .textTypeface(Typeface.DEFAULT_BOLD)
                                            .tintTarget(true)
                                            .drawShadow(true)
                                            .id(id)
                            );
                            ++id;
                        } catch (Exception e) {
                            //PPApplication.recordException(e);
                        }

                        targets.add(
                                TapTarget.forView(eventsRunStopIndicator, getString(R.string.editor_activity_targetHelps_trafficLightIcon_title), getString(R.string.editor_activity_targetHelps_trafficLightIcon_description))
                                        .outerCircleColor(outerCircleColor)
                                        .targetCircleColor(targetCircleColor)
                                        .textColor(textColor)
                                        .textTypeface(Typeface.DEFAULT_BOLD)
                                        .tintTarget(false)
                                        .drawShadow(true)
                                        .id(id)
                        );
                        ++id;
/*
                        targets.add(
                                TapTarget.forView(bottomNavigationView.findViewById(R.id.menu_profiles_view), getString(R.string.editor_activity_targetHelps_bottomNavigationProfiles_title),
                                        getString(R.string.editor_activity_targetHelps_bottomNavigationProfiles_description) + "\n" +
                                        getString(R.string.editor_activity_targetHelps_bottomNavigation_description_2))
                                        .outerCircleColor(outerCircleColor)
                                        .targetCircleColor(targetCircleColor)
                                        .textColor(textColor)
                                        .tintTarget(true)
                                        .drawShadow(true)
                                        .id(id)
                        );
                        ++id;
                        targets.add(
                                TapTarget.forView(bottomNavigationView.findViewById(R.id.menu_events_view), getString(R.string.editor_activity_targetHelps_bottomNavigationEvents_title),
                                        getString(R.string.editor_activity_targetHelps_bottomNavigationEvents_description) + "\n" +
                                        getString(R.string.editor_activity_targetHelps_bottomNavigation_description_2))
                                        .outerCircleColor(outerCircleColor)
                                        .targetCircleColor(targetCircleColor)
                                        .textColor(textColor)
                                        .tintTarget(true)
                                        .drawShadow(true)
                                        .id(id)
                        );
                        ++id;
 */
                        targets.add(
                                TapTarget.forView(bottomNavigationView, getString(R.string.editor_activity_targetHelps_bottomNavigation_title),
                                        getString(R.string.editor_activity_targetHelps_bottomNavigation_description))
                                        .outerCircleColor(outerCircleColor)
                                        .targetCircleColor(targetCircleColor)
                                        .textColor(textColor)
                                        .textTypeface(Typeface.DEFAULT_BOLD)
                                        .tintTarget(false)
                                        .drawShadow(true)
                                        .transparentTarget(true)
                                        .id(id)
                        );
                        ++id;


                    } else {
                        /*targets.add(
                                TapTarget.forToolbarNavigationIcon(editorToolbar, getString(R.string.editor_activity_targetHelps_navigationIcon_title), getString(R.string.editor_activity_targetHelps_navigationIcon_description))
                                        .outerCircleColor(outerCircleColor)
                                        .targetCircleColor(targetCircleColor)
                                        .textColor(textColor)
                                        .tintTarget(true)
                                        .drawShadow(true)
                                        .id(1)
                        );*/
                        /*if (editorSelectedView == 0)
                            targets.add(
                                    //TapTarget.forBounds(filterSpinnerTarget, getString(R.string.editor_activity_targetHelps_filterSpinner_title), getString(R.string.editor_activity_targetHelps_filterSpinner_description))
                                    TapTarget.forView(filterSpinner, getString(R.string.editor_activity_targetHelps_filterSpinner_title), getString(R.string.editor_activity_targetHelps_filterSpinner_description))
                                            .transparentTarget(true)
                                            .outerCircleColor(outerCircleColor)
                                            .targetCircleColor(targetCircleColor)
                                            .textColor(textColor)
                                            .tintTarget(true)
                                            .drawShadow(true)
                                            .id(1)
                            );
                        else
                            targets.add(
                                    //TapTarget.forBounds(filterSpinnerTarget, getString(R.string.editor_activity_targetHelps_filterSpinner_title), getString(R.string.editor_activity_targetHelps_filterSpinner_description))
                                    TapTarget.forView(filterSpinner, getString(R.string.editor_activity_targetHelps_filterSpinner_title), getString(R.string.editor_activity_targetHelps_filterSpinner_description))
                                            .transparentTarget(true)
                                            .outerCircleColor(outerCircleColor)
                                            .targetCircleColor(targetCircleColor)
                                            .textColor(textColor)
                                            .tintTarget(true)
                                            .drawShadow(true)
                                            .id(1)
                            );*/
                        targets.add(
                                TapTarget.forToolbarOverflow(editorToolbar, getString(R.string.editor_activity_targetHelps_applicationMenu_title), getString(R.string.editor_activity_targetHelps_applicationMenu_description))
                                        .outerCircleColor(outerCircleColor)
                                        .targetCircleColor(targetCircleColor)
                                        .textColor(textColor)
                                        .textTypeface(Typeface.DEFAULT_BOLD)
                                        .tintTarget(true)
                                        .drawShadow(true)
                                        .id(1)
                        );

                        int id = 2;
                        try {
                            targets.add(
                                    TapTarget.forToolbarMenuItem(editorToolbar, R.id.menu_run_stop_events, getString(R.string.editor_activity_targetHelps_runStopEvents_title), getString(R.string.editor_activity_targetHelps_runStopEvents_description))
                                            .outerCircleColor(outerCircleColor)
                                            .targetCircleColor(targetCircleColor)
                                            .textColor(textColor)
                                            .textTypeface(Typeface.DEFAULT_BOLD)
                                            .tintTarget(true)
                                            .drawShadow(true)
                                            .id(id)
                            );
                            ++id;
                        } catch (Exception e) {
                            //PPApplication.recordException(e);
                        }
                        try {
                            targets.add(
                                    TapTarget.forToolbarMenuItem(editorToolbar, R.id.menu_activity_log, getString(R.string.editor_activity_targetHelps_activityLog_title), getString(R.string.editor_activity_targetHelps_activityLog_description))
                                            .outerCircleColor(outerCircleColor)
                                            .targetCircleColor(targetCircleColor)
                                            .textColor(textColor)
                                            .textTypeface(Typeface.DEFAULT_BOLD)
                                            .tintTarget(true)
                                            .drawShadow(true)
                                            .id(id)
                            );
                            ++id;
                        } catch (Exception e) {
                            //PPApplication.recordException(e);
                        }
                        try {
                            targets.add(
                                    TapTarget.forToolbarMenuItem(editorToolbar, R.id.important_info, getString(R.string.editor_activity_targetHelps_importantInfoButton_title), getString(R.string.editor_activity_targetHelps_importantInfoButton_description))
                                            .outerCircleColor(outerCircleColor)
                                            .targetCircleColor(targetCircleColor)
                                            .textColor(textColor)
                                            .textTypeface(Typeface.DEFAULT_BOLD)
                                            .tintTarget(true)
                                            .drawShadow(true)
                                            .id(id)
                            );
                            ++id;
                        } catch (Exception e) {
                            //PPApplication.recordException(e);
                        }

                        targets.add(
                                TapTarget.forView(eventsRunStopIndicator, getString(R.string.editor_activity_targetHelps_trafficLightIcon_title), getString(R.string.editor_activity_targetHelps_trafficLightIcon_description))
                                        .outerCircleColor(outerCircleColor)
                                        .targetCircleColor(targetCircleColor)
                                        .textColor(textColor)
                                        .textTypeface(Typeface.DEFAULT_BOLD)
                                        .tintTarget(false)
                                        .drawShadow(true)
                                        .id(id)
                        );
                        ++id;
/*
                        targets.add(
                                TapTarget.forView(bottomNavigationView.findViewById(R.id.menu_profiles_view), getString(R.string.editor_activity_targetHelps_bottomNavigationProfiles_title),
                                        getString(R.string.editor_activity_targetHelps_bottomNavigationProfiles_description) + "\n" +
                                        getString(R.string.editor_activity_targetHelps_bottomNavigation_description_2))
                                        .outerCircleColor(outerCircleColor)
                                        .targetCircleColor(targetCircleColor)
                                        .textColor(textColor)
                                        .tintTarget(true)
                                        .drawShadow(true)
                                        .id(id)
                        );
                        ++id;
                        targets.add(
                                TapTarget.forView(bottomNavigationView.findViewById(R.id.menu_events_view), getString(R.string.editor_activity_targetHelps_bottomNavigationEvents_title),
                                        getString(R.string.editor_activity_targetHelps_bottomNavigationEvents_description) + "\n" +
                                        getString(R.string.editor_activity_targetHelps_bottomNavigation_description_2))
                                        .outerCircleColor(outerCircleColor)
                                        .targetCircleColor(targetCircleColor)
                                        .textColor(textColor)
                                        .tintTarget(true)
                                        .drawShadow(true)
                                        .id(id)
                        );
                        ++id;
*/
                        targets.add(
                                TapTarget.forView(bottomNavigationView, getString(R.string.editor_activity_targetHelps_bottomNavigation_title),
                                        getString(R.string.editor_activity_targetHelps_bottomNavigation_description))
                                        .outerCircleColor(outerCircleColor)
                                        .targetCircleColor(targetCircleColor)
                                        .textColor(textColor)
                                        .textTypeface(Typeface.DEFAULT_BOLD)
                                        .tintTarget(false)
                                        .drawShadow(true)
                                        .transparentTarget(true)
                                        .id(id)
                        );
                        ++id;

                    }
                }
                /*if (startTargetHelpsProfilesFilterSpinner) {
                    targets.add(
                            //TapTarget.forBounds(filterSpinnerTarget, getString(R.string.editor_activity_targetHelps_filterSpinner_title), getString(R.string.editor_activity_targetHelps_filterSpinner_description))
                            TapTarget.forView(filterSpinner, getString(R.string.editor_activity_targetHelps_filterSpinner_title), getString(R.string.editor_activity_targetHelps_filterSpinner_description))
                                    .transparentTarget(true)
                                    .outerCircleColor(outerCircleColor)
                                    .targetCircleColor(targetCircleColor)
                                    .textColor(textColor)
                                    .tintTarget(true)
                                    .drawShadow(true)
                                    .id(1)
                    );
                }
                if (startTargetHelpsEventsFilterSpinner) {
                    targets.add(
                            //TapTarget.forBounds(filterSpinnerTarget, getString(R.string.editor_activity_targetHelps_filterSpinner_title), getString(R.string.editor_activity_targetHelps_filterSpinner_description))
                            TapTarget.forView(filterSpinner, getString(R.string.editor_activity_targetHelps_filterSpinner_title), getString(R.string.editor_activity_targetHelps_filterSpinner_description))
                                    .transparentTarget(true)
                                    .outerCircleColor(outerCircleColor)
                                    .targetCircleColor(targetCircleColor)
                                    .textColor(textColor)
                                    .tintTarget(true)
                                    .drawShadow(true)
                                    .id(1)
                    );
                }*/
                if (startTargetHelpsRunStopIndicator) {
                    targets.add(
                            TapTarget.forView(eventsRunStopIndicator, getString(R.string.editor_activity_targetHelps_trafficLightIcon_title), getString(R.string.editor_activity_targetHelps_trafficLightIcon_description))
                                    .outerCircleColor(outerCircleColor)
                                    .targetCircleColor(targetCircleColor)
                                    .textColor(textColor)
                                    .textTypeface(Typeface.DEFAULT_BOLD)
                                    .tintTarget(false)
                                    .drawShadow(true)
                                    .id(1)
                    );
                }
                if (startTargetHelpsBottomNavigation) {
                    /*targets.add(
                            TapTarget.forView(bottomNavigationView.findViewById(R.id.menu_profiles_view), getString(R.string.editor_activity_targetHelps_bottomNavigationProfiles_title),
                                    getString(R.string.editor_activity_targetHelps_bottomNavigationProfiles_description) + "\n" +
                                    getString(R.string.editor_activity_targetHelps_bottomNavigation_description_2))
                                    .outerCircleColor(outerCircleColor)
                                    .targetCircleColor(targetCircleColor)
                                    .textColor(textColor)
                                    .tintTarget(true)
                                    .drawShadow(true)
                                    .id(1)
                    );
                    targets.add(
                            TapTarget.forView(bottomNavigationView.findViewById(R.id.menu_events_view), getString(R.string.editor_activity_targetHelps_bottomNavigationEvents_title),
                                    getString(R.string.editor_activity_targetHelps_bottomNavigationEvents_description) + "\n " +
                                    getString(R.string.editor_activity_targetHelps_bottomNavigation_description_2))
                                    .outerCircleColor(outerCircleColor)
                                    .targetCircleColor(targetCircleColor)
                                    .textColor(textColor)
                                    .tintTarget(true)
                                    .drawShadow(true)
                                    .id(2)
                    );
                    */
                    targets.add(
                            TapTarget.forView(bottomNavigationView, getString(R.string.editor_activity_targetHelps_bottomNavigation_title),
                                    getString(R.string.editor_activity_targetHelps_bottomNavigation_description))
                                    .outerCircleColor(outerCircleColor)
                                    .targetCircleColor(targetCircleColor)
                                    .textColor(textColor)
                                    .textTypeface(Typeface.DEFAULT_BOLD)
                                    .tintTarget(false)
                                    .drawShadow(true)
                                    .transparentTarget(true)
                                    .id(1)
                    );
                }

                sequence.targets(targets);

                sequence.listener(new TapTargetSequence.Listener() {
                    // This listener will tell us when interesting(tm) events happen in regards
                    // to the sequence
                    @Override
                    public void onSequenceFinish() {
                        //targetHelpsSequenceStarted = false;

                        SharedPreferences.Editor editor = ApplicationPreferences.getEditor(getApplicationContext());
                        editor.putBoolean(PREF_START_TARGET_HELPS_FINISHED, true);
                        editor.apply();
                        ApplicationPreferences.prefEditorActivityStartTargetHelpsFinished = true;

                        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
                        if (fragment != null) {
                            if (fragment instanceof EditorProfileListFragment)
                                ((EditorProfileListFragment) fragment).showTargetHelps();
                            else
                                ((EditorEventListFragment) fragment).showTargetHelps();
                        }
                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                        //Log.d("TapTargetView", "Clicked on " + lastTarget.id());
                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {
                        //targetHelpsSequenceStarted = false;
                        Editor editor = ApplicationPreferences.getEditor(getApplicationContext());
                        if (editorSelectedView == 0) {
                            editor.putBoolean(EditorProfileListFragment.PREF_START_TARGET_HELPS, false);
                            editor.putBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS, false);
                            if (filterProfilesSelectedItem == DSI_PROFILES_SHOW_IN_ACTIVATOR)
                                editor.putBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS_ORDER, false);
                            if (filterProfilesSelectedItem == DSI_PROFILES_ALL)
                                editor.putBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS_SHOW_IN_ACTIVATOR, false);

                            editor.putBoolean(EditorProfileListFragment.PREF_START_TARGET_HELPS_FINISHED, true);
                            //editor.putBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS_FINISHED, true);

                            ApplicationPreferences.prefEditorProfilesFragmentStartTargetHelps = false;
                            ApplicationPreferences.prefEditorProfilesAdapterStartTargetHelps = false;
                            if (filterProfilesSelectedItem == DSI_PROFILES_SHOW_IN_ACTIVATOR)
                                ApplicationPreferences.prefEditorProfilesAdapterStartTargetHelpsOrder = false;
                            if (filterProfilesSelectedItem == DSI_PROFILES_ALL)
                                ApplicationPreferences.prefEditorProfilesAdapterStartTargetHelpsShowInActivator = false;

                            ApplicationPreferences.prefEditorProfilesFragmentStartTargetHelpsFinished = true;
                            //ApplicationPreferences.prefEditorProfilesAdapterStartTargetHelpsFinished = true;

                        }
                        else {
                            editor.putBoolean(EditorEventListFragment.PREF_START_TARGET_HELPS, false);
                            editor.putBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS, false);
                            if (filterEventsSelectedItem == DSI_EVENTS_START_ORDER)
                                editor.putBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS_ORDER, false);
                            editor.putBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS_STATUS, false);

                            editor.putBoolean(EditorEventListFragment.PREF_START_TARGET_HELPS_FINISHED, true);
                            //editor.putBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS_FINISHED, true);

                            ApplicationPreferences.prefEditorEventsFragmentStartTargetHelps = false;
                            ApplicationPreferences.prefEditorEventsAdapterStartTargetHelps = false;
                            if (filterEventsSelectedItem == DSI_EVENTS_START_ORDER)
                                ApplicationPreferences.prefEditorEventsAdapterStartTargetHelpsOrder = false;
                            ApplicationPreferences.prefEditorEventsAdapterStartTargetHelpsStatus = false;

                            ApplicationPreferences.prefEditorEventsFragmentStartTargetHelpsFinished = true;
                            //ApplicationPreferences.prefEditorEventsAdapterStartTargetHelpsFinished = true;
                        }
                        editor.apply();
                    }
                });
                sequence.continueOnCancel(true)
                        .considerOuterCircleCanceled(true);
                //targetHelpsSequenceStarted = true;

                editor = ApplicationPreferences.getEditor(getApplicationContext());
                editor.putBoolean(PREF_START_TARGET_HELPS_FINISHED, false);
                editor.apply();
                ApplicationPreferences.prefEditorActivityStartTargetHelpsFinished = false;

                sequence.start();
            }
            else {
                //Log.d("EditorActivity.showTargetHelps", "PREF_START_TARGET_HELPS=false");
                //final Context context = getApplicationContext();
                final Handler handler = new Handler(getMainLooper());
                handler.postDelayed(() -> {
//                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=EditorActivity.showTargetHelps");

//                        PPApplication.logE("[LOCAL_BROADCAST_CALL] EditorProfileActivity.showTargetHelps", "xxx");
                    Intent intent = new Intent(PPApplication.PACKAGE_NAME + ".ShowEditorTargetHelpsBroadcastReceiver");
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                    /*if (EditorActivity.getInstance() != null) {
                        Fragment fragment = EditorActivity.getInstance().getFragmentManager().findFragmentById(R.id.editor_list_container);
                        if (fragment != null) {
                            if (fragment instanceof EditorProfileListFragment)
                                ((EditorProfileListFragment) fragment).showTargetHelps();
                            else
                                ((EditorEventListFragment) fragment).showTargetHelps();
                        }
                    }*/
                }, 500);
            }
        }
    }

    static void showDialogAboutRedText(final Profile profile, final Event event,
                                       final boolean forProfile,
                                       final boolean forActivator,
                                       final boolean forShowInActivator,
                                       final boolean forRunStopEvent,
                                       final Activity activity) {
        if (activity == null)
            return;

        String nTitle = "";
        String nText = "";

        if (profile != null) {
            nTitle = activity.getString(R.string.profile_preferences_red_texts_title);
            nText = activity.getString(R.string.profile_preferences_red_texts_text_1) + " " +
                    "\"" + profile._name + "\" " +
                    activity.getString(R.string.preferences_red_texts_text_2);
//            if (android.os.Build.VERSION.SDK_INT < 24) {
//                nTitle = activity.getString(R.string.ppp_app_name);
//                nText = activity.getString(R.string.profile_preferences_red_texts_title) + ": " +
//                        activity.getString(R.string.profile_preferences_red_texts_text_1) + " " +
//                        "\"" + profile._name + "\" " +
//                        activity.getString(R.string.preferences_red_texts_text_2);
//            }
            if (forShowInActivator)
                nText = nText + " " + activity.getString(R.string.profile_preferences_red_texts_text_3_new);
            else
                nText = nText + " " + activity.getString(R.string.profile_preferences_red_texts_text_2);

            nText = nText + "\n\n" + activity.getString(R.string.profile_preferences_red_texts_text_4);
        }

        if (event != null) {
            nTitle = activity.getString(R.string.event_preferences_red_texts_title);
            nText = activity.getString(R.string.event_preferences_red_texts_text_1) + " " +
                    "\"" + event._name + "\" " +
                    activity.getString(R.string.preferences_red_texts_text_2);
//            if (android.os.Build.VERSION.SDK_INT < 24) {
//                nTitle = activity.getString(R.string.ppp_app_name);
//                nText = activity.getString(R.string.event_preferences_red_texts_title) + ": " +
//                        activity.getString(R.string.event_preferences_red_texts_text_1) + " " +
//                        "\"" + event._name + "\" " +
//                        activity.getString(R.string.preferences_red_texts_text_2);
//            }
            if (forRunStopEvent)
                nText = nText + " " + activity.getString(R.string.event_preferences_red_texts_text_2);
            else
                nText = nText + " " + activity.getString(R.string.profile_preferences_red_texts_text_2);

            nText = nText + "\n\n" + activity.getString(R.string.event_preferences_red_texts_text_4);
        }

        if ((profile != null) || (event != null)) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
            dialogBuilder.setTitle(nTitle);
            dialogBuilder.setMessage(nText);
            if (forProfile) {
                dialogBuilder.setPositiveButton(R.string.show_dialog_about_red_text_show_profile_preferences,
                        (dialog, which) -> {
                            Intent intent;
                            if (profile != null) {
                                intent = new Intent(activity.getBaseContext(), ProfilesPrefsActivity.class);
                                if (forActivator)
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
                                intent.putExtra(EXTRA_NEW_PROFILE_MODE, EditorProfileListFragment.EDIT_MODE_EDIT);
                                intent.putExtra(EXTRA_PREDEFINED_PROFILE_INDEX, 0);
                            }
                            else {
                                intent = new Intent(activity.getBaseContext(), EditorActivity.class);
                                if (forActivator)
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_EDITOR_SHOW_IN_ACTIVATOR_FILTER);
                            }
                            activity.startActivity(intent);

                            try {
                                // close Activator
                                if (forActivator)
                                    activity.finish();
                            } catch (Exception e) {
                                PPApplication.recordException(e);
                            }
                        });
                if (forActivator) {
                    dialogBuilder.setNegativeButton(R.string.show_dialog_about_red_text_show_editor,
                            (dialog, which) -> {
                                Intent intent = new Intent(activity.getBaseContext(), EditorActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_EDITOR_SHOW_IN_ACTIVATOR_FILTER);
                                activity.startActivity(intent);

                                try {
                                    // close Activator
                                    activity.finish();
                                } catch (Exception e) {
                                    PPApplication.recordException(e);
                                }
                            });
                }
            }
            else {
                //    dialogBuilder.setPositiveButton(android.R.string.ok, null);
                dialogBuilder.setPositiveButton(R.string.show_dialog_about_red_text_show_event_preferences,
                        (dialog, which) -> {
                            Intent intent;
                            if (event != null) {
                                intent = new Intent(activity.getBaseContext(), EventsPrefsActivity.class);
                                if (forActivator)
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.putExtra(PPApplication.EXTRA_EVENT_ID, event._id);
                                intent.putExtra(EXTRA_NEW_EVENT_MODE, EditorProfileListFragment.EDIT_MODE_EDIT);
                                intent.putExtra(EXTRA_PREDEFINED_EVENT_INDEX, 0);
                            }
                            else {
                                intent = new Intent(activity.getBaseContext(), EditorActivity.class);
                                if (forActivator)
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_EDITOR_SHOW_IN_EDITOR_FILTER);
                            }
                            activity.startActivity(intent);

                            try {
                                // close Activator
                                if (forActivator)
                                    activity.finish();
                            } catch (Exception e) {
                                PPApplication.recordException(e);
                            }
                        });
                /*
                dialogBuilder.setNegativeButton(R.string.show_dialog_about_red_text_show_editor,
                        (dialog, which) -> {
                            Intent intent = new Intent(activity.getBaseContext(), EditorActivity.class);
                            if (forActivator)
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_EDITOR_SHOW_IN_EDITOR_FILTER);
                            activity.startActivity(intent);

                            try {
                                // close Activator
                                if (forActivator)
                                    activity.finish();
                            } catch (Exception e) {
                                PPApplication.recordException(e);
                            }
                        });
                 */
            }
            dialogBuilder.setCancelable(!forActivator);
            AlertDialog dialog = dialogBuilder.create();

//            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                @Override
//                public void onShow(DialogInterface dialog) {
//                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                    if (positive != null) positive.setAllCaps(false);
//                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                    if (negative != null) negative.setAllCaps(false);
//                }
//            });

            if (!activity.isFinishing())
                dialog.show();
        }
    }

    String getEmailBodyText() {
        String body;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
            body = getString(R.string.important_info_email_body_device) + " " +
                    Settings.Global.getString(getContentResolver(), Settings.Global.DEVICE_NAME) +
                    " (" + Build.MODEL + ")" + " \n";
        else {
            String manufacturer = Build.MANUFACTURER;
            String model = Build.MODEL;
            if (model.startsWith(manufacturer))
                body = getString(R.string.important_info_email_body_device) + " " + model + " \n";
            else
                body = getString(R.string.important_info_email_body_device) + " " + manufacturer + " " + model + " \n";
        }
        body = body + getString(R.string.important_info_email_body_android_version) + " " + Build.VERSION.RELEASE + " \n\n";
        return body;
    }

    private static int copyToBackupDirectory(DocumentFile pickedDir, File applicationDir, String fileName, Context context) {
        DocumentFile oldFile = pickedDir.findFile(fileName);
        if (oldFile != null) {
            // delete old file
            if (!oldFile.delete()) {
                // cannot delete existed file
                //PPApplication.logE("--------- EditorActivity.copyToBackupDirectory", "cannot delete existed file");
                return 0;
            }
        }
        // copy file
        DocumentFile newFile = pickedDir.createFile("application/x-binary", fileName);
        if (newFile != null) {
            try {
                File exportFile = new File(applicationDir, fileName);
                FileInputStream inStream = new FileInputStream(exportFile);
                OutputStream outStream = context.getContentResolver().openOutputStream(newFile.getUri());
                if (outStream != null) {
                    try {
                        byte[] buf = new byte[1024];
                        int len;
                        while ((len = inStream.read(buf)) > 0) {
                            outStream.write(buf, 0, len);
                        }
                    } finally {
                        inStream.close();
                        outStream.close();
                    }
                }
                else {
                    // cannot open fileName stream
                    //PPApplication.logE("--------- EditorActivity.copyToBackupDirectory", "cannot open fileName stream");
                    return 0;
                }
            } catch (Exception e) {
                PPApplication.recordException(e);
                //PPApplication.logE("--------- EditorActivity.copyToBackupDirectory", Log.getStackTraceString(e));
                return 0;
            }
        }
        else {
            // cannot create fileName
            //PPApplication.logE("--------- EditorActivity.copyToBackupDirectory", "cannot create fileName");
            return 0;
        }
        return 1;
    }

    private static int copyFromBackupDirectory(DocumentFile pickedDir, File applicationDir, String fileName, Context context) {
        File importFile = new File(applicationDir, fileName);
        if (importFile.exists()) {
            // delete old file
            if (!importFile.delete()) {
                // cannot delete existed file
                //PPApplication.logE("--------- EditorActivity.copyFromBackupDirectory", "cannot delete existed file");
                return 0;
            }
        }
        // copy file
        DocumentFile inputFile = pickedDir.findFile(fileName);
        if (inputFile != null) {
            try {
                FileOutputStream outStream = new FileOutputStream(importFile);
                InputStream inStream = context.getContentResolver().openInputStream(inputFile.getUri());
                if (inStream != null) {
                    try {
                        byte[] buf = new byte[1024];
                        int len;
                        while ((len = inStream.read(buf)) > 0) {
                            outStream.write(buf, 0, len);
                        }
                    } finally {
                        inStream.close();
                        outStream.close();
                    }
                }
                else {
                    // cannot open fileName stream
                    //PPApplication.logE("--------- EditorActivity.copyFromBackupDirectory", "cannot open fileName stream");
                    return 0;
                }
            } catch (Exception e) {
                PPApplication.recordException(e);
                //PPApplication.logE("--------- EditorActivity.copyFromBackupDirectory", Log.getStackTraceString(e));
                return 0;
            }
        }
        else {
            // cannot create fileName
            //PPApplication.logE("--------- EditorActivity.copyFromBackupDirectory", "cannot create fileName");
            return 0;
        }
        return 1;
    }

    private static int copySharedFile(DocumentFile pickedFile, File applicationDir, Context context) {
        // delete all zip files in local storage
        File sd = context.getApplicationContext().getExternalFilesDir(null);
        File[] oldZipFiles = sd.listFiles();
        if (oldZipFiles != null) {
            for (File f : oldZipFiles) {
                if (f.getName().startsWith(PPApplication.SHARED_EXPORT_FILENAME)) {
                    if (!f.delete())
                        return 0;
                }
            }
        }
        // copy file
        //DocumentFile inputFile = pickedFile;
        if (pickedFile != null) {
            try {
                File importFile = new File(applicationDir, PPApplication.SHARED_EXPORT_FILENAME + PPApplication.SHARED_EXPORT_FILEEXTENSION);
                FileOutputStream outStream = new FileOutputStream(importFile);
                InputStream inStream = context.getContentResolver().openInputStream(pickedFile.getUri());
                if (inStream != null) {
                    try {
                        byte[] buf = new byte[1024];
                        int len;
                        while ((len = inStream.read(buf)) > 0) {
                            outStream.write(buf, 0, len);
                        }
                    } finally {
                        inStream.close();
                        outStream.close();
                    }
                }
                else {
                    // cannot open fileName stream
                    //PPApplication.logE("--------- EditorActivity.copyFromBackupDirectory", "cannot open fileName stream");
                    return 0;
                }
            } catch (Exception e) {
                PPApplication.recordException(e);
                //PPApplication.logE("--------- EditorActivity.copyFromBackupDirectory", Log.getStackTraceString(e));
                return 0;
            }
        }
        else {
            // cannot create fileName
            //PPApplication.logE("--------- EditorActivity.copyFromBackupDirectory", "cannot create fileName");
            return 0;
        }
        return 1;
    }

    private static class BackupAsyncTask extends AsyncTask<Void, Integer, Integer> {
        DocumentFile pickedDir;
        final Uri treeUri;

        final int requestCode;
        int ok = 1;

        private final WeakReference<EditorActivity> activityWeakRef;

        public BackupAsyncTask(int requestCode, Uri treeUri, EditorActivity activity) {
            this.treeUri = treeUri;
            this.requestCode = requestCode;

            this.activityWeakRef = new WeakReference<>(activity);

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
            dialogBuilder.setMessage(R.string.backup_settings_alert_title);

            LayoutInflater inflater = (activity.getLayoutInflater());
            View layout = inflater.inflate(R.layout.dialog_progress_bar, null);
            dialogBuilder.setView(layout);

            activity.backupProgressDialog = dialogBuilder.create();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            EditorActivity activity = activityWeakRef.get();
            if (activity != null) {
                pickedDir = DocumentFile.fromTreeUri(activity.getApplicationContext(), treeUri);

                GlobalGUIRoutines.lockScreenOrientation(activity, false);
                activity.backupProgressDialog.setCancelable(false);
                activity.backupProgressDialog.setCanceledOnTouchOutside(false);
                if (!activity.isFinishing())
                    activity.backupProgressDialog.show();
            }
        }

        @Override
        protected Integer doInBackground(Void... params) {
            EditorActivity activity = activityWeakRef.get();
            if (activity != null) {
                if (pickedDir != null) {
                    if (pickedDir.canWrite()) {
                        if (requestCode == REQUEST_CODE_BACKUP_SETTINGS_2) {
                            // if directory exists, create new = "PhoneProfilesPlus (x)"
                            // create subdirectory
                            pickedDir = pickedDir.createDirectory("PhoneProfilesPlus");
                            if (pickedDir == null) {
                                // error for create directory
                                //PPApplication.logE("--------- EditorActivity.onActivityResult", "REQUEST_CODE_BACKUP_SETTINGS - error for create directory");
                                ok = 0;
                            }
                        }
                    } else {
                        // pickedDir is not writable
                        //PPApplication.logE("--------- EditorActivity.onActivityResult", "REQUEST_CODE_BACKUP_SETTINGS - pickedDir is not writable");
                        ok = 0;
                    }

                    if (ok == 1) {
                        if (pickedDir.canWrite()) {
                            File applicationDir = activity.getApplicationContext().getExternalFilesDir(null);

                            ok = copyToBackupDirectory(pickedDir, applicationDir, PPApplication.EXPORT_APP_PREF_FILENAME, activity.getApplicationContext());
                            if (ok == 1)
                                ok = copyToBackupDirectory(pickedDir, applicationDir, DatabaseHandler.EXPORT_DBFILENAME, activity.getApplicationContext());
                        } else {
                            // cannot copy backup files, pickedDir is not writable
                            //PPApplication.logE("--------- EditorActivity.onActivityResult", "REQUEST_CODE_BACKUP_SETTINGS - cannot copy backup files, pickedDir is not writable");
                            ok = 0;
                        }
                    }

                } else {
                    // pickedDir is null
                    //PPApplication.logE("--------- EditorActivity.onActivityResult", "REQUEST_CODE_BACKUP_SETTINGS - pickedDir is null");
                    ok = 0;
                }
            } else {
                ok = 0;
            }
            return ok;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            EditorActivity activity = activityWeakRef.get();
            if (activity != null) {
                if (!activity.isFinishing()) {
                    if ((activity.backupProgressDialog != null) && activity.backupProgressDialog.isShowing()) {
                        if (!activity.isDestroyed())
                            activity.backupProgressDialog.dismiss();
                        activity.backupProgressDialog = null;
                    }
                    GlobalGUIRoutines.unlockScreenOrientation(activity);
                }

                if (result == 0) {
                    //PPApplication.logE("--------- EditorActivity.onActivityResult", "REQUEST_CODE_BACKUP_SETTINGS - Error backup files");

                    if (!activity.isFinishing()) {
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                        dialogBuilder.setTitle(R.string.backup_settings_alert_title);
                        dialogBuilder.setMessage(R.string.backup_settings_error_on_backup);
                        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                        dialogBuilder.setPositiveButton(android.R.string.ok, null);
                        AlertDialog dialog = dialogBuilder.create();

                        //        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        //            @Override
                        //            public void onShow(DialogInterface dialog) {
                        //                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                        //                if (positive != null) positive.setAllCaps(false);
                        //                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                        //                if (negative != null) negative.setAllCaps(false);
                        //            }
                        //        });

                        dialog.show();
                    }
                } else {
                    PPApplication.showToast(activity.getApplicationContext(), activity.getString(R.string.backup_settings_ok_backed_up), Toast.LENGTH_SHORT);
                }
            }
        }
    }

    private static class RestoreAsyncTask extends AsyncTask<Void, Integer, Integer> {
        final boolean share;
        Uri treeUri;
        Uri fileUri;

        DocumentFile pickedDir;
        DocumentFile pickedFile;

        //int _requestCode;
        int ok = 1;

        private final WeakReference<EditorActivity> activityWeakRef;

        public RestoreAsyncTask(Uri treeFileUri, boolean share, EditorActivity activity) {
            this.share = share;
            if (share)
                this.fileUri = treeFileUri;
            else
                this.treeUri = treeFileUri;
            //this._requestCode = requestCode;

            this.activityWeakRef = new WeakReference<>(activity);

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
            if (share)
                dialogBuilder.setMessage(R.string.restore_shared_settings_alert_title);
            else
                dialogBuilder.setMessage(R.string.restore_settings_alert_title);

            LayoutInflater inflater = (activity.getLayoutInflater());
            View layout = inflater.inflate(R.layout.dialog_progress_bar, null);
            dialogBuilder.setView(layout);

            activity.restoreProgressDialog = dialogBuilder.create();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            EditorActivity activity = activityWeakRef.get();
            if (activity != null) {
                if (share) {
                    pickedFile = DocumentFile.fromSingleUri(activity.getApplicationContext(), fileUri);
                } else {
                    pickedDir = DocumentFile.fromTreeUri(activity.getApplicationContext(), treeUri);
                }

                GlobalGUIRoutines.lockScreenOrientation(activity, false);
                activity.restoreProgressDialog.setCancelable(false);
                activity.restoreProgressDialog.setCanceledOnTouchOutside(false);
                if (!activity.isFinishing())
                    activity.restoreProgressDialog.show();
            }
        }

        @Override
        protected Integer doInBackground(Void... params) {
            EditorActivity activity = activityWeakRef.get();
            if (activity != null) {
                if (share) {
                    if (pickedFile != null) {
                        if (pickedFile.canRead()) {
                            File applicationDir = activity.getApplicationContext().getExternalFilesDir(null);

                            // file name in local storage will be PPApplication.SHARED_EXPORT_FILENAME + PPApplication.SHARED_EXPORT_FILEEXTENSION
                            ok = copySharedFile(pickedFile, applicationDir, activity.getApplicationContext());

                            if (ok == 1) {
                                // delete backup files
                                File importFile = new File(applicationDir, PPApplication.EXPORT_APP_PREF_FILENAME);
                                if (importFile.exists()) {
                                    // delete old file
                                    if (!importFile.delete())
                                        ok = 0;
                                }
                                if (ok == 1) {
                                    importFile = new File(applicationDir, DatabaseHandler.EXPORT_DBFILENAME);
                                    if (importFile.exists()) {
                                        // delete old file
                                        if (!importFile.delete())
                                            ok = 0;
                                    }
                                }

                                if (ok == 1) {
                                    // unzip shared file
                                    ZipManager zipManager = new ZipManager();
                                    File zipFile = new File(applicationDir, PPApplication.SHARED_EXPORT_FILENAME + PPApplication.SHARED_EXPORT_FILEEXTENSION);
                                    String destinationDir = applicationDir.getAbsolutePath();
                                    if (!destinationDir.endsWith("/"))
                                        destinationDir = destinationDir + "/";
                                    if (!zipManager.unzip(zipFile.getAbsolutePath(), destinationDir))
                                        ok = 0;
                                }
                            }

                        } else {
                            // pickedDir is not writable
                            //PPApplication.logE("--------- EditorActivity.onActivityResult", "REQUEST_CODE_RESTORE_SETTINGS - fileDir is not readable");
                            ok = 0;
                        }
                    } else {
                        // pickedDir is null
                        //PPApplication.logE("--------- EditorActivity.onActivityResult", "REQUEST_CODE_RESTORE_SETTINGS - pickedFile is null");
                        ok = 0;
                    }
                } else {
                    if (pickedDir != null) {
                        if (pickedDir.canRead()) {
                            File applicationDir = activity.getApplicationContext().getExternalFilesDir(null);

                            ok = copyFromBackupDirectory(pickedDir, applicationDir, PPApplication.EXPORT_APP_PREF_FILENAME, activity.getApplicationContext());
                            if (ok == 1)
                                ok = copyFromBackupDirectory(pickedDir, applicationDir, DatabaseHandler.EXPORT_DBFILENAME, activity.getApplicationContext());
                        } else {
                            // pickedDir is not writable
                            //PPApplication.logE("--------- EditorActivity.onActivityResult", "REQUEST_CODE_RESTORE_SETTINGS - pickedDir is not readable");
                            ok = 0;
                        }
                    } else {
                        // pickedDir is null
                        //PPApplication.logE("--------- EditorActivity.onActivityResult", "REQUEST_CODE_RESTORE_SETTINGS - pickedDir is null");
                        ok = 0;
                    }
                }
            } else {
                ok = 0;
            }

            return ok;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            EditorActivity activity = activityWeakRef.get();
            if (activity != null) {
                if (!activity.isFinishing()) {
                    if ((activity.restoreProgressDialog != null) && activity.restoreProgressDialog.isShowing()) {
                        if (!activity.isDestroyed())
                            activity.restoreProgressDialog.dismiss();
                        activity.restoreProgressDialog = null;
                    }
                    GlobalGUIRoutines.unlockScreenOrientation(activity);
                }

                if (result == 0) {
                    //PPApplication.logE("--------- EditorActivity.onActivityResult", "REQUEST_CODE_RESTORE_SETTINGS - Error restore files");

                    if (!activity.isFinishing()) {
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                        if (share) {
                            dialogBuilder.setTitle(R.string.restore_shared_settings_alert_title);
                            dialogBuilder.setMessage(R.string.restore_shared_settings_error_on_backup);
                        } else {
                            dialogBuilder.setTitle(R.string.restore_settings_alert_title);
                            dialogBuilder.setMessage(R.string.restore_settings_error_on_backup);
                        }
                        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                        dialogBuilder.setPositiveButton(android.R.string.ok, null);
                        AlertDialog dialog = dialogBuilder.create();

                        //        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        //            @Override
                        //            public void onShow(DialogInterface dialog) {
                        //                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                        //                if (positive != null) positive.setAllCaps(false);
                        //                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                        //                if (negative != null) negative.setAllCaps(false);
                        //            }
                        //        });

                        dialog.show();
                    }
                } else {
                    if (share)
                        PPApplication.showToast(activity.getApplicationContext(), activity.getString(R.string.restore_shared_settings_ok_backed_up), Toast.LENGTH_SHORT);
                    else
                        PPApplication.showToast(activity.getApplicationContext(), activity.getString(R.string.restore_settings_ok_backed_up), Toast.LENGTH_SHORT);

                    activity.doImportData();
                }
            }
        }
    }

    private static class ImportAsyncTask extends AsyncTask<Void, Integer, Integer> {
        private final DataWrapper _dataWrapper;
        private int dbError = DatabaseHandler.IMPORT_OK;
        private boolean appSettingsError = false;
        //private boolean sharedProfileError = false;

        private final WeakReference<EditorActivity> activityWeakRef;

        public ImportAsyncTask(EditorActivity activity) {
            this.activityWeakRef = new WeakReference<>(activity);

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
            dialogBuilder.setMessage(R.string.import_profiles_alert_title);

            LayoutInflater inflater = (activity.getLayoutInflater());
            View layout = inflater.inflate(R.layout.dialog_progress_bar, null);
            dialogBuilder.setView(layout);

            activity.importProgressDialog = dialogBuilder.create();

//                    importProgressDialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                        @Override
//                        public void onShow(DialogInterface dialog) {
//                            Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                            if (positive != null) positive.setAllCaps(false);
//                            Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                            if (negative != null) negative.setAllCaps(false);
//                        }
//                    });

            _dataWrapper = activity.getDataWrapper();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            EditorActivity activity = activityWeakRef.get();
            if (activity != null) {
                doImport = true;

                GlobalGUIRoutines.lockScreenOrientation(activity, false);
                activity.importProgressDialog.setCancelable(false);
                activity.importProgressDialog.setCanceledOnTouchOutside(false);
                if (!activity.isFinishing())
                    activity.importProgressDialog.show();

                Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
                if (fragment != null) {
                    if (fragment instanceof EditorProfileListFragment)
                        ((EditorProfileListFragment) fragment).removeAdapter();
                    else
                        ((EditorEventListFragment) fragment).removeAdapter();
                }
            }
        }

        @Override
        protected Integer doInBackground(Void... params) {
            //PPApplication.logE("PPApplication.exitApp", "from EditorActivity.doImportData shutdown=false");
            EditorActivity activity = activityWeakRef.get();
            if (activity != null) {
                if (_dataWrapper != null) {
                    PPApplication.exitApp(false, _dataWrapper.context, _dataWrapper, null, false, true);

                    //File sd = Environment.getExternalStorageDirectory();
                    //File sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                    File sd = activity.getApplicationContext().getExternalFilesDir(null);

                    /*try {
                        File exportPath = new File(sd, _applicationDataPath);
                        if (exportPath.exists()) {
                            exportPath.setReadable(true, false);
                        }
                        if (exportPath.exists()) {
                            exportPath.setWritable(true, false);
                        }
                    } catch (Exception e) {
                        PPApplication.recordException(e);
                    }*/

                    // import application preferences must be first,
                    // because in DatabaseHandler.importDB is recompute of volumes in profiles
                    //File exportFile = new File(sd, _applicationDataPath + "/" + PPApplication.EXPORT_APP_PREF_FILENAME);
                    File exportFile = new File(sd, PPApplication.EXPORT_APP_PREF_FILENAME);
                    appSettingsError = !activity.importApplicationPreferences(exportFile/*, 1*/);
                    //exportFile = new File(sd, _applicationDataPath + "/" + GlobalGUIRoutines.EXPORT_DEF_PROFILE_PREF_FILENAME);
                    //exportFile = new File(sd, GlobalGUIRoutines.EXPORT_DEF_PROFILE_PREF_FILENAME);
                    //if (exportFile.exists())
                    //    sharedProfileError = !importApplicationPreferences(exportFile, 2);

                    //dbError = DatabaseHandler.getInstance(_dataWrapper.context).importDB(_applicationDataPath);
                    dbError = DatabaseHandler.getInstance(_dataWrapper.context).importDB(/*_applicationDataPath*/);
                    if (dbError == DatabaseHandler.IMPORT_OK) {
                        DatabaseHandler.getInstance(_dataWrapper.context).updateAllEventsStatus(Event.ESTATUS_RUNNING, Event.ESTATUS_PAUSE);
                        DatabaseHandler.getInstance(_dataWrapper.context).updateAllEventsSensorsPassed(EventPreferences.SENSOR_PASSED_WAITING);
                        DatabaseHandler.getInstance(_dataWrapper.context).deactivateProfile();
                        DatabaseHandler.getInstance(_dataWrapper.context).unblockAllEvents();
                        DatabaseHandler.getInstance(_dataWrapper.context).disableNotAllowedPreferences();
                        //this.dataWrapper.invalidateProfileList();
                        //this.dataWrapper.invalidateEventList();
                        //this.dataWrapper.invalidateEventTimelineList();
                        Event.setEventsBlocked(_dataWrapper.context, false);
                        DatabaseHandler.getInstance(_dataWrapper.context).unblockAllEvents();
                        Event.setForceRunEventRunning(_dataWrapper.context, false);
                    }

                    /*if (PPApplication.logEnabled()) {
                        PPApplication.logE("EditorActivity.doImportData", "dbError=" + dbError);
                        PPApplication.logE("EditorActivity.doImportData", "appSettingsError=" + appSettingsError);
                        PPApplication.logE("EditorActivity.doImportData", "sharedProfileError=" + sharedProfileError);
                    }*/

                    if (!appSettingsError) {
                        /*Editor editor = ApplicationPreferences.preferences.edit();
                        editor.putInt(EDITOR_PROFILES_VIEW_SELECTED_ITEM, 0);
                        editor.putInt(EDITOR_EVENTS_VIEW_SELECTED_ITEM, 0);
                        editor.putInt(EditorEventListFragment.SP_EDITOR_ORDER_SELECTED_ITEM, 0);
                        editor.apply();*/

                        Permissions.setAllShowRequestPermissions(_dataWrapper.context, true);

                        //WifiBluetoothScanner.setShowEnableLocationNotification(_dataWrapper.context, true, WifiBluetoothScanner.SCANNER_TYPE_WIFI);
                        //WifiBluetoothScanner.setShowEnableLocationNotification(_dataWrapper.context, true, WifiBluetoothScanner.SCANNER_TYPE_BLUETOOTH);
                        //MobileCellsScanner.setShowEnableLocationNotification(_dataWrapper.context, true);
                    }

                    if ((dbError == DatabaseHandler.IMPORT_OK) && (!(appSettingsError/* || sharedProfileError*/)))
                        return 1;
                    else
                        return 0;
                } else
                    return 0;
            }
            else
                return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            EditorActivity activity = activityWeakRef.get();
            if (activity != null) {
                doImport = false;

                if (!activity.isFinishing()) {
                    if ((activity.importProgressDialog != null) && activity.importProgressDialog.isShowing()) {
                        if (!activity.isDestroyed())
                            activity.importProgressDialog.dismiss();
                        activity.importProgressDialog = null;
                    }
                    GlobalGUIRoutines.unlockScreenOrientation(activity);
                }

                if (_dataWrapper != null) {
                    //PPApplication.logE("DataWrapper.updateNotificationAndWidgets", "from EditorActivity.doImportData");

                    // clear shared preferences for last activated profile
                    //Profile profile = DataWrapper.getNonInitializedProfile("", null, 0);
                    //Profile.saveProfileToSharedPreferences(profile, _dataWrapper.context);
                    PPApplication.setLastActivatedProfile(_dataWrapper.context, 0);

//                    PPApplication.logE("###### PPApplication.updateGUI", "from=EditorActivity.doImportData");
                    PPApplication.updateGUI(true, false, _dataWrapper.context);

                    PPApplication.setApplicationStarted(_dataWrapper.context, true);
                    Intent serviceIntent = new Intent(_dataWrapper.context, PhoneProfilesService.class);
                    //serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
                    //serviceIntent.putExtra(PhoneProfilesService.EXTRA_DEACTIVATE_PROFILE, true);
                    serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, true);
                    serviceIntent.putExtra(PPApplication.EXTRA_APPLICATION_START, true);
                    serviceIntent.putExtra(PPApplication.EXTRA_DEVICE_BOOT, false);
                    serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_PACKAGE_REPLACE, false);
//                    PPApplication.logE("[START_PP_SERVICE] EditorProfileActivity.doImportData", "xxx");
                    PPApplication.startPPService(activity, serviceIntent);
                }

                if ((_dataWrapper != null) && (dbError == DatabaseHandler.IMPORT_OK) && (!(appSettingsError/* || sharedProfileError*/))) {
                    //PPApplication.logE("EditorActivity.doImportData", "restore is ok");

                    // restart events
                    //if (Event.getGlobalEventsRunning(this.dataWrapper.context)) {
                    //    this.dataWrapper.restartEventsWithDelay(3, false, false, DatabaseHandler.ALTYPE_UNDEFINED);
                    //}

                    PPApplication.addActivityLog(_dataWrapper.context, PPApplication.ALTYPE_DATA_IMPORT, null, null, "");

                    // toast notification
                    if (!activity.isFinishing())
                        PPApplication.showToast(_dataWrapper.context.getApplicationContext(),
                                activity.getString(R.string.toast_import_ok),
                                Toast.LENGTH_SHORT);

                    // refresh activity
                    if (!activity.isFinishing())
                        GlobalGUIRoutines.reloadActivity(activity, true);

                    DrawOverAppsPermissionNotification.showNotification(_dataWrapper.context, true);
                    IgnoreBatteryOptimizationNotification.showNotification(_dataWrapper.context, true);

                    PPApplication.setCustomKey(PPApplication.CRASHLYTICS_LOG_RESTORE_BACKUP_OK, true);
                } else {
                    //PPApplication.logE("EditorActivity.doImportData", "error restore");

                    int appSettingsResult = 1;
                    if (appSettingsError) appSettingsResult = 0;
                    //int sharedProfileResult = 1;
                    //if (sharedProfileError) sharedProfileResult = 0;
                    if (!activity.isFinishing())
                        activity.importExportErrorDialog(IMPORTEXPORT_IMPORT, dbError, appSettingsResult/*, sharedProfileResult*/);

                    PPApplication.setCustomKey(PPApplication.CRASHLYTICS_LOG_RESTORE_BACKUP_OK, false);
                }
            }
        }
    }

    private static class ImportFromPPAsyncTask extends AsyncTask<Void, Integer, Integer> {
        private final DataWrapper _dataWrapper;
        private boolean profilesError = true;
        private boolean shortcutsError = true;
        private boolean intentsError = true;
        private boolean appSettingsError = true;
        private boolean deleteProfilesError = true;
        private ImportPPDataBroadcastReceiver importPPDataBroadcastReceiver = null;

        private final WeakReference<EditorActivity> activityWeakRef;
        final boolean importProfiles;
        final boolean deleteConfiguredProfiles;
        final boolean importApplicationPreferences;

        public ImportFromPPAsyncTask(
                final boolean importProfiles,
                final boolean deleteConfiguredProfiles,
                final boolean importApplicationPreferences,
                EditorActivity activity) {
            this.activityWeakRef = new WeakReference<>(activity);
            this.importProfiles = importProfiles;
            this.deleteConfiguredProfiles = deleteConfiguredProfiles;
            this.importApplicationPreferences = importApplicationPreferences;

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
            dialogBuilder.setMessage(R.string.import_profiles_from_pp_alert_title);

            LayoutInflater inflater = (activity.getLayoutInflater());
            View layout = inflater.inflate(R.layout.dialog_progress_bar, null);
            dialogBuilder.setView(layout);

            dialogBuilder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                // send stop into PP
                Intent intent = new Intent(PPApplication.ACTION_EXPORT_PP_DATA_STOP_FROM_PPP);
                activity.sendBroadcast(intent, PPApplication.EXPORT_PP_DATA_PERMISSION);
                importFromPPStopped = true;
            });

            activity.importProgressDialog = dialogBuilder.create();

//                    importProgressDialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                        @Override
//                        public void onShow(DialogInterface dialog) {
//                            Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                            if (positive != null) positive.setAllCaps(false);
//                            Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                            if (negative != null) negative.setAllCaps(false);
//                        }
//                    });

            _dataWrapper = activity.getDataWrapper();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            EditorActivity activity = activityWeakRef.get();
            if (activity != null) {
                doImport = true;

                importPPDataBroadcastReceiver = new ImportPPDataBroadcastReceiver();
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(PPApplication.ACTION_EXPORT_PP_DATA_STOP_FROM_PP);
                intentFilter.addAction(PPApplication.ACTION_EXPORT_PP_DATA_STARTED);
                intentFilter.addAction(PPApplication.ACTION_EXPORT_PP_DATA_ENDED);
                intentFilter.addAction(PPApplication.ACTION_EXPORT_PP_DATA_APPLICATION_PREFERENCES);
                //intentFilter.addAction(PPApplication.ACTION_EXPORT_PP_DATA_PROFILES_COUNT);
                intentFilter.addAction(PPApplication.ACTION_EXPORT_PP_DATA_PROFILES);
                //intentFilter.addAction(PPApplication.ACTION_EXPORT_PP_DATA_SHORTCUTS_COUNT);
                intentFilter.addAction(PPApplication.ACTION_EXPORT_PP_DATA_SHORTCUTS);
                //intentFilter.addAction(PPApplication.ACTION_EXPORT_PP_DATA_INTENTS_COUNT);
                intentFilter.addAction(PPApplication.ACTION_EXPORT_PP_DATA_INTENTS);
                activity.registerReceiver(importPPDataBroadcastReceiver, intentFilter,
                        PPApplication.EXPORT_PP_DATA_PERMISSION, null);


                GlobalGUIRoutines.lockScreenOrientation(activity, false);
                activity.importProgressDialog.setCancelable(false);
                activity.importProgressDialog.setCanceledOnTouchOutside(false);

                if (!activity.isFinishing())
                    activity.importProgressDialog.show();

                Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id.editor_list_container);
                if (fragment != null) {
                    if (fragment instanceof EditorProfileListFragment)
                        ((EditorProfileListFragment) fragment).removeAdapter();
                    else
                        ((EditorEventListFragment) fragment).removeAdapter();
                }
            }
        }

        @SuppressWarnings("StringConcatenationInLoop")
        @Override
        protected Integer doInBackground(Void... params) {
            //PPApplication.logE("PPApplication.exitApp", "from EditorActivity.doImportData shutdown=false");

            EditorActivity activity = activityWeakRef.get();
            if (activity != null) {
                if (_dataWrapper != null) {

                    // send start into PP
                    importFromPPStopped = false;
                    Intent intent = new Intent(PPApplication.ACTION_EXPORT_PP_DATA_START_FROM_PPP);
                    activity.sendBroadcast(intent, PPApplication.EXPORT_PP_DATA_PERMISSION);

                    // get PP data
                    long start = SystemClock.uptimeMillis();
                    do {
                        if (importFromPPStopped)
                            break;

                        if (importPPDataBroadcastReceiver.importStarted &&
                                importPPDataBroadcastReceiver.importEndeed)
                            break;

                        PPApplication.sleep(200);
                    } while (SystemClock.uptimeMillis() - start < 30 * 1000);

                    if (!importFromPPStopped) {
                        PPApplication.exitApp(false, _dataWrapper.context, _dataWrapper, null, false, true);

                        if (importPPDataBroadcastReceiver.importStarted &&
                                importPPDataBroadcastReceiver.importEndeed) {

                            // import application preferences must be first,
                            // because in DatabaseHandler.importDB is recompute of volumes in profiles
                            if (importApplicationPreferences) {
                                try {
                                    synchronized (PPApplication.applicationPreferencesMutex) {
                                        ApplicationPreferences.applicationStartOnBoot = importPPDataBroadcastReceiver.applicationData.applicationStartOnBoot;
                                        ApplicationPreferences.applicationActivate = importPPDataBroadcastReceiver.applicationData.applicationActivate;
                                        ApplicationPreferences.applicationActivateWithAlert = importPPDataBroadcastReceiver.applicationData.applicationActivateWithAlert;
                                        ApplicationPreferences.applicationClose = importPPDataBroadcastReceiver.applicationData.applicationClose;
                                        ApplicationPreferences.applicationLongClickActivation = importPPDataBroadcastReceiver.applicationData.applicationLongClickActivation;
                                        ApplicationPreferences.applicationTheme = importPPDataBroadcastReceiver.applicationData.applicationTheme;
                                        ApplicationPreferences.applicationEditorPrefIndicator = importPPDataBroadcastReceiver.applicationData.applicationEditorPrefIndicator;
                                        ApplicationPreferences.notificationsToast = importPPDataBroadcastReceiver.applicationData.notificationsToast;
                                        ApplicationPreferences.notificationStatusBarStyle = importPPDataBroadcastReceiver.applicationData.notificationStatusBarStyle;
                                        ApplicationPreferences.notificationShowInStatusBar = importPPDataBroadcastReceiver.applicationData.notificationShowInStatusBar;
                                        ApplicationPreferences.notificationTextColor = importPPDataBroadcastReceiver.applicationData.notificationTextColor;
                                        ApplicationPreferences.notificationHideInLockScreen = importPPDataBroadcastReceiver.applicationData.notificationHideInLockscreen;
                                        ApplicationPreferences.applicationWidgetListPrefIndicator = importPPDataBroadcastReceiver.applicationData.applicationWidgetListPrefIndicator;
                                        ApplicationPreferences.applicationWidgetListHeader = importPPDataBroadcastReceiver.applicationData.applicationWidgetListHeader;
                                        ApplicationPreferences.applicationWidgetListBackground = importPPDataBroadcastReceiver.applicationData.applicationWidgetListBackground;
                                        ApplicationPreferences.applicationWidgetListLightnessB = importPPDataBroadcastReceiver.applicationData.applicationWidgetListLightnessB;
                                        ApplicationPreferences.applicationWidgetListLightnessT = importPPDataBroadcastReceiver.applicationData.applicationWidgetListLightnessT;
                                        ApplicationPreferences.applicationWidgetIconColor = importPPDataBroadcastReceiver.applicationData.applicationWidgetIconColor;
                                        ApplicationPreferences.applicationWidgetIconLightness = importPPDataBroadcastReceiver.applicationData.applicationWidgetIconLightness;
                                        ApplicationPreferences.applicationWidgetListIconColor = importPPDataBroadcastReceiver.applicationData.applicationWidgetListIconColor;
                                        ApplicationPreferences.applicationWidgetListIconLightness = importPPDataBroadcastReceiver.applicationData.applicationWidgetListIconLightness;
                                        ApplicationPreferences.notificationPrefIndicator = importPPDataBroadcastReceiver.applicationData.notificationPrefIndicator;
                                        ApplicationPreferences.applicationActivatorGridLayout = importPPDataBroadcastReceiver.applicationData.applicationActivatorGridLayout;
                                        ApplicationPreferences.applicationWidgetListGridLayout = importPPDataBroadcastReceiver.applicationData.applicationWidgetListGridLayout;
                                        ApplicationPreferences.applicationWidgetIconHideProfileName = importPPDataBroadcastReceiver.applicationData.applicationWidgetIconHideProfileName;
                                        ApplicationPreferences.applicationShortcutEmblem = importPPDataBroadcastReceiver.applicationData.applicationShortcutEmblem;
                                        ApplicationPreferences.applicationWidgetIconBackground = importPPDataBroadcastReceiver.applicationData.applicationWidgetIconBackground;
                                        ApplicationPreferences.applicationWidgetIconLightnessB = importPPDataBroadcastReceiver.applicationData.applicationWidgetIconLightnessB;
                                        ApplicationPreferences.applicationWidgetIconLightnessT = importPPDataBroadcastReceiver.applicationData.applicationWidgetIconLightnessT;
                                        ApplicationPreferences.applicationUnlinkRingerNotificationVolumes = importPPDataBroadcastReceiver.applicationData.applicationUnlinkRingerNotificationVolumes;
                                        ApplicationPreferences.applicationForceSetMergeRingNotificationVolumes = importPPDataBroadcastReceiver.applicationData.applicationForceSetMergeRingNotificationVolumes;
                                        ApplicationPreferences.applicationSamsungEdgeHeader = importPPDataBroadcastReceiver.applicationData.applicationSamsungEdgeHeader;
                                        ApplicationPreferences.applicationSamsungEdgeBackground = importPPDataBroadcastReceiver.applicationData.applicationSamsungEdgeBackground;
                                        ApplicationPreferences.applicationSamsungEdgeLightnessB = importPPDataBroadcastReceiver.applicationData.applicationSamsungEdgeLightnessB;
                                        ApplicationPreferences.applicationSamsungEdgeLightnessT = importPPDataBroadcastReceiver.applicationData.applicationSamsungEdgeLightnessT;
                                        ApplicationPreferences.applicationSamsungEdgeIconColor = importPPDataBroadcastReceiver.applicationData.applicationSamsungEdgeIconColor;
                                        ApplicationPreferences.applicationSamsungEdgeIconLightness = importPPDataBroadcastReceiver.applicationData.applicationSamsungEdgeIconLightness;
                                        ApplicationPreferences.applicationWidgetListRoundedCorners = importPPDataBroadcastReceiver.applicationData.applicationWidgetListRoundedCorners;
                                        ApplicationPreferences.applicationWidgetIconRoundedCorners = importPPDataBroadcastReceiver.applicationData.applicationWidgetIconRoundedCorners;
                                        ApplicationPreferences.applicationWidgetListBackgroundType = importPPDataBroadcastReceiver.applicationData.applicationWidgetListBackgroundType;
                                        ApplicationPreferences.applicationWidgetListBackgroundColor = importPPDataBroadcastReceiver.applicationData.applicationWidgetListBackgroundColor;
                                        ApplicationPreferences.applicationWidgetIconBackgroundType = importPPDataBroadcastReceiver.applicationData.applicationWidgetIconBackgroundType;
                                        ApplicationPreferences.applicationWidgetIconBackgroundColor = importPPDataBroadcastReceiver.applicationData.applicationWidgetIconBackgroundColor;
                                        ApplicationPreferences.applicationSamsungEdgeBackgroundType = importPPDataBroadcastReceiver.applicationData.applicationSamsungEdgeBackgroundType;
                                        ApplicationPreferences.applicationSamsungEdgeBackgroundColor = importPPDataBroadcastReceiver.applicationData.applicationSamsungEdgeBackgroundColor;
                                        ApplicationPreferences.applicationNeverAskForGrantRoot = importPPDataBroadcastReceiver.applicationData.applicationNeverAskForGrantRoot;
                                        ApplicationPreferences.notificationShowButtonExit = importPPDataBroadcastReceiver.applicationData.notificationShowButtonExit;
                                        ApplicationPreferences.applicationWidgetOneRowPrefIndicator = importPPDataBroadcastReceiver.applicationData.applicationWidgetOneRowPrefIndicator;
                                        ApplicationPreferences.applicationWidgetOneRowBackground = importPPDataBroadcastReceiver.applicationData.applicationWidgetOneRowBackground;
                                        ApplicationPreferences.applicationWidgetOneRowLightnessB = importPPDataBroadcastReceiver.applicationData.applicationWidgetOneRowLightnessB;
                                        ApplicationPreferences.applicationWidgetOneRowLightnessT = importPPDataBroadcastReceiver.applicationData.applicationWidgetOneRowLightnessT;
                                        ApplicationPreferences.applicationWidgetOneRowIconColor = importPPDataBroadcastReceiver.applicationData.applicationWidgetOneRowIconColor;
                                        ApplicationPreferences.applicationWidgetOneRowIconLightness = importPPDataBroadcastReceiver.applicationData.applicationWidgetOneRowIconLightness;
                                        ApplicationPreferences.applicationWidgetOneRowRoundedCorners = importPPDataBroadcastReceiver.applicationData.applicationWidgetOneRowRoundedCorners;
                                        ApplicationPreferences.applicationWidgetOneRowBackgroundType = importPPDataBroadcastReceiver.applicationData.applicationWidgetOneRowBackgroundType;
                                        ApplicationPreferences.applicationWidgetOneRowBackgroundColor = importPPDataBroadcastReceiver.applicationData.applicationWidgetOneRowBackgroundColor;
                                        ApplicationPreferences.applicationWidgetListLightnessBorder = importPPDataBroadcastReceiver.applicationData.applicationWidgetListLightnessBorder;
                                        ApplicationPreferences.applicationWidgetOneRowLightnessBorder = importPPDataBroadcastReceiver.applicationData.applicationWidgetOneRowLightnessBorder;
                                        ApplicationPreferences.applicationWidgetIconLightnessBorder = importPPDataBroadcastReceiver.applicationData.applicationWidgetIconLightnessBorder;
                                        ApplicationPreferences.applicationWidgetListShowBorder = importPPDataBroadcastReceiver.applicationData.applicationWidgetListShowBorder;
                                        ApplicationPreferences.applicationWidgetOneRowShowBorder = importPPDataBroadcastReceiver.applicationData.applicationWidgetOneRowShowBorder;
                                        ApplicationPreferences.applicationWidgetIconShowBorder = importPPDataBroadcastReceiver.applicationData.applicationWidgetIconShowBorder;
                                        ApplicationPreferences.applicationWidgetListCustomIconLightness = importPPDataBroadcastReceiver.applicationData.applicationWidgetListCustomIconLightness;
                                        ApplicationPreferences.applicationWidgetOneRowCustomIconLightness = importPPDataBroadcastReceiver.applicationData.applicationWidgetOneRowCustomIconLightness;
                                        ApplicationPreferences.applicationWidgetIconCustomIconLightness = importPPDataBroadcastReceiver.applicationData.applicationWidgetIconCustomIconLightness;
                                        ApplicationPreferences.applicationSamsungEdgeCustomIconLightness = importPPDataBroadcastReceiver.applicationData.applicationSamsungEdgeCustomIconLightness;
                                        ApplicationPreferences.notificationUseDecoration = importPPDataBroadcastReceiver.applicationData.notificationUseDecoration;
                                        ApplicationPreferences.notificationLayoutType = importPPDataBroadcastReceiver.applicationData.notificationLayoutType;
                                        ApplicationPreferences.notificationBackgroundColor = importPPDataBroadcastReceiver.applicationData.notificationBackgroundColor;
                                        ApplicationPreferences.notificationNotificationStyle = "0"; // custom notification style
                                        //ApplicationPreferences.notificationNightMode = false;
                                        ApplicationPreferences.notificationShowProfileIcon = true;
                                        if (ApplicationPreferences.notificationBackgroundColor.equals("2")) {
                                            ApplicationPreferences.notificationBackgroundColor = "0";
                                            //ApplicationPreferences.notificationNightMode = true;
                                        }
                                        ApplicationPreferences.notificationShowRestartEventsAsButton = Build.VERSION.SDK_INT >= 31;
                                        ApplicationPreferences.applicationShortcutIconColor = importPPDataBroadcastReceiver.applicationData.applicationWidgetIconColor;
                                        ApplicationPreferences.applicationShortcutIconLightness = importPPDataBroadcastReceiver.applicationData.applicationWidgetIconLightness;
                                        ApplicationPreferences.applicationShortcutCustomIconLightness = importPPDataBroadcastReceiver.applicationData.applicationWidgetIconCustomIconLightness;

                                        SharedPreferences.Editor editor = ApplicationPreferences.getEditor(activity.getApplicationContext());
                                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_START_ON_BOOT, ApplicationPreferences.applicationStartOnBoot);
                                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_ACTIVATE, ApplicationPreferences.applicationActivate);
                                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_ALERT, ApplicationPreferences.applicationActivateWithAlert);
                                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_CLOSE, ApplicationPreferences.applicationClose);
                                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_LONG_PRESS_ACTIVATION, ApplicationPreferences.applicationLongClickActivation);
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_THEME, ApplicationPreferences.applicationTheme);
                                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EDITOR_PREF_INDICATOR, ApplicationPreferences.applicationEditorPrefIndicator);
                                        editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_TOAST, ApplicationPreferences.notificationsToast);
                                        editor.putString(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR_STYLE, ApplicationPreferences.notificationStatusBarStyle);
                                        editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR, ApplicationPreferences.notificationShowInStatusBar);
                                        editor.putString(ApplicationPreferences.PREF_NOTIFICATION_TEXT_COLOR, ApplicationPreferences.notificationTextColor);
                                        editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_HIDE_IN_LOCKSCREEN, ApplicationPreferences.notificationHideInLockScreen);
                                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR, ApplicationPreferences.applicationWidgetListPrefIndicator);
                                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_HEADER, ApplicationPreferences.applicationWidgetListHeader);
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND, ApplicationPreferences.applicationWidgetListBackground);
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B, ApplicationPreferences.applicationWidgetListLightnessB);
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_T, ApplicationPreferences.applicationWidgetListLightnessT);
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_COLOR, ApplicationPreferences.applicationWidgetIconColor);
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS, ApplicationPreferences.applicationWidgetIconLightness);
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ICON_COLOR, ApplicationPreferences.applicationWidgetListIconColor);
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ICON_LIGHTNESS, ApplicationPreferences.applicationWidgetListIconLightness);
                                        editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_PREF_INDICATOR, ApplicationPreferences.notificationPrefIndicator);
                                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_GRID_LAYOUT, ApplicationPreferences.applicationActivatorGridLayout);
                                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_GRID_LAYOUT, ApplicationPreferences.applicationWidgetListGridLayout);
                                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_HIDE_PROFILE_NAME, ApplicationPreferences.applicationWidgetIconHideProfileName);
                                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_SHORTCUT_EMBLEM, ApplicationPreferences.applicationShortcutEmblem);
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND, ApplicationPreferences.applicationWidgetIconBackground);
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B, ApplicationPreferences.applicationWidgetIconLightnessB);
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_T, ApplicationPreferences.applicationWidgetIconLightnessT);
                                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_UNLINK_RINGER_NOTIFICATION_VOLUMES, ApplicationPreferences.applicationUnlinkRingerNotificationVolumes);
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_FORCE_SET_MERGE_RINGER_NOTIFICATION_VOLUMES, String.valueOf(ApplicationPreferences.applicationForceSetMergeRingNotificationVolumes));
                                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_HEADER, ApplicationPreferences.applicationSamsungEdgeHeader);
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND, ApplicationPreferences.applicationSamsungEdgeBackground);
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_B, ApplicationPreferences.applicationSamsungEdgeLightnessB);
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_T, ApplicationPreferences.applicationSamsungEdgeLightnessT);
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_ICON_COLOR, ApplicationPreferences.applicationSamsungEdgeIconColor);
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_ICON_LIGHTNESS, ApplicationPreferences.applicationSamsungEdgeIconLightness);
                                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS, ApplicationPreferences.applicationWidgetListRoundedCorners);
                                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS, ApplicationPreferences.applicationWidgetIconRoundedCorners);
                                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_TYPE, ApplicationPreferences.applicationWidgetListBackgroundType);
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR, ApplicationPreferences.applicationWidgetListBackgroundColor);
                                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_TYPE, ApplicationPreferences.applicationWidgetIconBackgroundType);
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR, ApplicationPreferences.applicationWidgetIconBackgroundColor);
                                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_TYPE, ApplicationPreferences.applicationSamsungEdgeBackgroundType);
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR, ApplicationPreferences.applicationSamsungEdgeBackgroundColor);
                                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_NEVER_ASK_FOR_GRANT_ROOT, ApplicationPreferences.applicationNeverAskForGrantRoot);
                                        editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_SHOW_BUTTON_EXIT, ApplicationPreferences.notificationShowButtonExit);
                                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR, ApplicationPreferences.applicationWidgetOneRowPrefIndicator);
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND, ApplicationPreferences.applicationWidgetOneRowBackground);
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_B, ApplicationPreferences.applicationWidgetOneRowLightnessB);
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_T, ApplicationPreferences.applicationWidgetOneRowLightnessT);
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_COLOR, ApplicationPreferences.applicationWidgetOneRowIconColor);
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_LIGHTNESS, ApplicationPreferences.applicationWidgetOneRowIconLightness);
                                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS, ApplicationPreferences.applicationWidgetOneRowRoundedCorners);
                                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_TYPE, ApplicationPreferences.applicationWidgetOneRowBackgroundType);
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR, ApplicationPreferences.applicationWidgetOneRowBackgroundColor);
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_BORDER, ApplicationPreferences.applicationWidgetListLightnessBorder);
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_BORDER, ApplicationPreferences.applicationWidgetOneRowLightnessBorder);
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_BORDER, ApplicationPreferences.applicationWidgetIconLightnessBorder);
                                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_SHOW_BORDER, ApplicationPreferences.applicationWidgetListShowBorder);
                                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_SHOW_BORDER, ApplicationPreferences.applicationWidgetOneRowShowBorder);
                                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_SHOW_BORDER, ApplicationPreferences.applicationWidgetIconShowBorder);
                                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_CUSTOM_ICON_LIGHTNESS, ApplicationPreferences.applicationWidgetListCustomIconLightness);
                                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_CUSTOM_ICON_LIGHTNESS, ApplicationPreferences.applicationWidgetOneRowCustomIconLightness);
                                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_CUSTOM_ICON_LIGHTNESS, ApplicationPreferences.applicationWidgetIconCustomIconLightness);
                                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_CUSTOM_ICON_LIGHTNESS, ApplicationPreferences.applicationSamsungEdgeCustomIconLightness);
                                        editor.putBoolean(ApplicationPreferences.PREF_NOTIFICATION_USE_DECORATION, ApplicationPreferences.notificationUseDecoration);
                                        editor.putString(ApplicationPreferences.PREF_NOTIFICATION_LAYOUT_TYPE, ApplicationPreferences.notificationLayoutType);
                                        editor.putString(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_COLOR, ApplicationPreferences.notificationBackgroundColor);
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_SHORTCUT_ICON_COLOR, ApplicationPreferences.applicationShortcutIconColor);
                                        editor.putString(ApplicationPreferences.PREF_APPLICATION_SHORTCUT_ICON_LIGHTNESS, ApplicationPreferences.applicationShortcutIconLightness);
                                        editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_SHORTCUT_CUSTOM_ICON_LIGHTNESS, ApplicationPreferences.applicationShortcutCustomIconLightness);
                                        editor.apply();
                                    }
                                    for (int i = 0; i < PPApplication.quickTileProfileId.length; i++)
                                        ApplicationPreferences.setQuickTileProfileId(activity.getApplicationContext(),i, 0);

                                    PPApplication.loadGlobalApplicationData(activity.getApplicationContext());
                                    PPApplication.loadApplicationPreferences(activity.getApplicationContext());
                                    PPApplication.loadProfileActivationData(activity.getApplicationContext());

                                    appSettingsError = false;
                                } catch (Exception e) {
                                    appSettingsError = true;
                                }
                            } else
                                appSettingsError = false;

                            if (deleteConfiguredProfiles) {
                                deleteProfilesError = !DatabaseHandler.getInstance(_dataWrapper.context).deleteAllProfiles();
                            } else
                                deleteProfilesError = false;
                            if (importProfiles && (!deleteProfilesError)) {
                                List<Long> ppShortcutIds = new ArrayList<>();
                                List<Long> importedShortcutIds = new ArrayList<>();
                                List<Long> ppIntentIds = new ArrayList<>();
                                List<Long> importedIntentIds = new ArrayList<>();

                                // first import shortcuts and intents
                                try {
                                    for (PPShortcutForExport shortcutForImport : importPPDataBroadcastReceiver.shortcuts) {
                                        Shortcut shortcut = new Shortcut();
                                        shortcut._intent = shortcutForImport.KEY_S_INTENT;
                                        shortcut._name = shortcutForImport.KEY_S_NAME;

                                        DatabaseHandler.getInstance(_dataWrapper.context).addShortcut(shortcut);
                                        // save shortcut id
                                        ppShortcutIds.add(shortcutForImport.KEY_S_ID);
                                        importedShortcutIds.add(shortcut._id);
                                    }
                                    shortcutsError = false;
                                } catch (Exception e) {
                                    shortcutsError = true;
                                }
                                try {
                                    for (PPIntentForExport intentForImport : importPPDataBroadcastReceiver.intents) {
                                        PPIntent ppIntent = new PPIntent(
                                                intentForImport.KEY_IN_ID,
                                                intentForImport.KEY_IN_NAME,
                                                intentForImport.KEY_IN_PACKAGE_NAME,
                                                intentForImport.KEY_IN_CLASS_NAME,
                                                intentForImport.KEY_IN_ACTION,
                                                intentForImport.KEY_IN_DATA,
                                                intentForImport.KEY_IN_MIME_TYPE,
                                                intentForImport.KEY_IN_EXTRA_KEY_1,
                                                intentForImport.KEY_IN_EXTRA_VALUE_1,
                                                intentForImport.KEY_IN_EXTRA_TYPE_1,
                                                intentForImport.KEY_IN_EXTRA_KEY_2,
                                                intentForImport.KEY_IN_EXTRA_VALUE_2,
                                                intentForImport.KEY_IN_EXTRA_TYPE_2,
                                                intentForImport.KEY_IN_EXTRA_KEY_3,
                                                intentForImport.KEY_IN_EXTRA_VALUE_3,
                                                intentForImport.KEY_IN_EXTRA_TYPE_3,
                                                intentForImport.KEY_IN_EXTRA_KEY_4,
                                                intentForImport.KEY_IN_EXTRA_VALUE_4,
                                                intentForImport.KEY_IN_EXTRA_TYPE_4,
                                                intentForImport.KEY_IN_EXTRA_KEY_5,
                                                intentForImport.KEY_IN_EXTRA_VALUE_5,
                                                intentForImport.KEY_IN_EXTRA_TYPE_5,
                                                intentForImport.KEY_IN_EXTRA_KEY_6,
                                                intentForImport.KEY_IN_EXTRA_VALUE_6,
                                                intentForImport.KEY_IN_EXTRA_TYPE_6,
                                                intentForImport.KEY_IN_EXTRA_KEY_7,
                                                intentForImport.KEY_IN_EXTRA_VALUE_7,
                                                intentForImport.KEY_IN_EXTRA_TYPE_7,
                                                intentForImport.KEY_IN_EXTRA_KEY_8,
                                                intentForImport.KEY_IN_EXTRA_VALUE_8,
                                                intentForImport.KEY_IN_EXTRA_TYPE_8,
                                                intentForImport.KEY_IN_EXTRA_KEY_9,
                                                intentForImport.KEY_IN_EXTRA_VALUE_9,
                                                intentForImport.KEY_IN_EXTRA_TYPE_9,
                                                intentForImport.KEY_IN_EXTRA_KEY_10,
                                                intentForImport.KEY_IN_EXTRA_VALUE_10,
                                                intentForImport.KEY_IN_EXTRA_TYPE_10,
                                                intentForImport.KEY_IN_CATEGORIES,
                                                intentForImport.KEY_IN_FLAGS,
                                                //intentForImport.KEY_IN_USED_COUNT,
                                                intentForImport.KEY_IN_INTENT_TYPE,
                                                false
                                        );

                                        DatabaseHandler.getInstance(_dataWrapper.context).addIntent(ppIntent);
                                        // save intent id
                                        ppIntentIds.add(intentForImport.KEY_IN_ID);
                                        importedIntentIds.add(ppIntent._id);
                                    }
                                    intentsError = false;
                                } catch (Exception e) {
                                    intentsError = true;
                                }

                                try {
                                    for (PPProfileForExport profileForImport : importPPDataBroadcastReceiver.profiles) {
                                        Profile profile = new Profile(
                                                profileForImport.KEY_NAME,
                                                profileForImport.KEY_ICON,
                                                profileForImport.KEY_CHECKED,
                                                profileForImport.KEY_PORDER,
                                                profileForImport.KEY_VOLUME_RINGER_MODE,
                                                profileForImport.KEY_VOLUME_RINGTONE,
                                                profileForImport.KEY_VOLUME_NOTIFICATION,
                                                profileForImport.KEY_VOLUME_MEDIA,
                                                profileForImport.KEY_VOLUME_ALARM,
                                                profileForImport.KEY_VOLUME_SYSTEM,
                                                profileForImport.KEY_VOLUME_VOICE,
                                                profileForImport.KEY_SOUND_RINGTONE_CHANGE,
                                                profileForImport.KEY_SOUND_RINGTONE,
                                                profileForImport.KEY_SOUND_NOTIFICATION_CHANGE,
                                                profileForImport.KEY_SOUND_NOTIFICATION,
                                                profileForImport.KEY_SOUND_ALARM_CHANGE,
                                                profileForImport.KEY_SOUND_ALARM,
                                                profileForImport.KEY_DEVICE_AIRPLANE_MODE,
                                                profileForImport.KEY_DEVICE_WIFI,
                                                profileForImport.KEY_DEVICE_BLUETOOTH,
                                                profileForImport.KEY_DEVICE_SCREEN_TIMEOUT,
                                                profileForImport.KEY_DEVICE_BRIGHTNESS,
                                                profileForImport.KEY_DEVICE_WALLPAPER_CHANGE,
                                                profileForImport.KEY_DEVICE_WALLPAPER,
                                                profileForImport.KEY_DEVICE_MOBILE_DATA,
                                                profileForImport.KEY_DEVICE_MOBILE_DATA_PREFS,
                                                profileForImport.KEY_DEVICE_GPS,
                                                profileForImport.KEY_DEVICE_RUN_APPLICATION_CHANGE,
                                                profileForImport.KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME,
                                                profileForImport.KEY_DEVICE_AUTOSYNC,
                                                true,
                                                profileForImport.KEY_DEVICE_AUTOROTATE,
                                                profileForImport.KEY_DEVICE_LOCATION_SERVICE_PREFS,
                                                profileForImport.KEY_VOLUME_SPEAKER_PHONE,
                                                profileForImport.KEY_DEVICE_NFC,
                                                profileForImport.KEY_DURATION,
                                                profileForImport.KEY_AFTER_DURATION_DO,
                                                profileForImport.KEY_VOLUME_ZEN_MODE,
                                                profileForImport.KEY_DEVICE_KEYGUARD,
                                                profileForImport.KEY_VIBRATE_ON_TOUCH,
                                                profileForImport.KEY_DEVICE_WIFI_AP,
                                                profileForImport.KEY_DEVICE_POWER_SAVE_MODE,
                                                profileForImport.KEY_ASK_FOR_DURATION,
                                                profileForImport.KEY_DEVICE_NETWORK_TYPE,
                                                profileForImport.KEY_NOTIFICATION_LED,
                                                profileForImport.KEY_VIBRATE_WHEN_RINGING,
                                                profileForImport.KEY_DEVICE_WALLPAPER_FOR,
                                                profileForImport.KEY_HIDE_STATUS_BAR_ICON,
                                                profileForImport.KEY_LOCK_DEVICE,
                                                profileForImport.KEY_DEVICE_CONNECT_TO_SSID,
                                                0,
                                                0,
                                                profileForImport.KEY_DURATION_NOTIFICATION_SOUND,
                                                profileForImport.KEY_DURATION_NOTIFICATION_VIBRATE,
                                                profileForImport.KEY_DEVICE_WIFI_AP_PREFS,
                                                0,
                                                0,
                                                0,
                                                profileForImport.KEY_HEADS_UP_NOTIFICATIONS,
                                                profileForImport.KEY_DEVICE_FORCE_STOP_APPLICATION_CHANGE,
                                                profileForImport.KEY_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME,
                                                profileForImport.KEY_ACTIVATION_BY_USER_COUNT,
                                                profileForImport.KEY_DEVICE_NETWORK_TYPE_PREFS,
                                                profileForImport.KEY_DEVICE_CLOSE_ALL_APPLICATIONS,
                                                profileForImport.KEY_SCREEN_NIGHT_MODE,
                                                profileForImport.KEY_DTMF_TONE_WHEN_DIALING,
                                                profileForImport.KEY_SOUND_ON_TOUCH,
                                                profileForImport.KEY_VOLUME_DTMF,
                                                profileForImport.KEY_VOLUME_ACCESSIBILITY,
                                                profileForImport.KEY_VOLUME_BLUETOOTH_SCO,
                                                0,
                                                0,
                                                0,
                                                false,
                                                0,
                                                0,
                                                "0|0||",
                                                0,
                                                0,
                                                0,
                                                0,
                                                0,
                                                "0|0|0",
                                                0,
                                                0,
                                                0,
                                                "",
                                                0,
                                                "",
                                                0,
                                                "",
                                                0,
                                                "",
                                                0,
                                                "",
                                                0,
                                                "-",
                                                0,
                                                0,
                                                0,
                                                0,
                                                0
                                        );

                                        // replace ids in profile._deviceRunApplicationPackageName
                                        // with new shortcut and intent ids
                                        String[] splits = profile._deviceRunApplicationPackageName.split("\\|");
                                        String newValue = "";
                                        for (String split : splits) {
                                            String newSplit = "";
                                            if (Application.isShortcut(split)) {
                                                if (split.length() > 2) {
                                                    long id = Application.getShortcutId(split);
                                                    for (int i = 0; i < ppShortcutIds.size(); i++) {
                                                        if (id == ppShortcutIds.get(i)) {
                                                            id = importedShortcutIds.get(i);

                                                            // "(s)package_name/activity#shortcut_id#delay"
                                                            String[] valueSplits = split.split("#");
                                                            if (valueSplits.length == 3)
                                                                newSplit = valueSplits[0] + "#" + id + "#" + valueSplits[2];
                                                            else
                                                                newSplit = split;

                                                            break;
                                                        }
                                                    }
                                                } else
                                                    newSplit = split;
                                            } else if (Application.isIntent(split)) {
                                                if (split.length() > 2) {
                                                    long id = Application.getIntentId(split);
                                                    for (int i = 0; i < ppIntentIds.size(); i++) {
                                                        if (id == ppIntentIds.get(i)) {
                                                            id = importedIntentIds.get(i);

                                                            // "(i)intent_id#delay"
                                                            String[] valueSplits = split.split("#");
                                                            if (valueSplits.length == 2)
                                                                newSplit = "(i)" + id + "#" + valueSplits[1];
                                                            else
                                                                newSplit = split;

                                                            break;
                                                        }
                                                    }
                                                } else
                                                    newSplit = split;
                                            } else
                                                newSplit = split;

                                            if (!newValue.isEmpty())
                                                newValue = newValue + "|";
                                            newValue = newValue + newSplit;
                                        }
                                        profile._deviceRunApplicationPackageName = newValue;

                                        ContentResolver contentResolver = _dataWrapper.context.getContentResolver();

                                        // set profile parameters to "Not used" for non-granted Uri premissions
                                        String icon = profile._icon;
                                        if (!Profile.getIsIconResourceID(icon)) {
                                            String iconIdentifier = Profile.getIconIdentifier(icon);
                                            boolean isGranted = false;
                                            Uri uri = Uri.parse(iconIdentifier);
                                            if (uri != null) {
                                                try {
                                                    _dataWrapper.context.grantUriPermission(PPApplication.PACKAGE_NAME, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                                    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                                    isGranted = true;
                                                } catch (Exception e) {
                                                    //isGranted = false;
                                                }
                                            }
                                            if (!isGranted) {
                                                profile._icon = Profile.defaultValuesString.get(Profile.PREF_PROFILE_ICON);
                                            }
                                        }
                                        String tone = profile._soundRingtone;
                                        splits = tone.split("\\|");
                                        String ringtone = splits[0];
                                        if (!ringtone.isEmpty()) {
                                            if (ringtone.contains("content://media/external")) {
                                                boolean isGranted = false;
                                                Uri uri = ActivateProfileHelper.getUriOfSavedTone(_dataWrapper.context, ringtone, RingtoneManager.TYPE_RINGTONE);
                                                if (uri != null) {
                                                    try {
                                                        _dataWrapper.context.grantUriPermission(PPApplication.PACKAGE_NAME, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                                        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                                        isGranted = true;
                                                    } catch (Exception e) {
                                                        //isGranted = false;
                                                    }
                                                }
                                                if (!isGranted) {
                                                    //noinspection ConstantConditions
                                                    profile._soundRingtoneChange = Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE));
                                                    profile._soundRingtone = Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_RINGTONE);
                                                }
                                            }
                                        }
                                        tone = profile._soundNotification;
                                        splits = tone.split("\\|");
                                        ringtone = splits[0];
                                        if (!ringtone.isEmpty()) {
                                            if (ringtone.contains("content://media/external")) {
                                                boolean isGranted = false;
                                                Uri uri = ActivateProfileHelper.getUriOfSavedTone(_dataWrapper.context, ringtone, RingtoneManager.TYPE_NOTIFICATION);
                                                if (uri != null) {
                                                    try {
                                                        _dataWrapper.context.grantUriPermission(PPApplication.PACKAGE_NAME, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                                        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                                        isGranted = true;
                                                    } catch (Exception e) {
                                                        //isGranted = false;
                                                    }
                                                }
                                                if (!isGranted) {
                                                    //noinspection ConstantConditions
                                                    profile._soundNotificationChange = Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE));
                                                    profile._soundNotification = Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_NOTIFICATION);
                                                }
                                            }
                                        }
                                        tone = profile._soundAlarm;
                                        splits = tone.split("\\|");
                                        ringtone = splits[0];
                                        if (!ringtone.isEmpty()) {
                                            if (ringtone.contains("content://media/external")) {
                                                boolean isGranted = false;
                                                Uri uri = ActivateProfileHelper.getUriOfSavedTone(_dataWrapper.context, ringtone, RingtoneManager.TYPE_ALARM);
                                                if (uri != null) {
                                                    try {
                                                        _dataWrapper.context.grantUriPermission(PPApplication.PACKAGE_NAME, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                                        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                                        isGranted = true;
                                                    } catch (Exception e) {
                                                        //isGranted = false;
                                                    }
                                                }
                                                if (!isGranted) {
                                                    //noinspection ConstantConditions
                                                    profile._soundAlarmChange = Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_ALARM_CHANGE));
                                                    profile._soundAlarm = Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_ALARM);
                                                }
                                            }
                                        }
                                        String wallpaper = profile._deviceWallpaper;
                                        if (!wallpaper.isEmpty() && !wallpaper.equals(Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_WALLPAPER))) {
                                            boolean isGranted = false;
                                            Uri uri = Uri.parse(wallpaper);
                                            if (uri != null) {
                                                try {
                                                    _dataWrapper.context.grantUriPermission(PPApplication.PACKAGE_NAME, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                                    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                                    isGranted = true;
                                                } catch (Exception e) {
                                                    //isGranted = false;
                                                }
                                            }
                                            if (!isGranted) {
                                                //noinspection ConstantConditions
                                                profile._deviceWallpaperChange = Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE));
                                                profile._deviceWallpaper = Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_WALLPAPER);
                                            }
                                        }
                                        tone = profile._durationNotificationSound;
                                        if (!tone.isEmpty()) {
                                            if (tone.contains("content://media/external")) {
                                                boolean isGranted = false;
                                                Uri uri = Uri.parse(tone);
                                                if (uri != null) {
                                                    try {
                                                        _dataWrapper.context.grantUriPermission(PPApplication.PACKAGE_NAME, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                                        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                                        isGranted = true;
                                                    } catch (Exception e) {
                                                        //isGranted = false;
                                                    }
                                                }
                                                if (!isGranted) {
                                                    profile._durationNotificationSound = Profile.defaultValuesString.get(Profile.PREF_PROFILE_DURATION_NOTIFICATION_SOUND);
                                                }
                                            }
                                        }

                                        DatabaseHandler.getInstance(_dataWrapper.context).addProfile(profile, false);
                                    }
                                    profilesError = false;
                                } catch (Exception e) {
                                    profilesError = true;
                                }
                            } else {
                                profilesError = false;
                                shortcutsError = false;
                                intentsError = false;
                            }
                        }
                    }

                    if (importFromPPStopped)
                        return 2;

                    if (deleteProfilesError || profilesError || shortcutsError || intentsError || appSettingsError)
                        return 0;
                    else
                        return 1;
                } else
                    return 0;
            }
            else
                return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            EditorActivity activity = activityWeakRef.get();
            if (activity != null) {
                doImport = false;

                if (!activity.isFinishing()) {
                    if ((activity.importProgressDialog != null) && activity.importProgressDialog.isShowing()) {
                        if (!activity.isDestroyed())
                            activity.importProgressDialog.dismiss();
                        activity.importProgressDialog = null;
                    }
                    GlobalGUIRoutines.unlockScreenOrientation(activity);
                }

                if (importPPDataBroadcastReceiver != null) {
                    try {
                        activity.unregisterReceiver(importPPDataBroadcastReceiver);
                    } catch (Exception ignored) {
                    }
                }

                if (!importFromPPStopped) {
                    if (_dataWrapper != null) {
                        //PPApplication.logE("DataWrapper.updateNotificationAndWidgets", "from EditorActivity.doImportData");

                        // clear shared preferences for last activated profile
                        //Profile profile = DataWrapper.getNonInitializedProfile("", null, 0);
                        //Profile.saveProfileToSharedPreferences(profile, _dataWrapper.context);
                        PPApplication.setLastActivatedProfile(_dataWrapper.context, 0);

//                        PPApplication.logE("###### PPApplication.updateGUI", "from=EditorActivity.doImportData");
                        PPApplication.updateGUI(true, false, _dataWrapper.context);

                        PPApplication.setApplicationStarted(_dataWrapper.context, true);
                        Intent serviceIntent = new Intent(_dataWrapper.context, PhoneProfilesService.class);
                        //serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
                        //serviceIntent.putExtra(PhoneProfilesService.EXTRA_DEACTIVATE_PROFILE, true);
                        serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, true);
                        serviceIntent.putExtra(PPApplication.EXTRA_APPLICATION_START, true);
                        serviceIntent.putExtra(PPApplication.EXTRA_DEVICE_BOOT, false);
                        serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_PACKAGE_REPLACE, false);
//                        PPApplication.logE("[START_PP_SERVICE] EditorProfileActivity.doImportDataFromPP", "xxx");
                        PPApplication.startPPService(activity, serviceIntent);
                    }

                    if ((_dataWrapper != null) && (!deleteProfilesError) && (!profilesError) && (!shortcutsError) && (!intentsError) && (!appSettingsError)) {
                        //PPApplication.logE("EditorActivity.doImportData", "restore is ok");

                        // restart events
                        //if (Event.getGlobalEventsRunning(this.dataWrapper.context)) {
                        //    this.dataWrapper.restartEventsWithDelay(3, false, false, DatabaseHandler.ALTYPE_UNDEFINED);
                        //}

                        PPApplication.addActivityLog(_dataWrapper.context, PPApplication.ALTYPE_DATA_IMPORT_FROM_PP, null, null, "");

                        // toast notification
                        if (!activity.isFinishing())
                            PPApplication.showToast(_dataWrapper.context.getApplicationContext(),
                                    activity.getString(R.string.toast_import_from_pp_ok),
                                    Toast.LENGTH_SHORT);

                        // refresh activity
                        if (!activity.isFinishing())
                            GlobalGUIRoutines.reloadActivity(activity, true);

                        DrawOverAppsPermissionNotification.showNotification(_dataWrapper.context, true);
                        IgnoreBatteryOptimizationNotification.showNotification(_dataWrapper.context, true);

                        PPApplication.setCustomKey(PPApplication.CRASHLYTICS_LOG_IMPORT_FROM_PP_OK, true);
                    } else {
                        //PPApplication.logE("EditorActivity.doImportData", "error restore");

                        int appSettingsResult = 1;
                        if (appSettingsError) appSettingsResult = 0;
                        int dbError = DatabaseHandler.IMPORT_OK;
                        if (deleteProfilesError || profilesError || shortcutsError || intentsError)
                            dbError = DatabaseHandler.IMPORT_ERROR_BUG;
                        if (!activity.isFinishing())
                            activity.importExportErrorDialog(IMPORTEXPORT_IMPORTFROMPP, dbError, appSettingsResult/*, 1*/);

                        PPApplication.setCustomKey(PPApplication.CRASHLYTICS_LOG_IMPORT_FROM_PP_OK, false);
                    }
                }
            }
        }
    }

    private static class ExportAsyncTask extends AsyncTask<Void, Integer, Integer> {
        private final DataWrapper dataWrapper;
        private boolean runStopEvents;

        private final WeakReference<EditorActivity> activityWeakRef;
        final boolean email;
        final boolean toAuthor;
        final boolean share;
        File zipFile = null;

        public ExportAsyncTask(final boolean email, final boolean toAuthor, final boolean share,
                               EditorActivity activity) {
            this.activityWeakRef = new WeakReference<>(activity);
            this.email = email;
            this.toAuthor = toAuthor;
            this.share = share;

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
            dialogBuilder.setMessage(R.string.export_profiles_alert_title);

            LayoutInflater inflater = (activity.getLayoutInflater());
            View layout = inflater.inflate(R.layout.dialog_progress_bar, null);
            dialogBuilder.setView(layout);

            activity.exportProgressDialog = dialogBuilder.create();

//                    exportProgressDialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                        @Override
//                        public void onShow(DialogInterface dialog) {
//                            Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                            if (positive != null) positive.setAllCaps(false);
//                            Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                            if (negative != null) negative.setAllCaps(false);
//                        }
//                    });

            this.dataWrapper = activity.getDataWrapper();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            EditorActivity activity = activityWeakRef.get();
            if (activity != null) {
                GlobalGUIRoutines.lockScreenOrientation(activity, false);
                activity.exportProgressDialog.setCancelable(false);
                activity.exportProgressDialog.setCanceledOnTouchOutside(false);
                if (!activity.isFinishing())
                    activity.exportProgressDialog.show();

                runStopEvents = Event.getGlobalEventsRunning();
            }
        }

        @Override
        protected Integer doInBackground(Void... params) {

            EditorActivity activity = activityWeakRef.get();
            if (activity != null) {
                if (this.dataWrapper != null) {
                    //dataWrapper.globalRunStopEvents(true);
                    PPApplication.exitApp(false, activity.getApplicationContext(), this.dataWrapper, null, false, false);

                    // wait for end of PPService
                    PPApplication.sleep(3000);

                    File sd = activity.getApplicationContext().getExternalFilesDir(null);

                    int ret = DatabaseHandler.getInstance(this.dataWrapper.context).exportDB();
                    if (ret == 1) {
                        //File exportFile = new File(sd, PPApplication.EXPORT_PATH + "/" + PPApplication.EXPORT_APP_PREF_FILENAME);
                        File exportFile = new File(sd, PPApplication.EXPORT_APP_PREF_FILENAME);
                        if (activity.exportApplicationPreferences(exportFile, runStopEvents/*, 1*/)) {
                            /*exportFile = new File(sd, PPApplication.EXPORT_PATH + "/" + GlobalGUIRoutines.EXPORT_DEF_PROFILE_PREF_FILENAME);
                            if (!exportApplicationPreferences(exportFile, 2))
                                ret = 0;*/
                            //noinspection ConstantConditions
                            ret = 1;
                        } else
                            ret = 0;
                    }

                    if ((ret == 1) && (this.share)) {
                        String[] filesToZip = new String[2];

                        try {
                            // delete all zip files in local storage
                            sd = activity.getApplicationContext().getExternalFilesDir(null);
                            File[] oldZipFiles = sd.listFiles();
                            if (oldZipFiles != null) {
                                for (File f : oldZipFiles) {
                                    if (f.getName().startsWith(PPApplication.SHARED_EXPORT_FILENAME)) {
                                        //noinspection ResultOfMethodCallIgnored
                                        f.delete();
                                    }
                                }
                            }

                            CharSequence dateTime = android.text.format.DateFormat.format("yyyyMMddHHmmss", new java.util.Date());

                            String fileName = PPApplication.SHARED_EXPORT_FILENAME + "_" + dateTime +
                                                PPApplication.SHARED_EXPORT_FILEEXTENSION;
                            zipFile = new File(sd, fileName);
                            String zipFilePath = zipFile.getAbsolutePath();

                            filesToZip[0] = new File(sd, DatabaseHandler.EXPORT_DBFILENAME).getAbsolutePath();
                            filesToZip[1] = new File(sd, PPApplication.EXPORT_APP_PREF_FILENAME).getAbsolutePath();

                            ZipManager zipManager = new ZipManager();
                            if (!zipManager.zip(filesToZip, zipFilePath))
                                ret = 0;
                        } catch (Exception e) {
                            //PPApplication.recordException(e);
                            e.printStackTrace();
                            ret = 0;
                        }

                    }

                    PPApplication.addActivityLog(this.dataWrapper.context, PPApplication.ALTYPE_DATA_EXPORT, null, null, "");

                    //Event.setGlobalEventsRunning(this.dataWrapper.context, runStopEvents);
                    PPApplication.setApplicationStarted(activity.getApplicationContext(), true);
                    Intent serviceIntent = new Intent(activity.getApplicationContext(), PhoneProfilesService.class);
                    //serviceIntent.putExtra(PhoneProfilesService.EXTRA_DEACTIVATE_PROFILE, false);
                    serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, true);
                    //serviceIntent.putExtra(PPApplication.EXTRA_APPLICATION_START, true);
                    serviceIntent.putExtra(PPApplication.EXTRA_DEVICE_BOOT, false);
                    serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_PACKAGE_REPLACE, false);
//                        PPApplication.logE("[START_PP_SERVICE] EditorProfileActivity.doExportData", "xxx");
                    PPApplication.startPPService(activity.getApplicationContext(), serviceIntent);

                    return ret;
                } else
                    return 0;
            } else
                return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            EditorActivity activity = activityWeakRef.get();
            if (activity != null) {
                if (!activity.isFinishing()) {
                    if ((activity.exportProgressDialog != null) && activity.exportProgressDialog.isShowing()) {
                        if (!activity.isDestroyed())
                            activity.exportProgressDialog.dismiss();
                        activity.exportProgressDialog = null;
                    }
                    GlobalGUIRoutines.unlockScreenOrientation(activity);
                }

                if ((dataWrapper != null) && (result == 1)) {

                    Context context = this.dataWrapper.context.getApplicationContext();
                    // toast notification
                    if (!activity.isFinishing())
                        PPApplication.showToast(context, activity.getString(R.string.toast_export_ok), Toast.LENGTH_SHORT);

                    //dataWrapper.restartEventsWithRescan(false, false, true, false, false, false);

                    if (email) {
                        // email backup

                        ArrayList<Uri> uris = new ArrayList<>();

                        try {
                            //File sd = Environment.getExternalStorageDirectory();
                            //File sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                            File sd = context.getExternalFilesDir(null);

                            //File exportedDB = new File(sd, PPApplication.EXPORT_PATH + "/" + DatabaseHandler.EXPORT_DBFILENAME);
                            File exportedDB = new File(sd, DatabaseHandler.EXPORT_DBFILENAME);
                            Uri fileUri = FileProvider.getUriForFile(activity, PPApplication.PACKAGE_NAME + ".provider", exportedDB);
                            uris.add(fileUri);

                            //File appSettingsFile = new File(sd, PPApplication.EXPORT_PATH + "/" + PPApplication.EXPORT_APP_PREF_FILENAME);
                            File appSettingsFile = new File(sd, PPApplication.EXPORT_APP_PREF_FILENAME);
                            fileUri = FileProvider.getUriForFile(activity, PPApplication.PACKAGE_NAME + ".provider", appSettingsFile);
                            uris.add(fileUri);
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }

                        String emailAddress = "";
                        if (toAuthor)
                            emailAddress = "henrich.gron@gmail.com";
                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                                "mailto", emailAddress, null));

                        String packageVersion = "";
                        try {
                            PackageInfo pInfo = context.getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0);
                            packageVersion = " - v" + pInfo.versionName + " (" + PPApplication.getVersionCode(pInfo) + ")";
                        } catch (Exception e) {
                            //Log.e("EditorActivity.doExportData", Log.getStackTraceString(e));
                            PPApplication.recordException(e);
                        }
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "PhoneProfilesPlus" + packageVersion + " - " + activity.getString(R.string.export_data_email_subject));
                        emailIntent.putExtra(Intent.EXTRA_TEXT, activity.getEmailBodyText());
                        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        List<ResolveInfo> resolveInfo = context.getPackageManager().queryIntentActivities(emailIntent, 0);
                        List<LabeledIntent> intents = new ArrayList<>();
                        for (ResolveInfo info : resolveInfo) {
                            Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                            intent.setComponent(new ComponentName(info.activityInfo.packageName, info.activityInfo.name));
                            if (!emailAddress.isEmpty())
                                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddress});
                            intent.putExtra(Intent.EXTRA_SUBJECT, "PhoneProfilesPlus" + packageVersion + " - " + activity.getString(R.string.export_data_email_subject));
                            intent.putExtra(Intent.EXTRA_TEXT, activity.getEmailBodyText());
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris); //ArrayList<Uri> of attachment Uri's
                            intents.add(new LabeledIntent(intent, info.activityInfo.packageName, info.loadLabel(context.getPackageManager()), info.icon));
                        }
                        if (intents.size() > 0) {
                            try {
                                Intent chooser = Intent.createChooser(intents.remove(intents.size() - 1), context.getString(R.string.email_chooser));
                                //noinspection ToArrayCallWithZeroLengthArrayArgument
                                chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toArray(new LabeledIntent[intents.size()]));
                                activity.startActivity(chooser);
                            } catch (Exception e) {
                                //Log.e("EditorActivity.doExportData", Log.getStackTraceString(e));
                                PPApplication.recordException(e);
                            }
                        }
                    } else
                    if (share) {
                        //File sd = context.getExternalFilesDir(null);
                        //File zipFile = new File(sd, PPApplication.SHARED_EXPORT_LOCALFILENAME);
                        if (zipFile.exists()) {
                            Uri zipFileUri = FileProvider.getUriForFile(activity, PPApplication.PACKAGE_NAME + ".provider", zipFile);

                            ShareCompat.IntentBuilder shareBuilder = new ShareCompat.IntentBuilder(activity);
                            shareBuilder.setType("application/zip")
                                    .setStream(zipFileUri)
                                    .setChooserTitle(R.string.share_settings_choose_bar);

                            Intent intent = shareBuilder.createChooserIntent()
                                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                            //activity.startActivity(intent);
                            //noinspection deprecation
                            activity.startActivityForResult(intent, REQUEST_CODE_SHARE_SETTINGS);
                        }
                    } else {
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                        LayoutInflater inflater = (activity).getLayoutInflater();
                        View layout = inflater.inflate(R.layout.dialog_backup_settings_alert, null);
                        dialogBuilder.setView(layout);
                        dialogBuilder.setTitle(R.string.backup_settings_alert_title);

                        boolean createPPPSubfolder = ApplicationPreferences.getSharedPreferences(context).getBoolean(PREF_BACKUP_CREATE_PPP_SUBFOLDER, true);

                        final TextView rewriteInfo = layout.findViewById(R.id.backup_settings_alert_dialog_rewrite_files_info);
                        rewriteInfo.setEnabled(!createPPPSubfolder);

                        final CheckBox checkBox = layout.findViewById(R.id.backup_settings_alert_dialog_checkBox);
                        checkBox.setChecked(createPPPSubfolder);

                        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> rewriteInfo.setEnabled(!isChecked));
                        dialogBuilder.setPositiveButton(R.string.alert_button_yes, (dialog, which) -> {
                            boolean ok = false;
                            try {

                                boolean _createPPPSubfolder = checkBox.isChecked();
                                Editor editor = ApplicationPreferences.getEditor(context);
                                editor.putBoolean(PREF_BACKUP_CREATE_PPP_SUBFOLDER, _createPPPSubfolder);
                                editor.apply();

                                Intent intent;
                                if (Build.VERSION.SDK_INT >= 29) {
                                    StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
                                    intent = sm.getPrimaryStorageVolume().createOpenDocumentTreeIntent();
                                } else {
                                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                                    intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                }
                                // not supported by ACTION_OPEN_DOCUMENT_TREE
                                //intent.putExtra(Intent.EXTRA_LOCAL_ONLY, false);

                                //intent.putExtra("android.content.extra.SHOW_ADVANCED",true);
                                //intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, PPApplication.backupFolderUri);
                                //PPApplication.logE("--------- EditorActivity.doExportData", "checkBox.isChecked()="+checkBox.isChecked());
                                if (_createPPPSubfolder)
                                    //noinspection deprecation
                                    activity.startActivityForResult(intent, REQUEST_CODE_BACKUP_SETTINGS_2);
                                else
                                    //noinspection deprecation
                                    activity.startActivityForResult(intent, REQUEST_CODE_BACKUP_SETTINGS);
                                ok = true;
                            } catch (Exception e) {
                                PPApplication.recordException(e);
                            }
                            if (!ok) {
                                AlertDialog.Builder _dialogBuilder = new AlertDialog.Builder(activity);
                                _dialogBuilder.setMessage(R.string.directory_tree_activity_not_found_alert);
                                //_dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                                _dialogBuilder.setPositiveButton(android.R.string.ok, null);
                                AlertDialog _dialog = _dialogBuilder.create();

//                                        _dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                                            @Override
//                                            public void onShow(DialogInterface dialog) {
//                                                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                                                if (positive != null) positive.setAllCaps(false);
//                                                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                                                if (negative != null) negative.setAllCaps(false);
//                                            }
//                                        });

                                if (!activity.isFinishing())
                                    _dialog.show();
                            }
                        });
                        dialogBuilder.setNegativeButton(R.string.alert_button_no, null);
                        AlertDialog dialog = dialogBuilder.create();

                        //        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        //            @Override
                        //            public void onShow(DialogInterface dialog) {
                        //                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                        //                if (positive != null) positive.setAllCaps(false);
                        //                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                        //                if (negative != null) negative.setAllCaps(false);
                        //            }
                        //        });

                        if (!activity.isFinishing())
                            dialog.show();

                    }

                } else {
                    if (!activity.isFinishing())
                        activity.importExportErrorDialog(IMPORTEXPORT_EXPORT, 0, 0/*, 0*/);
                }
            }
        }

    }

    private static class Language {
        String language;
        String country;
        String script;
        String name;
    }

    private class LanguagesComparator implements Comparator<Language> {

        public int compare(Language lhs, Language rhs) {
            return languagesCollator.compare(lhs.name, rhs.name);
        }
    }

}

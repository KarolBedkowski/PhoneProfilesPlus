package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.lang.ref.WeakReference;
import java.util.Comparator;

public class TileChooserListFragment extends Fragment {

    DataWrapper activityDataWrapper;
    private TileChooserListAdapter profileListAdapter;
    private ListView listView;
    TextView textViewNoData;
    private LinearLayout progressBar;

    private WeakReference<LoadProfileListAsyncTask> asyncTaskContext;

    public TileChooserListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // this is really important in order to save the state across screen
        // configuration changes for example
        //noinspection deprecation
        setRetainInstance(true);

        //noinspection ConstantConditions
        activityDataWrapper = new DataWrapper(getActivity().getApplicationContext(), false, 0, false, DataWrapper.IT_FOR_EDITOR, 0, 0f);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView;

        rootView = inflater.inflate(R.layout.tile_chooser_list, container, false);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        doOnViewCreated(view);
    }

    private void doOnViewCreated(View view/*, Bundle savedInstanceState*/)
    {
        listView = view.findViewById(R.id.tile_chooser_profiles_list);
        textViewNoData = view.findViewById(R.id.tile_chooser_profiles_list_empty);
        progressBar = view.findViewById(R.id.tile_chooser_profiles_list_linla_progress);
        Button cancelButton = view.findViewById(R.id.tile_chooser_profiles_list_cancel);

        listView.setOnItemClickListener((parent, item, position, id) -> {
            if (getActivity() != null) {
                TileChooserListAdapter.ViewHolder viewHolder = (TileChooserListAdapter.ViewHolder) item.getTag();
                if (viewHolder != null)
                    viewHolder.radioButton.setChecked(true);
                Handler handler = new Handler(getActivity().getMainLooper());
                handler.postDelayed(() -> chooseTile(position), 200);
            }
        });

        cancelButton.setOnClickListener(v -> {
            if (getActivity() != null)
                getActivity().finish();
        });

        if (!activityDataWrapper.profileListFilled)
        {
            LoadProfileListAsyncTask asyncTask = new LoadProfileListAsyncTask(this);
            this.asyncTaskContext = new WeakReference<>(asyncTask );
            asyncTask.execute();
        }
        else
        {
            listView.setAdapter(profileListAdapter);
        }

    }

    private static class LoadProfileListAsyncTask extends AsyncTask<Void, Void, Void> {

        private final WeakReference<TileChooserListFragment> fragmentWeakRef;
        private final DataWrapper dataWrapper;

        final boolean applicationActivatorPrefIndicator;

        Handler progressBarHandler;
        Runnable progressBarRunnable;

        private static class ProfileComparator implements Comparator<Profile> {
            public int compare(Profile lhs, Profile rhs) {
                if (PPApplication.collator != null)
                    return PPApplication.collator.compare(lhs._name, rhs._name);
                else
                    return 0;
            }
        }

        public LoadProfileListAsyncTask (TileChooserListFragment fragment) {
            this.fragmentWeakRef = new WeakReference<>(fragment);
            //noinspection ConstantConditions
            this.dataWrapper = new DataWrapper(fragment.getActivity().getApplicationContext(), false, 0, false, DataWrapper.IT_FOR_EDITOR, 0, 0f);

            //applicationActivatorPrefIndicator = ApplicationPreferences.applicationActivatorPrefIndicator(this.dataWrapper.context);
            applicationActivatorPrefIndicator = ApplicationPreferences.applicationEditorPrefIndicator;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            final TileChooserListFragment fragment = this.fragmentWeakRef.get();

            if ((fragment != null) && (fragment.isAdded())) {
                progressBarHandler = new Handler(this.dataWrapper.context.getMainLooper());
                progressBarRunnable = () -> {
//                    PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=TileChooserListFragment.LoadProfileListAsyncTask");
                    //fragment.textViewNoData.setVisibility(GONE);
                    fragment.progressBar.setVisibility(View.VISIBLE);
                };
                progressBarHandler.postDelayed(progressBarRunnable, 100);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            this.dataWrapper.fillProfileList(true, applicationActivatorPrefIndicator);
            this.dataWrapper.profileList.sort(new ProfileComparator());

            // add restart events
            Profile profile = DataWrapper.getNonInitializedProfile(this.dataWrapper.context.getString(R.string.menu_restart_events), "ic_profile_restart_events|1|0|0", 0);
            this.dataWrapper.profileList.add(0, profile);

            return null;
        }

        @Override
        protected void onPostExecute(Void response) {
            super.onPostExecute(response);
            
            TileChooserListFragment fragment = this.fragmentWeakRef.get();
            
            if ((fragment != null) && (fragment.isAdded())) {
                progressBarHandler.removeCallbacks(progressBarRunnable);
                fragment.progressBar.setVisibility(View.GONE);

                // get local profileList
                this.dataWrapper.fillProfileList(true, applicationActivatorPrefIndicator);

                // set copy local profile list into activity profilesDataWrapper
                fragment.activityDataWrapper.copyProfileList(this.dataWrapper);

                synchronized (fragment.activityDataWrapper.profileList) {
                    if (fragment.activityDataWrapper.profileList.size() == 0)
                        fragment.textViewNoData.setVisibility(View.VISIBLE);
                }

                fragment.profileListAdapter = new TileChooserListAdapter(fragment, fragment.activityDataWrapper);
                fragment.listView.setAdapter(fragment.profileListAdapter);
            }
        }
    }

    private boolean isAsyncTaskPendingOrRunning() {
        return this.asyncTaskContext != null &&
              this.asyncTaskContext.get() != null &&
              !this.asyncTaskContext.get().getStatus().equals(AsyncTask.Status.FINISHED);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if (isAsyncTaskPendingOrRunning()) {
            this.asyncTaskContext.get().cancel(true);
        }

        if (listView != null)
            listView.setAdapter(null);
        if (profileListAdapter != null)
            profileListAdapter.release();

        //if (activityDataWrapper != null)
        //    activityDataWrapper.invalidateDataWrapper();
        activityDataWrapper = null;
    }

    @SuppressLint("StaticFieldLeak")
    void chooseTile(final int position)
    {
        if (getActivity() != null) {
//            PPApplication.logE("TileChooserListFragment.chooseTile", "position=" + position);
            int tileId = ((TileChooserActivity)getActivity()).tileId;
//            PPApplication.logE("TileChooserListFragment.chooseTile", "tileId="+tileId);
            Intent intent = new Intent(PPApplication.PACKAGE_NAME + ".ChooseTileBroadcastReceiver" + tileId);
            intent.putExtra(QuickTileChooseTileBroadcastReceiver.EXTRA_QUICK_TILE_ID, tileId);

            if (position != -1) {
                Profile profile;
                synchronized (activityDataWrapper.profileList) {
                    profile = activityDataWrapper.profileList.get(position);
                }
//                PPApplication.logE("TileChooserListFragment.chooseTile", "profile="+profile);

                if (profile != null) {
                    if (position == 0) {
                        intent.putExtra(PPApplication.EXTRA_PROFILE_ID, Profile.RESTART_EVENTS_PROFILE_ID);
//                        PPApplication.logE("TileChooserListFragment.chooseTile", "profile._id="+Profile.RESTART_EVENTS_PROFILE_ID);
                    }
                    else {
//                        PPApplication.logE("TileChooserListFragment.chooseTile", "profile._id="+profile._id);
                        intent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
                    }
                }
            }

            try {
                if (PPApplication.quickTileChooseTileBroadcastReceiver[tileId] != null) {
                    getActivity().getApplicationContext().unregisterReceiver(PPApplication.quickTileChooseTileBroadcastReceiver[tileId]);
                    //LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).unregisterReceiver(PPApplication.quickTileChooseTileBroadcastReceiver[tileId]);
                    PPApplication.quickTileChooseTileBroadcastReceiver[tileId] = null;
                }
            } catch (Exception ignored) {}
            if (PPApplication.quickTileChooseTileBroadcastReceiver[tileId] == null) {
                PPApplication.quickTileChooseTileBroadcastReceiver[tileId] = new QuickTileChooseTileBroadcastReceiver();
                getActivity().getApplicationContext().registerReceiver(PPApplication.quickTileChooseTileBroadcastReceiver[tileId],
                        new IntentFilter(PPApplication.PACKAGE_NAME + ".ChooseTileBroadcastReceiver" + tileId));
                //LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(PPApplication.quickTileChooseTileBroadcastReceiver[tileId],
                //        new IntentFilter(PPApplication.PACKAGE_NAME + ".ChooseTileBroadcastReceiver"+tileId));
            }

            getActivity().getApplicationContext().sendBroadcast(intent);
            //LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).sendBroadcast(intent);
            getActivity().finish();
//            PPApplication.logE("TileChooserListFragment.chooseTile", "after send broadcast");
        }
    }

}

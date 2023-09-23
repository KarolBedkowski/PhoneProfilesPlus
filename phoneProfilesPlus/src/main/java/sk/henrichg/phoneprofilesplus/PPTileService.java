package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

public class PPTileService extends TileService {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    public void onClick () {
        super.onClick();

        // Called when the user click the tile

        int tileId = getTileId();
        // get profileId from shaered preferences
        PPApplication.quickTileProfileId[tileId] = ApplicationPreferences.getQuickTileProfileId(getApplicationContext(), tileId);
        updateTile();


        boolean isOK = false;
        if ((PPApplication.quickTileProfileId[tileId] != 0) && (PPApplication.quickTileProfileId[tileId] != -1)) {
            boolean profileExists = false;
            if (PPApplication.quickTileProfileId[tileId] != Profile.RESTART_EVENTS_PROFILE_ID) {
                DataWrapper dataWrapper = new DataWrapper(getApplicationContext(), false, 0, false, 0, 0, 0f);
                //TODO len zistovanie, ci profil existuje
                profileExists = dataWrapper.profileExists(PPApplication.quickTileProfileId[tileId]);
                dataWrapper.invalidateDataWrapper();
            }
            if ((PPApplication.quickTileProfileId[tileId] == Profile.RESTART_EVENTS_PROFILE_ID) || profileExists) {
                isOK = true;
                Intent intent = new Intent(getApplicationContext(), BackgroundActivateProfileActivity.class);
                intent.setAction(Intent.ACTION_MAIN);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_QUICK_TILE);
                intent.putExtra(PPApplication.EXTRA_PROFILE_ID, PPApplication.quickTileProfileId[tileId]);
                startActivityAndCollapse(intent);
            }
        }
        if (!isOK) {
            try {
                if (PPApplication.quickTileChooseTileBroadcastReceiver[tileId] != null) {
                    getApplicationContext().unregisterReceiver(PPApplication.quickTileChooseTileBroadcastReceiver[tileId]);
                    //LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(PPApplication.quickTileChooseTileBroadcastReceiver[tileId]);
                    PPApplication.quickTileChooseTileBroadcastReceiver[tileId] = null;
                }
            } catch (Exception ignored) {}
            if (PPApplication.quickTileChooseTileBroadcastReceiver[tileId] == null) {
                PPApplication.quickTileChooseTileBroadcastReceiver[tileId] = new QuickTileChooseTileBroadcastReceiver();
                getApplicationContext().registerReceiver(PPApplication.quickTileChooseTileBroadcastReceiver[tileId],
                        new IntentFilter(PPApplication.PACKAGE_NAME + ".ChooseTileBroadcastReceiver" + tileId));
                //LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(PPApplication.quickTileChooseTileBroadcastReceiver[tileId],
                //        new IntentFilter(PPApplication.PACKAGE_NAME + ".ChooseTileBroadcastReceiver"+tileId));
            }

            Intent intent = new Intent(getApplicationContext(), TileChooserActivity.class);
            intent.setAction(Intent.ACTION_MAIN);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_QUICK_TILE);
            intent.putExtra(TileChooserActivity.EXTRA_TILE_ID, getTileId());
            startActivityAndCollapse(intent);
        }
    }

    @Override
    public void onTileRemoved () {
        super.onTileRemoved();
        // Do something when the user removes the Tile

        // set it inactive when removed
        int tileId = getTileId();
        PPApplication.quickTileProfileId[tileId] = 0;
        ApplicationPreferences.setQuickTileProfileId(getApplicationContext(), tileId, PPApplication.quickTileProfileId[tileId]);
        updateTile();

        final Context appContext = getApplicationContext();
        Runnable runnable = () -> {
//                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=PPTileService.onTileRemoved");

            DataWrapperStatic.setDynamicLauncherShortcuts(appContext);
        };
        PPApplicationStatic.createDelayedGuiExecutor();
        PPApplication.delayedGuiExecutor.submit(runnable);
    }

    @Override
    public void onTileAdded () {
        super.onTileAdded();
        // Do something when the user add the Tile

        // get profileId from SharedPreferences and update it
        int tileId = getTileId();
        PPApplication.quickTileProfileId[tileId] = ApplicationPreferences.getQuickTileProfileId(getApplicationContext(), tileId);
        updateTile();
    }

    @Override
    public void onStartListening () {
        super.onStartListening();
        // Called when the Tile becomes visible

        int tileId = getTileId();
        try {
            if (PPApplication.quickTileChooseTileBroadcastReceiver[tileId] != null) {
                getApplicationContext().unregisterReceiver(PPApplication.quickTileChooseTileBroadcastReceiver[tileId]);
                //LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(PPApplication.quickTileChooseTileBroadcastReceiver[tileId]);
                PPApplication.quickTileChooseTileBroadcastReceiver[tileId] = null;
            }
        } catch (Exception ignored) {}
        if (PPApplication.quickTileChooseTileBroadcastReceiver[tileId] == null) {
            PPApplication.quickTileChooseTileBroadcastReceiver[tileId] = new QuickTileChooseTileBroadcastReceiver();
            getApplicationContext().registerReceiver(PPApplication.quickTileChooseTileBroadcastReceiver[tileId],
                    new IntentFilter(PPApplication.PACKAGE_NAME + ".ChooseTileBroadcastReceiver" + tileId));
            //LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(PPApplication.quickTileChooseTileBroadcastReceiver[tileId],
            //        new IntentFilter(PPApplication.PACKAGE_NAME + ".ChooseTileBroadcastReceiver"+tileId));
        }

        // get profileId of tile from SharedPreferences and update it
        PPApplication.quickTileProfileId[tileId] = ApplicationPreferences.getQuickTileProfileId(getApplicationContext(), tileId);
        updateTile();
    }

    /*
    @Override
    public void onStopListening () {
        super.onStopListening();
        // Called when the tile is no longer visible
    }
    */

    /*
    @Override
    public void onDestroy () {
        super.onDestroy();
//        try {
//            getApplicationContext().unregisterReceiver(chooseTileBroadcastReceiver);
//            //LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(chooseTileBroadcastReceiver);
//        } catch (Exception ignored) {}
    }
    */


    int getTileId() {
        return 0;
    }

    void updateTile() {
        final Tile tile = getQsTile();
        if (tile == null)
            return;
        LocaleHelper.setApplicationLocale(this);

        int tileId = getTileId();

        if ((PPApplication.quickTileProfileId[tileId] != 0) && (PPApplication.quickTileProfileId[tileId] != -1)) {
            //PPApplication.startHandlerThreadWidget();
            //final Handler __handler = new Handler(PPApplication.handlerThreadWidget.getLooper());
            //__handler.post(new PPHandlerThreadRunnable(getApplicationContext(), tile) {
            //__handler.post(() -> {
            Runnable runnable = () -> {
//                            PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThreadWidget", "START run - from=IconWidgetProvider.onReceive");

                //Context appContext= appContextWeakRef.get();
                //Tile tile = tileWeakRef.get();

                //if ((appContext != null) && (tile != null)) {

                    if (PPApplication.quickTileProfileId[tileId] == Profile.RESTART_EVENTS_PROFILE_ID) {
                        tile.setLabel(getString(R.string.menu_restart_events));
                        if (Build.VERSION.SDK_INT >= 29) {
                            tile.setSubtitle(null);
                        }
                        /*
                        DataWrapper dataWrapper = new DataWrapper(getApplicationContext(), false, 0, false, 0, 0, 0f);
                        Profile restartEvents = DataWrapper.getNonInitializedProfile(dataWrapper.context.getString(R.string.menu_restart_events),
                                "ic_profile_restart_events|1|1|"+ApplicationPreferences.applicationRestartEventsIconColor, 0);
                        restartEvents.generateIconBitmap(dataWrapper.context, false, 0, false);
                        tile.setIcon(Icon.createWithBitmap(restartEvents._iconBitmap));
                        */
                        tile.setIcon(Icon.createWithResource(getApplicationContext(), R.drawable.ic_profile_restart_events));
                        tile.setState(Tile.STATE_INACTIVE);
                    }
                    else {
                        DataWrapper dataWrapper = new DataWrapper(getApplicationContext(), false, 0, false, 0, 0, 0f);
                        Profile profile = dataWrapper.getProfileById(PPApplication.quickTileProfileId[tileId], true, false, false);
                        dataWrapper.invalidateDataWrapper();
                        if (profile != null) {
                            tile.setLabel(profile._name);
                            if (Build.VERSION.SDK_INT >= 29) {
                                if (profile._checked)
                                    tile.setSubtitle(getString(R.string.quick_tile_subtile_activated));
                                else
                                    tile.setSubtitle(getString(R.string.quick_tile_subtile_not_activated));
                            }

                            if (profile.getIsIconResourceID()) {
                                if (profile._iconBitmap != null)
                                    tile.setIcon(Icon.createWithBitmap(profile._iconBitmap));
                                else {
                                    int res = ProfileStatic.getIconResource(profile.getIconIdentifier());
                                    tile.setIcon(Icon.createWithResource(getApplicationContext(), res));
                                }
                            } else {
                                tile.setIcon(Icon.createWithBitmap(profile._iconBitmap));
                            }

                            if (profile._checked)
                                tile.setState(Tile.STATE_ACTIVE);
                            else
                                tile.setState(Tile.STATE_INACTIVE);
                        }
                    }
                    tile.updateTile();

                    // save tile profileId into SharedPreferences
                //}
            }; //);
            PPApplicationStatic.createDelayedGuiExecutor();
            PPApplication.delayedGuiExecutor.submit(runnable);
        } else {
            tile.setLabel(getString(R.string.quick_tile_icon_label));
            tile.setIcon(Icon.createWithResource(getApplicationContext(), R.drawable.ic_profile_default));
            tile.setState(Tile.STATE_INACTIVE);
            tile.updateTile();
        }
    }

/*    private static abstract class PPHandlerThreadRunnable implements Runnable {

        final WeakReference<Context> appContextWeakRef;
        final WeakReference<Tile> tileWeakRef;

        PPHandlerThreadRunnable(Context appContext,
                                       Tile tile) {
            this.appContextWeakRef = new WeakReference<>(appContext);
            this.tileWeakRef = new WeakReference<>(tile);
        }

    }*/

}

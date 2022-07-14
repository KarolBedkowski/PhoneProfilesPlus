package sk.henrichg.phoneprofilesplus;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class DisableScreenTimeoutInternalChangeWorker extends Worker {

    static final String WORK_TAG = "disableScreenTimeoutInternalChangeWork";

    public DisableScreenTimeoutInternalChangeWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            long start = System.currentTimeMillis();
            PPApplication.logE("[IN_WORKER]  DisableScreenTimeoutInternalChangeWorker.doWork", "--------------- START");

            /*if (!PPApplication.getApplicationStarted(true))
                // application is not started
                return Result.success();

            boolean foundEnqueued = false;

            WorkManager workManager = PPApplication.getWorkManagerInstance();
            if (workManager != null) {
                ListenableFuture<List<WorkInfo>> statuses;
                statuses = workManager.getWorkInfosForUniqueWork(WORK_TAG);
                try {
                    List<WorkInfo> workInfoList = statuses.get();
                    for (WorkInfo workInfo : workInfoList) {
                        WorkInfo.State state = workInfo.getState();
                        if (state == WorkInfo.State.ENQUEUED) {
                            // any work is already enqueued, is not needed to enqueue new
                            foundEnqueued = true;
                            break;
                        }
                    }
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            //PPApplication.logE("DisableScreenTimeoutInternalChangeWorker.doWork", "foundEnqueued="+foundEnqueued);

            if (!foundEnqueued)*/
                ActivateProfileHelper.disableScreenTimeoutInternalChange = false;

            long finish = System.currentTimeMillis();
            long timeElapsed = finish - start;
            PPApplication.logE("[IN_WORKER]  DisableScreenTimeoutInternalChangeWorker.doWork", "--------------- END - timeElapsed="+timeElapsed);
            return Result.success();
        } catch (Exception e) {
            //Log.e("DisableScreenTimeoutInternalChangeWorker.doWork", Log.getStackTraceString(e));
            PPApplication.recordException(e);
            /*Handler _handler = new Handler(getApplicationContext().getMainLooper());
            Runnable r = new Runnable() {
                public void run() {
                    android.os.Process.killProcess(PPApplication.pid);
                }
            };
            _handler.postDelayed(r, 1000);*/
            return Result.failure();
        }
    }

}

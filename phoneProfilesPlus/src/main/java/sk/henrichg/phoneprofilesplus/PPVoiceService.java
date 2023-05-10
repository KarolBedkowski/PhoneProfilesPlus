package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.service.voice.VoiceInteractionService;
//import kotlin.Metadata;
//import kotlin.jvm.internal.C0326f;

//@Metadata(mo937bv = {1, 0, 3}, mo938d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0006\b\u0007\u0018\u0000 \u000b2\u00020\u0001:\u0001\u000bB\u0007¢\u0006\u0004\b\n\u0010\u0004J\u000f\u0010\u0003\u001a\u00020\u0002H\u0016¢\u0006\u0004\b\u0003\u0010\u0004J\u000f\u0010\u0005\u001a\u00020\u0002H\u0016¢\u0006\u0004\b\u0005\u0010\u0004R\u0016\u0010\t\u001a\u00020\u00068\u0002@\u0002X\u0004¢\u0006\u0006\n\u0004\b\u0007\u0010\b¨\u0006\f"}, mo939d2 = {"Lcom/arlosoft/macrodroid/voiceservice/MacroDroidVoiceService;", "Landroid/service/voice/VoiceInteractionService;", "Lkotlin/n;", "onReady", "()V", "onShutdown", "Landroid/content/BroadcastReceiver;", "c", "Landroid/content/BroadcastReceiver;", "broadcastReceiver", "<init>", "a", "app_standardRelease"}, mo940k = 1, mo941mv = {1, 5, 1})
/* compiled from: MacroDroidVoiceService.kt */
public final class PPVoiceService extends VoiceInteractionService {

    static final String ACTION_ASSISTANT = PPApplication.PACKAGE_NAME + ".ACTION_ASSISTANT";

    /* renamed from: a */
    @SuppressWarnings("unused")
    public static final PPPVoiceServiceInternal voiceServiceInternal = new PPPVoiceServiceInternal(null);

    /* renamed from: c */
    private final BroadcastReceiver voiceServiceBroadcastReceiver = new PPVoiceServiceBroadcastReceiver(this);

    /* renamed from: com.arlosoft.macrodroid.voiceservice.MacroDroidVoiceService$a */
    /* compiled from: MacroDroidVoiceService.kt */
    public static final class PPPVoiceServiceInternal {
        private PPPVoiceServiceInternal() {
        }

        @SuppressWarnings("rawtypes")
        public /* synthetic */ PPPVoiceServiceInternal(@SuppressWarnings("unused") Class fVar) {
            this();
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    public void onReady() {
        super.onReady();
        registerReceiver(this.voiceServiceBroadcastReceiver, new IntentFilter(ACTION_ASSISTANT));
    }

    public void onShutdown() {
        try {
            unregisterReceiver(this.voiceServiceBroadcastReceiver);
        } catch (Exception ignored) {
        }
        super.onShutdown();
    }
}

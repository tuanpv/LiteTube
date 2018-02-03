package com.liteyoutube.youtubelite.player;

import android.content.Intent;
import android.view.MenuItem;

import com.liteyoutube.youtubelite.util.PermissionHelper;

public final class BackgroundPlayerActivity extends ServicePlayerActivity {

    private static final String TAG = "BackgroundPlayerActivity";

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    public String getSupportActionTitle() {
        return getResources().getString(com.liteyoutube.youtubelite.R.string.title_activity_background_player);
    }

    @Override
    public Intent getBindIntent() {
        return new Intent(this, BackgroundPlayer.class);
    }

    @Override
    public void startPlayerListener() {
        if (player != null && player instanceof BackgroundPlayer.BasePlayerImpl) {
            ((BackgroundPlayer.BasePlayerImpl) player).setActivityListener(this);
        }
    }

    @Override
    public void stopPlayerListener() {
        if (player != null && player instanceof BackgroundPlayer.BasePlayerImpl) {
            ((BackgroundPlayer.BasePlayerImpl) player).removeActivityListener(this);
        }
    }

    @Override
    public int getPlayerOptionMenuResource() {
        return com.liteyoutube.youtubelite.R.menu.menu_play_queue_bg;
    }

    @Override
    public boolean onPlayerOptionSelected(MenuItem item) {
        if (item.getItemId() == com.liteyoutube.youtubelite.R.id.action_switch_popup) {

            if (!PermissionHelper.isPopupEnabled(this)) {
                PermissionHelper.showPopupEnablementToast(this);
                return true;
            }

            this.player.setRecovery();
            getApplicationContext().sendBroadcast(getPlayerShutdownIntent());
            getApplicationContext().startService(getSwitchIntent(PopupVideoPlayer.class));
            return true;
        }
        return false;
    }

    @Override
    public Intent getPlayerShutdownIntent() {
        return new Intent(BackgroundPlayer.ACTION_CLOSE);
    }
}

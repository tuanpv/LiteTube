package com.liteyoutube.youtubelite;

import android.app.IntentService;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.liteyoutube.youtubelite.playlist.ChannelPlayQueue;
import com.liteyoutube.youtubelite.playlist.PlaylistPlayQueue;
import com.liteyoutube.youtubelite.playlist.SinglePlayQueue;
import com.liteyoutube.youtubelite.report.UserAction;
import com.liteyoutube.youtubelite.util.ExtractorHelper;
import com.liteyoutube.youtubelite.util.NavigationHelper;
import com.liteyoutube.youtubelite.util.PermissionHelper;
import com.liteyoutube.youtubelite.util.ThemeHelper;

import org.schabi.newpipe.extractor.Info;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.StreamingService.LinkType;
import org.schabi.newpipe.extractor.channel.ChannelInfo;
import org.schabi.newpipe.extractor.playlist.PlaylistInfo;
import org.schabi.newpipe.extractor.stream.StreamInfo;
import com.liteyoutube.youtubelite.player.helper.PlayerHelper;
import com.liteyoutube.youtubelite.playlist.PlayQueue;

import java.io.Serializable;
import java.util.Arrays;

import icepick.State;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Get the url from the intent and open it in the chosen preferred player
 */
public class RouterPlayerActivity extends RouterActivity {

    @State
    protected int currentServiceId = -1;
    private StreamingService currentService;
    @State
    protected LinkType currentLinkType;
    @State
    protected int selectedRadioPosition = -1;
    protected int selectedPreviously = -1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setTheme(ThemeHelper.isLightThemeSelected(this) ? R.style.RouterActivityThemeLight : R.style.RouterActivityThemeDark);
    }

    @Override
    protected void handleUrl(String url) {
        disposables.add(Observable
                .fromCallable(() -> {
                    if (currentServiceId == -1) {
                        currentService = NewPipe.getServiceByUrl(url);
                        currentServiceId = currentService.getServiceId();
                        currentLinkType = currentService.getLinkTypeByUrl(url);
                        currentUrl = NavigationHelper.getCleanUrl(currentService, url, currentLinkType);
                    } else {
                        currentService = NewPipe.getService(currentServiceId);
                    }

                    return currentLinkType != LinkType.NONE;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (result) {
                        onSuccess();
                    } else {
                        onError();
                    }
                }, this::handleError));
    }

    protected void onError() {
        Toast.makeText(this, R.string.url_not_supported_toast, Toast.LENGTH_LONG).show();
        finish();
    }

    protected void onSuccess() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isExtVideoEnabled = preferences.getBoolean(getString(R.string.use_external_video_player_key), false);
        boolean isExtAudioEnabled = preferences.getBoolean(getString(R.string.use_external_audio_player_key), false);

        if ((isExtAudioEnabled || isExtVideoEnabled) && currentLinkType != LinkType.STREAM) {
            Toast.makeText(this, R.string.external_player_unsupported_link_type, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // TODO: Add some sort of "capabilities" field to services (audio only, video and audio, etc.)
        if (currentService == ServiceList.SoundCloud.getService()) {
            handleChoice(getString(R.string.background_player_key));
            return;
        }

        final String playerChoiceKey = preferences.getString(getString(R.string.preferred_player_key), getString(R.string.preferred_player_default));
        final String alwaysAskKey = getString(R.string.always_ask_player_key);

        if (playerChoiceKey.equals(alwaysAskKey)) {
            showDialog();
        } else {
            handleChoice(playerChoiceKey);
        }
    }

    private void showDialog() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final ContextThemeWrapper themeWrapper = new ContextThemeWrapper(this,
                ThemeHelper.isLightThemeSelected(this) ? R.style.LightTheme : R.style.DarkTheme);

        LayoutInflater inflater = LayoutInflater.from(themeWrapper);
        final LinearLayout rootLayout = (LinearLayout) inflater.inflate(R.layout.preferred_player_dialog_view, null, false);
        final RadioGroup radioGroup = rootLayout.findViewById(android.R.id.list);

        final AdapterChoiceItem[] choices = {
                new AdapterChoiceItem(getString(R.string.video_player_key), getString(R.string.video_player),
                        ThemeHelper.resolveResourceIdFromAttr(themeWrapper, R.attr.play)),
                new AdapterChoiceItem(getString(R.string.background_player_key), getString(R.string.background_player),
                        ThemeHelper.resolveResourceIdFromAttr(themeWrapper, R.attr.audio)),
                new AdapterChoiceItem(getString(R.string.popup_player_key), getString(R.string.popup_player),
                        ThemeHelper.resolveResourceIdFromAttr(themeWrapper, R.attr.popup))
        };

        final DialogInterface.OnClickListener dialogButtonsClickListener = (dialog, which) -> {
            final int indexOfChild = radioGroup.indexOfChild(radioGroup.findViewById(radioGroup.getCheckedRadioButtonId()));
            final AdapterChoiceItem choice = choices[indexOfChild];

            handleChoice(choice.key);

            if (which == DialogInterface.BUTTON_POSITIVE) {
                preferences.edit().putString(getString(R.string.preferred_player_key), choice.key).apply();
            }
        };

        final AlertDialog alertDialog = new AlertDialog.Builder(themeWrapper)
                .setTitle(R.string.preferred_player_share_menu_title)
                .setView(radioGroup)
                .setCancelable(true)
                .setNegativeButton(R.string.just_once, dialogButtonsClickListener)
                .setPositiveButton(R.string.always, dialogButtonsClickListener)
                .setOnDismissListener((dialog) -> finish())
                .create();

        alertDialog.setOnShowListener(dialog -> {
            setDialogButtonsState(alertDialog, radioGroup.getCheckedRadioButtonId() != -1);
        });

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> setDialogButtonsState(alertDialog, true));
        final View.OnClickListener radioButtonsClickListener = v -> {
            final int indexOfChild = radioGroup.indexOfChild(v);
            if (indexOfChild == -1) return;

            selectedPreviously = selectedRadioPosition;
            selectedRadioPosition = indexOfChild;

            if (selectedPreviously == selectedRadioPosition) {
                handleChoice(choices[selectedRadioPosition].key);
            }
        };

        int id = 12345;
        for (AdapterChoiceItem item : choices) {
            final RadioButton radioButton = (RadioButton) inflater.inflate(R.layout.list_radio_icon_item, null);
            radioButton.setText(item.description);
            radioButton.setCompoundDrawablesWithIntrinsicBounds(item.icon, 0, 0, 0);
            radioButton.setChecked(false);
            radioButton.setId(id++);
            radioButton.setLayoutParams(new RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            radioButton.setOnClickListener(radioButtonsClickListener);
            radioGroup.addView(radioButton);
        }

        if (selectedRadioPosition == -1) {
            final String lastSelectedPlayer = preferences.getString(getString(R.string.preferred_player_last_selected_key), null);
            if (!TextUtils.isEmpty(lastSelectedPlayer)) {
                for (int i = 0; i < choices.length; i++) {
                    AdapterChoiceItem c = choices[i];
                    if (lastSelectedPlayer.equals(c.key)) {
                        selectedRadioPosition = i;
                        break;
                    }
                }
            }
        }

        selectedRadioPosition = Math.min(Math.max(-1, selectedRadioPosition), choices.length - 1);
        if (selectedRadioPosition != -1) {
            ((RadioButton) radioGroup.getChildAt(selectedRadioPosition)).setChecked(true);
        }
        selectedPreviously = selectedRadioPosition;

        alertDialog.show();
    }

    private void setDialogButtonsState(AlertDialog dialog, boolean state) {
        final Button negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        final Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if (negativeButton == null || positiveButton == null) return;

        negativeButton.setEnabled(state);
        positiveButton.setEnabled(state);
    }

    private void handleChoice(final String playerChoiceKey) {
        if (Arrays.asList(getResources().getStringArray(R.array.preferred_player_values_list)).contains(playerChoiceKey)) {
            PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putString(getString(R.string.preferred_player_last_selected_key), playerChoiceKey).apply();
        }

        if (playerChoiceKey.equals(getString(R.string.popup_player_key)) && !PermissionHelper.isPopupEnabled(this)) {
            PermissionHelper.showPopupEnablementToast(this);
            finish();
            return;
        }

        final Intent intent = new Intent(this, FetcherService.class);
        intent.putExtra(FetcherService.KEY_CHOICE, new Choice(currentService.getServiceId(), currentLinkType, currentUrl, playerChoiceKey));
        startService(intent);

        finish();
    }

    private static class AdapterChoiceItem {
        final String description, key;
        @DrawableRes
        final int icon;

        AdapterChoiceItem(String key, String description, int icon) {
            this.description = description;
            this.key = key;
            this.icon = icon;
        }
    }

    private static class Choice implements Serializable {
        final int serviceId;
        final String url, playerChoice;
        final LinkType linkType;

        Choice(int serviceId, LinkType linkType, String url, String playerChoice) {
            this.serviceId = serviceId;
            this.linkType = linkType;
            this.url = url;
            this.playerChoice = playerChoice;
        }

        @Override
        public String toString() {
            return serviceId + ":" + url + " > " + linkType + " ::: " + playerChoice;
        }
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Service Fetcher
    //////////////////////////////////////////////////////////////////////////*/

    public static class FetcherService extends IntentService {

        private static final int ID = 456;
        public static final String KEY_CHOICE = "key_choice";
        private Disposable fetcher;

        public FetcherService() {
            super(FetcherService.class.getSimpleName());
        }

        @Override
        public void onCreate() {
            super.onCreate();
            startForeground(ID, createNotification().build());
        }

        @Override
        protected void onHandleIntent(@Nullable Intent intent) {
            if (intent == null) return;

            final Serializable serializable = intent.getSerializableExtra(KEY_CHOICE);
            if (!(serializable instanceof Choice)) return;
            Choice playerChoice = (Choice) serializable;
            handleChoice(playerChoice);
        }

        public void handleChoice(Choice choice) {
            Single<? extends Info> single = null;
            UserAction userAction = UserAction.SOMETHING_ELSE;

            switch (choice.linkType) {
                case STREAM:
                    single = ExtractorHelper.getStreamInfo(choice.serviceId, choice.url, false);
                    userAction = UserAction.REQUESTED_STREAM;
                    break;
                case CHANNEL:
                    single = ExtractorHelper.getChannelInfo(choice.serviceId, choice.url, false);
                    userAction = UserAction.REQUESTED_CHANNEL;
                    break;
                case PLAYLIST:
                    single = ExtractorHelper.getPlaylistInfo(choice.serviceId, choice.url, false);
                    userAction = UserAction.REQUESTED_PLAYLIST;
                    break;
            }


            if (single != null) {
                final UserAction finalUserAction = userAction;
                final Consumer<Info> resultHandler = getResultHandler(choice);
                fetcher = single
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(info -> {
                            resultHandler.accept(info);
                            if (fetcher != null) fetcher.dispose();
                        }, throwable -> ExtractorHelper.handleGeneralException(this,
                                choice.serviceId, choice.url, throwable, finalUserAction, ", opened with " + choice.playerChoice));
            }
        }

        public Consumer<Info> getResultHandler(Choice choice) {
            return info -> {
                final String videoPlayerKey = getString(R.string.video_player_key);
                final String backgroundPlayerKey = getString(R.string.background_player_key);
                final String popupPlayerKey = getString(R.string.popup_player_key);

                final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                boolean isExtVideoEnabled = preferences.getBoolean(getString(R.string.use_external_video_player_key), false);
                boolean isExtAudioEnabled = preferences.getBoolean(getString(R.string.use_external_audio_player_key), false);
                boolean useOldVideoPlayer = PlayerHelper.isUsingOldPlayer(this);

                PlayQueue playQueue;
                String playerChoice = choice.playerChoice;

                if (info instanceof StreamInfo) {
                    if (playerChoice.equals(backgroundPlayerKey) && isExtAudioEnabled) {
                        NavigationHelper.playOnExternalAudioPlayer(this, (StreamInfo) info);

                    } else if (playerChoice.equals(videoPlayerKey) && isExtVideoEnabled) {
                        NavigationHelper.playOnExternalVideoPlayer(this, (StreamInfo) info);

                    } else if (playerChoice.equals(videoPlayerKey) && useOldVideoPlayer) {
                        NavigationHelper.playOnOldVideoPlayer(this, (StreamInfo) info);

                    } else {
                        playQueue = new SinglePlayQueue((StreamInfo) info);

                        if (playerChoice.equals(videoPlayerKey)) {
                            NavigationHelper.playOnMainPlayer(this, playQueue);
                        } else if (playerChoice.equals(backgroundPlayerKey)) {
                            NavigationHelper.enqueueOnBackgroundPlayer(this, playQueue, true);
                        } else if (playerChoice.equals(popupPlayerKey)) {
                            NavigationHelper.enqueueOnPopupPlayer(this, playQueue, true);
                        }
                    }
                }

                if (info instanceof ChannelInfo || info instanceof PlaylistInfo) {
                    playQueue = info instanceof ChannelInfo ? new ChannelPlayQueue((ChannelInfo) info) : new PlaylistPlayQueue((PlaylistInfo) info);

                    if (playerChoice.equals(videoPlayerKey)) {
                        NavigationHelper.playOnMainPlayer(this, playQueue);
                    } else if (playerChoice.equals(backgroundPlayerKey)) {
                        NavigationHelper.playOnBackgroundPlayer(this, playQueue);
                    } else if (playerChoice.equals(popupPlayerKey)) {
                        NavigationHelper.playOnPopupPlayer(this, playQueue);
                    }
                }
            };
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            stopForeground(true);
            if (fetcher != null) fetcher.dispose();
        }

        private NotificationCompat.Builder createNotification() {
            return new NotificationCompat.Builder(this, getString(R.string.notification_channel_id))
                    .setOngoing(true)
                    .setSmallIcon(R.drawable.ic_newpipe_triangle_white)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setContentTitle(getString(R.string.preferred_player_fetcher_notification_title))
                    .setContentText(getString(R.string.preferred_player_fetcher_notification_message));
        }
    }
}

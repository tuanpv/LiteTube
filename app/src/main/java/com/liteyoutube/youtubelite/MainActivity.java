/*
 * Created by Christian Schabesberger on 02.08.16.
 * <p>
 * Copyright (C) Christian Schabesberger 2016 <chris.schabesberger@mailbox.org>
 * DownloadActivity.java is part of NewPipe.
 * <p>
 * NewPipe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * NewPipe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with NewPipe.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.liteyoutube.youtubelite;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.liteyoutube.youtubelite.database.AppDatabase;
import com.liteyoutube.youtubelite.database.history.dao.SearchHistoryDAO;
import com.liteyoutube.youtubelite.database.history.dao.WatchHistoryDAO;
import com.liteyoutube.youtubelite.fragments.detail.VideoDetailFragment;
import com.liteyoutube.youtubelite.util.AppRater;
import com.liteyoutube.youtubelite.util.Connectivity;
import com.liteyoutube.youtubelite.util.Constants;
import com.liteyoutube.youtubelite.util.NavigationHelper;
import com.liteyoutube.youtubelite.util.ThemeHelper;
import com.liteyoutube.youtubelite.database.history.dao.HistoryDAO;
import com.liteyoutube.youtubelite.database.history.model.HistoryEntry;
import com.liteyoutube.youtubelite.database.history.model.SearchHistoryEntry;
import com.liteyoutube.youtubelite.database.history.model.WatchHistoryEntry;

import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.StreamInfo;
import org.schabi.newpipe.extractor.stream.VideoStream;
import com.liteyoutube.youtubelite.fragments.BackPressable;
import com.liteyoutube.youtubelite.fragments.MainFragment;
import com.liteyoutube.youtubelite.fragments.list.search.SearchFragment;
import com.liteyoutube.youtubelite.history.HistoryListener;
import com.liteyoutube.youtubelite.util.ServiceHelper;
import com.liteyoutube.youtubelite.util.StateSaver;

import java.util.Date;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class MainActivity extends AppCompatActivity implements HistoryListener {
    private static final String TAG = "MainActivity";
    public static final boolean DEBUG = !BuildConfig.BUILD_TYPE.equals("release");

    private SharedPreferences sharedPreferences;
    private ActionBarDrawerToggle toggle = null;

    private AdRequest adRequest;
    private AdView adView;
    private InterstitialAd interstitial;
    private final static int LIMIT_INDEX = 7;
    //    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private int index = 0;
    private final static String INDEX_ADMOB = "index_admob";
    private final int AT_STREAM = 1;
    private final int AT_CHANNEL = 2;
    private final int AT_PLAYLIST = 3;
    private int standingByAt = 0;
    Intent mIntent;
    String url ;
    int serviceId ;
    String title;

    private SharedPreferences mPreferences;

    /*//////////////////////////////////////////////////////////////////////////
    // Activity's LifeCycle
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) Log.d(TAG, "onCreate() called with: savedInstanceState = [" + savedInstanceState + "]");

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        ThemeHelper.setTheme(this, ServiceHelper.getSelectedServiceId(this));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppRater.app_launched(this);

        if (getSupportFragmentManager() != null && getSupportFragmentManager().getBackStackEntryCount() == 0) {
            initFragments();
        }

        setSupportActionBar(findViewById(R.id.toolbar));
        setupDrawer();
        initHistory();

        // Preferences
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            editor = mPreferences.edit();
            index = mPreferences.getInt(INDEX_ADMOB, 1);
            if (index % LIMIT_INDEX == 0) {
                index--;
            } else {
                index++;
            }
            editor.putInt(INDEX_ADMOB, index).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (Connectivity.isConnected(this)) {
            adView = new AdView(this);
            adView.setAdSize(AdSize.SMART_BANNER);
            final LinearLayout layout = findViewById(R.id.list_grid_ll_ad);
            layout.setVisibility(View.VISIBLE);
            layout.addView(adView);
            adView.setAdUnitId(getString(R.string.banner_lite_tube));
            adRequest = new AdRequest.Builder()/*.addTestDevice("B36624619B602BB4EEEEB3EE6B0ECBC3")*/.build();
            // Start loading the ad in the background.
            adView.loadAd(adRequest);
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(int i) {
                    super.onAdFailedToLoad(i);
                    layout.setVisibility(View.GONE);
                    adView.loadAd(adRequest);
                }

                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    layout.setVisibility(View.VISIBLE);
                }
            });
        }


        // Create the interstitial.
        interstitial = new InterstitialAd(this);
        interstitial.setAdUnitId(getString(R.string.interstitial_lite_tube));
        requestNewInterstitial();
        interstitial.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int i) {
                requestNewInterstitial();
            }

            @Override
            public void onAdClosed() {
                requestNewInterstitial();

                switch (standingByAt) {
                    case AT_STREAM:
                        boolean autoPlay = false;
                        if (mIntent != null) {
                            autoPlay = mIntent.getBooleanExtra(VideoDetailFragment.AUTO_PLAY, false);
                        }
                        NavigationHelper.openVideoDetailFragment(getSupportFragmentManager(), serviceId, url, title, autoPlay);
                        break;
                    case AT_CHANNEL:
                        NavigationHelper.openChannelFragment(getSupportFragmentManager(), serviceId, url, title);
                        break;
                    case AT_PLAYLIST:
                        NavigationHelper.openPlaylistFragment(getSupportFragmentManager(), serviceId, url, title);
                        break;
                }
            }
        });
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("B36624619B602BB4EEEEB3EE6B0ECBC3")
                .build();
        interstitial.loadAd(adRequest);
    }

    private void setupDrawer() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        final NavigationView drawerItems = findViewById(R.id.navigation);

        drawerItems.setItemIconTintList(null);
        drawerItems.getMenu().getItem(ServiceHelper.getSelectedServiceId(this)).setChecked(true);

        if (!BuildConfig.BUILD_TYPE.equals("release")) {
            toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.drawer_open, R.string.drawer_close);
            toggle.syncState();
            drawer.addDrawerListener(toggle);
            drawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
                private int lastService;

                @Override
                public void onDrawerOpened(View drawerView) {
                    lastService = ServiceHelper.getSelectedServiceId(MainActivity.this);
                }

                @Override
                public void onDrawerClosed(View drawerView) {
                    if (lastService != ServiceHelper.getSelectedServiceId(MainActivity.this)) {
                        new Handler(Looper.getMainLooper()).post(MainActivity.this::recreate);
                    }
                }
            });

            drawerItems.setNavigationItemSelectedListener(item -> {
                if (item.getGroupId() == R.id.menu_services_group) {
                    drawerItems.getMenu().getItem(ServiceHelper.getSelectedServiceId(this)).setChecked(false);
                    ServiceHelper.setSelectedServiceId(this, item.getTitle().toString());
                    drawerItems.getMenu().getItem(ServiceHelper.getSelectedServiceId(this)).setChecked(true);
                }
                drawer.closeDrawers();
                return true;
            });
        } else {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!isChangingConfigurations()) {
            StateSaver.clearStateFiles();
        }

        disposeHistory();
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean(Constants.KEY_THEME_CHANGE, false)) {
            if (DEBUG) Log.d(TAG, "Theme has changed, recreating activity...");
            sharedPreferences.edit().putBoolean(Constants.KEY_THEME_CHANGE, false).apply();
            // https://stackoverflow.com/questions/10844112/runtimeexception-performing-pause-of-activity-that-is-not-resumed
            // Briefly, let the activity resume properly posting the recreate call to end of the message queue
            new Handler(Looper.getMainLooper()).post(MainActivity.this::recreate);
        }

        if (sharedPreferences.getBoolean(Constants.KEY_MAIN_PAGE_CHANGE, false)) {
            if (DEBUG) Log.d(TAG, "main page has changed, recreating main fragment...");
            sharedPreferences.edit().putBoolean(Constants.KEY_MAIN_PAGE_CHANGE, false).apply();
            NavigationHelper.openMainActivity(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (DEBUG) Log.d(TAG, "onNewIntent() called with: intent = [" + intent + "]");
        if (intent != null) {
            // Return if launched from a launcher (e.g. Nova Launcher, Pixel Launcher ...)
            // to not destroy the already created backstack
            String action = intent.getAction();
            if ((action != null && action.equals(Intent.ACTION_MAIN)) && intent.hasCategory(Intent.CATEGORY_LAUNCHER)) return;
        }

        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    public void onBackPressed() {
        if (DEBUG) Log.d(TAG, "onBackPressed() called");

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_holder);
        // If current fragment implements BackPressable (i.e. can/wanna handle back press) delegate the back press to it
        if (fragment instanceof BackPressable) {
            if (((BackPressable) fragment).onBackPressed()) return;
        }


        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            finish();
        } else super.onBackPressed();
    }

    private void onHomeButtonPressed() {
        NavigationHelper.gotoMainFragment(getSupportFragmentManager(), MainActivity.this);
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Menu
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (DEBUG) Log.d(TAG, "onCreateOptionsMenu() called with: menu = [" + menu + "]");
        super.onCreateOptionsMenu(menu);

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_holder);
        if (!(fragment instanceof VideoDetailFragment)) {
            findViewById(R.id.toolbar).findViewById(R.id.toolbar_spinner).setVisibility(View.GONE);
        }

        if (!(fragment instanceof SearchFragment)) {
            findViewById(R.id.toolbar).findViewById(R.id.toolbar_search_container).setVisibility(View.GONE);

            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.main_menu, menu);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }

        updateDrawerNavigation();

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (DEBUG) Log.d(TAG, "onOptionsItemSelected() called with: item = [" + item + "]");
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                onHomeButtonPressed();
                return true;
            case R.id.action_settings:
                NavigationHelper.openSettings(this);
                return true;
//            case R.id.action_show_downloads:
//                return NavigationHelper.openDownloads(this);
//            case R.id.action_about:
//                NavigationHelper.openAbout(this);
//                return true;
            case R.id.action_history:
                NavigationHelper.openHistory(this);
                return true;
            case R.id.menu_item_rate:
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.liteyoutube.youtubelite")));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Init
    //////////////////////////////////////////////////////////////////////////*/

    private void initFragments() {
        if (DEBUG) Log.d(TAG, "initFragments() called");
        StateSaver.clearStateFiles();
        if (getIntent() != null && getIntent().hasExtra(Constants.KEY_LINK_TYPE)) {
            handleIntent(getIntent());
        } else NavigationHelper.gotoMainFragment(getSupportFragmentManager(), MainActivity.this);
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Utils
    //////////////////////////////////////////////////////////////////////////*/

    private void updateDrawerNavigation() {
        if (getSupportActionBar() == null) return;

        final Toolbar toolbar = findViewById(R.id.toolbar);
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);

        final Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_holder);
        if (fragment instanceof MainFragment) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            if (toggle != null) {
                toggle.syncState();
                toolbar.setNavigationOnClickListener(v -> drawer.openDrawer(GravityCompat.START));
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNDEFINED);
            }
        } else {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> onHomeButtonPressed());
        }
    }

    private void handleIntent(Intent intent) {
        if (DEBUG) Log.d(TAG, "handleIntent() called with: intent = [" + intent + "]");

        if (intent.hasExtra(Constants.KEY_LINK_TYPE)) {
            mIntent = intent;
             url = intent.getStringExtra(Constants.KEY_URL);
             serviceId = intent.getIntExtra(Constants.KEY_SERVICE_ID, 0);
             title = intent.getStringExtra(Constants.KEY_TITLE);
            switch (((StreamingService.LinkType) intent.getSerializableExtra(Constants.KEY_LINK_TYPE))) {
                case STREAM:
                    standingByAt = AT_STREAM;
                    try {
                        index = mPreferences.getInt(INDEX_ADMOB, 0);
                        index++;

                        if (Connectivity.isConnected(this)) {
                            if (index % LIMIT_INDEX == 0) {
                                index = 0;
                                if (interstitial.isLoaded()) {
                                    interstitial.show();
                                } else {
                                    index--;
                                    boolean autoPlay = intent.getBooleanExtra(VideoDetailFragment.AUTO_PLAY, false);
                                    NavigationHelper.openVideoDetailFragment(getSupportFragmentManager(), serviceId, url, title, autoPlay);
                                }
                            } else {
                                boolean autoPlay = intent.getBooleanExtra(VideoDetailFragment.AUTO_PLAY, false);
                                NavigationHelper.openVideoDetailFragment(getSupportFragmentManager(), serviceId, url, title, autoPlay);
                            }
                        } else {
                            if (index % LIMIT_INDEX == 0) {
                                index--;
                            }
                            boolean autoPlay = intent.getBooleanExtra(VideoDetailFragment.AUTO_PLAY, false);
                            NavigationHelper.openVideoDetailFragment(getSupportFragmentManager(), serviceId, url, title, autoPlay);
                        }
                        editor.putInt(INDEX_ADMOB, index).apply();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

//                    boolean autoPlay = intent.getBooleanExtra(VideoDetailFragment.AUTO_PLAY, false);
//                    NavigationHelper.openVideoDetailFragment(getSupportFragmentManager(), serviceId, url, title, autoPlay);
                    break;
                case CHANNEL:
                    standingByAt = AT_CHANNEL;
                    try {
                        index = mPreferences.getInt(INDEX_ADMOB, 0);
                        index++;

                        if (Connectivity.isConnected(this)) {
                            if (index % LIMIT_INDEX == 0) {
                                index = 0;
                                if (interstitial.isLoaded()) {
                                    interstitial.show();
                                } else {
                                    index--;
                                    NavigationHelper.openChannelFragment(getSupportFragmentManager(), serviceId, url, title);
                                }
                            } else {
                                NavigationHelper.openChannelFragment(getSupportFragmentManager(), serviceId, url, title);
                            }
                        } else {
                            if (index % LIMIT_INDEX == 0) {
                                index--;
                            }
                            NavigationHelper.openChannelFragment(getSupportFragmentManager(), serviceId, url, title);
                        }
                        editor.putInt(INDEX_ADMOB, index).apply();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

//                    NavigationHelper.openChannelFragment(getSupportFragmentManager(), serviceId, url, title);
                    break;
                case PLAYLIST:
                    standingByAt = AT_PLAYLIST;
                    try {
                        index = mPreferences.getInt(INDEX_ADMOB, 0);
                        index++;

                        if (Connectivity.isConnected(this)) {
                            if (index % LIMIT_INDEX == 0) {
                                index = 0;
                                if (interstitial.isLoaded()) {
                                    interstitial.show();
                                } else {
                                    index--;
                                    NavigationHelper.openPlaylistFragment(getSupportFragmentManager(), serviceId, url, title);
                                }
                            } else {
                                NavigationHelper.openPlaylistFragment(getSupportFragmentManager(), serviceId, url, title);
                            }
                        } else {
                            if (index % LIMIT_INDEX == 0) {
                                index--;
                            }
                            NavigationHelper.openPlaylistFragment(getSupportFragmentManager(), serviceId, url, title);
                        }
                        editor.putInt(INDEX_ADMOB, index).apply();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

//                    NavigationHelper.openPlaylistFragment(getSupportFragmentManager(), serviceId, url, title);
                    break;
            }

        } else if (intent.hasExtra(Constants.KEY_OPEN_SEARCH)) {
            String searchQuery = intent.getStringExtra(Constants.KEY_QUERY);
            if (searchQuery == null) searchQuery = "";
            int serviceId = intent.getIntExtra(Constants.KEY_SERVICE_ID, 0);
            NavigationHelper.openSearchFragment(getSupportFragmentManager(), serviceId, searchQuery);
        } else {
            NavigationHelper.gotoMainFragment(getSupportFragmentManager(), MainActivity.this);
        }
    }

    /*//////////////////////////////////////////////////////////////////////////
    // History
    //////////////////////////////////////////////////////////////////////////*/

    private WatchHistoryDAO watchHistoryDAO;
    private SearchHistoryDAO searchHistoryDAO;
    private PublishSubject<HistoryEntry> historyEntrySubject;
    private Disposable disposable;

    private void initHistory() {
        final AppDatabase database = NewPipeDatabase.getInstance();
        watchHistoryDAO = database.watchHistoryDAO();
        searchHistoryDAO = database.searchHistoryDAO();
        historyEntrySubject = PublishSubject.create();
        disposable = historyEntrySubject
                .observeOn(Schedulers.io())
                .subscribe(getHistoryEntryConsumer());
    }

    private void disposeHistory() {
        if (disposable != null) disposable.dispose();
        watchHistoryDAO = null;
        searchHistoryDAO = null;
    }

    @NonNull
    private Consumer<HistoryEntry> getHistoryEntryConsumer() {
        return new Consumer<HistoryEntry>() {
            @Override
            public void accept(HistoryEntry historyEntry) throws Exception {
                //noinspection unchecked
                HistoryDAO<HistoryEntry> historyDAO = (HistoryDAO<HistoryEntry>)
                        (historyEntry instanceof SearchHistoryEntry ? searchHistoryDAO : watchHistoryDAO);

                HistoryEntry latestEntry = historyDAO.getLatestEntry();
                if (historyEntry.hasEqualValues(latestEntry)) {
                    latestEntry.setCreationDate(historyEntry.getCreationDate());
                    historyDAO.update(latestEntry);
                } else {
                    historyDAO.insert(historyEntry);
                }
            }
        };
    }

    private void addWatchHistoryEntry(StreamInfo streamInfo) {
        if (sharedPreferences.getBoolean(getString(R.string.enable_watch_history_key), true)) {
            WatchHistoryEntry entry = new WatchHistoryEntry(streamInfo);
            historyEntrySubject.onNext(entry);
        }
    }

    @Override
    public void onVideoPlayed(StreamInfo streamInfo, @Nullable VideoStream videoStream) {
        addWatchHistoryEntry(streamInfo);
    }

    @Override
    public void onAudioPlayed(StreamInfo streamInfo, AudioStream audioStream) {
        addWatchHistoryEntry(streamInfo);
    }

    @Override
    public void onSearch(int serviceId, String query) {
        // Add search history entry
        if (sharedPreferences.getBoolean(getString(R.string.enable_search_history_key), true)) {
            SearchHistoryEntry searchHistoryEntry = new SearchHistoryEntry(new Date(), serviceId, query);
            historyEntrySubject.onNext(searchHistoryEntry);
        }
    }
}

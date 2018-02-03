package com.liteyoutube.youtubelite.fragments.list.kiosk;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.liteyoutube.youtubelite.util.KioskTranslator;
import com.liteyoutube.youtubelite.R;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.UrlIdHandler;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.kiosk.KioskInfo;

import com.liteyoutube.youtubelite.fragments.list.BaseListInfoFragment;
import com.liteyoutube.youtubelite.report.UserAction;
import com.liteyoutube.youtubelite.util.ExtractorHelper;
import com.liteyoutube.youtubelite.util.NavigationHelper;
import com.liteyoutube.youtubelite.util.ServiceHelper;

import icepick.State;
import io.reactivex.Single;

import static com.liteyoutube.youtubelite.util.AnimationUtils.animateView;

/**
 * Created by Christian Schabesberger on 23.09.17.
 *
 * Copyright (C) Christian Schabesberger 2017 <chris.schabesberger@mailbox.org>
 * KioskFragment.java is part of NewPipe.
 *
 * NewPipe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe.  If not, see <http://www.gnu.org/licenses/>.
 */

public class KioskFragment extends BaseListInfoFragment<KioskInfo> {

    @State
    protected String kioskId = "";
    protected String kioskTranslatedName;

    /*//////////////////////////////////////////////////////////////////////////
    // Views
    //////////////////////////////////////////////////////////////////////////*/

    public static KioskFragment getInstance(int serviceId)
            throws ExtractionException {
        return getInstance(serviceId, NewPipe.getService(serviceId)
                .getKioskList()
                .getDefaultKioskId());
    }

    public static KioskFragment getInstance(int serviceId, String kioskId)
            throws ExtractionException {
        KioskFragment instance = new KioskFragment();
        StreamingService service = NewPipe.getService(serviceId);
        UrlIdHandler kioskTypeUrlIdHandler = service.getKioskList()
                .getUrlIdHandlerByType(kioskId);
        instance.setInitialData(serviceId,
                kioskTypeUrlIdHandler.getUrl(kioskId),
                kioskId);
        instance.kioskId = kioskId;
        return instance;
    }

    /*//////////////////////////////////////////////////////////////////////////
    // LifeCycle
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        kioskTranslatedName = KioskTranslator.getTranslatedKioskName(kioskId, activity);
        name = kioskTranslatedName;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(useAsFrontPage && isVisibleToUser && activity != null) {
            try {
                setTitle(kioskTranslatedName);
            } catch (Exception e) {
                onUnrecoverableError(e, UserAction.UI_ERROR,
                        "none",
                        "none", R.string.app_ui_crash);
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_kiosk, container, false);
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Menu
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (DEBUG) Log.d(TAG, "onCreateOptionsMenu() called with: menu = [" + menu + "], inflater = [" + inflater + "]");
        inflater.inflate(R.menu.main_fragment_menu, menu);

        ActionBar supportActionBar = activity.getSupportActionBar();
        if (supportActionBar != null && useAsFrontPage) {
            supportActionBar.setDisplayHomeAsUpEnabled(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                NavigationHelper.openSearchFragment(getFragmentManager(), ServiceHelper.getSelectedServiceId(activity), "");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Load and handle
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public Single<KioskInfo> loadResult(boolean forceReload) {
        String contentCountry = PreferenceManager
                .getDefaultSharedPreferences(activity)
                .getString(getString(R.string.content_country_key),
                        getString(R.string.default_country_value));
        return ExtractorHelper.getKioskInfo(serviceId, url, contentCountry, forceReload);
    }

    @Override
    public Single<ListExtractor.NextItemsResult> loadMoreItemsLogic() {
        String contentCountry = PreferenceManager
                .getDefaultSharedPreferences(activity)
                .getString(getString(R.string.content_country_key),
                        getString(R.string.default_country_value));
        return ExtractorHelper.getMoreKioskItems(serviceId, url, currentNextItemsUrl, contentCountry);
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Contract
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public void showLoading() {
        super.showLoading();
        animateView(itemsList, false, 100);
    }

    @Override
    public void handleResult(@NonNull final KioskInfo result) {
        super.handleResult(result);

        name = kioskTranslatedName;
        setTitle(kioskTranslatedName);

        if (!result.getErrors().isEmpty()) {
            showSnackBarError(result.getErrors(),
                    UserAction.REQUESTED_PLAYLIST,
                    NewPipe.getNameOfService(result.getServiceId()), result.getUrl(), 0);
        }
    }

    @Override
    public void handleNextItems(ListExtractor.NextItemsResult result) {
        super.handleNextItems(result);

        if (!result.getErrors().isEmpty()) {
            showSnackBarError(result.getErrors(),
                    UserAction.REQUESTED_PLAYLIST, NewPipe.getNameOfService(serviceId)
                    , "Get next page of: " + url, 0);
        }
    }
}

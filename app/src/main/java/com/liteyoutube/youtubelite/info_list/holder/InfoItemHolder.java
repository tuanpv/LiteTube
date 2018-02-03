package com.liteyoutube.youtubelite.info_list.holder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.liteyoutube.youtubelite.info_list.InfoItemBuilder;

import org.schabi.newpipe.extractor.InfoItem;

/*
 * Created by Christian Schabesberger on 12.02.17.
 *
 * Copyright (C) Christian Schabesberger 2016 <chris.schabesberger@mailbox.org>
 * InfoItemHolder.java is part of NewPipe.
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

public abstract class InfoItemHolder extends RecyclerView.ViewHolder {
    protected final InfoItemBuilder itemBuilder;

    public InfoItemHolder(InfoItemBuilder infoItemBuilder, int layoutId, ViewGroup parent) {
        super(LayoutInflater.from(infoItemBuilder.getContext()).inflate(layoutId, parent, false));
        this.itemBuilder = infoItemBuilder;
    }

    public abstract void updateFromItem(final InfoItem infoItem);

    /*//////////////////////////////////////////////////////////////////////////
    // ImageLoaderOptions
    //////////////////////////////////////////////////////////////////////////*/

    /**
     * Base display options
     */
    public static final DisplayImageOptions BASE_DISPLAY_IMAGE_OPTIONS =
            new DisplayImageOptions.Builder()
                    .cacheInMemory(true)
                    .build();
}

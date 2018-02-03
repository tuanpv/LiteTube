package com.liteyoutube.youtubelite.info_list;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.channel.ChannelInfoItem;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import com.liteyoutube.youtubelite.info_list.holder.ChannelInfoItemHolder;
import com.liteyoutube.youtubelite.info_list.holder.ChannelMiniInfoItemHolder;
import com.liteyoutube.youtubelite.info_list.holder.InfoItemHolder;
import com.liteyoutube.youtubelite.info_list.holder.PlaylistInfoItemHolder;
import com.liteyoutube.youtubelite.info_list.holder.StreamInfoItemHolder;
import com.liteyoutube.youtubelite.info_list.holder.StreamMiniInfoItemHolder;

/*
 * Created by Christian Schabesberger on 26.09.16.
 * <p>
 * Copyright (C) Christian Schabesberger 2016 <chris.schabesberger@mailbox.org>
 * InfoItemBuilder.java is part of NewPipe.
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

public class InfoItemBuilder {
    private static final String TAG = InfoItemBuilder.class.toString();

    public interface OnInfoItemSelectedListener<T extends InfoItem> {
        void selected(T selectedItem);
        void held(T selectedItem);
    }

    private final Context context;
    private ImageLoader imageLoader = ImageLoader.getInstance();

    private OnInfoItemSelectedListener<StreamInfoItem> onStreamSelectedListener;
    private OnInfoItemSelectedListener<ChannelInfoItem> onChannelSelectedListener;
    private OnInfoItemSelectedListener<PlaylistInfoItem> onPlaylistSelectedListener;

    public InfoItemBuilder(Context context) {
        this.context = context;
    }

    public View buildView(@NonNull ViewGroup parent, @NonNull final InfoItem infoItem) {
        return buildView(parent, infoItem, false);
    }

    public View buildView(@NonNull ViewGroup parent, @NonNull final InfoItem infoItem, boolean useMiniVariant) {
        InfoItemHolder holder = holderFromInfoType(parent, infoItem.info_type, useMiniVariant);
        holder.updateFromItem(infoItem);
        return holder.itemView;
    }

    private InfoItemHolder holderFromInfoType(@NonNull ViewGroup parent, @NonNull InfoItem.InfoType infoType, boolean useMiniVariant) {
        switch (infoType) {
            case STREAM:
                return useMiniVariant ? new StreamMiniInfoItemHolder(this, parent) : new StreamInfoItemHolder(this, parent);
            case CHANNEL:
                return useMiniVariant ? new ChannelMiniInfoItemHolder(this, parent) : new ChannelInfoItemHolder(this, parent);
            case PLAYLIST:
                return new PlaylistInfoItemHolder(this, parent);
            default:
                Log.e(TAG, "Trollolo");
                throw new RuntimeException("InfoType not expected = " + infoType.name());
        }
    }

    public Context getContext() {
        return context;
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }

    public OnInfoItemSelectedListener<StreamInfoItem> getOnStreamSelectedListener() {
        return onStreamSelectedListener;
    }

    public void setOnStreamSelectedListener(OnInfoItemSelectedListener<StreamInfoItem> listener) {
        this.onStreamSelectedListener = listener;
    }

    public OnInfoItemSelectedListener<ChannelInfoItem> getOnChannelSelectedListener() {
        return onChannelSelectedListener;
    }

    public void setOnChannelSelectedListener(OnInfoItemSelectedListener<ChannelInfoItem> listener) {
        this.onChannelSelectedListener = listener;
    }

    public OnInfoItemSelectedListener<PlaylistInfoItem> getOnPlaylistSelectedListener() {
        return onPlaylistSelectedListener;
    }

    public void setOnPlaylistSelectedListener(OnInfoItemSelectedListener<PlaylistInfoItem> listener) {
        this.onPlaylistSelectedListener = listener;
    }

}

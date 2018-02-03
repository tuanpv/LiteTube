package com.liteyoutube.youtubelite.info_list.holder;

import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.TextView;

import com.liteyoutube.youtubelite.info_list.InfoItemBuilder;
import com.liteyoutube.youtubelite.R;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import com.liteyoutube.youtubelite.util.Localization;

/*
 * Created by Christian Schabesberger on 01.08.16.
 * <p>
 * Copyright (C) Christian Schabesberger 2016 <chris.schabesberger@mailbox.org>
 * StreamInfoItemHolder.java is part of NewPipe.
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

public class StreamInfoItemHolder extends StreamMiniInfoItemHolder {

    public final TextView itemAdditionalDetails;

    public StreamInfoItemHolder(InfoItemBuilder infoItemBuilder, ViewGroup parent) {
        super(infoItemBuilder, R.layout.list_stream_item, parent);
        itemAdditionalDetails = itemView.findViewById(R.id.itemAdditionalDetails);
    }

    @Override
    public void updateFromItem(final InfoItem infoItem) {
        super.updateFromItem(infoItem);

        if (!(infoItem instanceof StreamInfoItem)) return;
        final StreamInfoItem item = (StreamInfoItem) infoItem;

        itemAdditionalDetails.setText(getStreamInfoDetailLine(item));
    }

    private String getStreamInfoDetailLine(final StreamInfoItem infoItem) {
        String viewsAndDate = "";
        if (infoItem.view_count >= 0) {
            viewsAndDate = Localization.shortViewCount(itemBuilder.getContext(), infoItem.view_count);
        }
        if (!TextUtils.isEmpty(infoItem.upload_date)) {
            if (viewsAndDate.isEmpty()) {
                viewsAndDate = infoItem.upload_date;
            } else {
                viewsAndDate += " • " + infoItem.upload_date;
            }
        }
        return viewsAndDate;
    }
}

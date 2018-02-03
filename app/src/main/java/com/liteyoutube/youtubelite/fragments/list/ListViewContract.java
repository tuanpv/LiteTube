package com.liteyoutube.youtubelite.fragments.list;

import com.liteyoutube.youtubelite.fragments.ViewContract;

public interface ListViewContract<I, N> extends ViewContract<I> {
    void showListFooter(boolean show);

    void handleNextItems(N result);
}

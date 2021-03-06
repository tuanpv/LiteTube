package com.liteyoutube.youtubelite.playlist;

import android.util.Log;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.ListInfo;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.SingleObserver;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

abstract class AbstractInfoPlayQueue<T extends ListInfo, U extends InfoItem> extends PlayQueue {
    boolean isInitial;
    boolean isComplete;

    int serviceId;
    String baseUrl;
    String nextUrl;

    transient Disposable fetchReactor;

    AbstractInfoPlayQueue(final U item) {
        this(item.getServiceId(), item.getUrl(), null, Collections.<InfoItem>emptyList(), 0);
    }

    AbstractInfoPlayQueue(final int serviceId,
                          final String url,
                          final String nextPageUrl,
                          final List<InfoItem> streams,
                          final int index) {
        super(index, extractListItems(streams));

        this.baseUrl = url;
        this.nextUrl = nextPageUrl;
        this.serviceId = serviceId;

        this.isInitial = streams.isEmpty();
        this.isComplete = !isInitial && (nextPageUrl == null || nextPageUrl.isEmpty());
    }

    abstract protected String getTag();

    @Override
    public boolean isComplete() {
        return isComplete;
    }

    SingleObserver<T> getHeadListObserver() {
        return new SingleObserver<T>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                if (isComplete || !isInitial || (fetchReactor != null && !fetchReactor.isDisposed())) {
                    d.dispose();
                } else {
                    fetchReactor = d;
                }
            }

            @Override
            public void onSuccess(@NonNull T result) {
                isInitial = false;
                if (!result.has_more_streams) isComplete = true;
                nextUrl = result.next_streams_url;

                append(extractListItems(result.related_streams));

                fetchReactor.dispose();
                fetchReactor = null;
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Log.e(getTag(), "Error fetching more playlist, marking playlist as complete.", e);
                isComplete = true;
                append(); // Notify change
            }
        };
    }

    SingleObserver<ListExtractor.NextItemsResult> getNextItemsObserver() {
        return new SingleObserver<ListExtractor.NextItemsResult>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                if (isComplete || isInitial || (fetchReactor != null && !fetchReactor.isDisposed())) {
                    d.dispose();
                } else {
                    fetchReactor = d;
                }
            }

            @Override
            public void onSuccess(@NonNull ListExtractor.NextItemsResult result) {
                if (!result.hasMoreStreams()) isComplete = true;
                nextUrl = result.nextItemsUrl;

                append(extractListItems(result.nextItemsList));

                fetchReactor.dispose();
                fetchReactor = null;
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Log.e(getTag(), "Error fetching more playlist, marking playlist as complete.", e);
                isComplete = true;
                append(); // Notify change
            }
        };
    }

    @Override
    public void dispose() {
        super.dispose();
        if (fetchReactor != null) fetchReactor.dispose();
    }

    private static List<PlayQueueItem> extractListItems(final List<InfoItem> infos) {
        List<PlayQueueItem> result = new ArrayList<>();
        for (final InfoItem stream : infos) {
            if (stream instanceof StreamInfoItem) {
                result.add(new PlayQueueItem((StreamInfoItem) stream));
            }
        }
        return result;
    }
}

package com.liteyoutube.youtubelite.database.history.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import com.liteyoutube.youtubelite.database.history.model.SearchHistoryEntry;

import java.util.List;

import io.reactivex.Flowable;

import static com.liteyoutube.youtubelite.database.history.model.SearchHistoryEntry.CREATION_DATE;
import static com.liteyoutube.youtubelite.database.history.model.SearchHistoryEntry.ID;
import static com.liteyoutube.youtubelite.database.history.model.SearchHistoryEntry.SEARCH;
import static com.liteyoutube.youtubelite.database.history.model.SearchHistoryEntry.SERVICE_ID;
import static com.liteyoutube.youtubelite.database.history.model.SearchHistoryEntry.TABLE_NAME;

@Dao
public interface SearchHistoryDAO extends HistoryDAO<SearchHistoryEntry> {

    String ORDER_BY_CREATION_DATE = " ORDER BY " + CREATION_DATE + " DESC";

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + ID + " = (SELECT MAX(" + ID + ") FROM " + TABLE_NAME + ")")
    @Override
    SearchHistoryEntry getLatestEntry();

    @Query("DELETE FROM " + TABLE_NAME)
    @Override
    int deleteAll();

    @Query("DELETE FROM " + TABLE_NAME + " WHERE " + SEARCH + " = :query")
    int deleteAllWhereQuery(String query);

    @Query("SELECT * FROM " + TABLE_NAME + ORDER_BY_CREATION_DATE)
    @Override
    Flowable<List<SearchHistoryEntry>> getAll();

    @Query("SELECT * FROM " + TABLE_NAME + " GROUP BY " + SEARCH + ORDER_BY_CREATION_DATE + " LIMIT :limit")
    Flowable<List<SearchHistoryEntry>> getUniqueEntries(int limit);

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + SERVICE_ID + " = :serviceId" + ORDER_BY_CREATION_DATE)
    @Override
    Flowable<List<SearchHistoryEntry>> listByService(int serviceId);

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + SEARCH + " LIKE :query || '%' GROUP BY " + SEARCH + " LIMIT :limit")
    Flowable<List<SearchHistoryEntry>> getSimilarEntries(String query, int limit);
}

package com.liteyoutube.youtubelite.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import com.liteyoutube.youtubelite.database.history.dao.WatchHistoryDAO;
import com.liteyoutube.youtubelite.database.history.Converters;
import com.liteyoutube.youtubelite.database.history.dao.SearchHistoryDAO;
import com.liteyoutube.youtubelite.database.history.model.SearchHistoryEntry;
import com.liteyoutube.youtubelite.database.history.model.WatchHistoryEntry;
import com.liteyoutube.youtubelite.database.subscription.SubscriptionDAO;
import com.liteyoutube.youtubelite.database.subscription.SubscriptionEntity;

@TypeConverters({Converters.class})
@Database(entities = {SubscriptionEntity.class, WatchHistoryEntry.class, SearchHistoryEntry.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "videorocket.db";

    public abstract SubscriptionDAO subscriptionDAO();

    public abstract WatchHistoryDAO watchHistoryDAO();

    public abstract SearchHistoryDAO searchHistoryDAO();
}

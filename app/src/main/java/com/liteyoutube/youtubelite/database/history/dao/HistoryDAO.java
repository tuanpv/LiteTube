package com.liteyoutube.youtubelite.database.history.dao;

import com.liteyoutube.youtubelite.database.BasicDAO;

public interface HistoryDAO<T> extends BasicDAO<T> {
    T getLatestEntry();
}

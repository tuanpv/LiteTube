package com.liteyoutube.youtubelite.database.subscription;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import com.liteyoutube.youtubelite.util.Constants;

import org.schabi.newpipe.extractor.channel.ChannelInfoItem;

import static com.liteyoutube.youtubelite.database.subscription.SubscriptionEntity.SUBSCRIPTION_SERVICE_ID;
import static com.liteyoutube.youtubelite.database.subscription.SubscriptionEntity.SUBSCRIPTION_TABLE;
import static com.liteyoutube.youtubelite.database.subscription.SubscriptionEntity.SUBSCRIPTION_URL;

@Entity(tableName = SUBSCRIPTION_TABLE,
        indices = {@Index(value = {SUBSCRIPTION_SERVICE_ID, SUBSCRIPTION_URL}, unique = true)})
public class SubscriptionEntity {

    final static String SUBSCRIPTION_TABLE              = "subscriptions";
    final static String SUBSCRIPTION_SERVICE_ID         = "service_id";
    final static String SUBSCRIPTION_URL                = "url";
    final static String SUBSCRIPTION_NAME               = "name";
    final static String SUBSCRIPTION_AVATAR_URL         = "avatar_url";
    final static String SUBSCRIPTION_SUBSCRIBER_COUNT   = "subscriber_count";
    final static String SUBSCRIPTION_DESCRIPTION        = "description";

    @PrimaryKey(autoGenerate = true)
    private long uid = 0;

    @ColumnInfo(name = SUBSCRIPTION_SERVICE_ID)
    private int serviceId = Constants.NO_SERVICE_ID;

    @ColumnInfo(name = SUBSCRIPTION_URL)
    private String url;

    @ColumnInfo(name = SUBSCRIPTION_NAME)
    private String name;

    @ColumnInfo(name = SUBSCRIPTION_AVATAR_URL)
    private String avatarUrl;

    @ColumnInfo(name = SUBSCRIPTION_SUBSCRIBER_COUNT)
    private Long subscriberCount;

    @ColumnInfo(name = SUBSCRIPTION_DESCRIPTION)
    private String description;

    public long getUid() {
        return uid;
    }

    /* Keep this package-private since UID should always be auto generated by Room impl */
    void setUid(long uid) {
        this.uid = uid;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Long getSubscriberCount() {
        return subscriberCount;
    }

    public void setSubscriberCount(Long subscriberCount) {
        this.subscriberCount = subscriberCount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Ignore
    public void setData(final String name,
                        final String avatarUrl,
                        final String description,
                        final Long subscriberCount) {
        this.setName(name);
        this.setAvatarUrl(avatarUrl);
        this.setDescription(description);
        this.setSubscriberCount(subscriberCount);
    }

    @Ignore
    public ChannelInfoItem toChannelInfoItem() {
        ChannelInfoItem item = new ChannelInfoItem(getServiceId(), getUrl(), getName());
        item.thumbnail_url = getAvatarUrl();
        item.subscriber_count = getSubscriberCount();
        item.description = getDescription();
        return item;
    }
}

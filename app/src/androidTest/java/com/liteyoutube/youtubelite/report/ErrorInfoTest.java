package com.liteyoutube.youtubelite.report;

import android.os.Parcel;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import com.liteyoutube.youtubelite.R;

import static org.junit.Assert.assertEquals;

/**
 * Instrumented tests for {@link ErrorActivity.ErrorInfo}
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ErrorInfoTest {

    @Test
    public void errorInfo_testParcelable() {
        ErrorActivity.ErrorInfo info = ErrorActivity.ErrorInfo.make(UserAction.USER_REPORT, "youtube", "request", R.string.general_error);
        // Obtain a Parcel object and write the parcelable object to it:
        Parcel parcel = Parcel.obtain();
        info.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        ErrorActivity.ErrorInfo infoFromParcel = ErrorActivity.ErrorInfo.CREATOR.createFromParcel(parcel);

        assertEquals(UserAction.USER_REPORT, infoFromParcel.userAction);
        assertEquals("youtube", infoFromParcel.serviceName);
        assertEquals("request", infoFromParcel.request);
        assertEquals(R.string.general_error, infoFromParcel.message);

        parcel.recycle();
    }
}
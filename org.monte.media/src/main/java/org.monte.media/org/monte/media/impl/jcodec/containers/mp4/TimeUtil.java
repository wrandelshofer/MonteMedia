package org.monte.media.impl.jcodec.containers.mp4;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * References:
 * <p>
 * This code has been derived from JCodecProject.
 * <dl>
 *     <dt>JCodecProject. Copyright 2008-2019 JCodecProject.
 *     <br><a href="https://github.com/jcodec/jcodec/blob/7e5283408a75c3cdbefba98a57d546e170f0b7d0/LICENSE">BSD 2-Clause License.</a></dt>
 *     <dd><a href="https://github.com/jcodec/jcodec">github.com</a></dd>
 * </dl>
 *
 * @author The JCodec project
 */
public class TimeUtil {

    public final static long MOV_TIME_OFFSET;

    static {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.set(1904, 0, 1, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        MOV_TIME_OFFSET = calendar.getTimeInMillis();
    }

    public static Date macTimeToDate(int movSec) {
        return new Date(fromMovTime(movSec));
    }

    public static long fromMovTime(int movSec) {
        return ((long) movSec) * 1000L + MOV_TIME_OFFSET;
    }

    public static int toMovTime(long millis) {
        return (int) ((millis - MOV_TIME_OFFSET) / 1000L);
    }
}
package com.probuild.retail.web.catalog.ext.domain;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateUtil {

    private static Long overrideDate = null;
    private static Long today = null;
    private static Long tomorrow = null;

    public void setOverrideDate(Date overrideDate) {
        DateUtil.overrideDate = overrideDate.getTime();
    }

    public static Long getNow() {
        Long ret = overrideDate;
        if (ret == null) {
            ret = System.currentTimeMillis();
        }
        return ret;
    }

    public static Long getToday() {
        if (today == null || System.currentTimeMillis() > tomorrow) {
        Calendar cal = Calendar.getInstance();
            cal.getTime().setTime(getNow());
            Calendar calToday = new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            today = calToday.getTimeInMillis();
            calToday.add(Calendar.DATE, 1);
            tomorrow = calToday.getTimeInMillis();
        }
        return today;
    }

    public static boolean isActive(Date startDate, Date endDate, boolean includeTime) {
        Long date = null;
        if (includeTime) {
            date = getNow();
        } else {
            date = getToday();
        }
        //System.out.println ( "Using date: " + date + " start: " + startDate.getTime() + " end: " + endDate.getTime() );
        if ( startDate == null || startDate.getTime() > date || (endDate != null && endDate.getTime() < date)) {
            return false;
        }
        return true;
    }
}

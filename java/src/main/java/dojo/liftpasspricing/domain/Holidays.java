package dojo.liftpasspricing.domain;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

public class Holidays {
    private final Collection<Date> allHolidays;

    public Holidays(Collection<Date> allHolidays) {
        this.allHolidays = allHolidays;
    }

    public Integer calculateReductionForMondayBusinessDay(Date requestedDate) {
        final boolean isNotHoliday = allHolidays.stream()
                .noneMatch(holiday -> holiday.compareTo(requestedDate) == 0);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(requestedDate);
        if (isNotHoliday && calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
            return 35;
        }
        return 0;
    }

}

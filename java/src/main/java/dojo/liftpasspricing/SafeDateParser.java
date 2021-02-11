package dojo.liftpasspricing;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

public class SafeDateParser {
    static Optional<Date> parse(String date) {
        DateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date requestedDate = isoFormat.parse(date);
            return Optional.of(requestedDate);
        } catch (ParseException e) {
            return Optional.empty();
        }
    }
}

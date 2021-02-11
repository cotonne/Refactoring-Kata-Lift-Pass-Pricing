package dojo.liftpasspricing;

import java.text.MessageFormat;

public class JsonResponse {
    static String build(double cost) {
        return MessageFormat.format("'{' \"cost\": {0}'}'", (int) Math.ceil(cost));
    }
}

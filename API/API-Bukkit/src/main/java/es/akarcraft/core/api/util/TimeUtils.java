package es.akarcraft.core.api.util;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public class TimeUtils {
    private static final Map<String,ChronoUnit> units;

    static {
        units = new HashMap<>();
        units.put("s", ChronoUnit.SECONDS);
        units.put("m", ChronoUnit.MINUTES);
        units.put("h", ChronoUnit.HOURS);
        units.put("d", ChronoUnit.DAYS);
        units.put("w", ChronoUnit.WEEKS);
        units.put("S", ChronoUnit.WEEKS);
        units.put("M", ChronoUnit.MONTHS);
        units.put("y", ChronoUnit.YEARS);
    }

    public static Duration parseDuration(String stringDuration){
        Duration sum = Duration.of(0, ChronoUnit.SECONDS);
        String number = "";

        for (final char c : stringDuration.toCharArray()) {
            if(Character.isDigit(c)){
                number = number + c;
            } else {
                if(units.containsKey(c + "") && !number.isEmpty()) {
                    long parsedLong = Long.parseLong(number);

                    ChronoUnit unit = units.get(c + "");

                    sum = sum.plus(unit.getDuration().multipliedBy(parsedLong));

                    number = "";
                }
            }


        }

        return sum;
    }
}

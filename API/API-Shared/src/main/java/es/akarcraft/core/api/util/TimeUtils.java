package es.akarcraft.core.api.util;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

public class TimeUtils {
    private static final Map<String, ChronoUnit> units;

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

    public static Duration parseDuration(String stringDuration) {
        Duration sum = Duration.ZERO;
        String number = "";

        for (final char c : stringDuration.toCharArray()) {
            if (Character.isDigit(c)) {
                number = number + c;
            } else {
                if (units.containsKey(c + "") && !number.isEmpty()) {
                    long parsedLong = Long.parseLong(number);

                    ChronoUnit unit = units.get(c + "");

                    sum = sum.plus(unit.getDuration().multipliedBy(parsedLong));

                    number = "";
                }
            }


        }

        return sum;
    }

    public static String durationToHumanTime(Duration duration) {
        StringJoiner joiner = new StringJoiner(" ");

        long seconds = duration.getSeconds();

        int unitValue = Math.toIntExact(seconds / TimeUnit.DAYS.toSeconds(365));
        if (unitValue > 0) {
            seconds %= TimeUnit.DAYS.toSeconds(365);
            joiner.add(unitValue + " año(s)");
        }

        unitValue = Math.toIntExact(seconds / TimeUnit.DAYS.toSeconds(30));
        if(unitValue > 0){
            seconds %= TimeUnit.DAYS.toSeconds(30);
            joiner.add(unitValue + " mes(es)");
        }

        unitValue = Math.toIntExact(seconds / TimeUnit.DAYS.toSeconds(1));
        if(unitValue > 0){
            seconds %= TimeUnit.DAYS.toSeconds(1);
            joiner.add(unitValue + " dia(s)");
        }

        unitValue = Math.toIntExact(seconds / TimeUnit.HOURS.toSeconds(1));
        if(unitValue > 0){
            seconds %= TimeUnit.HOURS.toSeconds(1);
            joiner.add(unitValue + " hora(s)");
        }

        unitValue = Math.toIntExact(seconds / TimeUnit.MINUTES.toSeconds(1));
        if(unitValue > 0){
            seconds %= TimeUnit.MINUTES.toSeconds(1);
            joiner.add(unitValue + " minuto(s)");
        }

        if(seconds > 0 || joiner.length() == 0){
            joiner.add(seconds + " segundo(s)");
        }

        return joiner.toString();
    }
}

package com.offsec.nethunter.GPS;


/**
 * Created by Danial on 2/23/2015.
 *  https://github.com/danialgoodwin/android-app-samples/blob/master/gps-satellite-nmea-info/app/src/main/java/net/simplyadvanced/gpsandsatelliteinfo/NmeaUtils.java
 */
public class NmeaUtils {

    /** Parses and stores NMEA sentence in GpsPosition. If nmeaSentence doesn't start with "$",
     * then nothing happens.
     * @param nmeaSentence info from NMEA source
     * @param position instance to store extracted values
     * @return true is parse is successful, false if parse failed
     */
    public static boolean parse(String nmeaSentence, GpsPosition position) {
        boolean isSuccessfulParse = false;
        if (nmeaSentence != null && !nmeaSentence.isEmpty() && nmeaSentence.startsWith("$")) {
            String nmea = nmeaSentence.substring(1);
            String[] tokens = nmea.split(",");
            String type = tokens[0];
            if (NmeaGpsSentence.getValues().containsKey(type)) {
                isSuccessfulParse = NmeaGpsSentence.getValues().get(type).parse(tokens, position);
            }
            position.updateIsfixed();
        }
        return isSuccessfulParse;
    }

    /** Returns the talker ID and sentence ID as a single String, ex: "GPGGA". */
    public static String getType(String nmeaSentence) {
        if (nmeaSentence != null && !nmeaSentence.isEmpty() && nmeaSentence.startsWith("$")) {
            return nmeaSentence.substring(1, 6);
        } else {
            return "Unknown";
        }
    }

    static float getLatitudeFromNmeaString(String latitude, String NS) {
        float med = Float.parseFloat(latitude.substring(2)) / 60.0f;
        med +=  Float.parseFloat(latitude.substring(0, 2));
        if (NS.startsWith("S")) {
            med = -med;
        }
        return med;
    }

    static float getLongitudeFromNmeaString(String longitude, String WE) {
        float med = Float.parseFloat(longitude.substring(3)) / 60.0f;
        med +=  Float.parseFloat(longitude.substring(0, 3));
        if (WE.startsWith("W")) {
            med = -med;
        }
        return med;
    }

}
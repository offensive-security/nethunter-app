package com.offsec.nethunter.GPS;

/*
* Created by Danial on 2/23/2015.
* https://github.com/danialgoodwin/android-app-samples/blob/master/gps-satellite-nmea-info/app/src/main/java/net/simplyadvanced/gpsandsatelliteinfo/NmeaGpsSentence.java
*/

import android.util.Log;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum NmeaGpsSentence {
    GGA {
        @Override
        protected void parseHelper(String[] tokens, GpsPosition position) {
            position.time = Float.parseFloat(tokens[1]);
            position.latitude = NmeaUtils.getLatitudeFromNmeaString(tokens[2], tokens[3]);
            position.longitude = NmeaUtils.getLongitudeFromNmeaString(tokens[4], tokens[5]);
            position.quality = Integer.parseInt(tokens[6]);
            position.altitude = Float.parseFloat(tokens[9]);
        }
    },
    GGL {
        @Override
        protected void parseHelper(String[] tokens, GpsPosition position) {
            position.latitude = NmeaUtils.getLatitudeFromNmeaString(tokens[1], tokens[2]);
            position.longitude = NmeaUtils.getLongitudeFromNmeaString(tokens[3], tokens[4]);
            position.time = Float.parseFloat(tokens[5]);
        }
    },
    RMC {
        @Override
        protected void parseHelper(String[] tokens, GpsPosition position) {
            position.time = Float.parseFloat(tokens[1]);
            position.latitude = NmeaUtils.getLatitudeFromNmeaString(tokens[3], tokens[4]);
            position.longitude = NmeaUtils.getLongitudeFromNmeaString(tokens[5], tokens[6]);
            position.velocity = Float.parseFloat(tokens[7]);
            position.direction = Float.parseFloat(tokens[8]);
        }
    },
    VTG {
        @Override
        protected void parseHelper(String[] tokens, GpsPosition position) {
            position.direction = Float.parseFloat(tokens[3]);
        }
    },
    RMZ {
        @Override
        protected void parseHelper(String[] tokens, GpsPosition position) {
            position.altitude = Float.parseFloat(tokens[1]);
        }
    };


    private static final NmeaGpsSentence[] VALUES = NmeaGpsSentence.values();
    private static final Map<String, NmeaGpsSentence> VALUES_MAP = new HashMap<>();

    static {
        for (NmeaGpsSentence value : VALUES) {
            VALUES_MAP.put("GP" + value.name(), value);
        }
    }

    private NmeaGpsSentence() {}

    /** Returns a list of all the NmeaGpsSentence types. */
    public static NmeaGpsSentence[] getValuesArray() {
        return VALUES;
    }

    public static Map<String, NmeaGpsSentence> getValues() {
        return VALUES_MAP;
    }

    protected abstract void parseHelper(String[] tokens, GpsPosition position);

    public boolean parse(String[] tokens, GpsPosition position) {
        try {
            parseHelper(tokens, position);
            return true;
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.d("DEBUG: NmeaGpsSentence", "parse(), caught ArrayIndexOutOfBoundsException, tokens=" + Arrays.toString(tokens));
            return false;
        } catch (NumberFormatException e) {
            Log.d("DEBUG: NmeaGpsSentence", "parse(), caught NumberFormatException, tokens=" + Arrays.toString(tokens));
            return false;
        }
    }

    public String getTalkerId() {
        return "GP";
    }

    public String getSentenceId() {
        return this.toString();
    }

}
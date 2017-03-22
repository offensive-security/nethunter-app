package com.offsec.nethunter.gps;


import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class is a container for several static methods which help
 * with generating NMEA data.
 * <p>
 * A nice reference for NMEA is at http://www.gpsinformation.org/dale/nmea.htm
 */
final class NMEA {


    /**
     * Formats the speed in knots from the #Location into a string.
     * If the speed is unknown, it returns an empty string.
     */
    public static String formatSpeedKt(Location location) {
        String s = "";
        if (location.hasSpeed())
            // http://www.google.com/search?q=m%2Fs+to+kt
            s += (location.getSpeed() * 1.94384449);
        return s;
    }

    /**
     * Formats the bearing from the #Location into a string.  If the
     * bearing is unknown, it returns an empty string.
     */
    public static String formatBearing(Location location) {
        String s = "";
        if (location.hasBearing())
            s += location.getBearing();
        return s;
    }

    public static String formatGpsGsa(GpsStatus gps) {
        String fix = "1";
        String prn = "";
        int nbr_sat = 0;
        Iterator<GpsSatellite> satellites = gps.getSatellites().iterator();
        for (int i = 0; i < 12; i++) {
            if (satellites.hasNext()) {
                GpsSatellite sat = satellites.next();
                if (sat.usedInFix()) {
                    prn = prn + sat.getPrn();
                    nbr_sat++;
                }
            }

            prn = prn + ",";
        }

        if (nbr_sat > 3)
            fix = "3";
        else if (nbr_sat > 0)
            fix = "2";

        //TODO: calculate DOP values
        return fix + "," + prn + ",,,";
    }

    public static List<String> formatGpsGsv(GpsStatus gps) {
        List<String> gsv = new ArrayList<String>();
        int nbr_sat = 0;
        for (GpsSatellite sat : gps.getSatellites())
            nbr_sat++;

        Iterator<GpsSatellite> satellites = gps.getSatellites().iterator();
        for (int i = 0; i < 3; i++) {
            if (satellites.hasNext()) {
                String g = Integer.toString(nbr_sat);
                for (int n = 0; n < 4; n++) {
                    if (satellites.hasNext()) {
                        GpsSatellite sat = satellites.next();
                        g = g + "," + sat.getPrn() + "," + sat.getElevation() +
                                "," + sat.getAzimuth() + "," + sat.getSnr();
                    }
                }
                gsv.add(g);
            }
        }
        return gsv;
    }
}

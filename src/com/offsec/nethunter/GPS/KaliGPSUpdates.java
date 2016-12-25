package com.offsec.nethunter.GPS;

public interface KaliGPSUpdates {

    interface Receiver {
        void onPositionUpdate(String nmeaSentences);
        void onServerReady();

        void onFirstPositionUpdate();
    }

    interface Provider {
        void onLocationUpdatesRequested(Receiver receiver);
        void onStopRequested();
    }


}

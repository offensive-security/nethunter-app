package com.offsec.nethunter.GPS;


import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.offsec.nethunter.utils.ShellExecuter;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.HashMap;


public class GpsdServer extends AsyncTask<Void, Void, Void> {
    private static final String SCRIPT_PATH = "/data/data/com.offsec.nethunter/files/scripts/";

    private static final String TAG = "GpsdServer";
    private ServerSocketChannel server = null;
    private ServerSocket serverSocket = null;
    private Handler handler;
    private static HashMap<String, String> pendingData;

    public GpsdServer(ConnectionListener listener) throws IOException {
        this.listener = listener;
    }
    public interface ConnectionListener {
        void onSocketConnected(Socket clientSocket, ServerSocket serverSocket);
    }

    private ConnectionListener listener;

    /**
     * The TCP/IP port used for Socket communication.
     */
    private static final int PORT = 10110;

    /**
     * The maximum length of a GGA sentence.
     */
    private static final int GGA_LENGTH_MAX = 80;


    @Override
    protected Void doInBackground(Void... params) {
        Log.d(TAG, "Started Listening");


        try {
            SocketAddress socketAddress = new InetSocketAddress("127.0.0.1", PORT);
            serverSocket = new ServerSocket();
            serverSocket.bind(socketAddress);

            new Thread(new Runnable() {
                public void run() {

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    ShellExecuter exe = new ShellExecuter();
                   String command = "su -c '" + SCRIPT_PATH + File.separator + "bootkali start_gpsd " + String.valueOf(PORT) + "'";
                    Log.d(TAG, command);
                    String response = exe.RunAsRootOutput(command);
                    Log.d(TAG, "Response = " + response);
                }
            }).start();


            Socket clientSocket = serverSocket.accept();
            listener.onSocketConnected(clientSocket, serverSocket);
           Log.d(TAG, "Client bound");
        } catch (IOException e) {
            Log.d(TAG, "Unable to create ServerSocket for port: " + PORT);
            Log.d(TAG, e.getMessage());
            return null;
        }

        return null;
    }

}
package com.offsec.nethunter;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;

public class Server {

    // http://stackoverflow.com/questions/27927279/java-tcp-echo-server-broadcast

    public static ServerSocket server;
    private static ArrayList<Socket> clients = new ArrayList<>();

    public static void broadcast(String message) {
        try {
            for (Socket socket : clients) {
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println(message);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void main() {
        try {
            Log.d("Server: ", "Starting server on port 9000 bound to localhost only");
            SocketAddress socketAddress = new InetSocketAddress( "127.0.0.1", 9000);
            server = new ServerSocket();
            server.bind(socketAddress);

            while (true) {

                clients.add(server.accept());

                for (Socket socket : clients) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String line = in.readLine();
                    if (line != null) {
                        broadcast(line);
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void Shutdown() {
        try {
            server.close();
            Log.d("Server: ", "closing server");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

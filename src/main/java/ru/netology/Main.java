package ru.netology;

import java.io.BufferedOutputStream;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        final var server = new Server(9999);

        server.addHandler("GET", "/messages", (request, responseStream) -> {
            var response = "HTTP/1.1 200 OK\r\nContent-Length: 13\r\nConnection: close\r\n\r\nHello, World!";
            responseStream.write(response.getBytes());
            responseStream.flush();
        });

        server.addHandler("POST", "/messages", (request, responseStream) -> {
            var response = "HTTP/1.1 200 OK\r\nContent-Length: 7\r\nConnection: close\r\n\r\nPosted!";
            responseStream.write(response.getBytes());
            responseStream.flush();
        });

        try {
            server.listen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class Server {
    private final int port;
    private final ExecutorService threadPool;
    private final List<String> validPaths;
    private final Map<String, Map<String, Handler>> handlers;

    public Server(int port) {
        this.port = port;
        this.threadPool = Executors.newFixedThreadPool(64);
        this.validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
        this.handlers = new ConcurrentHashMap<>();
    }

    public void addHandler(String method, String path, Handler handler) {
        handlers.computeIfAbsent(path, k -> new ConcurrentHashMap<>()).put(method, handler);
    }

    public void listen() throws IOException {
        try (var serverSocket = new ServerSocket(port)) {
            while (true) {
                final var socket = serverSocket.accept();
                threadPool.submit(() -> handleConnection(socket));
            }
        }
    }

    private void handleConnection(Socket socket) {
        try (
                final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final var out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            final var requestLine = in.readLine();
            final var parts = requestLine.split(" ");
            if (parts.length != 3) {
                return;
            }

            final var method = parts[0];
            final var path = parts[1];
            final var request = new Request(method, path, Map.of(), null);

            var handler = handlers.getOrDefault(path, Map.of()).get(method);
            if (handler != null) {
                handler.handle(request, out);
            } else if (validPaths.contains(path)) {
                handleFileRequest(path, out);
            } else {
                out.write(("HTTP/1.1 404 Not Found\r\nContent-Length: 0\r\nConnection: close\r\n\r\n").getBytes());
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleFileRequest(String path, BufferedOutputStream out) throws IOException {
        final var filePath = Path.of(".", "public", path);
        final var mimeType = Files.probeContentType(filePath);

        if (path.equals("/classic.html")) {
            final var template = Files.readString(filePath);
            final var content = template.replace("{time}", LocalDateTime.now().toString()).getBytes();
            out.write(("HTTP/1.1 200 OK\r\nContent-Type: " + mimeType + "\r\nContent-Length: " + content.length + "\r\nConnection: close\r\n\r\n").getBytes());
            out.write(content);
        } else {
            final var length = Files.size(filePath);
            out.write(("HTTP/1.1 200 OK\r\nContent-Type: " + mimeType + "\r\nContent-Length: " + length + "\r\nConnection: close\r\n\r\n").getBytes());
            Files.copy(filePath, out);
        }
        out.flush();
    }
}
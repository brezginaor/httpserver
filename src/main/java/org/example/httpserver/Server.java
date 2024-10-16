package org.example.httpserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.function.BiConsumer;

public class Server {

    private final String host;
    private final int port;
    private final RouteHandler routeHandler;
    private volatile boolean ready = false;  // Флаг готовности сервера
    private volatile boolean running;
    private Selector selector;

    public Server(int port) {
        host = "localhost";
        this.port = port;
        routeHandler = new RouteHandler();
        this.running = true;
    }

    public boolean isReady() {
        return ready;
    }

    public void startServer() throws IOException {

        try (Selector selector = Selector.open();
             ServerSocketChannel serverChannel = ServerSocketChannel.open()) {

            serverChannel.bind(new InetSocketAddress(host, port));
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            ready = true;

            initializeRoutes();
            while (running) {
                selector.select();


                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();

                    if (key.isAcceptable()) {
                        register(key, selector);
                    } else if (key.isReadable()) {
                        handleRequest(key);
                    }
                }
            }

        }
        finally {
            if (selector != null) {
                selector.close();  // Закрываем selector
            }
        }

        }

    public void stopServer() {
        running = false;
        if (selector != null) {
            selector.wakeup();  // Прерываем блокировку selector
        }
        System.out.println("Сервер остановлен.");
    }
    private void initializeRoutes() {
        routeHandler.addRoute("GET", "/", (req, res) -> {
            try {
                res.sendText(200, "Welcome to the homepage!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        routeHandler.addRoute("POST", "/data", (req, res) -> {
            try {
                res.sendText(200, "Data received: " + req.getBody());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        routeHandler.addRoute("POST", "/submit", (req, res) -> {
            try {
                res.sendText(200, "Received POST request !!!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        routeHandler.addRoute("PUT", "/data", (req, res) -> {
            try {
                res.sendText(200, "Data updated: " + req.getBody());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        routeHandler.addRoute("PATCH", "/data", (req, res) -> {
            try {
                res.sendText(200, "Data partially updated: " + req.getBody());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        routeHandler.addRoute("DELETE", "/data", (req, res) -> {
            try {
                res.sendText(200, "Data deleted");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        routeHandler.setDefaultRoute((req, res) -> {
            try {
                res.sendText(404, "Not Found");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


    }
    private void register(SelectionKey key, Selector selector) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel client = serverChannel.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
    }

    private void handleRequest(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int dataBytes = clientChannel.read(buffer);

        if (dataBytes == -1) {
            clientChannel.close();
            return;
        }

        buffer.flip();
        String requestDataStr = new String(buffer.array(), 0, dataBytes);
        ServerRequest request = ServerRequest.parse(requestDataStr);
        ServerResponse response = new ServerResponse(clientChannel);

        // Получаем обработчик для маршрута
        BiConsumer<ServerRequest, ServerResponse> routeHandler = this.routeHandler.getHandler(request.getMethod(), request.getPath());

        if (routeHandler != null) {
            // Если обработчик найден, вызываем его
            System.out.println("Обработчик найден для: " + request.getPath());
            routeHandler.accept(request, response);
        } else {
            // Если обработчик не найден, используем обработчик по умолчанию
            System.out.println("Обработчик не найден для: " + request.getPath() + ". Используем маршрут по умолчанию.");
            this.routeHandler.getDefaultRoute().accept(request, response);
            //response.sendText(404, "Not Found");
        }


    }

    public void addRoute(String method, String path, BiConsumer<ServerRequest, ServerResponse> handler) {
        routeHandler.addRoute(method, path, handler);
    }

    public void setDefaultRoute(BiConsumer<ServerRequest, ServerResponse> defaultRoute) {
        routeHandler.setDefaultRoute(defaultRoute);
    }





}
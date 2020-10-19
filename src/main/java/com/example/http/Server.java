package com.example.http;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    //Порт пользователя
    private int port;

    //Директория файлов, которые сервер
    //будет отдавать пользователю при запросе
    private String directory;

    //Инициализация
    public Server(int port, String directory) {
        this.port = port;
        this.directory = directory;
    }

    //Метод запуска сервера
    void start() {
        //Создаём экземпляр серверСокет, передаём порт пользователя
        try (ServerSocket serverSocket = new ServerSocket(this.port)) {
            while (true) {
                //Получение экземпляра сокета через метод accept
                Socket socket = serverSocket.accept();
                //Создаём новый поток
                Thread thread = new Handler(socket, this.directory);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        String directory = args[1];
        new Server(port, directory).start();
    }
}

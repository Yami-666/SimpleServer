package com.example.http;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Handler extends Thread{
    //Файлы контента
    //Соответствие запроса и пути, где искать конент
    private static Map<String, String> CONTENT_TYPES = new HashMap<>() {{
        put("jpeg", "image/jpeg");
        put("html", "text/html");
        put("txt", "plain/text");
        put("", "text/plain");

    }};

    public static final String NOT_FOUND_MESSAGE = "NOT FOUND";

    private Socket socket;
    private String directory;

    //Инициализация
    Handler(Socket socket, String directory) {
        this.socket = socket;
        this.directory = directory;
    }

    @Override
    public void run() {
        try (InputStream input = this.socket.getInputStream();
             OutputStream output = this.socket.getOutputStream()){
            String url = this.getRequestUrl(input);
            //**
            Path filePath = Path.of(this.directory, url);
            //Проверяем, есть ли такой путь и НЕ является ли он директорией
            if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
                String extension = this.getFileExtenstion(filePath);
                String type = CONTENT_TYPES.get(extension);
                //Передаём массив байтов
                byte[] fileBytes = Files.readAllBytes(filePath);
                //Передаём header с ответом 200
                this.sendHeader(output, 200, "OK", type, fileBytes.length);
                output.write(fileBytes);
            } else {
                //Определяем тип сообщения
                String type = CONTENT_TYPES.get("text");
                //Отправляем header ответа
                this.sendHeader(output, 404, "Not Found", type, NOT_FOUND_MESSAGE.length());
                //**
                output.write(NOT_FOUND_MESSAGE.getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Получение строк запроса по url
    private String getRequestUrl(InputStream input) {
        //С помощью сканера читаем строки запроса
        //Разделённые \r\n между собой
        Scanner reader = new Scanner(input).useDelimiter("\r\n");
        //Первую линию заносим в переменную
        String line = reader.next();
        //Без пробелов мы получаем второй элемент HTTP - url
        return line.split(" ")[1];
    }

    private String getFileExtenstion(Path path) {
        String name = path.getFileName().toString();
        //Получение индекса символа, с которого начинается расширение
        //Т.е. символа точки
        int extensionStart = name.lastIndexOf(".");
        return extensionStart == -1 ? "" : name.substring(extensionStart + 1);
    }

    //Вспомогательный метод
    //Который отправляет ответ, при ненахождении файла
    private void sendHeader(OutputStream output, int statusCode, String statusText, String type, long length) {
        PrintStream ps = new PrintStream(output);
        ps.printf("HTTP/1.1 %s %s%n", statusCode, statusText);
        ps.printf("Content-Type: %s%n", type);
        ps.printf("Content-Length: %s%n%n", length);
    }
}

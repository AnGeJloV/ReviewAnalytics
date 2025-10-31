package com.github.stasangelov.reviewanalytics.client;

/**
 * Класс-запускатель для исполняемого JAR-файла.
 *
 * Эта "обертка" необходима для обхода проблем с модульностью JavaFX
 * при сборке приложения в один "fat JAR".
 * Он просто вызывает главный метод основного класса приложения.
 */
public class Launcher {
    public static void main(String[] args) {
        ClientApplication.main(args);
    }
}
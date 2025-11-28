package org.application;

import org.application.manager.KdTreeManager;
import org.application.menu.Menu;

import java.util.Locale;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        Locale.setDefault(Locale.US);  // Utilise le point comme separateur decimal

        KdTreeManager kdTreeManager = new KdTreeManager(2); // Initialisation d'un arbre KD avec 2 dimensions
        Menu menu = new Menu(kdTreeManager);
        menu.start();
    }
}

package org.application.utils;

import java.util.Comparator;
import java.util.List;

public class HeapSort {

    /**
     * Trie une liste en utilisant l'algorithme HeapSort.
     *
     * @param <T> Le type des elements de la liste.
     * @param list La liste a trier.
     * @param comparator Le comparateur utilise pour determiner l'ordre.
     */
    public static <T> void heapSort(List<T> list, Comparator<? super T> comparator) {
        int n = list.size();

        // Construire le tas (max-heap)
        buildHeap(list, n, comparator);

        // Extraire les elements un par un du tas
        for (int i = n - 1; i >= 1; i--) {
            // Echanger le premier element (maximum) avec le dernier element du tas actuel
            swap(list, 0, i);

            // Restaurer la propriete du tas sur le tas reduit
            heapify(list, i, 0, comparator);
        }
    }

    /**
     * Construit un tas max a partir de la liste donnee.
     *
     * @param <T> Le type des elements de la liste.
     * @param list La liste a transformer en tas.
     * @param n La taille de la liste.
     * @param comparator Le comparateur utilise pour determiner l'ordre.
     */
    public static <T> void buildHeap(List<T> list, int n, Comparator<? super T> comparator) {
        // Commencer a partir du premier parent du dernier element
        for (int i = n / 2 - 1; i >= 0; i--) {
            heapify(list, n, i, comparator);
        }
    }

    /**
     * Maintient la propriete du tas pour le sous-arbre enracine a l'indice i.
     *
     * @param <T> Le type des elements de la liste.
     * @param list La liste representant le tas.
     * @param n La taille effective du tas.
     * @param i L'indice de la racine du sous-arbre.
     * @param comparator Le comparateur utilise pour determiner l'ordre.
     */
    public static <T> void heapify(List<T> list, int n, int i, Comparator<? super T> comparator) {
        int l = left(i);
        int r = right(i);
        int largest = i;

        // Si le fils gauche est plus grand que la racine
        if (l < n && comparator.compare(list.get(l), list.get(largest)) > 0) {
            largest = l;
        }

        // Si le fils droit est plus grand que le plus grand jusqu'a present
        if (r < n && comparator.compare(list.get(r), list.get(largest)) > 0) {
            largest = r;
        }

        // Si le plus grand n'est pas la racine
        if (largest != i) {
            swap(list, i, largest);

            // Recursivement, heapifier le sous-arbre affecte
            heapify(list, n, largest, comparator);
        }
    }

    /**
     * Renvoie l'indice de l'enfant gauche de l'element a l'indice i.
     *
     * @param i l'indice du parent
     * @return l'indice de l'enfant gauche
     */
    private static int left(int i) {
        return 2 * i + 1;
    }

    /**
     * Renvoie l'indice de l'enfant droit de l'element a l'indice i.
     *
     * @param i l'indice du parent
     * @return l'indice de l'enfant droit
     */
    private static int right(int i) {
        return 2 * i + 2;
    }

    /**
     * Echange deux elements dans une liste.
     *
     * @param <T> Le type des elements de la liste.
     * @param list La liste contenant les elements.
     * @param i L'indice du premier element.
     * @param j L'indice du second element.
     */
    private static <T> void swap(List<T> list, int i, int j) {
        T temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
    }
}

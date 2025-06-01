package org.application.menu;

import org.application.manager.KdTreeManager;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * <p>
 * La classe {@code QueryExecutor} fournit une méthode statique permettant d'exécuter
 * une requête SQL simple sur un arbre KdTree, via un {@link KdTreeManager}, puis
 * d'afficher les résultats ou un éventuel message d'erreur.
 * </p>
 *
 * <p>
 * <strong>Auteur :</strong> KHAMLICHI Mohammed Amine
 * </p>
 *
 * <h3>Fonctionnement :</h3>
 * <ul>
 *   <li>Lit la requête SQL depuis l'entrée utilisateur (via un {@code Scanner}).</li>
 *   <li>Appelle {@code kdTreeManager.getKdTree().executeQuery(query)} pour l'exécuter.</li>
 *   <li>Affiche les résultats retournés sous forme de liste de {@code Map}.</li>
 *   <li>En cas d'erreur (requête invalide, etc.), un message d'erreur est affiché.</li>
 * </ul>
 */
public class QueryExecutor {

    /**
     * Lit une requête SQL saisie par l'utilisateur, l'exécute sur le {@code KdTree}
     * géré par {@code kdTreeManager}, puis affiche les résultats ou les erreurs.
     *
     * @param scanner
     *         un {@code Scanner} pour lire l'entrée utilisateur (la requête SQL).
     * @param kdTreeManager
     *         le gestionnaire du KdTree à interroger.
     */
    public static void executeQuery(Scanner scanner, KdTreeManager kdTreeManager) {
        System.out.print("Entrez votre requête SQL: ");
        String query = scanner.nextLine().trim();
        try {
            // Exécution de la requête sur le KdTree
            List<Map<String, String>> results = kdTreeManager.getKdTree().executeQuery(query);

            // Vérification s'il y a des résultats
            if (results.isEmpty()) {
                System.out.println("Aucun résultat trouvé pour la requête donnée.");
            } else {
                System.out.println("Résultats de la requête:");
                for (Map<String, String> result : results) {
                    // Chaque result est une map<Colonne, Valeur>
                    System.out.println(result);
                }
            }
        } catch (IllegalArgumentException e) {
            // Gestion d'erreurs liées à l'analyse de la requête (ex. opérateur inconnu)
            System.out.println("Erreur dans la requête: " + e.getMessage());
        }
    }
}

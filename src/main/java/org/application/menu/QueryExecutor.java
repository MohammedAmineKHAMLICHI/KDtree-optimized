package org.application.menu;

import org.application.manager.KdTreeManager;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * <p>
 * La classe {@code QueryExecutor} fournit une methode statique permettant d'executer
 * une requete SQL simple sur un arbre KdTree, via un {@link KdTreeManager}, puis
 * d'afficher les resultats ou un eventuel message d'erreur.
 * </p>
 *
 * <p>
 * <strong>Auteur :</strong> KHAMLICHI Mohammed Amine
 * </p>
 *
 * <h3>Fonctionnement :</h3>
 * <ul>
 *   <li>Lit la requete SQL depuis l'entree utilisateur (via un {@code Scanner}).</li>
 *   <li>Appelle {@code kdTreeManager.getKdTree().executeQuery(query)} pour l'executer.</li>
 *   <li>Affiche les resultats retournes sous forme de liste de {@code Map}.</li>
 *   <li>En cas d'erreur (requete invalide, etc.), un message d'erreur est affiche.</li>
 * </ul>
 */
public class QueryExecutor {

    /**
     * Lit une requete SQL saisie par l'utilisateur, l'execute sur le {@code KdTree}
     * gere par {@code kdTreeManager}, puis affiche les resultats ou les erreurs.
     *
     * @param scanner
     *         un {@code Scanner} pour lire l'entree utilisateur (la requete SQL).
     * @param kdTreeManager
     *         le gestionnaire du KdTree a interroger.
     */
    public static void executeQuery(Scanner scanner, KdTreeManager kdTreeManager) {
        System.out.print("Saisir une requete SQL : ");
        String query = scanner.nextLine().trim();
        try {
            // Execution de la requete sur le KdTree
            List<Map<String, String>> results = kdTreeManager.getKdTree().executeQuery(query);

            // Verification s'il y a des resultats
            if (results.isEmpty()) {
                System.out.println("Aucun resultat trouve pour la requete donnee.");
            } else {
                System.out.println("Resultats de la requete:");
                for (Map<String, String> result : results) {
                    // Chaque result est une map<Colonne, Valeur>
                    System.out.println(result);
                }
            }
        } catch (IllegalArgumentException e) {
            // Gestion d'erreurs liees a l'analyse de la requete (ex. operateur inconnu)
            System.out.println("Erreur dans la requete: " + e.getMessage());
        }
    }
}

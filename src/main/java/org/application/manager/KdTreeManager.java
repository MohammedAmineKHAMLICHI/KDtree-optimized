package org.application.manager;

import org.application.kdtree.KdTree;
import org.application.kdtree.KdTreeNode;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * <p>
 * La classe {@code KdTreeManager} centralise les opérations permettant de manipuler un
 * {@link KdTree} : chargement, sauvegarde, insertion de points, affichage, et recherches
 * (min, max, intervalle).
 * </p>
 *
 * <p>
 * <strong>Auteur :</strong> KHAMLICHI Mohammed Amine
 * </p>
 *
 * <h3>Rôles principaux :</h3>
 * <ul>
 *   <li>Gérer les E/S (lecture/écriture de fichier, saisie utilisateur via {@link Scanner}).</li>
 *   <li>Appeler les méthodes du {@link KdTree} correspondant (reconstruire l'arbre, effectuer des recherches, etc.).</li>
 *   <li>Assurer la cohérence des opérations (ex. empêcher l'ajout de points si l'échantillon est vide).</li>
 * </ul>
 */
public class KdTreeManager {
    /**
     * Instance de {@link KdTree} gérée par ce manager.
     */
    private final KdTree kdTree;

    /**
     * Construit un manager pour un KdTree à {@code dimensions} dimensions.
     *
     * @param dimensions
     *         le nombre de dimensions du KdTree (ex. 2 pour un Kd-Tree 2D).
     */
    public KdTreeManager(int dimensions) {
        this.kdTree = new KdTree(dimensions);
    }

    /**
     * Demande à l'utilisateur le nom d'un fichier, puis charge un échantillon (points) depuis ce fichier.
     * L'arbre est alors reconstruit à partir de ces données.
     *
     * @param scanner
     *         Le {@link Scanner} permettant de lire la saisie utilisateur (nom de fichier).
     * @throws IOException
     *         En cas de problème d'entrée/sortie (fichier introuvable, etc.).
     * @throws org.application.manager.InvalidCriteriaException
     *         Si le contenu du fichier ne correspond pas aux critères attendus (ex. M < 2).
     */
    public void loadSample(Scanner scanner)
            throws org.application.manager.InvalidCriteriaException, IOException {
        System.out.print("Entrez le nom du fichier: ");
        String filename = scanner.nextLine().trim();
        kdTree.loadFromFile(filename);
        System.out.println("Échantillon chargé avec succès.");
    }

    /**
     * Demande à l'utilisateur le nom d'un fichier, puis sauvegarde l'échantillon actuel
     * (points et critères) dans ce fichier. Les données sont celles stockées dans le {@link KdTree}.
     *
     * @param scanner
     *         Le {@link Scanner} permettant de lire la saisie utilisateur (nom de fichier).
     * @throws IOException
     *         En cas d'erreur d'écriture (répertoire introuvable, etc.).
     * @throws IllegalStateException
     *         Si l'échantillon est vide (pas de points) et qu'on ne peut donc rien sauvegarder.
     */
    public void saveSample(Scanner scanner) throws IOException {
        if (kdTree.isEmpty()) {
            throw new IllegalStateException("Impossible de sauvegarder : l'échantillon est vide.");
        }
        System.out.print("Entrez le nom du fichier: ");
        String filename = scanner.nextLine().trim();
        kdTree.saveToFile(filename);
        System.out.println("Échantillon sauvegardé avec succès.");
    }

    /**
     * Ajoute un point à l'échantillon (demande d'abord les coordonnées
     * et critères supplémentaires à l'utilisateur) puis reconstruit le KdTree.
     *
     * @param scanner
     *         Le {@link Scanner} permettant de lire les valeurs du point
     *         (coordonnées x, y) et des éventuels critères supplémentaires.
     * @throws org.application.manager.InvalidCriteriaException
     *         Si le nombre de critères supplémentaires ne correspond pas à celui attendu
     *         (défini dans {@code kdTree.criteriaNames}).
     * @throws IllegalStateException
     *         Si on tente d'ajouter un point alors que l'échantillon est vide ou non initialisé.
     */
    public void addPoint(Scanner scanner) throws org.application.manager.InvalidCriteriaException {
        if (kdTree.isEmpty()) {
            throw new IllegalStateException("Impossible d'ajouter un point : l'échantillon est vide.");
        }

        // Affichage des critères connus
        System.out.println("Critères disponibles:");
        for (int i = 0; i < kdTree.criteriaNames.length; i++) {
            System.out.println(i + ": " + kdTree.criteriaNames[i]);
        }

        // Lecture sécurisée des coordonnées
        double[] point = new double[2];
        String[] extraCriteria = new String[kdTree.criteriaNames.length - 2];

        // Première coordonnée (index 0)
        while (true) {
            System.out.print("Entrez la valeur pour " + kdTree.criteriaNames[0] + ": ");
            String input = scanner.nextLine();
            try {
                point[0] = Double.parseDouble(input);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Entrée non valide. Veuillez entrer un nombre.");
            }
        }

        // Deuxième coordonnée (index 1)
        while (true) {
            System.out.print("Entrez la valeur pour " + kdTree.criteriaNames[1] + ": ");
            String input = scanner.nextLine();
            try {
                point[1] = Double.parseDouble(input);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Entrée non valide. Veuillez entrer un nombre.");
            }
        }

        // Lecture des critères supplémentaires (si k=2 => 0 supplémentaire, sinon davantage)
        for (int i = 0; i < extraCriteria.length; i++) {
            System.out.print("Entrez la valeur pour " + kdTree.criteriaNames[i + 2] + ": ");
            extraCriteria[i] = scanner.nextLine();
        }

        kdTree.addPointAndRebuild(point, extraCriteria);
        System.out.println("Point ajouté et arbre KD reconstruit avec succès.");
    }

    /**
     * Affiche l'arbre KD sous forme textuelle (type ASCII-art).
     *
     * @throws IllegalStateException
     *         Si l'échantillon est vide et que l'arbre n'a pas été construit.
     */
    public void printTree() {
        if (kdTree.isEmpty()) {
            throw new IllegalStateException(
                    "Impossible d'afficher l'arbre : l'arbre n'a pas été construit car l'échantillon est vide."
            );
        }
        kdTree.printTree();
    }

    /**
     * Demande à l'utilisateur un critère (0 ou 1), puis recherche la valeur minimale
     * pour ce critère dans le {@link KdTree}, et l'affiche.
     *
     * @param scanner
     *         Le {@link Scanner} pour lire la saisie utilisateur.
     * @throws IllegalStateException
     *         Si l'arbre est vide.
     */
    public void findMinimum(Scanner scanner) {
        if (kdTree.isEmpty()) {
            throw new IllegalStateException("Impossible de trouver la valeur minimale : l'échantillon est vide.");
        }
        int dim = -1;
        while (true) {
            System.out.print("Entrez le critère (0 pour " + kdTree.criteriaNames[0]
                    + ", 1 pour " + kdTree.criteriaNames[1] + "): ");
            String input = scanner.nextLine();
            try {
                dim = Integer.parseInt(input);
                if (dim < 0 || dim >= kdTree.k) {
                    System.out.println("Critère invalide. Veuillez entrer 0 ou 1.");
                } else {
                    break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Entrée non valide. Veuillez entrer un entier.");
            }
        }

        double[] minPoint = kdTree.findMin(dim);
        if (minPoint != null) {
            System.out.println("Valeur minimale pour " + kdTree.criteriaNames[dim]
                    + ": " + minPoint[dim]);
        } else {
            System.out.println("Aucun point n'est présent dans l'arbre.");
        }
    }

    /**
     * Demande à l'utilisateur un critère (0 ou 1), puis recherche la valeur maximale
     * pour ce critère dans le {@link KdTree}, et l'affiche.
     *
     * @param scanner
     *         Le {@link Scanner} pour lire la saisie utilisateur.
     * @throws IllegalStateException
     *         Si l'échantillon est vide.
     */
    public void findMaximum(Scanner scanner) {
        if (kdTree.isEmpty()) {
            throw new IllegalStateException("Impossible de trouver la valeur maximale : l'échantillon est vide.");
        }
        int dim = -1;
        while (true) {
            System.out.print("Entrez le critère (0 pour " + kdTree.criteriaNames[0]
                    + ", 1 pour " + kdTree.criteriaNames[1] + "): ");
            String input = scanner.nextLine();
            try {
                dim = Integer.parseInt(input);
                if (dim < 0 || dim >= kdTree.k) {
                    System.out.println("Critère invalide. Veuillez entrer 0 ou 1.");
                } else {
                    break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Entrée non valide. Veuillez entrer un entier.");
            }
        }

        double[] maxPoint = kdTree.findMax(dim);
        if (maxPoint != null) {
            System.out.println("Valeur maximale pour " + kdTree.criteriaNames[dim]
                    + ": " + maxPoint[dim]);
        } else {
            System.out.println("Aucun point n'est présent dans l'arbre.");
        }
    }

    /**
     * Effectue une recherche par intervalle sur les deux premières dimensions (ex. x, y)
     * et affiche les points trouvés. L'utilisateur doit saisir les bornes inférieure
     * et supérieure pour chaque dimension.
     *
     * @param scanner
     *         Le {@link Scanner} pour lire les bornes de recherche.
     * @throws IllegalStateException
     *         Si l'échantillon est vide.
     */
    public void rangeSearch(Scanner scanner) {
        if (kdTree.isEmpty()) {
            throw new IllegalStateException("Impossible d'effectuer la recherche : l'échantillon est vide.");
        }

        double[] low = new double[2];
        double[] high = new double[2];

        // Lecture des bornes pour chaque dimension (0 => x, 1 => y)
        for (int i = 0; i < 2; i++) {
            // Borne inférieure
            while (true) {
                System.out.print("Entrez la valeur basse pour " + kdTree.criteriaNames[i] + ": ");
                String input = scanner.nextLine();
                try {
                    low[i] = Double.parseDouble(input);
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("Entrée non valide. Veuillez entrer un nombre.");
                }
            }

            // Borne supérieure
            while (true) {
                System.out.print("Entrez la valeur haute pour " + kdTree.criteriaNames[i] + ": ");
                String input = scanner.nextLine();
                try {
                    high[i] = Double.parseDouble(input);
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("Entrée non valide. Veuillez entrer un nombre.");
                }
            }
        }

        // Recherche par intervalle
        List<KdTreeNode> rangeSearchResults = kdTree.rangeSearch(low, high);
        if (rangeSearchResults.isEmpty()) {
            System.out.println("Aucun point trouvé dans la plage spécifiée.");
        } else {
            System.out.println("Points dans la plage:");
            for (KdTreeNode node : rangeSearchResults) {
                // Affichage des coordonnées + critères extras
                System.out.println(Arrays.toString(node.getPoint())
                        + " " + Arrays.toString(node.getExtraCriteria()));
            }
        }
    }

    /**
     * Retourne l'instance de {@link KdTree} gérée par ce manager.
     * <p>
     * Permet d'accéder directement aux méthodes du KdTree si nécessaire,
     * bien que l'usage normal s'effectue via ce manager.
     * </p>
     *
     * @return le {@link KdTree} interne.
     */
    public KdTree getKdTree() {
        return kdTree;
    }
}

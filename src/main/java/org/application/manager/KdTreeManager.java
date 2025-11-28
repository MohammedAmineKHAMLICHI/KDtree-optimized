package org.application.manager;

import org.application.kdtree.KdTree;
import org.application.kdtree.KdTreeNode;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * <p>
 * La classe {@code KdTreeManager} centralise les operations permettant de manipuler un
 * {@link KdTree} : chargement, sauvegarde, insertion de points, affichage, et recherches
 * (min, max, intervalle).
 * </p>
 *
 * <p>
 * <strong>Auteur :</strong> KHAMLICHI Mohammed Amine
 * </p>
 *
 * <h3>Roles principaux :</h3>
 * <ul>
 *   <li>Gerer les E/S (lecture/ecriture de fichier, saisie utilisateur via {@link Scanner}).</li>
 *   <li>Appeler les methodes du {@link KdTree} correspondant (reconstruire l'arbre, effectuer des recherches, etc.).</li>
 *   <li>Assurer la coherence des operations (ex. empecher l'ajout de points si l'echantillon est vide).</li>
 * </ul>
 */
public class KdTreeManager {
    /**
     * Instance de {@link KdTree} geree par ce manager.
     */
    private final KdTree kdTree;

    /**
     * Construit un manager pour un KdTree a {@code dimensions} dimensions.
     *
     * @param dimensions
     *         le nombre de dimensions du KdTree (ex. 2 pour un Kd-Tree 2D).
     */
    public KdTreeManager(int dimensions) {
        this.kdTree = new KdTree(dimensions);
    }

    /**
     * Demande a l'utilisateur le nom d'un fichier, puis charge un echantillon (points) depuis ce fichier.
     * L'arbre est alors reconstruit a partir de ces donnees.
     *
     * @param scanner
     *         Le {@link Scanner} permettant de lire la saisie utilisateur (nom de fichier).
     * @throws IOException
     *         En cas de probleme d'entree/sortie (fichier introuvable, etc.).
     * @throws org.application.manager.InvalidCriteriaException
     *         Si le contenu du fichier ne correspond pas aux criteres attendus (ex. M < 2).
     */
    public void loadSample(Scanner scanner)
            throws org.application.manager.InvalidCriteriaException, IOException {
        System.out.print("Entrez le nom du fichier: ");
        String filename = scanner.nextLine().trim();
        kdTree.loadFromFile(filename);
        System.out.println("Echantillon charge avec succes.");
    }

    /**
     * Demande a l'utilisateur le nom d'un fichier, puis sauvegarde l'echantillon actuel
     * (points et criteres) dans ce fichier. Les donnees sont celles stockees dans le {@link KdTree}.
     *
     * @param scanner
     *         Le {@link Scanner} permettant de lire la saisie utilisateur (nom de fichier).
     * @throws IOException
     *         En cas d'erreur d'ecriture (repertoire introuvable, etc.).
     * @throws IllegalStateException
     *         Si l'echantillon est vide (pas de points) et qu'on ne peut donc rien sauvegarder.
     */
    public void saveSample(Scanner scanner) throws IOException {
        if (kdTree.isEmpty()) {
            throw new IllegalStateException("Impossible de sauvegarder : l'echantillon est vide.");
        }
        System.out.print("Entrez le nom du fichier: ");
        String filename = scanner.nextLine().trim();
        kdTree.saveToFile(filename);
        System.out.println("Echantillon sauvegarde avec succes.");
    }

    /**
     * Ajoute un point a l'echantillon (demande d'abord les coordonnees
     * et criteres supplementaires a l'utilisateur) puis reconstruit le KdTree.
     *
     * @param scanner
     *         Le {@link Scanner} permettant de lire les valeurs du point
     *         (coordonnees x, y) et des eventuels criteres supplementaires.
     * @throws org.application.manager.InvalidCriteriaException
     *         Si le nombre de criteres supplementaires ne correspond pas a celui attendu
     *         (defini dans {@code kdTree.criteriaNames}).
     * @throws IllegalStateException
     *         Si on tente d'ajouter un point alors que l'echantillon est vide ou non initialise.
     */
    public void addPoint(Scanner scanner) throws org.application.manager.InvalidCriteriaException {
        if (kdTree.isEmpty()) {
            throw new IllegalStateException("Impossible d'ajouter un point : l'echantillon est vide.");
        }

        // Affichage des criteres connus
        System.out.println("Criteres disponibles:");
        for (int i = 0; i < kdTree.criteriaNames.length; i++) {
            System.out.println(i + ": " + kdTree.criteriaNames[i]);
        }

        // Lecture securisee des coordonnees
        double[] point = new double[2];
        String[] extraCriteria = new String[kdTree.criteriaNames.length - 2];

        // Premiere coordonnee (index 0)
        while (true) {
            System.out.print("Entrez la valeur pour " + kdTree.criteriaNames[0] + ": ");
            String input = scanner.nextLine();
            try {
                point[0] = Double.parseDouble(input);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Entree non valide. Veuillez entrer un nombre.");
            }
        }

        // Deuxieme coordonnee (index 1)
        while (true) {
            System.out.print("Entrez la valeur pour " + kdTree.criteriaNames[1] + ": ");
            String input = scanner.nextLine();
            try {
                point[1] = Double.parseDouble(input);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Entree non valide. Veuillez entrer un nombre.");
            }
        }

        // Lecture des criteres supplementaires (si k=2 => 0 supplementaire, sinon davantage)
        for (int i = 0; i < extraCriteria.length; i++) {
            System.out.print("Entrez la valeur pour " + kdTree.criteriaNames[i + 2] + ": ");
            extraCriteria[i] = scanner.nextLine();
        }

        kdTree.addPointAndRebuild(point, extraCriteria);
        System.out.println("Point ajoute et arbre KD reconstruit avec succes.");
    }

    /**
     * Affiche l'arbre KD sous forme textuelle (type ASCII-art).
     *
     * @throws IllegalStateException
     *         Si l'echantillon est vide et que l'arbre n'a pas ete construit.
     */
    public void printTree() {
        if (kdTree.isEmpty()) {
            throw new IllegalStateException(
                    "Impossible d'afficher l'arbre : l'arbre n'a pas ete construit car l'echantillon est vide."
            );
        }
        kdTree.printTree();
    }

    /**
     * Demande a l'utilisateur un critere (0 ou 1), puis recherche la valeur minimale
     * pour ce critere dans le {@link KdTree}, et l'affiche.
     *
     * @param scanner
     *         Le {@link Scanner} pour lire la saisie utilisateur.
     * @throws IllegalStateException
     *         Si l'arbre est vide.
     */
    public void findMinimum(Scanner scanner) {
        if (kdTree.isEmpty()) {
            throw new IllegalStateException("Impossible de trouver la valeur minimale : l'echantillon est vide.");
        }
        int dim = -1;
        while (true) {
            System.out.print("Entrez le critere (0 pour " + kdTree.criteriaNames[0]
                    + ", 1 pour " + kdTree.criteriaNames[1] + "): ");
            String input = scanner.nextLine();
            try {
                dim = Integer.parseInt(input);
                if (dim < 0 || dim >= kdTree.k) {
                    System.out.println("Critere invalide. Veuillez entrer 0 ou 1.");
                } else {
                    break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Entree non valide. Veuillez entrer un entier.");
            }
        }

        double[] minPoint = kdTree.findMin(dim);
        if (minPoint != null) {
            System.out.println("Valeur minimale pour " + kdTree.criteriaNames[dim]
                    + ": " + minPoint[dim]);
        } else {
            System.out.println("Aucun point n'est present dans l'arbre.");
        }
    }

    /**
     * Demande a l'utilisateur un critere (0 ou 1), puis recherche la valeur maximale
     * pour ce critere dans le {@link KdTree}, et l'affiche.
     *
     * @param scanner
     *         Le {@link Scanner} pour lire la saisie utilisateur.
     * @throws IllegalStateException
     *         Si l'echantillon est vide.
     */
    public void findMaximum(Scanner scanner) {
        if (kdTree.isEmpty()) {
            throw new IllegalStateException("Impossible de trouver la valeur maximale : l'echantillon est vide.");
        }
        int dim = -1;
        while (true) {
            System.out.print("Entrez le critere (0 pour " + kdTree.criteriaNames[0]
                    + ", 1 pour " + kdTree.criteriaNames[1] + "): ");
            String input = scanner.nextLine();
            try {
                dim = Integer.parseInt(input);
                if (dim < 0 || dim >= kdTree.k) {
                    System.out.println("Critere invalide. Veuillez entrer 0 ou 1.");
                } else {
                    break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Entree non valide. Veuillez entrer un entier.");
            }
        }

        double[] maxPoint = kdTree.findMax(dim);
        if (maxPoint != null) {
            System.out.println("Valeur maximale pour " + kdTree.criteriaNames[dim]
                    + ": " + maxPoint[dim]);
        } else {
            System.out.println("Aucun point n'est present dans l'arbre.");
        }
    }

    /**
     * Effectue une recherche par intervalle sur les deux premieres dimensions (ex. x, y)
     * et affiche les points trouves. L'utilisateur doit saisir les bornes inferieure
     * et superieure pour chaque dimension.
     *
     * @param scanner
     *         Le {@link Scanner} pour lire les bornes de recherche.
     * @throws IllegalStateException
     *         Si l'echantillon est vide.
     */
    public void rangeSearch(Scanner scanner) {
        if (kdTree.isEmpty()) {
            throw new IllegalStateException("Impossible d'effectuer la recherche : l'echantillon est vide.");
        }

        double[] low = new double[2];
        double[] high = new double[2];

        // Lecture des bornes pour chaque dimension (0 => x, 1 => y)
        for (int i = 0; i < 2; i++) {
            // Borne inferieure
            while (true) {
                System.out.print("Entrez la valeur basse pour " + kdTree.criteriaNames[i] + ": ");
                String input = scanner.nextLine();
                try {
                    low[i] = Double.parseDouble(input);
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("Entree non valide. Veuillez entrer un nombre.");
                }
            }

            // Borne superieure
            while (true) {
                System.out.print("Entrez la valeur haute pour " + kdTree.criteriaNames[i] + ": ");
                String input = scanner.nextLine();
                try {
                    high[i] = Double.parseDouble(input);
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("Entree non valide. Veuillez entrer un nombre.");
                }
            }
        }

        // Recherche par intervalle
        List<KdTreeNode> rangeSearchResults = kdTree.rangeSearch(low, high);
        if (rangeSearchResults.isEmpty()) {
            System.out.println("Aucun point trouve dans la plage specifiee.");
        } else {
            System.out.println("Points dans la plage:");
            for (KdTreeNode node : rangeSearchResults) {
                // Affichage des coordonnees + criteres extras
                System.out.println(Arrays.toString(node.getPoint())
                        + " " + Arrays.toString(node.getExtraCriteria()));
            }
        }
    }

    /**
     * Retourne l'instance de {@link KdTree} geree par ce manager.
     * <p>
     * Permet d'acceder directement aux methodes du KdTree si necessaire,
     * bien que l'usage normal s'effectue via ce manager.
     * </p>
     *
     * @return le {@link KdTree} interne.
     */
    public KdTree getKdTree() {
        return kdTree;
    }
}

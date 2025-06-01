package org.application.menu;

import org.application.manager.KdTreeManager;
import org.application.manager.InvalidCriteriaException;
import java.util.Scanner;

/**
 * <p>
 * La classe {@code Menu} fournit une interface utilisateur textuelle pour interagir
 * avec un {@link KdTreeManager}. Son rôle est de guider l'utilisateur à travers
 * différentes options permettant de :
 * </p>
 * <ul>
 *   <li>Charger un échantillon depuis un fichier</li>
 *   <li>Sauvegarder un échantillon dans un fichier</li>
 *   <li>Ajouter un point</li>
 *   <li>Afficher l'arbre</li>
 *   <li>Rechercher la valeur minimale ou maximale pour un critère</li>
 *   <li>Effectuer une recherche par intervalle</li>
 *   <li>Construire interactivement un échantillon</li>
 *   <li>Exécuter une requête SQL</li>
 * </ul>
 *
 * <p>
 * Le menu reste actif en continu jusqu'à ce que l'utilisateur sélectionne l'option de sortie (10).
 * En cas d'erreur lors de l'exécution d'une option, un message explicite est affiché.
 * </p>
 *
 * <p>
 * <strong>Auteur :</strong> KHAMLICHI Mohammed Amine
 * </p>
 */
public class Menu {
    /**
     * Le gestionnaire de KdTree permettant d'effectuer les opérations
     * (chargement, sauvegarde, recherches, etc.).
     */
    private final KdTreeManager kdTreeManager;

    /**
     * Un {@link Scanner} pour lire les entrées utilisateur (saisie au clavier).
     */
    private final Scanner scanner;

    /**
     * Construit une instance de {@code Menu} avec le gestionnaire de KdTree spécifié.
     *
     * @param kdTreeManager
     *         le gestionnaire de KdTree sur lequel effectuer les opérations.
     */
    public Menu(KdTreeManager kdTreeManager) {
        this.kdTreeManager = kdTreeManager;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Démarre l'interface de menu et attend en boucle les choix de l'utilisateur.
     * <p>
     * Après chaque action, un délai de 2 secondes est marqué, puis le menu se réaffiche.
     * Pour quitter, l'utilisateur sélectionne l'option 10.
     * </p>
     *
     * @throws InterruptedException
     *         si l'exécution du thread est interrompue durant la pause entre les itérations.
     */
    public void start() throws InterruptedException {
        while (true) {
            displayOptions();
            int choice = getUserChoice();

            try {
                handleChoice(choice);
            } catch (Exception e) {
                System.out.println("Erreur: " + e.getMessage());
            }

            // Pause avant de réafficher le menu
            Thread.sleep(2000);
            System.out.println();
        }
    }

    /**
     * Affiche les options disponibles dans le menu sous forme numérotée.
     */
    private void displayOptions() {
        System.out.println("Options:");
        System.out.println("1. Charger un échantillon depuis un fichier");
        System.out.println("2. Sauvegarder un échantillon dans un fichier");
        System.out.println("3. Ajouter un point");
        System.out.println("4. Afficher l'arbre");
        System.out.println("5. Trouver la valeur minimale pour un critère");
        System.out.println("6. Trouver la valeur maximale pour un critère");
        System.out.println("7. Effectuer une recherche de plage");
        System.out.println("8. Construire interactivement un échantillon");
        System.out.println("9. Exécuter une requête SQL");
        System.out.println("10. Quitter");
        System.out.print("Choisissez une option: ");
    }

    /**
     * Lit et retourne le choix de l'utilisateur depuis l'entrée standard.
     * <p>
     * Si la saisie n'est pas un entier valide, on indique l'erreur et renvoie -1.
     * </p>
     *
     * @return le choix de l'utilisateur (entier), ou -1 si la saisie est invalide.
     */
    private int getUserChoice() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Option invalide. Veuillez entrer un nombre.");
            return -1;
        }
    }

    /**
     * Exécute l'action correspondant au choix de l'utilisateur.
     * <p>
     * Chaque choix (1 à 10) est associé à une opération spécifique. Si le choix ne
     * correspond à aucune option valide, un message l'indique.
     * </p>
     *
     * @param choice
     *         le choix de l'utilisateur (un entier).
     * @throws Exception
     *         si une erreur survient lors de l'exécution de l'action (ex. fichier inexistant).
     */
    private void handleChoice(int choice) throws Exception {
        switch (choice) {
            case 1:
                kdTreeManager.loadSample(scanner);
                break;
            case 2:
                kdTreeManager.saveSample(scanner);
                break;
            case 3:
                kdTreeManager.addPoint(scanner);
                break;
            case 4:
                kdTreeManager.printTree();
                break;
            case 5:
                kdTreeManager.findMinimum(scanner);
                break;
            case 6:
                kdTreeManager.findMaximum(scanner);
                break;
            case 7:
                kdTreeManager.rangeSearch(scanner);
                break;
            case 8:
                SampleBuilder.buildSample(scanner, kdTreeManager);
                break;
            case 9:
                QueryExecutor.executeQuery(scanner, kdTreeManager);
                break;
            case 10:
                System.out.println("Au revoir!");
                scanner.close();
                System.exit(0);
                break;
            default:
                System.out.println("Option invalide.");
        }
    }
}

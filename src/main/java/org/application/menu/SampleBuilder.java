package org.application.menu;

import org.application.manager.InvalidCriteriaException;
import org.application.manager.KdTreeManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * <p>
 * La classe {@code SampleBuilder} fournit une méthode statique permettant de construire
 * interactivement un échantillon de points, de le sauvegarder dans un fichier texte,
 * puis de le charger dans un {@code KdTree} via un {@link KdTreeManager}.
 * </p>
 *
 * <p>
 * <strong>Auteur :</strong> KHAMLICHI Mohammed Amine
 * </p>
 *
 * <h3>Fonctionnement :</h3>
 * <ol>
 *   <li>On demande à l'utilisateur le <em>nombre de critères</em> (au moins 2) et leurs noms.</li>
 *   <li>On demande le <em>nombre de points</em> à créer.</li>
 *   <li>Pour chacun des points :
 *       <ul>
 *         <li>On lit deux coordonnées numériques (ex. x, y).</li>
 *         <li>On lit les éventuels autres critères (chaînes de caractères).</li>
 *       </ul>
 *   </li>
 *   <li>On <strong>sauvegarde</strong> l'échantillon dans un fichier, dans le répertoire
 *       {@code src/main/resources}, au nom spécifié.</li>
 *   <li>On <strong>charge</strong> ensuite l'échantillon dans le KdTree géré par {@code kdTreeManager}.</li>
 * </ol>
 */
public class SampleBuilder {

    /**
     * Construit interactivement un échantillon de points, le sauvegarde dans un fichier texte
     * (au format demandé) et le charge dans l'arbre KD.
     * <p>
     * Si une erreur survient lors de la sauvegarde ou du chargement, un message d'erreur
     * s'affiche, puis la méthode se termine.
     * </p>
     *
     * @param scanner
     *         Le {@link Scanner} pour lire les entrées utilisateur (noms de critères,
     *         nombre de points, coordonnées...).
     * @param kdTreeManager
     *         Le gestionnaire de l'arbre KD (permet d'appeler {@code loadFromFile}).
     */
    public static void buildSample(Scanner scanner, KdTreeManager kdTreeManager) {
        try {
            System.out.println("Construction interactive d'un échantillon");

            // Lecture du nombre de critères (au moins 2)
            int numCriteria = readPositiveInt(
                    scanner,
                    "Entrez le nombre de critères : ",
                    2,
                    "Le nombre de critères doit être au moins 2."
            );

            // Lecture des noms de critères
            String[] criteriaNames = new String[numCriteria];
            for (int i = 0; i < numCriteria; i++) {
                System.out.print("Entrez le nom du critère " + (i + 1) + " : ");
                criteriaNames[i] = scanner.nextLine().trim();
            }

            // Lecture du nombre de points (strictement positif)
            int numPoints = readPositiveInt(
                    scanner,
                    "Entrez le nombre de points : ",
                    1,
                    "Le nombre de points doit être supérieur à 0."
            );

            // Lecture des points
            List<Map.Entry<double[], String[]>> points = new ArrayList<>();
            for (int i = 0; i < numPoints; i++) {
                System.out.println("Saisie du point " + (i + 1) + " :");
                double[] pointCoords = new double[2];

                // Lecture des deux premières coordonnées (numériques)
                for (int j = 0; j < 2; j++) {
                    pointCoords[j] = readDouble(scanner, "Entrez la valeur pour " + criteriaNames[j] + " : ");
                }

                // Lecture des critères supplémentaires (s'il y en a : numCriteria - 2)
                String[] extraCriteria = (numCriteria > 2)
                        ? new String[numCriteria - 2]
                        : new String[0];
                for (int j = 0; j < extraCriteria.length; j++) {
                    System.out.print("Entrez la valeur pour " + criteriaNames[j + 2] + " : ");
                    extraCriteria[j] = scanner.nextLine().trim();
                }

                points.add(new AbstractMap.SimpleEntry<>(pointCoords, extraCriteria));
            }

            // Sauvegarde de l'échantillon dans un fichier
            System.out.print("Entrez le nom du fichier où sauvegarder l'échantillon: ");
            String filename = scanner.nextLine().trim();

            String projectPath = new File("").getAbsolutePath();
            String resourceDirPath = projectPath + File.separator
                    + "src" + File.separator + "main" + File.separator + "resources";
            File resourceDir = new File(resourceDirPath);
            if (!resourceDir.exists() && !resourceDir.mkdirs()) {
                throw new IOException("Impossible de créer le répertoire des ressources.");
            }
            String resourceFilePath = resourceDirPath + File.separator + filename;

            // Écriture de l'échantillon dans le fichier
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(resourceFilePath))) {
                writer.write(numCriteria + "\n");
                for (String criterion : criteriaNames) {
                    writer.write(criterion + "\n");
                }
                writer.write(numPoints + "\n");
                for (Map.Entry<double[], String[]> entry : points) {
                    writer.write(entry.getKey()[0] + " " + entry.getKey()[1]);
                    for (String extra : entry.getValue()) {
                        writer.write(" " + extra);
                    }
                    writer.write("\n");
                }
            } catch (IOException e) {
                System.out.println("Erreur lors de la sauvegarde du fichier : " + e.getMessage());
                return;
            }

            // Tentative de chargement de l'échantillon sauvegardé dans l'arbre KD
            try {
                // Utilisation du chemin absolu du fichier sauvegardé
                kdTreeManager.getKdTree().loadFromFile(resourceFilePath);
                System.out.println("Échantillon construit et chargé avec succès.");
            } catch (IOException | InvalidCriteriaException e) {
                System.out.println("Erreur lors de la construction ou du chargement de l'échantillon: "
                        + e.getMessage());
            }

        } catch (IOException e) {
            // Erreur imprévue lors de la création du répertoire ou autre
            throw new RuntimeException(e);
        }
    }

    /**
     * Lit un entier depuis l'entrée standard en vérifiant qu'il est supérieur ou égal
     * à une valeur minimale.
     *
     * @param scanner
     *         Le {@link Scanner} pour lire l'entrée.
     * @param prompt
     *         Le message à afficher pour inviter l'utilisateur à saisir.
     * @param minValue
     *         La valeur minimale acceptable.
     * @param errorMessage
     *         Le message d'erreur à afficher si la valeur saisie est insuffisante.
     * @return L'entier lu, garanti supérieur ou égal à {@code minValue}.
     */
    private static int readPositiveInt(Scanner scanner,
                                       String prompt,
                                       int minValue,
                                       String errorMessage) {
        int value = 0;
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();
            try {
                value = Integer.parseInt(input);
                if (value < minValue) {
                    System.out.println(errorMessage);
                } else {
                    break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Entrée non valide. Veuillez entrer un entier.");
            }
        }
        return value;
    }

    /**
     * Lit un {@code double} depuis l'entrée standard en affichant un message d'invite.
     * <p>
     * Réessaye tant que la saisie n'est pas un nombre valide.
     * </p>
     *
     * @param scanner
     *         Le {@link Scanner} pour lire l'entrée.
     * @param prompt
     *         Le message à afficher pour inviter l'utilisateur à saisir un nombre.
     * @return La valeur numérique saisie (double).
     */
    private static double readDouble(Scanner scanner, String prompt) {
        double value = 0.0;
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();
            try {
                value = Double.parseDouble(input);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Entrée non valide. Veuillez entrer un nombre.");
            }
        }
        return value;
    }
}

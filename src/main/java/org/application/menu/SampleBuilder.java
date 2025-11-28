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
 * La classe {@code SampleBuilder} fournit une methode statique permettant de construire
 * interactivement un echantillon de points, de le sauvegarder dans un fichier texte,
 * puis de le charger dans un {@code KdTree} via un {@link KdTreeManager}.
 * </p>
 *
 * <p>
 * <strong>Auteur :</strong> KHAMLICHI Mohammed Amine
 * </p>
 *
 * <h3>Fonctionnement :</h3>
 * <ol>
 *   <li>On demande a l'utilisateur le <em>nombre de criteres</em> (au moins 2) et leurs noms.</li>
 *   <li>On demande le <em>nombre de points</em> a creer.</li>
 *   <li>Pour chacun des points :
 *       <ul>
 *         <li>On lit deux coordonnees numeriques (ex. x, y).</li>
 *         <li>On lit les eventuels autres criteres (chaines de caracteres).</li>
 *       </ul>
 *   </li>
 *   <li>On <strong>sauvegarde</strong> l'echantillon dans un fichier, dans le repertoire
 *       {@code src/main/resources}, au nom specifie.</li>
 *   <li>On <strong>charge</strong> ensuite l'echantillon dans le KdTree gere par {@code kdTreeManager}.</li>
 * </ol>
 */
public class SampleBuilder {

    /**
     * Construit interactivement un echantillon de points, le sauvegarde dans un fichier texte
     * (au format demande) et le charge dans l'arbre KD.
     * <p>
     * Si une erreur survient lors de la sauvegarde ou du chargement, un message d'erreur
     * s'affiche, puis la methode se termine.
     * </p>
     *
     * @param scanner
     *         Le {@link Scanner} pour lire les entrees utilisateur (noms de criteres,
     *         nombre de points, coordonnees...).
     * @param kdTreeManager
     *         Le gestionnaire de l'arbre KD (permet d'appeler {@code loadFromFile}).
     */
    public static void buildSample(Scanner scanner, KdTreeManager kdTreeManager) {
        try {
            System.out.println("Construction interactive d'un echantillon");

            // Lecture du nombre de criteres (au moins 2)
            int numCriteria = readPositiveInt(
                    scanner,
                    "Entrez le nombre de criteres : ",
                    2,
                    "Le nombre de criteres doit etre au moins 2."
            );

            // Lecture des noms de criteres
            String[] criteriaNames = new String[numCriteria];
            for (int i = 0; i < numCriteria; i++) {
                System.out.print("Entrez le nom du critere " + (i + 1) + " : ");
                criteriaNames[i] = scanner.nextLine().trim();
            }

            // Lecture du nombre de points (strictement positif)
            int numPoints = readPositiveInt(
                    scanner,
                    "Entrez le nombre de points : ",
                    1,
                    "Le nombre de points doit etre superieur a 0."
            );

            // Lecture des points
            List<Map.Entry<double[], String[]>> points = new ArrayList<>();
            for (int i = 0; i < numPoints; i++) {
                System.out.println("Saisie du point " + (i + 1) + " :");
                double[] pointCoords = new double[2];

                // Lecture des deux premieres coordonnees (numeriques)
                for (int j = 0; j < 2; j++) {
                    pointCoords[j] = readDouble(scanner, "Entrez la valeur pour " + criteriaNames[j] + " : ");
                }

                // Lecture des criteres supplementaires (s'il y en a : numCriteria - 2)
                String[] extraCriteria = (numCriteria > 2)
                        ? new String[numCriteria - 2]
                        : new String[0];
                for (int j = 0; j < extraCriteria.length; j++) {
                    System.out.print("Entrez la valeur pour " + criteriaNames[j + 2] + " : ");
                    extraCriteria[j] = scanner.nextLine().trim();
                }

                points.add(new AbstractMap.SimpleEntry<>(pointCoords, extraCriteria));
            }

            // Sauvegarde de l'echantillon dans un fichier
            System.out.print("Entrez le nom du fichier ou sauvegarder l'echantillon: ");
            String filename = scanner.nextLine().trim();

            String projectPath = new File("").getAbsolutePath();
            String resourceDirPath = projectPath + File.separator
                    + "src" + File.separator + "main" + File.separator + "resources";
            File resourceDir = new File(resourceDirPath);
            if (!resourceDir.exists() && !resourceDir.mkdirs()) {
                throw new IOException("Impossible de creer le repertoire des ressources.");
            }
            String resourceFilePath = resourceDirPath + File.separator + filename;

            // Ecriture de l'echantillon dans le fichier
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

            // Tentative de chargement de l'echantillon sauvegarde dans l'arbre KD
            try {
                // Utilisation du chemin absolu du fichier sauvegarde
                kdTreeManager.getKdTree().loadFromFile(resourceFilePath);
                System.out.println("Echantillon construit et charge avec succes.");
            } catch (IOException | InvalidCriteriaException e) {
                System.out.println("Erreur lors de la construction ou du chargement de l'echantillon: "
                        + e.getMessage());
            }

        } catch (IOException e) {
            // Erreur imprevue lors de la creation du repertoire ou autre
            throw new RuntimeException(e);
        }
    }

    /**
     * Lit un entier depuis l'entree standard en verifiant qu'il est superieur ou egal
     * a une valeur minimale.
     *
     * @param scanner
     *         Le {@link Scanner} pour lire l'entree.
     * @param prompt
     *         Le message a afficher pour inviter l'utilisateur a saisir.
     * @param minValue
     *         La valeur minimale acceptable.
     * @param errorMessage
     *         Le message d'erreur a afficher si la valeur saisie est insuffisante.
     * @return L'entier lu, garanti superieur ou egal a {@code minValue}.
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
                System.out.println("Entree non valide. Veuillez entrer un entier.");
            }
        }
        return value;
    }

    /**
     * Lit un {@code double} depuis l'entree standard en affichant un message d'invite.
     * <p>
     * Reessaye tant que la saisie n'est pas un nombre valide.
     * </p>
     *
     * @param scanner
     *         Le {@link Scanner} pour lire l'entree.
     * @param prompt
     *         Le message a afficher pour inviter l'utilisateur a saisir un nombre.
     * @return La valeur numerique saisie (double).
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
                System.out.println("Entree non valide. Veuillez entrer un nombre.");
            }
        }
        return value;
    }
}

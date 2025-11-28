import org.application.kdtree.KdTree;
import org.application.kdtree.KdTreeNode;
import org.application.manager.InvalidCriteriaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <p>
 * La classe de test {@code KdTreeTest} contient un ensemble de tests JUnit (version 5)
 * pour valider les fonctionnalites de la classe {@link KdTree}. Ces tests portent
 * notamment sur :
 * </p>
 * <ul>
 *   <li>Le chargement des donnees depuis des fichiers</li>
 *   <li>L'ajout (insertion) de points et la reconstruction de l'arbre</li>
 *   <li>Les recherches de min et max sur les deux premieres dimensions</li>
 *   <li>La recherche par intervalle (range search)</li>
 *   <li>L'execution de requetes SQL simples</li>
 *   <li>La gestion des erreurs (fichiers invalides, doublons, etc.)</li>
 * </ul>
 *
 * <p>
 * <strong>Auteur :</strong> KHAMLICHI Mohammed Amine
 * </p>
 */
public class KdTreeTest {

    /**
     * L'instance de {@link KdTree} testee dans chaque methode.
     */
    private KdTree kdTree;

    /**
     * Initialise une instance de {@link KdTree} (2D) avant chaque test.
     */
    @BeforeEach
    public void setUp() {
        kdTree = new KdTree(2);
    }

    /**
     * Renvoie le chemin absolu d'un fichier de test localise dans le dossier des ressources de test.
     *
     * @param filename
     *         Le nom du fichier (ex. "1.txt").
     * @return Le chemin absolu du fichier, si trouve dans les ressources (classpath).
     */
    private String getTestFilePath(String filename) {
        return Objects.requireNonNull(getClass().getClassLoader().getResource(filename)).getPath();
    }

    /**
     * Teste le chargement d'un echantillon depuis un fichier texte.
     * <p>
     * Le fichier contient un nombre de points connu ; on verifie que le {@link KdTree#nodeList}
     * correspond a la taille attendue.
     * </p>
     *
     * @throws IOException
     *         si le fichier est introuvable ou si une erreur de lecture survient.
     * @throws InvalidCriteriaException
     *         si le nombre de criteres dans le fichier est invalide.
     */
    @Test
    public void testLoadFromFile() throws IOException, InvalidCriteriaException {
        String filename = getTestFilePath("1.txt");
        kdTree.loadFromFile(filename);
        assertEquals(6, kdTree.nodeList.size());
    }

    /**
     * Teste l'ajout d'un point et la reconstruction de l'arbre KdTree.
     * <p>
     * On definit d'abord {@link KdTree#criteriaNames}, puis on ajoute un point avec
     * {@code addPointAndRebuild}. On verifie ensuite que la liste des nuds contient
     * le point ajoute.
     * </p>
     *
     * @throws InvalidCriteriaException
     *         si le nombre de criteres fournis a l'insertion est incorrect.
     */
    @Test
    public void testAddPointAndRebuild() throws InvalidCriteriaException {
        kdTree.criteriaNames = new String[]{"year", "height", "department"};
        kdTree.addPointAndRebuild(new double[]{3.0, 182.0}, new String[]{"informatique"});
        assertEquals(1, kdTree.nodeList.size());
    }

    /**
     * Teste la recherche de la valeur minimale pour chaque dimension dans un arbre prealablement charge.
     *
     * @throws IOException
     *         si le fichier est introuvable ou non lisible.
     * @throws InvalidCriteriaException
     *         si le format du fichier est invalide.
     */
    @Test
    public void testFindMin() throws IOException, InvalidCriteriaException {
        String filename = getTestFilePath("1.txt");
        kdTree.loadFromFile(filename);
        double[] minYear = kdTree.findMin(0);
        double[] minHeight = kdTree.findMin(1);

        assertArrayEquals(new double[]{1.0, 164.63}, minYear);
        assertArrayEquals(new double[]{1.0, 164.63}, minHeight);
    }

    /**
     * Teste la recherche de la valeur maximale pour chaque dimension dans un arbre prealablement charge.
     *
     * @throws IOException
     *         si le fichier est introuvable.
     * @throws InvalidCriteriaException
     *         si le fichier contient des criteres invalides.
     */
    @Test
    public void testFindMax() throws IOException, InvalidCriteriaException {
        String filename = getTestFilePath("1.txt");
        kdTree.loadFromFile(filename);
        double[] maxYear = kdTree.findMax(0);
        double[] maxHeight = kdTree.findMax(1);

        assertArrayEquals(new double[]{4.0, 181.13}, maxYear);
        assertArrayEquals(new double[]{3.0, 182.0}, maxHeight);
    }

    /**
     * Teste la recherche par intervalle (range search) avec des bornes specifiques.
     *
     * @throws IOException
     *         si le fichier n'existe pas.
     * @throws InvalidCriteriaException
     *         si le fichier est mal forme.
     */
    @Test
    public void testRangeSearch() throws IOException, InvalidCriteriaException {
        String filename = getTestFilePath("1.txt");
        kdTree.loadFromFile(filename);

        // Intervalle [3.0, 4.0] pour x, et [170.0, 180.0] pour y
        List<KdTreeNode> results = kdTree.rangeSearch(new double[]{3.0, 170.0}, new double[]{4.0, 180.0});
        assertEquals(1, results.size());
        assertArrayEquals(new double[]{4.0, 176.33}, results.get(0).getPoint());

        // Intervalle plus large
        results = kdTree.rangeSearch(new double[]{1.0, 160.0}, new double[]{4.0, 182.0});
        assertEquals(6, results.size());

        // Verifie que tous les points de results respectent la plage
        for (KdTreeNode node : results) {
            assertTrue(node.getPoint()[0] >= 1.0 && node.getPoint()[0] <= 4.0);
            assertTrue(node.getPoint()[1] >= 160.0 && node.getPoint()[1] <= 182.0);
        }
    }

    /**
     * Teste l'execution d'une requete SQL simple sur l'arbre charge.
     *
     * @throws IOException
     *         si probleme de fichier.
     * @throws InvalidCriteriaException
     *         si fichier incorrect.
     */
    @Test
    public void testExecuteQuery() throws IOException, InvalidCriteriaException {
        String filename = getTestFilePath("1.txt");
        kdTree.loadFromFile(filename);

        // "SELECT department FROM P WHERE year = 3 AND height in [170,190]"
        List<Map<String, String>> results =
                kdTree.executeQuery("SELECT department FROM P WHERE year = 3 AND height in [170,190]");
        assertEquals(1, results.size());
        assertEquals("informatique", results.get(0).get("department"));
    }

    /**
     * Teste l'execution d'une requete SQL avec plusieurs conditions (AND).
     *
     * @throws IOException
     *         si probleme de lecture de fichier.
     * @throws InvalidCriteriaException
     *         si les criteres sont invalides.
     */
    @Test
    public void testExecuteQueryWithMultipleConditions() throws IOException, InvalidCriteriaException {
        String filename = getTestFilePath("1.txt");
        kdTree.loadFromFile(filename);

        // "SELECT department,year FROM P WHERE height >= 170 AND height <= 180"
        List<Map<String, String>> results =
                kdTree.executeQuery("SELECT department,year FROM P WHERE height >= 170 AND height <= 180");

        assertEquals(2, results.size());

        Map<String, String> expected1 = new HashMap<>();
        expected1.put("year", "1.0");
        expected1.put("department", "mathematique");

        Map<String, String> expected2 = new HashMap<>();
        expected2.put("year", "4.0");
        expected2.put("department", "physique");

        List<Map<String, String>> expectedResults = Arrays.asList(expected1, expected2);
        assertTrue(results.containsAll(expectedResults));
    }

    /**
     * Teste la recherche par intervalle pour un point exact.
     *
     * @throws IOException
     *         si probleme de fichier.
     * @throws InvalidCriteriaException
     *         si format incorrect.
     */
    @Test
    public void testRangeSearchExactMatch() throws IOException, InvalidCriteriaException {
        String filename = getTestFilePath("1.txt");
        kdTree.loadFromFile(filename);

        // Intervalle exact pour x=3.0,y=182.0
        List<KdTreeNode> results = kdTree.rangeSearch(new double[]{3.0, 182.0}, new double[]{3.0, 182.0});
        assertEquals(1, results.size());
        assertArrayEquals(new double[]{3.0, 182.0}, results.get(0).getPoint());
    }

    /**
     * Teste la recherche par intervalle qui ne retourne aucun resultat.
     */
    @Test
    public void testRangeSearchNoResults() throws IOException, InvalidCriteriaException {
        String filename = getTestFilePath("1.txt");
        kdTree.loadFromFile(filename);

        // Intervalle dans lequel aucun point ne se situe
        List<KdTreeNode> results = kdTree.rangeSearch(new double[]{5.0, 190.0}, new double[]{6.0, 200.0});
        assertEquals(0, results.size());
    }

    /**
     * Teste la recherche par intervalle quand plusieurs points partagent la meme valeur
     * sur une dimension (ex. x=4).
     */
    @Test
    public void testRangeSearchSameValues() throws IOException, InvalidCriteriaException {
        String filename = getTestFilePath("1.txt");
        kdTree.loadFromFile(filename);

        // x=4.0 sur [160,200] => doit retourner les points ayant x=4
        List<KdTreeNode> results = kdTree.rangeSearch(new double[]{4.0, 160.0}, new double[]{4.0, 200.0});
        assertEquals(2, results.size());
        for (KdTreeNode node : results) {
            assertEquals(4.0, node.getPoint()[0]);
        }
    }

    /**
     * Teste l'execution de requetes SQL sur un fichier avec 3 criteres (fichier 2.txt).
     */
    @Test
    public void testExecuteQueryWithThreeCriteria() throws IOException, InvalidCriteriaException {
        kdTree = new KdTree(2);
        String filename = getTestFilePath("2.txt");
        kdTree.loadFromFile(filename);

        // "SELECT name FROM P WHERE height in [170,175]"
        List<Map<String, String>> results =
                kdTree.executeQuery("SELECT name FROM P WHERE height in [170,175]");
        assertEquals(4, results.size());

        List<String> expectedNames = Arrays.asList("Frank", "Jack", "Bob", "Kate");
        for (Map<String, String> result : results) {
            assertTrue(expectedNames.contains(result.get("name")));
        }
    }

    /**
     * Teste la recherche par intervalle (range search) avec des donnees du fichier 2.txt.
     */
    @Test
    public void testRangeSearchWithFile2() throws IOException, InvalidCriteriaException {
        kdTree = new KdTree(2);
        String filename = getTestFilePath("2.txt");
        kdTree.loadFromFile(filename);

        List<KdTreeNode> results =
                kdTree.rangeSearch(new double[]{2.0, 160.0}, new double[]{2.0, 170.0});
        assertEquals(2, results.size());

        for (KdTreeNode node : results) {
            assertEquals(2.0, node.getPoint()[0]);
            assertTrue(node.getPoint()[1] >= 160.0 && node.getPoint()[1] <= 170.0);
        }
    }

    /**
     * Teste la recherche par intervalle avec des valeurs hors bornes (aucun resultat attendu).
     */
    @Test
    public void testRangeSearchOutOfBounds() throws IOException, InvalidCriteriaException {
        String filename = getTestFilePath("1.txt");
        kdTree.loadFromFile(filename);

        // En dessous des bornes
        List<KdTreeNode> results = kdTree.rangeSearch(
                new double[]{-100.0, -100.0}, new double[]{0.0, 0.0});
        assertEquals(0, results.size());

        // Au-dessus des bornes
        results = kdTree.rangeSearch(
                new double[]{100.0, 200.0}, new double[]{200.0, 300.0});
        assertEquals(0, results.size());
    }

    /**
     * Teste la recherche par intervalle pour deux plages qui se chevauchent.
     * On compare ensuite avec une plage globale englobant les deux.
     */
    @Test
    public void testRangeSearchOverlappingRanges() throws IOException, InvalidCriteriaException {
        String filename = getTestFilePath("1.txt");
        kdTree.loadFromFile(filename);

        List<KdTreeNode> results1 = kdTree.rangeSearch(
                new double[]{1.0, 160.0}, new double[]{2.0, 180.0});
        List<KdTreeNode> results2 = kdTree.rangeSearch(
                new double[]{2.0, 170.0}, new double[]{4.0, 190.0});

        Set<KdTreeNode> combinedResults = new HashSet<>();
        combinedResults.addAll(results1);
        combinedResults.addAll(results2);

        // Plage globale [1.0,160.0] - [4.0,190.0]
        List<KdTreeNode> combinedSearch = kdTree.rangeSearch(
                new double[]{1.0, 160.0}, new double[]{4.0, 190.0});

        assertEquals(combinedResults.size(), combinedSearch.size());
    }

    /**
     * Teste l'execution de requetes SQL avec les operateurs >= et <=.
     */
    @Test
    public void testExecuteQueryWithOperators() throws IOException, InvalidCriteriaException {
        String filename = getTestFilePath("1.txt");
        kdTree.loadFromFile(filename);

        // Requete avec >=
        List<Map<String, String>> results =
                kdTree.executeQuery("SELECT department FROM P WHERE height >= 180");
        assertEquals(2, results.size());

        Set<String> departments = new HashSet<>();
        for (Map<String, String> result : results) {
            departments.add(result.get("department"));
        }
        assertTrue(departments.contains("biologie"));
        assertTrue(departments.contains("informatique"));

        // Requete avec <=
        results = kdTree.executeQuery("SELECT department FROM P WHERE height <= 165");
        assertEquals(1, results.size());
        assertEquals("chimie", results.get(0).get("department"));
    }

    /**
     * Teste qu'une insertion de points en double (memes coordonnees) leve une exception,
     * car les doublons sont interdits.
     *
     * @throws InvalidCriteriaException
     *         si les criteres supplementaires sont invalides.
     */
    @Test
    public void testInsertDuplicateValues() throws InvalidCriteriaException {
        kdTree.criteriaNames = new String[]{"year", "height", "department"};

        // Insertion d'un point
        kdTree.addPointAndRebuild(new double[]{2.0, 170.0}, new String[]{"informatique"});

        // Tentative d'insertion d'un point aux memes coordonnees
        assertThrows(IllegalArgumentException.class, () -> {
            kdTree.addPointAndRebuild(new double[]{2.0, 170.0}, new String[]{"mathematique"});
        });

        // Verification qu'il n'y a qu'un seul point dans l'arbre
        List<KdTreeNode> results =
                kdTree.rangeSearch(new double[]{2.0, 170.0}, new double[]{2.0, 170.0});
        assertEquals(1, results.size());
    }

    /**
     * Teste l'execution de requetes SQL avec des criteres non numeriques (operateur "=").
     *
     * @throws IOException
     *         si fichier introuvable.
     * @throws InvalidCriteriaException
     *         si probleme de format.
     */
    @Test
    public void testExecuteQueryWithValidCriteria() throws IOException, InvalidCriteriaException {
        String filename = getTestFilePath("1.txt");
        kdTree.loadFromFile(filename);

        // year=1 AND height <=170 => (year=1, height <=170)
        List<Map<String, String>> results =
                kdTree.executeQuery("SELECT department FROM P WHERE year = 1 AND height <= 170");
        assertEquals(1, results.size());
        assertEquals("chimie", results.get(0).get("department"));
    }

    /**
     * Teste la levee d'exception lorsque le nombre de criteres supplementaires
     * ne correspond pas a {@link KdTree#criteriaNames}.
     */
    @Test
    public void testAddPointWithInvalidCriteria() {
        kdTree.criteriaNames = new String[]{"year", "height", "department"};
        assertThrows(InvalidCriteriaException.class, () -> {
            // Pas assez de criteres texte
            kdTree.addPointAndRebuild(new double[]{3.0, 182.0}, new String[]{});
        });
    }

    /**
     * Teste la construction de l'arbre avec un grand nombre de points (1000).
     * Verifie que l'arbre contient bien tous les points et que min < max sur la dimension 0.
     *
     * @throws InvalidCriteriaException
     *         si un probleme de criteres survient lors des insertions.
     */
    @Test
    public void testBuildTreeWithLargeDataset() throws InvalidCriteriaException {
        kdTree.criteriaNames = new String[]{"x", "y", "label"};
        Random rand = new Random();

        int numPoints = 1000;
        for (int i = 0; i < numPoints; i++) {
            double x = rand.nextDouble() * 1000;
            double y = rand.nextDouble() * 1000;
            kdTree.addPointAndRebuild(new double[]{x, y}, new String[]{"point" + i});
        }

        assertEquals(numPoints, kdTree.nodeList.size());

        double[] minX = kdTree.findMin(0);
        double[] maxX = kdTree.findMax(0);
        assertTrue(minX[0] <= maxX[0]);
    }

    /**
     * Teste la methode {@link KdTree#printTree()} pour verifier qu'elle ne lance pas d'exception
     * et produit une sortie non vide (contenu ASCII).
     *
     * @throws IOException
     *         si le fichier n'existe pas.
     * @throws InvalidCriteriaException
     *         si format invalide.
     */
    @Test
    public void testPrintTree() throws IOException, InvalidCriteriaException {
        String filename = getTestFilePath("1.txt");
        kdTree.loadFromFile(filename);

        PrintStream originalOut = System.out;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bos));

        kdTree.printTree();

        System.setOut(originalOut);

        String output = bos.toString();
        assertFalse(output.isEmpty());
    }

    /**
     * Teste la recherche par intervalle avec des bornes egales (low == high) sur x et y,
     * pour reperer un point exact.
     *
     * @throws IOException
     *         si fichier introuvable.
     * @throws InvalidCriteriaException
     *         si format invalide.
     */
    @Test
    public void testRangeSearchWithEqualBounds() throws IOException, InvalidCriteriaException {
        String filename = getTestFilePath("1.txt");
        kdTree.loadFromFile(filename);

        List<KdTreeNode> results =
                kdTree.rangeSearch(new double[]{2.0, 167.45}, new double[]{2.0, 167.45});
        assertEquals(1, results.size());
        assertArrayEquals(new double[]{2.0, 167.45}, results.get(0).getPoint());
    }

    /**
     * Teste l'execution d'une requete SQL avec un operateur non supporte ("<>").
     *
     * @throws IOException
     *         si probleme de fichier.
     * @throws InvalidCriteriaException
     *         si fichier mal forme.
     */
    @Test
    public void testExecuteQueryWithUnsupportedOperator() throws IOException, InvalidCriteriaException {
        String filename = getTestFilePath("1.txt");
        kdTree.loadFromFile(filename);

        assertThrows(IllegalArgumentException.class, () -> {
            kdTree.executeQuery("SELECT department FROM P WHERE height <> 170");
        });
    }

    /**
     * Teste la recherche par intervalle avec des valeurs nulles.
     * On s'attend a une {@link NullPointerException}.
     */
    @Test
    public void testRangeSearchWithNullValues() {
        assertThrows(NullPointerException.class, () -> {
            kdTree.rangeSearch(null, null);
        });
    }

    /**
     * Teste le chargement d'un fichier au format invalide (ex. critere manquant).
     */
    @Test
    public void testLoadFromFileWithInvalidFormat() {
        String filename = getTestFilePath("invalid.txt");
        assertThrows(IOException.class, () -> {
            kdTree.loadFromFile(filename);
        });
    }

    /**
     * Teste la sauvegarde dans un fichier sans permission d'ecriture (ex. repertoire protege).
     */
    @Test
    public void testSaveToFileWithoutWritePermission() throws IOException, InvalidCriteriaException {
        String filename = getTestFilePath("1.txt");
        kdTree.loadFromFile(filename);

        // Tentative de sauvegarde dans "/root/outputData.txt" qui, sur certains systemes,
        // devrait lever une IOException (pas de droit d'ecriture).
        String outputFilename = "/root/outputData.txt";
        assertThrows(IOException.class, () -> {
            kdTree.saveToFile(outputFilename);
        });
    }

    /**
     * Teste l'appel a {@link KdTree#printTree()} lorsque l'arbre n'est pas construit,
     * devant lever une {@link IllegalStateException}.
     */
    @Test
    public void testPrintTreeWhenTreeNotBuilt() {
        KdTree kdTree = new KdTree(2);
        Exception exception = assertThrows(IllegalStateException.class, kdTree::printTree);
        assertEquals("Impossible d'afficher l'arbre : l'arbre n'a pas ete construit car l'echantillon est vide.",
                exception.getMessage());
    }

    /**
     * Teste la sauvegarde d'un arbre vide dans un fichier,
     * attendant une {@link IllegalStateException}.
     */
    @Test
    public void testSaveSampleWithEmptyTree() {
        KdTree kdTree = new KdTree(2);
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            kdTree.saveToFile("test.txt");
        });
        assertEquals("Impossible de sauvegarder : l'echantillon est vide.", exception.getMessage());
    }
}

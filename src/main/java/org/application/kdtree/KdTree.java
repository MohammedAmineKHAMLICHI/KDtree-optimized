package org.application.kdtree;

import org.application.manager.InvalidCriteriaException;
import org.application.utils.HeapSort;

import java.io.*;
import java.util.*;

/**
 * <p>
 * La classe {@code KdTree} implémente un arbre k-dimensionnel (Kd-Tree) permettant de gérer un ensemble
 * de points dans un espace k-dimensionnel (k ≥ 2). Les deux premières dimensions sont numériques et
 * utilisées pour la structure même de l'arbre et les recherches (min, max, range search). Les autres
 * dimensions (le cas échéant) sont stockées comme critères supplémentaires (typés {@code String}).
 * </p>
 *
 * <p>
 * <strong>Auteur :</strong> KHAMLICHI Mohammed Amine
 * </p>
 *
 * <h3>Fonctionnalités principales :</h3>
 * <ul>
 *   <li>Construction d'un Kd-Tree à partir d'un échantillon de points</li>
 *   <li>Insertion (avec reconstruction) et interdiction de doublons</li>
 *   <li>Recherche de min/max sur les deux premières dimensions</li>
 *   <li>Recherche par intervalle (range search) sur les deux premières dimensions</li>
 *   <li>Exécution de requêtes SQL simples (critères numériques sur les deux premières dimensions,
 *       opérateur "=" sur les critères texte)</li>
 *   <li>Chargement/sauvegarde de l'échantillon depuis/vers un fichier texte</li>
 * </ul>
 */
public class KdTree {

    ///////////////////////////////////////////////////////////////////////////////////////
    // ATTRIBUTS
    ///////////////////////////////////////////////////////////////////////////////////////

    /**
     * Racine de l'arbre (pouvant être un nœud interne ou une feuille).
     */
    private KdTreeNode root;

    /**
     * Nombre de dimensions gérées par l'arbre pour la partie numérique.
     */
    public final int k;

    /**
     * Tableau des noms de critères. Les k premières cases correspondent aux dimensions
     * numériques (ex. x, y), et les éventuelles suivantes sont des critères supplémentaires.
     */
    public String[] criteriaNames;

    /**
     * Liste de tous les nœuds feuilles de l'arbre. Chaque feuille contient un point.
     */
    public List<KdTreeNode> nodeList;

    /**
     * Liste des points constituant l'échantillon. Chaque entrée associe :
     * <ul>
     *     <li>Un tableau double[] (pour les k dimensions numériques)</li>
     *     <li>Un tableau String[] (pour les critères supplémentaires)</li>
     * </ul>
     */
    private List<Map.Entry<double[], String[]>> samplePoints;

    ///////////////////////////////////////////////////////////////////////////////////////
    // CONSTRUCTEUR
    ///////////////////////////////////////////////////////////////////////////////////////

    /**
     * Construit un arbre Kd-Tree vide, avec un nombre de dimensions spécifié.
     *
     * @param dimensions
     *         Le nombre de dimensions k pour les critères numériques
     *         (ex. 2 pour un Kd-Tree 2D).
     */
    public KdTree(int dimensions) {
        this.k = dimensions;
        this.root = null;
        this.criteriaNames = new String[dimensions];
        this.nodeList = new ArrayList<>();
        this.samplePoints = new ArrayList<>();
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // MÉTHODES DE BASE
    ///////////////////////////////////////////////////////////////////////////////////////

    /**
     * Vérifie si l'arbre est vide, c'est-à-dire si {@link #root} est nul ou si {@link #nodeList} est vide.
     *
     * @return {@code true} si l'arbre ne contient aucun point, sinon {@code false}.
     */
    public boolean isEmpty() {
        return root == null || nodeList == null || nodeList.isEmpty();
    }

    /**
     * Génère la clé composite (primary, secondary) à partir d'un tableau de coordonnées,
     * en fonction de l'axe {@code axis}.
     * <ul>
     *     <li>Si {@code axis == 0}, on place {@code (x, y)} en primary, et {@code (y, x)} en secondary.</li>
     *     <li>Si {@code axis != 0}, on place {@code (y, x)} en primary, et {@code (x, y)} en secondary.</li>
     * </ul>
     *
     * @param point
     *         Tableau double[] représentant les coordonnées du point (ex. [x, y]).
     * @param axis
     *         L'axe de tri (0 ou 1 pour un Kd-Tree 2D).
     * @return Un {@link CompositePoint} encapsulant la logique de comparaison.
     */
    public static CompositePoint compositeKey(double[] point, int axis) {
        double[] primary, secondary;
        if (axis == 0) {
            primary = new double[]{point[0], point[1]};
            secondary = new double[]{point[1], point[0]};
        } else {
            primary = new double[]{point[1], point[0]};
            secondary = new double[]{point[0], point[1]};
        }
        return new CompositePoint(primary, secondary);
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // CONSTRUCTION DE L'ARBRE
    ///////////////////////////////////////////////////////////////////////////////////////

    /**
     * Construit récursivement le Kd-Tree à partir de deux listes de points, l'une triée selon l'axe 0
     * et l'autre triée selon l'axe 1. On choisit la liste de tri en fonction du niveau ({@code depth})
     * pour trouver la médiane, puis on partitionne à gauche et à droite.
     *
     * @param pointsSortedX
     *         Liste de points triée selon la dimension 0 (souvent x).
     * @param pointsSortedY
     *         Liste de points triée selon la dimension 1 (souvent y).
     * @param depth
     *         Profondeur courante dans l'arbre (commence généralement à 0).
     * @param leafSize
     *         Nombre maximum de points par feuille (ici, on utilise 1).
     * @return Le nœud racine (de type {@link KdTreeNode}) du sous-arbre construit.
     */
    public KdTreeNode buildTree(List<Map.Entry<double[], String[]>> pointsSortedX,
                                List<Map.Entry<double[], String[]>> pointsSortedY,
                                int depth, int leafSize) {
        if (pointsSortedX.isEmpty()) return null;

        // Cas de base : 1 seul point => feuille directe
        if (pointsSortedX.size() == 1) {
            Map.Entry<double[], String[]> entry = pointsSortedX.get(0);
            KdTreeNode leaf = new KdTreeNode(entry.getKey(), entry.getValue());
            leaf.setAxis(depth % k);
            leaf.setSplit(compositeKey(entry.getKey(), leaf.getAxis()));
            return leaf;
        }

        int axis = depth % k;
        // Sélection de la liste principale selon l'axe (X ou Y).
        List<Map.Entry<double[], String[]>> sorted = (axis == 0) ? pointsSortedX : pointsSortedY;

        // Récupération de la médiane (index "medianIndex")
        int medianIndex = sorted.size() / 2;
        Map.Entry<double[], String[]> medianEntry = sorted.get(medianIndex);
        double[] medianPoint = medianEntry.getKey();
        CompositePoint splitValue = compositeKey(medianPoint, axis);

        // Partitionnement gauche/droite
        List<Map.Entry<double[], String[]>> leftSortedX = partitionPoints(pointsSortedX, axis, splitValue, true);
        List<Map.Entry<double[], String[]>> rightSortedX = partitionPoints(pointsSortedX, axis, splitValue, false);
        List<Map.Entry<double[], String[]>> leftSortedY = partitionPoints(pointsSortedY, axis, splitValue, true);
        List<Map.Entry<double[], String[]>> rightSortedY = partitionPoints(pointsSortedY, axis, splitValue, false);

        // Appels récursifs
        KdTreeNode leftChild = buildTree(leftSortedX, leftSortedY, depth + 1, leafSize);
        KdTreeNode rightChild = buildTree(rightSortedX, rightSortedY, depth + 1, leafSize);

        // Création du nœud interne (split)
        KdTreeNode node = new KdTreeNode(null, null);
        node.setAxis(axis);
        node.setSplit(splitValue);
        node.setLeft(leftChild);
        node.setRight(rightChild);

        return node;
    }

    /**
     * Partitionne une liste de points en fonction de la valeur de séparation {@code splitValue}
     * (comparaison sur l'axe {@code axis}).
     *
     * @param points
     *         Liste de points à partitionner.
     * @param axis
     *         Dimension considérée (0 ou 1 en 2D).
     * @param splitValue
     *         Valeur de séparation (clé composite).
     * @param lower
     *         Si {@code true}, on collecte les points dont la clé est < à {@code splitValue}.
     *         Sinon, on collecte ceux dont la clé est ≥.
     * @return La sous-liste des points correspondant au critère.
     */
    private List<Map.Entry<double[], String[]>> partitionPoints(List<Map.Entry<double[], String[]>> points,
                                                                int axis, CompositePoint splitValue,
                                                                boolean lower) {
        List<Map.Entry<double[], String[]>> partition = new ArrayList<>();
        for (Map.Entry<double[], String[]> entry : points) {
            CompositePoint cp = compositeKey(entry.getKey(), axis);
            if ((lower && cp.compareTo(splitValue) < 0)
                    || (!lower && cp.compareTo(splitValue) >= 0)) {
                partition.add(entry);
            }
        }
        return partition;
    }

    /**
     * Ajoute un nouveau point (coordonnées numériques + critères supplémentaires)
     * à l'échantillon, puis reconstruit l'arbre pour prendre en compte ce nouveau point.
     * <p>
     * Les doublons sont interdits : si un point a déjà les mêmes coordonnées (x,y),
     * on lève une exception.
     *
     * @param point
     *         Tableau double[] représentant la partie numérique (souvent [x, y]).
     * @param extraCriteria
     *         Tableau String[] pour les critères supplémentaires (ex. nom, département...).
     * @throws InvalidCriteriaException
     *         Si le nombre de critères supplémentaires ne correspond pas (ex. mismatch de longueur).
     * @throws IllegalArgumentException
     *         Si un doublon de coordonnées est détecté.
     */
    public void addPointAndRebuild(double[] point, String[] extraCriteria) throws InvalidCriteriaException {
        if (extraCriteria.length != criteriaNames.length - 2) {
            throw new InvalidCriteriaException("Le nombre de critères supplémentaires fourni est incorrect.");
        }
        // Vérification de doublon (x,y déjà existant).
        for (Map.Entry<double[], String[]> entry : samplePoints) {
            double[] existing = entry.getKey();
            if (existing[0] == point[0] && existing[1] == point[1]) {
                throw new IllegalArgumentException("Doublon non autorisé (coordonnées identiques).");
            }
        }
        samplePoints.add(new AbstractMap.SimpleEntry<>(point, extraCriteria));
        rebuildTree();
    }

    /**
     * Reconstruit l'arbre Kd-Tree à partir de la liste interne {@link #samplePoints}.
     * <p>
     * On trie l'échantillon selon l'axe 0 et l'axe 1 à l'aide de {@link HeapSort}, puis on appelle
     * {@link #buildTree(List, List, int, int)}.
     */
    private void rebuildTree() {
        // Copie pour tri
        List<Map.Entry<double[], String[]>> pointsSortedX = new ArrayList<>(samplePoints);
        List<Map.Entry<double[], String[]>> pointsSortedY = new ArrayList<>(samplePoints);

        // Tri selon x
        HeapSort.heapSort(pointsSortedX, (e1, e2) ->
                compositeKey(e1.getKey(), 0).compareTo(compositeKey(e2.getKey(), 0)));
        // Tri selon y
        HeapSort.heapSort(pointsSortedY, (e1, e2) ->
                compositeKey(e1.getKey(), 1).compareTo(compositeKey(e2.getKey(), 1)));

        // Construction de la racine
        root = buildTree(pointsSortedX, pointsSortedY, 0, 1);

        // Mise à jour de la liste des feuilles
        nodeList.clear();
        populateLeafNodes(root);
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // MÉTHODES POUR REMPLIR LA LISTE DES FEUILLES
    ///////////////////////////////////////////////////////////////////////////////////////

    /**
     * Parcourt l'arbre pour localiser les nœuds feuilles et les stocker dans {@link #nodeList}.
     *
     * @param node
     *         Nœud racine du sous-arbre à explorer.
     */
    private void populateLeafNodes(KdTreeNode node) {
        if (node == null) return;
        if (node.getPoint() != null) {
            // C'est une feuille, on l'ajoute à la liste
            nodeList.add(node);
        } else {
            // Parcours récursif
            populateLeafNodes(node.getLeft());
            populateLeafNodes(node.getRight());
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // MÉTHODES D'AFFICHAGE
    ///////////////////////////////////////////////////////////////////////////////////////

    /**
     * Affiche l'arbre Kd-Tree sous forme textuelle (type ASCII-art).
     *
     * @throws IllegalStateException
     *         Si l'arbre est vide (root == null).
     */
    public void printTree() {
        if (root == null) {
            throw new IllegalStateException("Impossible d'afficher l'arbre : l'arbre n'a pas été construit car l'échantillon est vide.");
        }
        List<String> lines = new ArrayList<>();
        buildTreeString(root, "", true, lines, true);
        for (String line : lines) {
            System.out.println(line);
        }
    }

    /**
     * Construit récursivement une représentation textuelle de l'arbre pour l'affichage.
     *
     * @param node
     *         Nœud courant.
     * @param prefix
     *         Préfixe (indentation).
     * @param isTail
     *         Indique si c'est le dernier enfant (affichage).
     * @param lines
     *         Liste accumulant les lignes à afficher.
     * @param isRoot
     *         Indique si le nœud en cours est la racine.
     */
    private void buildTreeString(KdTreeNode node, String prefix, boolean isTail,
                                 List<String> lines, boolean isRoot) {
        if (node == null) return;

        if (node.getRight() != null) {
            buildTreeString(node.getRight(), prefix + (isTail ? "│   " : "    "),
                    false, lines, false);
        }

        // Construction de la représentation du nœud
        String nodeInfo;
        if (node.getPoint() == null) {
            // Nœud interne => affiche "Split x = ..." ou "Split y = ..."
            nodeInfo = (node.getAxis() % 2 == 0)
                    ? "Split x = " + node.getSplit().getPrimary()[0]
                    : "Split y = " + node.getSplit().getPrimary()[0];
        } else {
            // Feuille => affiche les coordonnées + extraCriteria
            nodeInfo = Arrays.toString(node.getPoint())
                    + " " + Arrays.toString(node.getExtraCriteria());
        }

        // Ajout de la ligne
        lines.add(prefix + (isTail ? (isRoot ? "Root── " : "└── ") : "┌── ") + nodeInfo);

        if (node.getLeft() != null) {
            buildTreeString(node.getLeft(), prefix + (isTail ? "    " : "│   "),
                    true, lines, false);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // MÉTHODES DE RECHERCHE (MIN, MAX) - OPTIMISÉES
    ///////////////////////////////////////////////////////////////////////////////////////

    /**
     * Recherche optimisée du point dont la valeur est minimale sur la dimension {@code dim}.
     * <p>
     * Exploite l'axe de séparation pour élaguer les branches inutiles :
     * <ul>
     *   <li>Si l'axe du nœud courant est {@code dim}, on ne visite que le sous-arbre gauche, puis on compare avec le pivot.</li>
     *   <li>Sinon, on recherche sur les deux sous-arbres et on compare également avec le pivot.</li>
     * </ul>
     *
     * @param dim
     *         Dimension (0 pour x, 1 pour y en 2D).
     * @return Un tableau {@code double[]} (coordonnées du point min), ou {@code null} si l'arbre est vide.
     */
    public double[] findMin(int dim) {
        return findMinRecOptimized(root, dim);
    }

    /**
     * Méthode récursive optimisée pour trouver le minimum sur la dimension {@code dim} dans
     * le sous-arbre enraciné à {@code node}.
     * <p>
     *   - Si {@code node} est une feuille (nœud avec {@link KdTreeNode#getPoint()} non nul),
     *     on retourne directement {@code node.getPoint()}.<br>
     *   - Sinon, on récupère l'axe (x ou y) du nœud et on exploite la logique d'élagage.
     * </p>
     *
     * @param node
     *         Nœud courant (peut être interne ou feuille).
     * @param dim
     *         Dimension pour laquelle on recherche la valeur minimale.
     * @return Le point minimal (tableau {@code double[]}), ou {@code null} si le sous-arbre est vide.
     */
    private double[] findMinRecOptimized(KdTreeNode node, int dim) {
        if (node == null) return null;

        // Cas feuille : on renvoie le point directement
        if (node.getPoint() != null) {
            return node.getPoint();
        }

        int axis = node.getAxis();
        double[] pivot = getPivotCoords(node);

        if (axis == dim) {
            // On n'explore que le sous-arbre gauche + compare pivot
            double[] leftMin = findMinRecOptimized(node.getLeft(), dim);
            if (leftMin == null) {
                return pivot;
            }
            return (compositeKey(leftMin, dim).compareTo(compositeKey(pivot, dim)) < 0)
                    ? leftMin
                    : pivot;
        } else {
            // On compare dans les deux sous-arbres + pivot
            double[] leftMin  = findMinRecOptimized(node.getLeft(),  dim);
            double[] rightMin = findMinRecOptimized(node.getRight(), dim);

            double[] bestChild;
            if (leftMin == null && rightMin == null) {
                bestChild = null;
            } else if (leftMin == null) {
                bestChild = rightMin;
            } else if (rightMin == null) {
                bestChild = leftMin;
            } else {
                bestChild = (compositeKey(leftMin, dim).compareTo(compositeKey(rightMin, dim)) < 0)
                        ? leftMin
                        : rightMin;
            }
            if (bestChild == null) {
                return pivot;
            } else {
                return (compositeKey(bestChild, dim).compareTo(compositeKey(pivot, dim)) < 0)
                        ? bestChild
                        : pivot;
            }
        }
    }

    /**
     * Recherche optimisée du point dont la valeur est maximale sur la dimension {@code dim}.
     *
     * @param dim
     *         Dimension (0 pour x, 1 pour y).
     * @return Le point maximal sur l'axe {@code dim}, ou {@code null} si l'arbre est vide.
     */
    public double[] findMax(int dim) {
        return findMaxRecOptimized(root, dim);
    }

    /**
     * Méthode récursive optimisée pour trouver le maximum sur la dimension {@code dim}.
     * <p>
     *   - Si l'axe du nœud est {@code dim}, on n'explore que le sous-arbre droit + compare avec le pivot.<br>
     *   - Sinon, on compare le maximum obtenu en gauche, en droite, et le pivot.
     * </p>
     *
     * @param node
     *         Nœud courant (peut être interne ou feuille).
     * @param dim
     *         Dimension pour laquelle on recherche la valeur maximale.
     * @return Un tableau {@code double[]} (coord. du point max) ou {@code null} si vide.
     */
    private double[] findMaxRecOptimized(KdTreeNode node, int dim) {
        if (node == null) return null;

        // Cas feuille
        if (node.getPoint() != null) {
            return node.getPoint();
        }

        int axis = node.getAxis();
        double[] pivot = getPivotCoords(node);

        if (axis == dim) {
            // On n'explore que la branche droite
            double[] rightMax = findMaxRecOptimized(node.getRight(), dim);
            if (rightMax == null) {
                return pivot;
            }
            return (compositeKey(rightMax, dim).compareTo(compositeKey(pivot, dim)) > 0)
                    ? rightMax
                    : pivot;
        } else {
            // Compare sur les deux sous-arbres + pivot
            double[] leftMax  = findMaxRecOptimized(node.getLeft(),  dim);
            double[] rightMax = findMaxRecOptimized(node.getRight(), dim);

            double[] bestChild;
            if (leftMax == null && rightMax == null) {
                bestChild = null;
            } else if (leftMax == null) {
                bestChild = rightMax;
            } else if (rightMax == null) {
                bestChild = leftMax;
            } else {
                bestChild = (compositeKey(leftMax, dim).compareTo(compositeKey(rightMax, dim)) > 0)
                        ? leftMax
                        : rightMax;
            }
            if (bestChild == null) {
                return pivot;
            } else {
                return (compositeKey(bestChild, dim).compareTo(compositeKey(pivot, dim)) > 0)
                        ? bestChild
                        : pivot;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // MÉTHODES PRIVÉES UTILES
    ///////////////////////////////////////////////////////////////////////////////////////

    /**
     * Retourne les coordonnées (x,y) du pivot pour un nœud interne,
     * reconstruites depuis le champ {@link KdTreeNode#getSplit()}.
     * <p>
     * Rappel : si l'axe est 0, alors <em>primary = [x,y]</em> dans {@code CompositePoint}.
     * S'il est 1, alors <em>primary = [y,x]</em>.
     * On recompose donc toujours un tableau (x,y).
     *
     * @param node
     *         Nœud (interne) dont on souhaite extraire le pivot.
     * @return Un tableau {@code double[]} = (x,y).
     */
    private double[] getPivotCoords(KdTreeNode node) {
        if (node.getSplit() == null) {
            return null;
        }
        double[] p = node.getSplit().getPrimary();
        if (node.getAxis() == 0) {
            // axis=0 => primary=[x,y]
            return new double[]{ p[0], p[1] };
        } else {
            // axis=1 => primary=[y,x], on recompose (x,y)
            return new double[]{ p[1], p[0] };
        }
    }

    /**
     * Effectue une recherche par intervalle (range search) pour récupérer
     * les nœuds feuilles dont les coordonnées se situent entre {@code low} et {@code high}
     * sur chaque dimension numérique (0..k-1).
     *
     * @param low
     *         Tableau double[] des bornes inférieures pour chaque dimension.
     * @param high
     *         Tableau double[] des bornes supérieures pour chaque dimension.
     * @return Liste de nœuds (feuilles) satisfaisant la condition.
     * @throws NullPointerException
     *         Si {@code low} ou {@code high} est {@code null}.
     * @throws IllegalArgumentException
     *         Si la taille de {@code low} ou {@code high} n'est pas égale à {@code k}.
     */
    public List<KdTreeNode> rangeSearch(double[] low, double[] high) {
        if (low == null || high == null) {
            throw new NullPointerException("Les bornes ne peuvent pas être nulles.");
        }
        if (low.length != this.k || high.length != this.k) {
            throw new IllegalArgumentException("Les tableaux de bornes doivent avoir une longueur égale à " + this.k + ".");
        }

        CompositePoint[] compLow = new CompositePoint[k];
        CompositePoint[] compHigh = new CompositePoint[k];

        // Conversion en bornes "CompositePoint"
        for (int i = 0; i < k; i++) {
            compLow[i] = createCompositeBound(low[i], true);
            compHigh[i] = createCompositeBound(high[i], false);
        }

        return rangeSearchCompositeNodes(compLow, compHigh);
    }

    /**
     * Crée un {@link CompositePoint} borne pour une valeur {@code value},
     * gérant différemment la borne inférieure et la borne supérieure (lexicographiquement).
     *
     * @param value
     *         Valeur numérique de la borne.
     * @param isLower
     *         {@code true} si c'est la borne inférieure, {@code false} sinon.
     * @return Le {@link CompositePoint} correspondant à cette borne.
     */
    private CompositePoint createCompositeBound(double value, boolean isLower) {
        if (isLower) {
            return new CompositePoint(
                    new double[]{value, Double.NEGATIVE_INFINITY},
                    new double[]{Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY}
            );
        } else {
            return new CompositePoint(
                    new double[]{value, Double.POSITIVE_INFINITY},
                    new double[]{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY}
            );
        }
    }

    /**
     * Lance la recherche par intervalle (Range Search) en manipulant directement
     * des bornes "CompositePoint" pour chaque dimension.
     *
     * @param low
     *         Bornes inférieures sous forme de {@link CompositePoint}.
     * @param high
     *         Bornes supérieures sous forme de {@link CompositePoint}.
     * @return Liste des nœuds feuilles se trouvant dans les intervalles spécifiés.
     */
    private List<KdTreeNode> rangeSearchCompositeNodes(CompositePoint[] low,
                                                       CompositePoint[] high) {
        List<KdTreeNode> result = new ArrayList<>();

        // Bornes globales couvrant l'espace complet (pour init).
        CompositePoint[] minBound = new CompositePoint[]{
                new CompositePoint(new double[]{Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY},
                        new double[]{Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY}),
                new CompositePoint(new double[]{Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY},
                        new double[]{Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY})
        };

        CompositePoint[] maxBound = new CompositePoint[]{
                new CompositePoint(new double[]{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY},
                        new double[]{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY}),
                new CompositePoint(new double[]{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY},
                        new double[]{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY})
        };

        // Recherche récursive
        searchKDTreeCompositeNodes(root, minBound, maxBound, low, high, result, 0);
        return result;
    }

    /**
     * Parcourt récursivement le Kd-Tree pour trouver toutes les feuilles dont les
     * coordonnées sont comprises dans [low, high]. Les régions des nœuds
     * sont comparées à l'intervalle pour déterminer si on doit descendre complètement
     * ou partiellement dans un sous-arbre.
     *
     * @param node
     *         Nœud courant (racine du sous-arbre).
     * @param minBound
     *         Borne inférieure de la région associée à {@code node}.
     * @param maxBound
     *         Borne supérieure de la région associée à {@code node}.
     * @param low
     *         Bornes inférieures de la requête.
     * @param high
     *         Bornes supérieures de la requête.
     * @param result
     *         Liste accumulant les nœuds feuilles trouvés.
     * @param depth
     *         Profondeur courante (pour déterminer l'axe).
     */
    private void searchKDTreeCompositeNodes(KdTreeNode node,
                                            CompositePoint[] minBound,
                                            CompositePoint[] maxBound,
                                            CompositePoint[] low,
                                            CompositePoint[] high,
                                            List<KdTreeNode> result,
                                            int depth) {
        if (node == null) return;
        final int dims = low.length;

        // Feuille => vérification simple de l'appartenance à la plage
        if (node.getLeft() == null && node.getRight() == null) {
            boolean inRange = true;
            for (int i = 0; i < dims; i++) {
                CompositePoint ck = compositeKey(node.getPoint(), i);
                if (ck.compareTo(low[i]) < 0 || ck.compareTo(high[i]) > 0) {
                    inRange = false;
                    break;
                }
            }
            if (inRange) {
                result.add(node);
            }
            return;
        }

        int axis = node.getAxis();
        // Construction des bornes pour les sous-arbres gauche / droit
        CompositePoint[] leftMinBound = minBound.clone();
        CompositePoint[] leftMaxBound = maxBound.clone();
        leftMaxBound[axis] = node.getSplit();

        CompositePoint[] rightMinBound = minBound.clone();
        CompositePoint[] rightMaxBound = maxBound.clone();
        rightMinBound[axis] = node.getSplit();

        // Sous-arbre gauche
        if (node.getLeft() != null) {
            if (regionFullyContained(leftMinBound, leftMaxBound, low, high)) {
                reportSubtreeNodes(node.getLeft(), result);
            } else if (regionIntersects(leftMinBound, leftMaxBound, low, high)) {
                searchKDTreeCompositeNodes(node.getLeft(), leftMinBound, leftMaxBound, low, high,
                        result, depth + 1);
            }
        }

        // Sous-arbre droit
        if (node.getRight() != null) {
            if (regionFullyContained(rightMinBound, rightMaxBound, low, high)) {
                reportSubtreeNodes(node.getRight(), result);
            } else if (regionIntersects(rightMinBound, rightMaxBound, low, high)) {
                searchKDTreeCompositeNodes(node.getRight(), rightMinBound, rightMaxBound, low, high,
                        result, depth + 1);
            }
        }
    }

    /**
     * Ajoute récursivement tous les nœuds (feuilles et internes) d'un sous-arbre complet
     * dans la liste {@code result} sans autre test.
     *
     * @param node
     *         Racine du sous-arbre à explorer.
     * @param result
     *         Liste de nœuds accumulés.
     */
    private void reportSubtreeNodes(KdTreeNode node, List<KdTreeNode> result) {
        if (node == null) return;
        result.add(node);
        reportSubtreeNodes(node.getLeft(), result);
        reportSubtreeNodes(node.getRight(), result);
    }

    /**
     * Vérifie si la région [minBound, maxBound] est entièrement contenue dans [low, high].
     *
     * @param minBound
     *         Bornes min de la région.
     * @param maxBound
     *         Bornes max de la région.
     * @param low
     *         Bornes min de la requête.
     * @param high
     *         Bornes max de la requête.
     * @return {@code true} si la région est totalement contenue, {@code false} sinon.
     */
    private boolean regionFullyContained(CompositePoint[] minBound,
                                         CompositePoint[] maxBound,
                                         CompositePoint[] low,
                                         CompositePoint[] high) {
        for (int i = 0; i < low.length; i++) {
            if (low[i].compareTo(minBound[i]) > 0
                    || maxBound[i].compareTo(high[i]) > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Vérifie si la région [minBound, maxBound] intersecte [low, high].
     *
     * @param minBound
     *         Bornes inf de la région.
     * @param maxBound
     *         Bornes sup de la région.
     * @param low
     *         Bornes inf de la requête.
     * @param high
     *         Bornes sup de la requête.
     * @return {@code true} si intersection, sinon {@code false}.
     */
    private boolean regionIntersects(CompositePoint[] minBound,
                                     CompositePoint[] maxBound,
                                     CompositePoint[] low,
                                     CompositePoint[] high) {
        for (int i = 0; i < low.length; i++) {
            if (maxBound[i].compareTo(low[i]) < 0
                    || minBound[i].compareTo(high[i]) > 0) {
                return false;
            }
        }
        return true;
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // GESTION DES FICHIERS
    ///////////////////////////////////////////////////////////////////////////////////////

    /**
     * Sauvegarde l'échantillon (listes de critères et points) dans un fichier texte,
     * situé par défaut dans "src/main/resources/{filename}".
     *
     * @param filename
     *         Nom du fichier à créer (ou écraser).
     * @throws IOException
     *         En cas d'erreur d'écriture (dossier inaccessible, etc.).
     */
    public void saveToFile(String filename) throws IOException {
        if (isEmpty()) {
            throw new IllegalStateException("Impossible de sauvegarder : l'échantillon est vide.");
        }
        String projectPath = new File("").getAbsolutePath();
        String resourceDirPath = projectPath + File.separator + "src" + File.separator
                + "main" + File.separator + "resources";
        String resourceFilePath = resourceDirPath + File.separator + filename;

        File resourceDir = new File(resourceDirPath);
        if (!resourceDir.exists() && !resourceDir.mkdirs()) {
            throw new IOException("Impossible de créer le répertoire des ressources.");
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(resourceFilePath))) {
            writeSample(bw);
        }
    }

    /**
     * Écrit le contenu de {@link #samplePoints} avec les noms de critères
     * dans un {@link BufferedWriter}. Format :
     * <ol>
     *     <li>M (nombre total de critères)</li>
     *     <li>M lignes pour les noms de critères</li>
     *     <li>N (nombre de points)</li>
     *     <li>N lignes : x y [critèreSup1 ... critèreSup(M-2)]</li>
     * </ol>
     *
     * @param bw
     *         {@link BufferedWriter} vers lequel écrire.
     * @throws IOException
     *         Si une erreur d'écriture survient.
     */
    private void writeSample(BufferedWriter bw) throws IOException {
        // 1) Nombre de critères
        bw.write(criteriaNames.length + "\n");
        // 2) Critères
        for (String criterion : criteriaNames) {
            bw.write(criterion + "\n");
        }
        // 3) Nombre de points
        bw.write(samplePoints.size() + "\n");
        // 4) Les données
        for (Map.Entry<double[], String[]> entry : samplePoints) {
            bw.write(entry.getKey()[0] + " " + entry.getKey()[1]);
            for (String extra : entry.getValue()) {
                bw.write(" " + extra);
            }
            bw.write("\n");
        }
    }

    /**
     * Charge un fichier texte (avec la même structure que {@link #writeSample(BufferedWriter)})
     * et reconstruit l'échantillon + l'arbre. Tente d'abord de le trouver via
     * {@code getClass().getResourceAsStream}, puis en local si échec.
     *
     * @param filename
     *         Nom du fichier ou chemin relatif/absolu.
     * @throws IOException
     *         Si le fichier est introuvable ou si un problème de lecture survient.
     * @throws InvalidCriteriaException
     *         Si le nombre de critères M est < 2 ou autre invalidité.
     */
    public void loadFromFile(String filename) throws IOException, InvalidCriteriaException {
        InputStream inputStream = getClass().getResourceAsStream("/" + filename);
        if (inputStream == null) {
            File file = new File(filename);
            if (file.exists()) {
                inputStream = new FileInputStream(file);
            } else {
                throw new FileNotFoundException("Le fichier n'a pas été trouvé : " + filename);
            }
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            int lineNumber = 1;
            String line = br.readLine();
            if (line == null) {
                throw new IOException("Le fichier est vide ou le nombre de critères est manquant (ligne " + lineNumber + ").");
            }
            int M;
            try {
                M = Integer.parseInt(line.trim());
            } catch (NumberFormatException e) {
                throw new IOException("Le nombre de critères (M) n'est pas valide (ligne " + lineNumber + ").", e);
            }
            if (M < 2) {
                throw new IOException("Le nombre de critères (M) doit être >= 2, lu : " + M);
            }

            criteriaNames = new String[M];
            for (int i = 0; i < M; i++) {
                lineNumber++;
                line = br.readLine();
                if (line == null) {
                    throw new IOException("Nom du critère manquant (ligne " + lineNumber + ").");
                }
                criteriaNames[i] = line.trim();
            }

            lineNumber++;
            line = br.readLine();
            if (line == null) {
                throw new IOException("Le nombre de points (N) est manquant (ligne " + lineNumber + ").");
            }
            int N;
            try {
                N = Integer.parseInt(line.trim());
            } catch (NumberFormatException e) {
                throw new IOException("Le nombre de points (N) n'est pas valide (ligne " + lineNumber + ").", e);
            }

            List<Map.Entry<double[], String[]>> points = new ArrayList<>();
            samplePoints.clear();
            for (int i = 0; i < N; i++) {
                lineNumber++;
                line = br.readLine();
                if (line == null) {
                    throw new IOException("Point manquant (ligne " + lineNumber + ").");
                }
                String[] tokens = line.trim().split("\\s+");
                if (tokens.length < M) {
                    throw new IOException("Le point à la ligne " + lineNumber + " ne contient pas assez de valeurs. Attendu : " + M + ".");
                }

                double[] point = new double[2];
                try {
                    point[0] = Double.parseDouble(tokens[0]);
                    point[1] = Double.parseDouble(tokens[1]);
                } catch (NumberFormatException e) {
                    throw new IOException("Valeur numérique invalide (ligne " + lineNumber + ").", e);
                }
                String[] extra = Arrays.copyOfRange(tokens, 2, tokens.length);

                Map.Entry<double[], String[]> entry = new AbstractMap.SimpleEntry<>(point, extra);
                points.add(entry);
                samplePoints.add(entry);
            }
            // Reconstruction de l'arbre
            rebuildTree();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // EXÉCUTION DE REQUÊTES SQL SIMPLES
    ///////////////////////////////////////////////////////////////////////////////////////

    /**
     * Exécute une requête SQL très basique (type "SELECT x FROM P WHERE x >= 5 AND ...")
     * sur les deux premières dimensions numériques et sur les critères supplémentaires en chaîne.
     * <p>
     * Opérateurs gérés pour la partie numérique : "=, >=, <=, in [x,y]".
     * Opérateur géré pour les critères texte : "=".
     *
     * @param query
     *         La requête SQL à exécuter (ex. "SELECT x,y FROM P WHERE x >= 5 AND nom = Alice").
     * @return Une liste de {@code Map<String, String>} représentant les lignes résultats.
     *         Chaque map associe "nomCritere" -> "valeur" pour toutes les colonnes sélectionnées.
     * @throws IllegalArgumentException
     *         Si la requête ou une condition est invalide (syntaxe inconnue).
     */
    public List<Map<String, String>> executeQuery(String query) {
        String[] parts = query.split(" ");
        if (parts.length < 4) {
            throw new IllegalArgumentException("Requête invalide (syntaxe trop courte).");
        }

        // Extraction des colonnes dans SELECT
        String[] selectColumns = parts[1].split(",");
        // Localisation du WHERE
        String whereClause = query.substring(query.indexOf("WHERE") + 6);
        String[] conditions = whereClause.split(" AND ");

        double[] lowNumeric = new double[k];
        double[] highNumeric = new double[k];
        Arrays.fill(lowNumeric, Double.NEGATIVE_INFINITY);
        Arrays.fill(highNumeric, Double.POSITIVE_INFINITY);

        // Map pour les conditions sur les critères texte
        Map<String, String> extraConditions = new HashMap<>();

        // Analyse de chaque condition
        for (String condition : conditions) {
            String[] conditionParts = condition.split(" ");
            if (conditionParts.length < 3) {
                throw new IllegalArgumentException("Condition invalide : " + condition);
            }
            String criteria = conditionParts[0];
            String operator = conditionParts[1];

            int index = -1;
            for (int i = 0; i < criteriaNames.length; i++) {
                if (criteriaNames[i].equals(criteria)) {
                    index = i;
                    break;
                }
            }
            if (index == -1) {
                throw new IllegalArgumentException("Critère inconnu : " + criteria);
            }

            // Si c'est l'une des k dimensions => comparaison numérique
            if (index < k) {
                if ("in".equals(operator)) {
                    // Format "in [val1,val2]"
                    String range = conditionParts[2].replace("[", "").replace("]", "");
                    String[] values = range.split(",");
                    double lowValue = Double.parseDouble(values[0]);
                    double highValue = Double.parseDouble(values[1]);
                    lowNumeric[index] = lowValue;
                    highNumeric[index] = highValue;
                } else {
                    double value = Double.parseDouble(conditionParts[2]);
                    switch (operator) {
                        case "=":
                            lowNumeric[index] = value;
                            highNumeric[index] = value;
                            break;
                        case ">=":
                            lowNumeric[index] = value;
                            break;
                        case "<=":
                            highNumeric[index] = value;
                            break;
                        default:
                            throw new IllegalArgumentException("Opérateur inconnu : " + operator);
                    }
                }
            } else {
                // Critère supplémentaire => opérateur "=" géré
                extraConditions.put(criteria + " " + operator, conditionParts[2]);
            }
        }

        // Conversion en CompositePoint pour lancer la recherche
        CompositePoint[] compLow = new CompositePoint[k];
        CompositePoint[] compHigh = new CompositePoint[k];
        for (int i = 0; i < k; i++) {
            compLow[i] = createCompositeBound(lowNumeric[i], true);
            compHigh[i] = createCompositeBound(highNumeric[i], false);
        }

        // Récupération des nœuds feuilles dans la plage
        List<KdTreeNode> rangeSearchResults = rangeSearchCompositeNodes(compLow, compHigh);

        // Vérification des conditions texte
        List<Map<String, String>> results = new ArrayList<>();
        for (KdTreeNode node : rangeSearchResults) {
            if (node.getPoint() == null) continue;

            boolean matches = true;
            for (Map.Entry<String, String> entry : extraConditions.entrySet()) {
                String[] keyParts = entry.getKey().split(" ");
                String extraCriteriaName = keyParts[0];
                String extraOperator = keyParts[1];
                String extraValue = entry.getValue();

                int extraIndex = -1;
                for (int i = 0; i < criteriaNames.length; i++) {
                    if (criteriaNames[i].equals(extraCriteriaName)) {
                        extraIndex = i - k; // Pour pointer dans node.getExtraCriteria()
                        break;
                    }
                }
                if (extraIndex < 0 || extraIndex >= node.getExtraCriteria().length) {
                    throw new IllegalArgumentException("Critère supplémentaire inconnu : " + extraCriteriaName);
                }
                // Vérifie l'opérateur "="
                String nodeExtraValue = node.getExtraCriteria()[extraIndex];
                if ("=".equals(extraOperator)) {
                    if (!nodeExtraValue.equals(extraValue)) {
                        matches = false;
                    }
                } else {
                    throw new IllegalArgumentException("Opérateur inconnu pour le critère supplémentaire : " + extraOperator);
                }
            }

            // Si toutes les conditions sont satisfaites => on collecte les colonnes demandées
            if (matches) {
                Map<String, String> result = new HashMap<>();
                for (String column : selectColumns) {
                    column = column.trim();
                    // Vérifie si c'est l'une des deux premières dimensions
                    if (column.equals(criteriaNames[0])) {
                        result.put(column, String.valueOf(node.getPoint()[0]));
                    } else if (column.equals(criteriaNames[1])) {
                        result.put(column, String.valueOf(node.getPoint()[1]));
                    } else {
                        // Recherche dans extraCriteria
                        for (int i = 0; i < node.getExtraCriteria().length; i++) {
                            if (column.equals(criteriaNames[i + k])) {
                                result.put(column, node.getExtraCriteria()[i]);
                            }
                        }
                    }
                }
                results.add(result);
            }
        }
        return results;
    }

}

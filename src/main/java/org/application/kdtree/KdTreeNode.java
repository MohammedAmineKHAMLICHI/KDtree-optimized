package org.application.kdtree;

import java.util.Arrays;

/**
 * <p>
 * La classe {@code KdTreeNode} représente un nœud dans un arbre Kd-Tree. Chaque nœud peut être :
 * <ul>
 *   <li>Une <strong>feuille</strong> : il contient alors un point dans l'espace k-dimensionnel
 *       ainsi que des critères supplémentaires.</li>
 *   <li>Un <strong>nœud interne</strong> : il n'a pas de point (i.e. {@code point == null}),
 *       mais il dispose d'une clé de séparation {@link CompositePoint} et d'un axe
 *       ({@code axis}) permettant de discriminer les sous-arbres gauche et droit.</li>
 * </ul>
 * </p>
 *
 * <p>
 * <strong>Auteur :</strong> KHAMLICHI Mohammed Amine
 * </p>
 *
 * <h3>Attributs principaux :</h3>
 * <ul>
 *   <li>{@code point}, {@code extraCriteria} : définis pour les feuilles (null pour un nœud interne).</li>
 *   <li>{@code axis} : axe de séparation (0, 1, etc.).</li>
 *   <li>{@code split} : clé composite de séparation utilisée par les nœuds internes.</li>
 *   <li>{@code left}, {@code right} : références sur les sous-arbres gauche et droit.</li>
 * </ul>
 */
public class KdTreeNode {

    /**
     * Coordonnées du point dans l'espace k-dimensionnel (non null pour une feuille).
     * <p>
     * Si ce nœud est un nœud interne, cette valeur reste {@code null}.
     * </p>
     */
    private double[] point;

    /**
     * Critères supplémentaires associés au point (non null pour une feuille).
     * <p>
     * Ces critères ne sont pas utilisés pour la construction de l'arbre,
     * mais stockés dans le nœud feuille.
     * </p>
     */
    private String[] extraCriteria;

    /**
     * Axe utilisé pour la séparation dans ce nœud.
     * <p>
     * Représente l'indice de la dimension (ex. 0 ou 1 dans un Kd-Tree 2D).
     * </p>
     */
    private int axis;

    /**
     * Clé composite de séparation (utilisée pour les nœuds internes).
     * <p>
     * Dans un Kd-Tree 2D, ce {@link CompositePoint} peut représenter (x,y) ou (y,x).
     * </p>
     */
    private CompositePoint split;

    /**
     * Sous-arbre gauche.
     */
    private KdTreeNode left;

    /**
     * Sous-arbre droit.
     */
    private KdTreeNode right;

    /**
     * Construit un nœud feuille pour le Kd-Tree avec le point et les critères supplémentaires fournis.
     * <p>
     * Si ce constructeur est appelé avec {@code point != null}, on considère qu'il s'agit d'une feuille.
     * Les nœuds internes n'utilisent pas ce constructeur pour définir un point.
     * </p>
     *
     * @param point
     *         Les coordonnées du point dans l'espace k-dimensionnel (ne doit pas être null
     *         pour une feuille).
     * @param extraCriteria
     *         Les critères supplémentaires associés à ce point (peuvent être vides mais pas null
     *         pour une feuille).
     */
    public KdTreeNode(double[] point, String[] extraCriteria) {
        this.point = point;
        this.extraCriteria = extraCriteria;
        this.left = null;
        this.right = null;
    }

    /**
     * Retourne les coordonnées du point associé à ce nœud.
     *
     * @return un tableau de double représentant le point, ou {@code null} si ce nœud est interne.
     */
    public double[] getPoint() {
        return point;
    }

    /**
     * Retourne les critères supplémentaires associés à ce nœud.
     *
     * @return un tableau de {@link String} contenant les critères supplémentaires,
     *         ou {@code null} si ce nœud est interne.
     */
    public String[] getExtraCriteria() {
        return extraCriteria;
    }

    /**
     * Retourne l'axe utilisé pour la séparation dans ce nœud.
     * <p>
     * Dans un Kd-Tree 2D, cet entier sera 0 ou 1 pour distinguer
     * l'axe X ou l'axe Y.
     * </p>
     *
     * @return l'axe de séparation.
     */
    public int getAxis() {
        return axis;
    }

    /**
     * Définit l'axe utilisé pour la séparation dans ce nœud.
     *
     * @param axis
     *         l'axe à définir (ex. 0 pour x, 1 pour y).
     */
    public void setAxis(int axis) {
        this.axis = axis;
    }

    /**
     * Retourne la clé composite de séparation pour ce nœud (pertinent pour les nœuds internes).
     *
     * @return un objet {@link CompositePoint} représentant la clé de séparation,
     *         ou {@code null} si ce nœud est une feuille.
     */
    public CompositePoint getSplit() {
        return split;
    }

    /**
     * Définit la clé composite de séparation pour ce nœud (utile uniquement pour les nœuds internes).
     *
     * @param split
     *         l'objet {@link CompositePoint} à définir comme clé de séparation.
     */
    public void setSplit(CompositePoint split) {
        this.split = split;
    }

    /**
     * Retourne le sous-arbre gauche de ce nœud.
     *
     * @return le nœud gauche, ou {@code null} si inexistant.
     */
    public KdTreeNode getLeft() {
        return left;
    }

    /**
     * Définit le sous-arbre gauche de ce nœud.
     *
     * @param left
     *         le nœud gauche à définir (peut être {@code null}).
     */
    public void setLeft(KdTreeNode left) {
        this.left = left;
    }

    /**
     * Retourne le sous-arbre droit de ce nœud.
     *
     * @return le nœud droit, ou {@code null} si inexistant.
     */
    public KdTreeNode getRight() {
        return right;
    }

    /**
     * Définit le sous-arbre droit de ce nœud.
     *
     * @param right
     *         le nœud droit à définir (peut être {@code null}).
     */
    public void setRight(KdTreeNode right) {
        this.right = right;
    }

    /**
     * Retourne une représentation textuelle de ce nœud.
     * <ul>
     *   <li>Pour une <strong>feuille</strong>, elle inclut le point et ses critères supplémentaires.</li>
     *   <li>Pour un <strong>nœud interne</strong>, elle affiche la clé de séparation et l'axe associé.</li>
     * </ul>
     *
     * @return une chaîne décrivant le nœud de manière lisible.
     */
    @Override
    public String toString() {
        if (point != null) {
            return "KdTreeNode{point=" + Arrays.toString(point) +
                    ", extraCriteria=" + Arrays.toString(extraCriteria) + "}";
        } else {
            return "KdTreeNode{split=" + split + ", axis=" + axis + "}";
        }
    }
}

package org.application.kdtree;

import java.util.Arrays;

/**
 * <p>
 * La classe {@code KdTreeNode} represente un nud dans un arbre Kd-Tree. Chaque nud peut etre :
 * <ul>
 *   <li>Une <strong>feuille</strong> : il contient alors un point dans l'espace k-dimensionnel
 *       ainsi que des criteres supplementaires.</li>
 *   <li>Un <strong>nud interne</strong> : il n'a pas de point (i.e. {@code point == null}),
 *       mais il dispose d'une cle de separation {@link CompositePoint} et d'un axe
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
 *   <li>{@code point}, {@code extraCriteria} : definis pour les feuilles (null pour un nud interne).</li>
 *   <li>{@code axis} : axe de separation (0, 1, etc.).</li>
 *   <li>{@code split} : cle composite de separation utilisee par les nuds internes.</li>
 *   <li>{@code left}, {@code right} : references sur les sous-arbres gauche et droit.</li>
 * </ul>
 */
public class KdTreeNode {

    /**
     * Coordonnees du point dans l'espace k-dimensionnel (non null pour une feuille).
     * <p>
     * Si ce nud est un nud interne, cette valeur reste {@code null}.
     * </p>
     */
    private double[] point;

    /**
     * Criteres supplementaires associes au point (non null pour une feuille).
     * <p>
     * Ces criteres ne sont pas utilises pour la construction de l'arbre,
     * mais stockes dans le nud feuille.
     * </p>
     */
    private String[] extraCriteria;

    /**
     * Axe utilise pour la separation dans ce nud.
     * <p>
     * Represente l'indice de la dimension (ex. 0 ou 1 dans un Kd-Tree 2D).
     * </p>
     */
    private int axis;

    /**
     * Cle composite de separation (utilisee pour les nuds internes).
     * <p>
     * Dans un Kd-Tree 2D, ce {@link CompositePoint} peut representer (x,y) ou (y,x).
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
     * Construit un nud feuille pour le Kd-Tree avec le point et les criteres supplementaires fournis.
     * <p>
     * Si ce constructeur est appele avec {@code point != null}, on considere qu'il s'agit d'une feuille.
     * Les nuds internes n'utilisent pas ce constructeur pour definir un point.
     * </p>
     *
     * @param point
     *         Les coordonnees du point dans l'espace k-dimensionnel (ne doit pas etre null
     *         pour une feuille).
     * @param extraCriteria
     *         Les criteres supplementaires associes a ce point (peuvent etre vides mais pas null
     *         pour une feuille).
     */
    public KdTreeNode(double[] point, String[] extraCriteria) {
        this.point = point;
        this.extraCriteria = extraCriteria;
        this.left = null;
        this.right = null;
    }

    /**
     * Retourne les coordonnees du point associe a ce nud.
     *
     * @return un tableau de double representant le point, ou {@code null} si ce nud est interne.
     */
    public double[] getPoint() {
        return point;
    }

    /**
     * Retourne les criteres supplementaires associes a ce nud.
     *
     * @return un tableau de {@link String} contenant les criteres supplementaires,
     *         ou {@code null} si ce nud est interne.
     */
    public String[] getExtraCriteria() {
        return extraCriteria;
    }

    /**
     * Retourne l'axe utilise pour la separation dans ce nud.
     * <p>
     * Dans un Kd-Tree 2D, cet entier sera 0 ou 1 pour distinguer
     * l'axe X ou l'axe Y.
     * </p>
     *
     * @return l'axe de separation.
     */
    public int getAxis() {
        return axis;
    }

    /**
     * Definit l'axe utilise pour la separation dans ce nud.
     *
     * @param axis
     *         l'axe a definir (ex. 0 pour x, 1 pour y).
     */
    public void setAxis(int axis) {
        this.axis = axis;
    }

    /**
     * Retourne la cle composite de separation pour ce nud (pertinent pour les nuds internes).
     *
     * @return un objet {@link CompositePoint} representant la cle de separation,
     *         ou {@code null} si ce nud est une feuille.
     */
    public CompositePoint getSplit() {
        return split;
    }

    /**
     * Definit la cle composite de separation pour ce nud (utile uniquement pour les nuds internes).
     *
     * @param split
     *         l'objet {@link CompositePoint} a definir comme cle de separation.
     */
    public void setSplit(CompositePoint split) {
        this.split = split;
    }

    /**
     * Retourne le sous-arbre gauche de ce nud.
     *
     * @return le nud gauche, ou {@code null} si inexistant.
     */
    public KdTreeNode getLeft() {
        return left;
    }

    /**
     * Definit le sous-arbre gauche de ce nud.
     *
     * @param left
     *         le nud gauche a definir (peut etre {@code null}).
     */
    public void setLeft(KdTreeNode left) {
        this.left = left;
    }

    /**
     * Retourne le sous-arbre droit de ce nud.
     *
     * @return le nud droit, ou {@code null} si inexistant.
     */
    public KdTreeNode getRight() {
        return right;
    }

    /**
     * Definit le sous-arbre droit de ce nud.
     *
     * @param right
     *         le nud droit a definir (peut etre {@code null}).
     */
    public void setRight(KdTreeNode right) {
        this.right = right;
    }

    /**
     * Retourne une representation textuelle de ce nud.
     * <ul>
     *   <li>Pour une <strong>feuille</strong>, elle inclut le point et ses criteres supplementaires.</li>
     *   <li>Pour un <strong>nud interne</strong>, elle affiche la cle de separation et l'axe associe.</li>
     * </ul>
     *
     * @return une chaine decrivant le nud de maniere lisible.
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

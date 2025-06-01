package org.application.kdtree;

import java.util.Arrays;

/**
 * <p>
 * La classe {@code CompositePoint} définit un point "composite" constitué de deux ensembles
 * de coordonnées : <strong>primary</strong> et <strong>secondary</strong>. Cette structure
 * est souvent utilisée pour gérer un ordre lexicographique personnalisé (dans un Kd-Tree, par exemple).
 * </p>
 *
 * <p>
 * <strong>Auteur :</strong> KHAMLICHI Mohammed Amine
 * </p>
 *
 * <h3>Caractéristiques principales :</h3>
 * <ul>
 *   <li>Les coordonnées <em>primary</em> sont comparées en premier.</li>
 *   <li>Si les coordonnées <em>primary</em> sont égales, les coordonnées <em>secondary</em> sont comparées.</li>
 *   <li>L'objet est immuable : des copies défensives sont faites à la construction.</li>
 * </ul>
 *
 * <p>
 * Cette classe implémente {@link Comparable}, permettant le tri et la comparaison
 * lexicographique de deux {@code CompositePoint}.
 * </p>
 */
public class CompositePoint implements Comparable<CompositePoint> {

    /**
     * Tableau des coordonnées principales (comparées en premier).
     */
    private final double[] primary;

    /**
     * Tableau des coordonnées secondaires (comparées en second).
     */
    private final double[] secondary;

    /**
     * Construit un objet {@code CompositePoint} avec les coordonnées principales et secondaires fournies.
     * <p>
     * L'objet résultant est immuable : on effectue des copies défensives des tableaux passés en paramètre.
     * </p>
     *
     * @param primary
     *         le tableau de coordonnées principales (ne doit pas être {@code null}).
     * @param secondary
     *         le tableau de coordonnées secondaires (ne doit pas être {@code null} et
     *         doit avoir la même longueur que {@code primary}).
     * @throws IllegalArgumentException
     *         si l'un des tableaux est {@code null} ou si leurs tailles diffèrent.
     */
    public CompositePoint(double[] primary, double[] secondary) {
        if (primary == null || secondary == null) {
            throw new IllegalArgumentException("Les tableaux de coordonnées ne peuvent pas être null.");
        }
        if (primary.length != secondary.length) {
            throw new IllegalArgumentException(
                    "Les tableaux 'primary' et 'secondary' doivent avoir la même longueur.");
        }
        // Copie défensive pour garantir l'immuabilité
        this.primary = Arrays.copyOf(primary, primary.length);
        this.secondary = Arrays.copyOf(secondary, secondary.length);
    }

    /**
     * Retourne une <em>copie</em> des coordonnées principales du point.
     * <p>
     * L'emploi d'une copie garantit la non-mutabilité de l'objet.
     * </p>
     *
     * @return un nouveau tableau (double[]) contenant les coordonnées principales.
     */
    public double[] getPrimary() {
        return Arrays.copyOf(primary, primary.length);
    }

    /**
     * Retourne une <em>copie</em> des coordonnées secondaires du point.
     *
     * @return un nouveau tableau (double[]) contenant les coordonnées secondaires.
     */
    public double[] getSecondary() {
        return Arrays.copyOf(secondary, secondary.length);
    }

    /**
     * Compare ce {@code CompositePoint} à un autre {@code CompositePoint} pour un tri lexicographique.
     * <p>
     * L'ordre est déterminé de la façon suivante :
     * <ol>
     *   <li>On compare séquentiellement chaque valeur de {@code primary}.</li>
     *   <li>Si toutes les valeurs de {@code primary} sont égales, on compare alors
     *       séquentiellement chaque valeur de {@code secondary}.</li>
     * </ol>
     * </p>
     *
     * @param other
     *         le {@code CompositePoint} à comparer.
     * @return un entier négatif si ce point est <em>inférieur</em> à {@code other},
     *         positif s'il est <em>supérieur</em>, ou 0 s'ils sont équivalents.
     */
    @Override
    public int compareTo(CompositePoint other) {
        // Comparaison des coordonnées primary
        for (int i = 0; i < primary.length; i++) {
            if (this.primary[i] < other.primary[i]) {
                return -1;
            } else if (this.primary[i] > other.primary[i]) {
                return 1;
            }
        }
        // Si les coordonnées primary sont égales, comparaison de secondary
        for (int i = 0; i < secondary.length; i++) {
            if (this.secondary[i] < other.secondary[i]) {
                return -1;
            } else if (this.secondary[i] > other.secondary[i]) {
                return 1;
            }
        }
        return 0; // Identiques
    }

    /**
     * Fournit une représentation textuelle du {@code CompositePoint}.
     *
     * @return une chaîne représentant les coordonnées primary et secondary.
     */
    @Override
    public String toString() {
        return "CompositePoint{" +
                "primary=" + Arrays.toString(primary) +
                ", secondary=" + Arrays.toString(secondary) +
                '}';
    }
}

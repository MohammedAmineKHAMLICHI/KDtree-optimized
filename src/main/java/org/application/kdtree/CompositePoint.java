package org.application.kdtree;

import java.util.Arrays;

/**
 * <p>
 * La classe {@code CompositePoint} definit un point "composite" constitue de deux ensembles
 * de coordonnees : <strong>primary</strong> et <strong>secondary</strong>. Cette structure
 * est souvent utilisee pour gerer un ordre lexicographique personnalise (dans un Kd-Tree, par exemple).
 * </p>
 *
 * <p>
 * <strong>Auteur :</strong> KHAMLICHI Mohammed Amine
 * </p>
 *
 * <h3>Caracteristiques principales :</h3>
 * <ul>
 *   <li>Les coordonnees <em>primary</em> sont comparees en premier.</li>
 *   <li>Si les coordonnees <em>primary</em> sont egales, les coordonnees <em>secondary</em> sont comparees.</li>
 *   <li>L'objet est immuable : des copies defensives sont faites a la construction.</li>
 * </ul>
 *
 * <p>
 * Cette classe implemente {@link Comparable}, permettant le tri et la comparaison
 * lexicographique de deux {@code CompositePoint}.
 * </p>
 */
public class CompositePoint implements Comparable<CompositePoint> {

    /**
     * Tableau des coordonnees principales (comparees en premier).
     */
    private final double[] primary;

    /**
     * Tableau des coordonnees secondaires (comparees en second).
     */
    private final double[] secondary;

    /**
     * Construit un objet {@code CompositePoint} avec les coordonnees principales et secondaires fournies.
     * <p>
     * L'objet resultant est immuable : on effectue des copies defensives des tableaux passes en parametre.
     * </p>
     *
     * @param primary
     *         le tableau de coordonnees principales (ne doit pas etre {@code null}).
     * @param secondary
     *         le tableau de coordonnees secondaires (ne doit pas etre {@code null} et
     *         doit avoir la meme longueur que {@code primary}).
     * @throws IllegalArgumentException
     *         si l'un des tableaux est {@code null} ou si leurs tailles different.
     */
    public CompositePoint(double[] primary, double[] secondary) {
        if (primary == null || secondary == null) {
            throw new IllegalArgumentException("Les tableaux de coordonnees ne peuvent pas etre null.");
        }
        if (primary.length != secondary.length) {
            throw new IllegalArgumentException(
                    "Les tableaux 'primary' et 'secondary' doivent avoir la meme longueur.");
        }
        // Copie defensive pour garantir l'immuabilite
        this.primary = Arrays.copyOf(primary, primary.length);
        this.secondary = Arrays.copyOf(secondary, secondary.length);
    }

    /**
     * Retourne une <em>copie</em> des coordonnees principales du point.
     * <p>
     * L'emploi d'une copie garantit la non-mutabilite de l'objet.
     * </p>
     *
     * @return un nouveau tableau (double[]) contenant les coordonnees principales.
     */
    public double[] getPrimary() {
        return Arrays.copyOf(primary, primary.length);
    }

    /**
     * Retourne une <em>copie</em> des coordonnees secondaires du point.
     *
     * @return un nouveau tableau (double[]) contenant les coordonnees secondaires.
     */
    public double[] getSecondary() {
        return Arrays.copyOf(secondary, secondary.length);
    }

    /**
     * Compare ce {@code CompositePoint} a un autre {@code CompositePoint} pour un tri lexicographique.
     * <p>
     * L'ordre est determine de la facon suivante :
     * <ol>
     *   <li>On compare sequentiellement chaque valeur de {@code primary}.</li>
     *   <li>Si toutes les valeurs de {@code primary} sont egales, on compare alors
     *       sequentiellement chaque valeur de {@code secondary}.</li>
     * </ol>
     * </p>
     *
     * @param other
     *         le {@code CompositePoint} a comparer.
     * @return un entier negatif si ce point est <em>inferieur</em> a {@code other},
     *         positif s'il est <em>superieur</em>, ou 0 s'ils sont equivalents.
     */
    @Override
    public int compareTo(CompositePoint other) {
        // Comparaison des coordonnees primary
        for (int i = 0; i < primary.length; i++) {
            if (this.primary[i] < other.primary[i]) {
                return -1;
            } else if (this.primary[i] > other.primary[i]) {
                return 1;
            }
        }
        // Si les coordonnees primary sont egales, comparaison de secondary
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
     * Fournit une representation textuelle du {@code CompositePoint}.
     *
     * @return une chaine representant les coordonnees primary et secondary.
     */
    @Override
    public String toString() {
        return "CompositePoint{" +
                "primary=" + Arrays.toString(primary) +
                ", secondary=" + Arrays.toString(secondary) +
                '}';
    }
}

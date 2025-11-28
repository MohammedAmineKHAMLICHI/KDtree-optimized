package org.application.manager;

/**
 * <p>
 * Lexception {@code InvalidCriteriaException} est levee lorsquune methode recoit un
 * nombre de criteres different de celui attendu, generalement lors de linsertion ou
 * de la manipulation de points dans un KdTree.
 * </p>
 *
 * <p>
 * <strong>Auteur :</strong> KHAMLICHI Mohammed
 * </p>
 *
 * <h3>Utilisation :</h3>
 * <ul>
 *   <li>Signale que les criteres supplementaires fournis ne correspondent pas
 *       au nombre de criteres definis dans la configuration du KdTree.</li>
 *   <li>Permet de gerer proprement les erreurs de configuration ou d'insertion.</li>
 * </ul>
 */
public class InvalidCriteriaException extends Exception {

    /**
     * Construit une nouvelle exception avec le message specifie.
     *
     * @param message
     *         Le message detaillant la cause de l'exception.
     */
    public InvalidCriteriaException(String message) {
        super(message);
    }
}

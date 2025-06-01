package org.application.manager;

/**
 * <p>
 * L’exception {@code InvalidCriteriaException} est levée lorsqu’une méthode reçoit un
 * nombre de critères différent de celui attendu, généralement lors de l’insertion ou
 * de la manipulation de points dans un KdTree.
 * </p>
 *
 * <p>
 * <strong>Auteur :</strong> KHAMLICHI Mohammed
 * </p>
 *
 * <h3>Utilisation :</h3>
 * <ul>
 *   <li>Signale que les critères supplémentaires fournis ne correspondent pas
 *       au nombre de critères définis dans la configuration du KdTree.</li>
 *   <li>Permet de gérer proprement les erreurs de configuration ou d'insertion.</li>
 * </ul>
 */
public class InvalidCriteriaException extends Exception {

    /**
     * Construit une nouvelle exception avec le message spécifié.
     *
     * @param message
     *         Le message détaillant la cause de l'exception.
     */
    public InvalidCriteriaException(String message) {
        super(message);
    }
}

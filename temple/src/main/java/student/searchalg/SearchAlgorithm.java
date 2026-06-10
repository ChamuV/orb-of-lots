package student.searchalg;

import game.ExplorationState;

/**
 * Defines an exploration strategy for locating the Orb of Lots.
 *
 * Implementations are responsible for navigating the cavern using only the
 * information available through the {@link ExplorationState} API.
 *
 * When {@code findOrb} returns, the explorer must be standing on the Orb.
 */
public interface SearchAlgorithm {
    /**
     * Executes the exploration strategy and navigates the explorer to the Orb.
     *
     * The method should return only after the Orb has been found. Returning
     * while not standing on the Orb is considered a failed exploration.
     *
     * @param state the current exploration state provided by the game engine
     */
    void findOrb(ExplorationState state);
}

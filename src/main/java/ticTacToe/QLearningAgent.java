package ticTacToe;

import java.util.List;
import java.util.Random;

/**
 * A Q-Learning agent with a Q-Table, i.e. a table of Q-Values. This table is
 * implemented in the {@link QTable} class.
 * 
 * The methods to implement are: (1) {@link QLearningAgent#train} (2)
 * {@link QLearningAgent#extractPolicy}
 * 
 * Your agent acts in a {@link TTTEnvironment} which provides the method
 * {@link TTTEnvironment#executeMove} which returns an {@link Outcome} object,
 * in other words an [s,a,r,s']: source state, action taken, reward received,
 * and the target state after the opponent has played their move. You may
 * want/need to edit {@link TTTEnvironment} - but you probably won't need to.
 * 
 * @author Mohammed Faiz Iqbal
 */

public class QLearningAgent extends Agent {

	/**
	 * The learning rate, between 0 and 1.
	 */
	double alpha = 0.1;

	/**
	 * The number of episodes to train for
	 */
	int numEpisodes = 69900;

	/**
	 * The discount factor (gamma)
	 */
	double discount = 0.9;

	/**
	 * The epsilon in the epsilon greedy policy used during training.
	 */
	double epsilon = 0.1;

	/**
	 * This is the Q-Table. To get an value for an (s,a) pair, i.e. a (game, move)
	 * pair.
	 * 
	 */

	QTable qTable = new QTable();

	/**
	 * This is the Reinforcement Learning environment that this agent will interact
	 * with when it is training. By default, the opponent is the random agent which
	 * should make your q learning agent learn the same policy as your value
	 * iteration and policy iteration agents.
	 */
	TTTEnvironment env = new TTTEnvironment();

	/**
	 * Construct a Q-Learning agent that learns from interactions with
	 * {@code opponent}.
	 * 
	 * @param opponent     the opponent agent that this Q-Learning agent will
	 *                     interact with to learn.
	 * @param learningRate This is the rate at which the agent learns. Alpha from
	 *                     your lectures.
	 * @param numEpisodes  The number of episodes (games) to train for
	 */
	public QLearningAgent(Agent opponent, double learningRate, int numEpisodes, double discount) {
		env = new TTTEnvironment(opponent);
		this.alpha = learningRate;
		this.numEpisodes = numEpisodes;
		this.discount = discount;
		initQTable();
		train();
	}

	/**
	 * Initialises all valid q-values -- Q(g,m) -- to 0.
	 * 
	 */

	protected void initQTable() {
		List<Game> allGames = Game.generateAllValidGames('X');// all valid games where it is X's turn, or it's terminal.
		for (Game g : allGames) {
			List<Move> moves = g.getPossibleMoves();
			for (Move m : moves) {
				this.qTable.addQValue(g, m, 0.0);
				// System.out.println("initing q value. Game:"+g);
				// System.out.println("Move:"+m);
			}

		}

	}

	/**
	 * Uses default parameters for the opponent (a RandomAgent) and the learning
	 * rate (0.2). Use other constructor to set these manually.
	 */
	public QLearningAgent() {
		this(new RandomAgent(), 0.1, 69900, 0.9);
	}

	/**
	 * Implement this method. It should play {@code this.numEpisodes} episodes of
	 * Tic-Tac-Toe with the TTTEnvironment, updating q-values according to the
	 * Q-Learning algorithm as required. The agent should play according to an
	 * epsilon-greedy policy where with the probability {@code epsilon} the agent
	 * explores, and with probability {@code 1-epsilon}, it exploits.
	 * 
	 * At the end of this method you should always call the {@code extractPolicy()}
	 * method to extract the policy from the learned q-values. This is currently
	 * done for you on the last line of the method.
	 */

	public void train() {
		// Iterate over all the episodes that represent he different interactions with
		// the environment
		for (int i = 0; i < numEpisodes; i++) {
			while (!this.env.isTerminal()) {
				// Get the current state of the game
				Game game = this.env.getCurrentGameState();
				// Skips if the state is a terminal state
				if (game.isTerminal()) {
					continue;
				}

				// Choose a move based on an epsilon-greedy policy for the current game state
				Move selectedMove = chooseMoveWithEpsilonGreedyPolicy(game);
				// Execute the selected move on the environment with exception handling.
				Outcome result = executeMoveSafely(selectedMove);

				// These variables holds the current q-value and updated q-value.
				double qvalue;
				// adds to the current Q-value with the new information learned from the
				// executed move
				double qLearnedValue;

				// Gets the q-value from the q-table associated with the state
				qvalue = this.qTable.getQValue(result.s, result.move);

				// Calculate the weighted old value by multiplying the current Q-value by the
				// factor (1 - alpha)
				double oldWeightedValue = (1 - this.alpha) * qvalue;
				// Estimate the future cumulative reward by adding the immediate reward to the
				// discounted maximum Q-value of the next state.
				double futureRewardEstimate = result.localReward + this.discount * calculateMaxQValue(result.sPrime);
				// Calculate the updated Q-value by combining the old value with
				// the future reward estimate.
				qLearnedValue = oldWeightedValue + this.alpha * futureRewardEstimate;
				// Update the Q-value in the Q-table for the given state-action pair
				this.qTable.addQValue(result.s, result.move, qLearnedValue);
			}
			this.env.reset(); // resets the environment to its initial state, allowing the agent to start a
								// new episode from the beginning
		}

		// --------------------------------------------------------
		// you shouldn't need to delete the following lines of code.
		this.policy = extractPolicy();
		if (this.policy == null) {
			System.out.println("Unimplemented methods! First implement the train() & extractPolicy methods");
			// System.exit(1);
		}
	}

	/**
	 * Executes a move on the environment safely, handling any potential illegal
	 * moves.
	 *
	 * @param move The move to be executed.
	 * @return The outcome of the move execution. Null if an IllegalMoveException
	 *         occurs.
	 */
	private Outcome executeMoveSafely(Move move) {
		Outcome outcome = null; // variable to store the outcome of the attempted move.
		try {
			outcome = this.env.executeMove(move); // attempt a specific move within the environment
		} catch (IllegalMoveException e) {
			e.printStackTrace();
		}
		// returns the result of the move (state, reward, and the next move)
		return outcome;
	}

	/**
	 * Chooses a move based on an epsilon-greedy policy, balancing exploration and
	 * exploitation.
	 *
	 * @param gameState The current state of the game.
	 * @return The selected move according to the epsilon-greedy policy.
	 */
	private Move chooseMoveWithEpsilonGreedyPolicy(Game gameState) {
		List<Move> possibleMoves = gameState.getPossibleMoves(); // List of possible moves in the current game state.
		Move selectedMove = null; // move to be selected based on the policy.
		Random random = new Random(); // generating random values.
		double currentQValue = 0; // stores the q-value of the current move
		double maxQValue = -Double.MAX_VALUE; // maximum Q-value encountered so far.
		double randomValue = random.nextDouble(); // random value used for epsilon-greedy exploration

		// If the random value is less than the epsilon it explores by choosing a random
		// move from the possible moves. Else if the random value is greater it exploits
		// by choosing the move with the highest Q-value
		if (randomValue < epsilon) {
			if (!possibleMoves.isEmpty()) {
				// Generates a random index within the range of the list of possible moves
				int randomIndex = random.nextInt(possibleMoves.size());
				// move at the randomly generated index
				selectedMove = possibleMoves.get(randomIndex);
			}
		} else {
			// selects the move with the highest qvalue among the possible moves. This
			// represents the exploitation strategy, where the agent chooses moves
			// that have higher estimated values based on its learned Q-table.
			for (Move move : possibleMoves) { // iterates over each move in the list of possible moves
				currentQValue = qTable.getQValue(gameState, move);// retrieves the qvalue from the table for each move
				// If the current q value is greater than or equal to the maximum q value, it
				// updates maxQValue with the current q value.
				if (currentQValue >= maxQValue) {
					maxQValue = currentQValue;
					selectedMove = move;
				}
			}
		}

		return selectedMove; // selected move is returned
	}

	/**
	 * Implement this method. It should use the q-values in the {@code qTable} to
	 * extract a policy and return it.
	 *
	 * @return the policy currently inherent in the QTable
	 */
	public Policy extractPolicy() {
		// Create a new policy
		Policy extractedPolicy = new Policy();

		// Iterate through all games in the QTable
		for (Game currentGame : this.qTable.keySet()) {
			// If the current game is terminal,skip to the next iteration
			if (currentGame.isTerminal()) {
				continue;
			}

			double maxQValue = -Integer.MAX_VALUE; // Initialize the maximum Q-value to a very low value
			Move bestMove = null; // Initialize the best move to null

			// Iterate through all possible moves of the current game and
			// find a move that gives the maximum Q-value
			for (Move currentMove : currentGame.getPossibleMoves()) {
				// To store the Q-value that the agent has learned for a specific game state and
				// move
				double currentQValue = qTable.getQValue(currentGame, currentMove);
				// iterates through all possible moves and finds the move with the maximum
				// Q-value.
				if (currentQValue > maxQValue) {
					maxQValue = currentQValue;
					bestMove = currentMove;
				}
			}
			// Set the move associated with the current state to the best move in the policy
			extractedPolicy.policy.put(currentGame, bestMove);
		}
		// Return the extracted policy
		return extractedPolicy;
	}

	/**
	 * Calculates the maximum Q-value for a given game state. If the game state is
	 * terminal, the method returns 0.0.
	 *
	 * @param gameStatePrime The game state for which to calculate the maximum
	 *                       Q-value.
	 * @return The maximum Q-value associated with any possible move in the given
	 *         game state.
	 */
	private double calculateMaxQValue(Game gameStatePrime) {
		//Initialize the maxQvalue to a very low number.
		double maxQValue = -Double.MAX_VALUE;
		//Variable to store the q-value of the current move during the iteration of all the possible moves
		double currentQValue = 0;
		// If the game state is a terminal state than it returns 0 for the q-value
		if (gameStatePrime.isTerminal()) {
			return 0.0;
		}
		// iterates through all possible moves in the given game state
		for (Move move : gameStatePrime.getPossibleMoves()) {
	        // Get the Q-value for the current move in the given game state
			currentQValue = qTable.getQValue(gameStatePrime, move);
			//update maximum Q-value if the current Q-value is greater than existing max value
			if (currentQValue > maxQValue) {
				maxQValue = currentQValue;
			}
		}
		return maxQValue; // Return the maximum Q-value found among all possible moves in the given game state
	}

	public static void main(String a[]) throws IllegalMoveException {
		// Test method to play your agent against a human agent (yourself).
		QLearningAgent agent = new QLearningAgent();

		HumanAgent d = new HumanAgent();

		Game g = new Game(agent, d, d);
		g.playOut();

	}

}

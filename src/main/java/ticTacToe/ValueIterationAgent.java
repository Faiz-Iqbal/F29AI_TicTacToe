package ticTacToe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Value Iteration Agent, only very partially implemented. The methods to
 * implement are: (1) {@link ValueIterationAgent#iterate} (2)
 * {@link ValueIterationAgent#extractPolicy}
 * 
 * You may also want/need to edit {@link ValueIterationAgent#train} - feel free
 * to do this, but you probably won't need to.
 * 
 * {@link Large Language Model Support} - As mentioned in the coursework specification, we were allowed to use
 * a large language model in order to assist us in learning and solving value
 * iteration. Mentioned below is my conversation with ChatGPTs LLM 3.5:
 * https://chat.openai.com/share/e2e08910-804c-4979-90db-f714ee0f10e2
 * 
 * @author Mohammed Faiz Iqbal
 * 
 *
 */
public class ValueIterationAgent extends Agent {

	/**
	 * This map is used to store the values of states
	 */
	Map<Game, Double> valueFunction = new HashMap<Game, Double>();

	/**
	 * the discount factor
	 */
	double discount = 0.9;

	/**
	 * the MDP model
	 */
	TTTMDP mdp = new TTTMDP();

	/**
	 * the number of iterations to perform - feel free to change this/try out
	 * different numbers of iterations
	 */
	int k = 10;

	/**
	 * This constructor trains the agent offline first and sets its policy
	 */
	public ValueIterationAgent() {
		super();
		mdp = new TTTMDP();
		this.discount = 0.9;
		initValues();
		train();
	}

	/**
	 * Use this constructor to initialise your agent with an existing policy
	 * 
	 * @param p
	 */
	public ValueIterationAgent(Policy p) {
		super(p);

	}

	public ValueIterationAgent(double discountFactor) {

		this.discount = discountFactor;
		mdp = new TTTMDP();
		initValues();
		train();
	}

	/**
	 * Initialises the {@link ValueIterationAgent#valueFunction} map, and sets the
	 * initial value of all states to 0 (V0 from the lectures). Uses
	 * {@link Game#inverseHash} and {@link Game#generateAllValidGames(char)} to do
	 * this.
	 * 
	 */
	public void initValues() {

		List<Game> allGames = Game.generateAllValidGames('X');// all valid games where it is X's turn, or it's terminal.
		for (Game g : allGames)
			this.valueFunction.put(g, 0.0);

	}

	public ValueIterationAgent(double discountFactor, double winReward, double loseReward, double livingReward,
			double drawReward) {
		this.discount = discountFactor;
		mdp = new TTTMDP(winReward, loseReward, livingReward, drawReward);
	}

	/**
	 * 
	 * 
	 * /* Performs {@link #k} value iteration steps. After running this method, the
	 * {@link ValueIterationAgent#valueFunction} map should contain the (current)
	 * values of each reachable state. You should use the {@link TTTMDP} provided to
	 * do this.
	 * 
	 * References: GeeksforGeeks. (2016). Finding optimal move in Tic Tac Toe using
	 * Minimax Algorithm in Game Theory. [online] Available at:
	 * https://www.geeksforgeeks.org/finding-optimal-move-in-tic-tac-toe-using-minimax-algorithm-in-game-theory/
	 * [Accessed 27 Nov. 2023]. The Coding Train (2019). Coding Challenge 154: Tic
	 * Tac Toe AI with Minimax Algorithm. YouTube. Available at:
	 * https://www.youtube.com/watch?v=trKjYdBASyQ [Accessed 27 Nov. 2023].
	 */
	public void iterate() {
		double totalReward; // Variable that stores the cumulative reward.
		double maxQval; // Variable that store the maximum q-value for each state

		// The Expectimax Algortihm
		for (int i = 0; i < k; i++) {
			// Iterates over all the states in the MDP
			for (Game gameState : this.valueFunction.keySet()) {
				// Skips if the current state is a terminal state
				if (gameState.isTerminal()) {
					/*
					 * Setting terminal state value to 0 to avoid later setting the terminal state
					 * to a very low negative value as we are using -Integer.MAX_VALUE to find the
					 * maximum.
					 */
					this.valueFunction.put(gameState, 0.0);
					continue; // move to the next game state
				}
				maxQval = -Integer.MAX_VALUE; // initializes the variable max to a very small negative value.
				// Iterates over all the moves in the current state
				for (Move move : gameState.getPossibleMoves()) {
					totalReward = 0;
					// Iterate over all possible outcomes for the current move
					for (TransitionProb transitionProb : mdp.generateTransitions(gameState, move)) {
						// Calculate the q-value for the current outcome
						double qValue = transitionProb.prob * (transitionProb.outcome.localReward
								+ (discount * this.valueFunction.get(transitionProb.outcome.sPrime)));
						// Sum the q-values for each outcome
						totalReward += qValue;
					}
					// Sets max to the sum value if the sum is bigger than the original max.
					if (totalReward > maxQval) {
						maxQval = totalReward;
					}
				}
				// Update the value function for the current state with the new max
				this.valueFunction.put(gameState, maxQval);
			}
		}
	}

	/**
	 * This method should be run AFTER the train method to extract a policy
	 * according to {@link ValueIterationAgent#valueFunction} You will need to do a
	 * single step of expectimax from each game (state) key in
	 * {@link ValueIterationAgent#valueFunction} to extract a policy.
	 * 
	 * @return the policy according to {@link ValueIterationAgent#valueFunction}
	 * 
	 *         References: GeeksforGeeks. (2016). Finding optimal move in Tic Tac
	 *         Toe using Minimax Algorithm in Game Theory. [online] Available at:
	 *         https://www.geeksforgeeks.org/finding-optimal-move-in-tic-tac-toe-using-minimax-algorithm-in-game-theory/
	 *         [Accessed 27 Nov. 2023]. The Coding Train (2019). Coding Challenge
	 *         154: Tic Tac Toe AI with Minimax Algorithm. YouTube. Available at:
	 *         https://www.youtube.com/watch?v=trKjYdBASyQ [Accessed 27 Nov. 2023].
	 *         Ntu.edu.sg. (2023). Tic-tac-toe AI - Java Game Programming Case
	 *         Study. [online] Available at:
	 *         https://www3.ntu.edu.sg/home/ehchua/programming/java/javagame_tictactoe_ai.html
	 *         [Accessed 27 Nov. 2023].
	 */
	public Policy extractPolicy() {
		/*
		 * Creating a new instance of the Policy class to store the extracted policy.
		 * Which will be used for decision-making when the agent learns the optimal
		 * moves.
		 */
		Policy policy = new Policy();

		// Variables to track the move that gives the maximum Q-value
		// The optimal move will be taken based on the q-value, It starts as null and
		// gets updated with each iteration as the agent finds moves with higher
		// Q-values
		Move optimalMove = null; // Variable of instance Move
		// Variables that will keep track of the rewards the agent can recieve.
		double totalReward; // Counter to add up all rewards
		double maxCumulativeReward; // Stores the highest total reward that the agent has come across.

		// For loop to iterate over all states
		for (Game currentGameState : this.valueFunction.keySet()) {
			// if the current state is a terminal state then continue.
			if (currentGameState.isTerminal()) {
				/*
				 * Setting terminal state value to 0 to avoid later setting the terminal state
				 * to a very low negative value as we are using -Integer.MAX_VALUE to find the
				 * maximum.
				 */
				this.valueFunction.put(currentGameState, 0.0);
				continue;
			}
			// initializes the variable max to a very small negative value.
			maxCumulativeReward = -Integer.MAX_VALUE;
			// for loop that iterate over all possible moves for the current state
			for (Move possibleMove : currentGameState.getPossibleMoves()) {
				// set cumulative reward for the current move to 0
				totalReward = 0;
				// Iterate over all possible outcomes for the current move
				for (TransitionProb transitionProb : mdp.generateTransitions(currentGameState, possibleMove)) {
					// Calculate the q-value for the current outcome
					double qValue = transitionProb.prob * (transitionProb.outcome.localReward
							+ (discount * this.valueFunction.get(transitionProb.outcome.sPrime)));
					// Accumulate q-values for each outcome
					totalReward += qValue;
				}
				// Sets maxCumulativeReward to the value of total rewards if it is greater than
				// the highest cumalative reward known till now
				if (totalReward > maxCumulativeReward) {
					maxCumulativeReward = totalReward;
					optimalMove = possibleMove; // holds the move with the highest reward
				}
			}
			policy.policy.put(currentGameState, optimalMove); // Set the move associated with the current state to
																// maxMove in the policy
		}
		return policy;
	}

	/**
	 * This method solves the mdp using your implementation of
	 * {@link ValueIterationAgent#extractPolicy} and
	 * {@link ValueIterationAgent#iterate}.
	 */
	public void train() {
		/**
		 * First run value iteration
		 */
		this.iterate();
		/**
		 * now extract policy from the values in
		 * {@link ValueIterationAgent#valueFunction} and set the agent's policy
		 * 
		 */

		super.policy = extractPolicy();

		if (this.policy == null) {
			System.out.println("Unimplemented methods! First implement the iterate() & extractPolicy() methods");
			// System.exit(1);
		}

	}

	public static void main(String a[]) throws IllegalMoveException {
		// Test method to play the agent against a human agent.
		ValueIterationAgent agent = new ValueIterationAgent();
		HumanAgent d = new HumanAgent();

		Game g = new Game(agent, d, d);
		g.playOut();

	}
}

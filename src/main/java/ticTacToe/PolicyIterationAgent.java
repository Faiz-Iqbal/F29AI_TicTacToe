package ticTacToe;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * A policy iteration agent. You should implement the following methods: (1)
 * {@link PolicyIterationAgent#evaluatePolicy}: this is the policy evaluation
 * step from your lectures (2) {@link PolicyIterationAgent#improvePolicy}: this
 * is the policy improvement step from your lectures (3)
 * {@link PolicyIterationAgent#train}: this is a method that should
 * runs/alternate (1) and (2) until convergence.
 * 
 * NOTE: there are two types of convergence involved in Policy Iteration:
 * Convergence of the Values of the current policy, and Convergence of the
 * current policy to the optimal policy. The former happens when the values of
 * the current policy no longer improve by much (i.e. the maximum improvement is
 * less than some small delta). The latter happens when the policy improvement
 * step no longer updates the policy, i.e. the current policy is already
 * optimal. The algorithm should stop when this happens.
 * 
 * @author Mohammed Faiz Iqbal
 *
 */
public class PolicyIterationAgent extends Agent {

	/**
	 * This map is used to store the values of states according to the current
	 * policy (policy evaluation).
	 */
	HashMap<Game, Double> policyValues = new HashMap<Game, Double>();

	/**
	 * This stores the current policy as a map from {@link Game}s to {@link Move}.
	 */
	HashMap<Game, Move> curPolicy = new HashMap<Game, Move>();

	double discount = 0.9;

	/**
	 * The mdp model used, see {@link TTTMDP}
	 */
	TTTMDP mdp;

	/**
	 * loads the policy from file if one exists. Policies should be stored in .pol
	 * files directly under the project folder.
	 */
	public PolicyIterationAgent() {
		super();
		this.mdp = new TTTMDP();
		initValues();
		initRandomPolicy();
		train();

	}

	/**
	 * Use this constructor to initialise your agent with an existing policy
	 * 
	 * @param p
	 */
	public PolicyIterationAgent(Policy p) {
		super(p);

	}

	/**
	 * Use this constructor to initialise a learning agent with default MDP
	 * paramters (rewards, transitions, etc) as specified in {@link TTTMDP}
	 * 
	 * @param discountFactor
	 */
	public PolicyIterationAgent(double discountFactor) {

		this.discount = discountFactor;
		this.mdp = new TTTMDP();
		initValues();
		initRandomPolicy();
		train();
	}

	/**
	 * Use this constructor to set the various parameters of the Tic-Tac-Toe MDP
	 * 
	 * @param discountFactor
	 * @param winningReward
	 * @param losingReward
	 * @param livingReward
	 * @param drawReward
	 */
	public PolicyIterationAgent(double discountFactor, double winningReward, double losingReward, double livingReward,
			double drawReward) {
		this.discount = discountFactor;
		this.mdp = new TTTMDP(winningReward, losingReward, livingReward, drawReward);
		initValues();
		initRandomPolicy();
		train();
	}

	/**
	 * Initialises the {@link #policyValues} map, and sets the initial value of all
	 * states to 0 (V0 under some policy pi ({@link #curPolicy} from the lectures).
	 * Uses {@link Game#inverseHash} and {@link Game#generateAllValidGames(char)} to
	 * do this.
	 * 
	 */
	public void initValues() {
		List<Game> allGames = Game.generateAllValidGames('X');// all valid games where it is X's turn, or it's terminal.
		for (Game g : allGames)
			this.policyValues.put(g, 0.0);

	}

	/**
	 * You should implement this method to initially generate a random policy, i.e.
	 * fill the {@link #curPolicy} for every state. Take care that the moves you
	 * choose for each state ARE VALID. You can use the
	 * {@link Game#getPossibleMoves()} method to get a list of valid moves and
	 * choose randomly between them.
	 */
	public void initRandomPolicy() {
		Random rand = new Random(); // Generate random numbers to randomnly generate moves.
		// Maintains a list of all the possible moves from a state
		List<Move> possibleMoves;
		// If statement to iterate over all the possible game states in a given policy
		for (Game game : this.policyValues.keySet()) {
			if (game.isTerminal()) { // Skip if its a terminal state as they do not require a policy
				continue;
			}
			possibleMoves = game.getPossibleMoves(); // store the list of all possible moves for the current game state
			// Randomize the selection of a move and assign it to the policy.
			this.curPolicy.put(game, possibleMoves.get(rand.nextInt(possibleMoves.size())));
		}
	}

	/**
	 * Performs policy evaluation steps until the maximum change in values is less
	 * than {@code delta}, in other words until the values under the currrent policy
	 * converge. After running this method, the
	 * {@link PolicyIterationAgent#policyValues} map should contain the values of
	 * each reachable state under the current policy. You should use the
	 * {@link TTTMDP} {@link PolicyIterationAgent#mdp} provided to do this.
	 *
	 * @param delta
	 */
	protected void evaluatePolicy(double delta) {
		// Iterate over all the game states that are in the policyValues Hashmap.
		for (Game game : this.policyValues.keySet()) {
			// Skip any terminal states
			if (game.isTerminal()) {
				this.policyValues.put(game, 0.0); // Set terminal states value to 0
				continue;
			}
			// variables to store the updated and old values for the current game state.
			double updatedValue; // updated value during each iteration
			double oldValue; // (old) value of the current game state

			// The following loop will run till the values start to converge
			do {
				updatedValue = 0;
				// Iterates over all possible transitions for the current game state and policy.
				for (TransitionProb tProbability : this.mdp.generateTransitions(game, this.curPolicy.get(game))) {
					// Applying Bellmans equation to calculate q-value
					// probability of transitioning from the current state to the next state given
					// the current action.
					double transitionProbability = tProbability.prob;
					// immediate reward received after taking the current action in the current
					// state
					double immediateReward = tProbability.outcome.localReward;
					// The discount factor (gamma)
					double discountFactor = discount;
					// estimated value of the next state V(sPrime)
					double estimatedValueofNextState = this.policyValues.get(tProbability.outcome.sPrime);
					// Equation
					double qValue = transitionProbability
							* (immediateReward + (discountFactor * estimatedValueofNextState));
					// Updating the equation
					updatedValue += qValue;

				}
				// Stores previous value before the update
				oldValue = this.policyValues.get(game);
				this.policyValues.put(game, updatedValue);

				// If the change in the value of the current state is larger than the defined
				// threshold delta. The condition returns false when the values converge and
				// stops the loop.
			} while (Math.abs(this.policyValues.get(game) - oldValue) > delta);

		}

	}

	/**
	 * This method should be run AFTER the
	 * {@link PolicyIterationAgent#evaluatePolicy} train method to improve the
	 * current policy according to {@link PolicyIterationAgent#policyValues}. You
	 * will need to do a single step of expectimax from each game (state) key in
	 * {@link PolicyIterationAgent#curPolicy} to look for a move/action that
	 * potentially improves the current policy.
	 * 
	 * @return true if the policy improved. Returns false if there was no
	 *         improvement, i.e. the policy already returned the optimal actions.
	 */
	protected boolean improvePolicy() {
		// creates a copy of the existing current policy. This will be used to compare
		// later.
		Policy duplicatePolicy = new Policy(new HashMap<>(this.curPolicy));
		// Apply the expectimax algorithm
		for (Game game : this.curPolicy.keySet()) {
			//// Gets the current value of the state from policyValues and stores it in
			//// currentMaxValue
			double currentMaxValue = this.policyValues.get(game);
			// Store the current best move in the instance of type Move.
			Move bestMove = this.curPolicy.get(game);
			// Iterate through all possible moves for the current state
			for (Move possibleMove : game.getPossibleMoves()) {
				double sum = 0;
				// Iterate through all possible outcomes for the current move
				for (TransitionProb t : this.mdp.generateTransitions(game, possibleMove)) {
					// calculate the discounted future value by multiplying the future state's value
					// with the discount factor
					double discountedFutureValue = discount * this.policyValues.get(t.outcome.sPrime);
					// calculate the total expected value of the transition by adding the
					// reward to the discounted future value
					double totalExpectedValue = t.outcome.localReward + discountedFutureValue;
					// calculate the q-value for the transition
					double qValue = t.prob * totalExpectedValue;
					sum += qValue;
				}
				if (sum > currentMaxValue) {
					currentMaxValue = sum;
					bestMove = possibleMove; // Update the best move
				}
			}
			// Update the policy with the best move for the current state
			this.curPolicy.put(game, bestMove);
		}
		// Compare the current policy with the copy of the last policy,
		// if no change, improvePolicy returns false, else true.
		if (this.curPolicy.equals(duplicatePolicy.policy)) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * The (convergence) delta
	 */
	double delta = 0.1;

	/**
	 * This method should perform policy evaluation and policy improvement steps
	 * until convergence (i.e. until the policy no longer changes), and so uses your
	 * {@link PolicyIterationAgent#evaluatePolicy} and
	 * {@link PolicyIterationAgent#improvePolicy} methods.
	 */
	public void train() {
		// Initialize the policy with a random policy
		this.initRandomPolicy();
		// run following while loop as long as there is policy change
		while (this.improvePolicy()) {
			// Evaluate the current policy to update the values of states
			this.evaluatePolicy(delta);
		}
		// Once the loop concludes (until the policy no longer changes), create a new
		// Policy
		// using the current policy and assign it to the superclass's policy field
		super.policy = new Policy(curPolicy);
	}

	public static void main(String[] args) throws IllegalMoveException {
		/**
		 * Test code to run the Policy Iteration Agent agains a Human Agent.
		 */
		PolicyIterationAgent pi = new PolicyIterationAgent();

		HumanAgent h = new HumanAgent();

		Game g = new Game(pi, h, h);

		g.playOut();

	}

}

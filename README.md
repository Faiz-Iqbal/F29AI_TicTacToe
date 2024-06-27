# F29AI_TicTacToe

Coursework 2, Part 1 is an individual assignment, where you will each implement Value Iteration, Policy Iteration that plan/learn to play 3x3 Tic-Tac-Toe game. You will test your agents against other rule-based agents that are provided. You can also play against all the agents including your own agents to test them.

The Starter Code for this project is commented extensively to guide you, and includes Javadoc under `src/main/javadoc/` folder in the main project folder - you should read these carefully to learn to use the classes. This is comprised of the files below:

1. **ValueIterationAgent.java**: A Value Iteration agent for solving the Tic-Tac-Toe game with an assumed MDP model.
2. **PolicyIterationAgent.java**: A Policy Iteration agent for solving the Tic-Tac-Toe game with an assumed MDP model.
3. **QLearningAgent.java**: A q-learner, Reinforcement Learning agent for the Tic-Tac-Toe game.

### Files you should read & use but shouldn’t need to edit

1. **Game.java**: The 3x3 Tic-Tac-Toe game implementation.
2. **TTTMDP.java**: Defines the Tic-Tac-Toe MDP model.
3. **TTTEnvironment.java**: Defines the Tic-Tac-Toe Reinforcement Learning environment.
4. **Agent.java**: Abstract class defining a general agent, which other agents subclass.
5. **HumanAgent.java**: Defines a human agent that uses the command line to ask the user for the next move.
6. **RandomAgent.java**: Tic-Tac-Toe agent that plays randomly according to a RandomPolicy.
7. **Move.java**: Defines a Tic-Tac-Toe game move.
8. **Outcome.java**: A transition outcome tuple (s,a,r,s’).
9. **Policy.java**: An abstract class defining a policy – you should subclass this to define your own policies.
10. **TransitionProb.java**: A tuple containing an Outcome object and a probability of the Outcome occurring.
11. **RandomPolicy.java**: A subclass of policy – it’s a random policy used by a RandomAgent instance.

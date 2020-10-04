package student_player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import boardgame.Board;
import boardgame.Move;
import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoMove;

public class MyTools {
    public static Move getSomething(PentagoBoardState pbs) {
    	Move m = MCTS.pickMove(pbs);
        return m;
    }
}

class Node{
	
	Node parent;
    List<Node> children;
	State state;
    
    /* pretty standard node object, stores its parent, children, and a state object for all important 
     * information
     */
    
    public Node(PentagoBoardState pbs) {
    	// constructor for when first ever initializing a node (like the root basically at the beginning of MCTS)
        this.state = new State(pbs);
        children = new ArrayList<>();
    }
    
    public Node(State state) {
    	// constructor used during the expansion phase (where we create a new state, and then make a node for it)
    	this.state = state;
    	children = new ArrayList<>();
    }
    
    public Node(Node inputNode) {
    	/* constructor used when appending a node to an already existing node (for creating the children of 
    	 * a node)
    	 */
        this.children = new ArrayList<>();
        this.state = inputNode.getState();
        
        if (inputNode.getParent() != null){
        // if the input node isn't the root
        	this.parent = inputNode.getParent();
        }
        
        List<Node> inputChildren = inputNode.getChildren();
        
        for (Node child : inputChildren) {
            this.children.add(new Node(child));
        }
    }

    public State getState() {
        return state;
    }
    
    public Node getParent() {
        return parent;
    }
    
    public List<Node> getChildren() {
        return children;
    }
    // basic getters for each variable in the node object
    
    public Node getRandomChild() {
    // used when deciding on a node to simulate/explore
        int possibleMovesSize = this.children.size();
        // number meant to bound the random value to the size of the children of the node
        
        int randomIndex = (int) (Math.random() * possibleMovesSize);
        // gives the index selected randomly associated with a child node
        
        return this.children.get(randomIndex);
    }

    public void setState(State state) {
        this.state = state;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }
    
    // basic setters for each variable in the node object
    
    public class visitComparison implements Comparator<Node> {
	    public int compare(Node n1, Node n2) {
	        if (n1.getState().getTotalVisits() > n2.getState().getTotalVisits()) {
	        	return 1;
	        } 
	        if (n1.getState().getTotalVisits() == n2.getState().getTotalVisits()) {
	            return 0;
	        }
	        return -1;
	    }
	}

    public Node getMaxChild() {
    /* called at the end of MCTS, after running all steps of the algorithm, returns the node with the move we
     *	will decide on.
     */
    	Node maxChild = Collections.max(this.children, new visitComparison());
    	/* Collections.max returns the maximum object within the collection (array list in this case)
    	 * Comparator is necessary to define how each of the user objects in the list (nodes) are compared
    	 * in this case, based on the total visits of the nodes
    	 */
    	
        return maxChild;
    }
}

class Tree {
	// building the tree that monte carlo actually searches
	Node root;
	// only has a root node (the children of each node is stored within the node objects themselves and not here)
	
	public Tree(PentagoBoardState pbs) {
		// basic constructor, at the beginning of the main MCTS algorithm
		root = new Node(pbs);
		// create the root by passing the PentagoBoardState into it (the pbs originating from studentplayer.java)
	}
	
	public Node getRoot() {
        return root;
    }

    public void setRoot(Node root) {
        this.root = root;
    }
    // basis get and set.
}

class State {
	/* an object stored inside every node, apart from the properties in PentagoBoardState 
	 * we also want to store the number of times a node is visited over one run of the algorithm, and the score
	 */
	private PentagoBoardState pbs;
	private PentagoMove pm;
    private int totalVisits;
    private double score;
    private int turnPlayer;
    
    public State(PentagoBoardState inputPBS) {
        this.pbs = (PentagoBoardState)inputPBS.clone();
    }
    // this is the constructor used when first initializing a state and node (at the beginning of MCTS)
    
    public State(State state) {
    	// this is the constructor used when 
        this.pbs = (PentagoBoardState)state.getPBS().clone();
        this.pm = state.getPM();
        
        this.totalVisits = state.getTotalVisits();
        this.score = state.getScore();
        this.turnPlayer = state.getTurnPlayer();
        
        /* we simply copy all of the variables from the input state to our current state
    	 * except we use .clone() when copying the PentagoBoardState (so we store a copy of it, so each node...
    	 * doesn't mess with the PentagoBoardState of other nodes)
    	 */
    }
    	

    PentagoBoardState getPBS() {
        return pbs;
    }
    
    PentagoMove getPM() {
        return pm;
    }
    
    int getTurnPlayer() {
        return turnPlayer;
    }
    
    int getOpponent() {
        return 1 - turnPlayer;
    }
    
    public int getTotalVisits() {
        return totalVisits;
    }
    
    double getScore() {
        return score;
    }
    // basic getters for each variable of the state object

    
    void setPBS(PentagoBoardState inputPBS) {
        this.pbs = (PentagoBoardState) inputPBS.clone();
    }
    
    void setPM(PentagoMove inputPM) {
        this.pm = inputPM;
    }

    void setTurnPlayer(int inputTurnPlayer) {
        this.turnPlayer = inputTurnPlayer;
    }

    void setScore(double inputScore) {
        this.score = inputScore;
    }
    // basic setters for each variable of the state object, again .clone() is used for the PentagoBoardState
    
    
    void increaseVisit() {
    	/* add 1 to the number of visits, called during back propagation of the MCTS algorithm to sum up the total
        * of visits for each node eventually
        */
    	this.totalVisits++;
    }

    void increaseScore(double inputScore) {
        if (this.score == Integer.MIN_VALUE) {
        	return;
        	/* this situation only occurs when during a simulation, the current node leads to a win
        	 * for the opponent in the next turn, in which case we do not want to increase the score of the node
        	 */
        }
        else {
        	this.score += inputScore;
        }
    }
    
    void switchTurnPlayer() {
    	// just switch the player each state is representing
        this.turnPlayer = 1 - this.turnPlayer;
    }
    

    public List<State> allSubsequentStates() {
    	/* method to return every possible state that can be achieved from the current state
    	 * i.e: Stores a list of all the states that can be reached by the current list of legal moves available
    	 */ 
        List<State> subsequentStates = new ArrayList<>();
        
        List<PentagoMove> allLegalMoves = this.pbs.getAllLegalMoves();
        
        allLegalMoves.forEach(p -> {
            State newState = new State(this.pbs);
            newState.setTurnPlayer(1 - this.turnPlayer);
            
            newState.setPM(p);
            newState.getPBS().processMove(p);
            subsequentStates.add(newState);
            
            /* for ever legal move, create a new state, store the move, and the boardstate achieved by the move
             * inside of it, making sure to switch the turnPlayer as well for the new state
             */
        	}
        );
        
        return subsequentStates;
    }

    void makeRandomMove() {
        // from the list of legal moves, we want to play a random one
    	List<PentagoMove> allLegalMoves = this.pbs.getAllLegalMoves();
        
    	int sizeOfLegalMoves = allLegalMoves.size();
    	// number made for ensuring the random index we generate is within range
        
        int randomIndex = (int) (Math.random() * sizeOfLegalMoves);
        // creating a random index of the list to access
        
        this.pbs.processMove(allLegalMoves.get(randomIndex));
        // running the move at that random index
    }

}

class UCT {
	
    public static double evaluateUCT(int totalTreeVisits, double score, int totalNodeVisits) {
    	/* based on the upper confidence tree formula associated with MCTS (also mentioned in class)
    	 * in the selection phase, called for each child (of the node in question) to get the one with the 
    	 * best UCT score
    	 */
        if (totalNodeVisits == 0) {
            return Integer.MAX_VALUE;
            /* in the case where this node's not been explored at all yet, we want to try and maximize its
             * likelihood that it's chosen
             */
        }
        
        double uctExploitation = (score / (double) totalNodeVisits);
        // gives the win ratio. This half of the sum is to account for exploitation
        
        double uctExploration = 1.41 * Math.sqrt(Math.log(totalTreeVisits) / (double) totalNodeVisits);
        /* our scaling constant is currently sqrt(2), as that seems what it theoretically equates to
         * in MCTS generally. This half of the sum is to account for exploration
         */
        
        double uctVal = uctExploitation + uctExploration;
        // add the two halves to get the full formula value
        
        return uctVal;
    }
    
    public static class uctComparison implements Comparator<Node> {
	    public int compare(Node n1, Node n2) {
	    	double n1UCT = evaluateUCT(n1.getParent().getState().getTotalVisits(), n1.getState().getScore(), n1.getState().getTotalVisits());
	    	double n2UCT = evaluateUCT(n2.getParent().getState().getTotalVisits(), n2.getState().getScore(), n2.getState().getTotalVisits());
	    	/* the current node in this case is what we're running MCTS from in the current round
	         * based on how we set the counting of total visits, it should hold all of the total visits for the
	         * tree at this point (or in subsequent rounds, of the subtree we're running MCTS through)
	         */
	    	if (n1UCT > n2UCT) {
	        	return 1;
	        } 
	        if (n1UCT == n2UCT) {
	            return 0;
	        }
	        return -1;
	    }
	}

    static Node getMaxUCTNode(Node currNode) {
    	// called when trying to choose the best child of a node to go with in the selection phase
    	
    	Node maxChild = Collections.max(currNode.children, new uctComparison());
    	return maxChild;
    }
    
}

class MCTS{
	/* Now that we've set all of the object classes (Nodes, Trees, and States) and have defined how to calculate
	 * the UCT, we can run the main algorithm of MCTS. All 4 phases.
	 */

    public MCTS() {}

    public static PentagoMove pickMove(PentagoBoardState inputPBS) {
    	
        long startTime = System.currentTimeMillis();
        long deadline = startTime + 600;
        /* these two values are to bound the algorithm to a timer, ensuring we stay within the allowed time
         * limit, and in general we can't allow MCTS to run ad infinitum/until it simulates every possibility
         * as that defeats the point.
         */
        
        PentagoBoardState pbs = (PentagoBoardState) inputPBS.clone();
        /* so the main parameter we're given from StudentPlayer.java, we need to clone it in order to work with
         * the board (run MCTS on it) without making an actual move in the game (PBS the server/client)
         * is keeping up with.
         */
        
        Tree tree = new Tree(pbs);
        /* instantiate the tree with the cloned PentagoBoardState, which will be carried through to the node
         * and state objects
         */
        Node root = tree.getRoot();
        // we'll start the search algorithm from the root (which is also the only existing node at this point)
        
        root.getState().setTurnPlayer(pbs.getOpponent());
        while (System.currentTimeMillis() < deadline) {
        // while we're still within our time limit
        	
            Node chosenNode = selection(root);
            // We select the node here
            
            if (chosenNode.getState().getPBS().getWinner() == Board.NOBODY) {
            // if we're sure the node chosen in the selection stage isn't representing a finished match
            	expansion(chosenNode);
            	// expand the node
            }

            Node simulationNode = chosenNode;
            /* this is just to instantiate the node we want to run the simulation from. Ideally, we want to run
             * simulation on a random child of the selected node, but we can't guarantee the selected node has
             * children.
             */
            
            if (chosenNode.getChildren().size() > 0) {
            // so if the selected node does have children
            	simulationNode = chosenNode.getRandomChild();
                // we choose a random one of them to simulate a game from
            }
            
            int rollout = simulation(simulationNode);
            // now we can simulate a game from the node
            
            backPropogation(simulationNode, rollout);
            /* we can now backpropagate with the rollout result and update information along the path from
             * the node we start the simulation from to the last node 
             * Note*: The logic of each stage of MCTS is explained in each of the 4 below methods
             */
        }
        // After we've run through MCTS as much as possible within the allotted time frame
        Node nextMoveNode = root.getMaxChild();
        PentagoMove nextMove = nextMoveNode.getState().getPM();
        /* We've got a much more informed tree, and from the root, we can pick the node with the highest value
         * based on the UCT formula
         */
        tree.setRoot(nextMoveNode);
        return nextMove;
        // Then we can extract the move from the best node
    }

    private static Node selection(Node root) {
        Node chosenNode = root;
        /* ideally don't want the root, but the child with the best UCT, but we need to instantiate it as such
         * if the root doesn't have children (in the very 1st run through the MCTS loop), then we just return
         * the root itself
         */
        
        while (chosenNode.getChildren().size() != 0) {
        	chosenNode = UCT.getMaxUCTNode(chosenNode);
            // we pick the child of the root with the very best UCT to run MCTS on
        }
        return chosenNode;
    }

    private static void expansion(Node currNode) {
    	
        List<State> possibleStates = currNode.getState().allSubsequentStates();
        /* to expand from the current node we basically need all possible PentagBoardStates that can be reached
         * from this node in 1 move
         */
        
        possibleStates.forEach(state -> {
        	// then for each of these possible states
            Node newNode = new Node(state);
            // we build the node with the information stored in it (info relating to the new state)
            newNode.setParent(currNode);
            // this new node is the child of our current one       
            newNode.getState().setTurnPlayer(currNode.getState().getOpponent());
            // the child has the inverse turn player of our current node
            currNode.getChildren().add(newNode);
            // now with the fully constructed node, we can set its connection as a child to our current node
        });
    }
    
    private static int simulation(Node currNode) {
    	
        Node copyNode = new Node(currNode);
        State copyState = copyNode.getState();
        /* we don't want to mess with the current chosen node here, so we create a temporary copy to play with
         * during this method
         */
        
        int whoWon = copyState.getPBS().getWinner();
        int opponentWon = copyState.getOpponent();

        if (whoWon != opponentWon) {
        // if the opponent hasn't already won at the current node, then we can play further
        	while (whoWon == Board.NOBODY) {
        	// as long as the game's still going (nobody's won)
        		copyState.switchTurnPlayer();
        		copyState.makeRandomMove();
        		// then we keep randomly playing moves for the other
        		whoWon = copyState.getPBS().getWinner();
        		// check if someone's won afterwards
            }

            return whoWon;
            // then return the result of the simulated game
        }
        else{
        // in the case where the opponent has already won at our current node, then no need to further simulate
        	copyNode.getParent().getState().setScore(Integer.MIN_VALUE);
        	/* then we need to set the score of the current node as low as possible because it leads to a
        	 * victory for the opponent of the current node, and we want to avoid that as hard as possible
        	 */
            return whoWon;
        }
    }

    private static void backPropogation(Node simulationNode, int winningPlayer) {
    	
        Node currNode = simulationNode;
        /* we don't want to mess with the current chosen node here, so we create a temporary copy to play with
         * during this method
         */
        
        while (currNode != null) {
        // until we go from the node at the end of the simulation, to the root
        	currNode.getState().increaseVisit();
            // increment the visit count for the current node
            if (currNode.getState().getTurnPlayer() == winningPlayer) {
            // if the node correlates to the same player as that who won
            	currNode.getState().increaseScore(10);
            	// then we want to increase the score of the node
            }
                
            currNode = currNode.getParent();
            // go up the tree and continue propagating
        }
    }
}
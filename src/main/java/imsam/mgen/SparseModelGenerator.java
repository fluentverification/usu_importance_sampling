package imsam.mgen;


public class SparseModelGenerator extends MGen {

    
    @Override
    protected void generateModel() {
        stateSpace = new State[numberOfStates];
        // Initialize State objects
        logger.debug("Initializing state space");
        for (int stateId=0; stateId<numberOfStates; stateId++) {
            stateSpace[stateId] = new State(stateId);
        }
        logger.debug("Generating transitions");
        for (int stateId=0; stateId<numberOfStates; stateId++) {
            stateSpace[stateId] = new State(stateId);
            int transitionCount = (int) transitionCountDistribution.random();
            logger.trace("Generating " + transitionCount + " transitions for state " + stateId);
            for (int transitionId = 0; transitionId < transitionCount; transitionId++) {
                int successor = (int) (Math.random() * numberOfStates);
                if (stateId != successor) {
                    TransitionPath transition = new TransitionPath(
                            stateId,
                            successor, 
                            transitionRateDistribution.random()
                    );
                    stateSpace[stateId].transitionsOut.add(transition);
                    stateSpace[successor].transitionsIn.add(transition);
                }
            }
        }
        int temp = 0;
        boolean[] seedTracker = new boolean[numberOfStates];
        seedTracker[0] = true;
        for (int stateId=0; stateId+1<numberOfStates; stateId++) {
            //assign successor to a random state
            int successor = (int) (Math.random() * numberOfStates);
            //Randomly move through state space until unvisited state is found
            while (seedTracker[successor]) {
                successor = (int) (Math.random() * numberOfStates);
            }
            //if successor is not the current state, create new transition
            if (temp != successor) {
                TransitionPath transition = new TransitionPath(
                        stateId,
                        successor,
                        transitionRateDistribution.random()
                );
                stateSpace[stateId].transitionsOut.add(transition);
                stateSpace[successor].transitionsIn.add(transition);
            }
            //set state as visited 
            seedTracker[successor] = true;
            //set successor to be current state
            temp = successor;
        }
    }

}

package imsam.mgen;

import java.util.ArrayList;

/**
 * This is a model generator that will accept a list of target states and force all paths to converge on one of said states
 * 
 * //TODO//
 * Add support for multiple target states
 * 
 * @author Eric Reiss
 */
public class TerminatingPathModelGenerator extends MGen{
    /**
     * Generate model by creating path from target state to initial 
     * then ensuring every path converges to target state path
     */
    @Override
    protected void generateModel(){
        //initializing state space
        stateSpace = new State[numberOfStates];
        logger.debug("Initializing state space");
        for(int stateId=0; stateId < numberOfStates; stateId++){
            stateSpace[stateId] = new State(stateId);
        }

        //Create path from initial to target
        logger.debug("Generating target path");
        boolean[] onTargetPath = new boolean[numberOfStates];
        onTargetPath[targetState] = true;
        int current = targetState;
        int initial = 0;
        int predecessor = (int)(Math.random()*numberOfStates);
        while(current != initial){
            if(predecessor != targetState){
                TransitionPath transition = new TransitionPath(
                    predecessor, 
                    current, 
                    transitionRateDistribution.random());
                stateSpace[current].transitionsIn.add(transition);
                stateSpace[predecessor].transitionsOut.add(transition);
            }
            onTargetPath[predecessor] = true;
            current = predecessor;
            predecessor = (int)(Math.random()*numberOfStates);
        }

        //Add other transitions to connect to target path
        for(int stateId = 0; stateId<numberOfStates; stateId++){
            int transitionCount = (int)transitionCountDistribution.random();
            logger.trace("Generating " + transitionCount + " transitions for state " + stateId);
            for(int transitionId = 0; transitionId<transitionCount; transitionId++){
                int successor = (int)(Math.random()*numberOfStates);
                if(stateId != successor && stateId != targetState){
                    TransitionPath transition = new TransitionPath(stateId, successor, transitionRateDistribution.random());
                    stateSpace[stateId].transitionsOut.add(transition);
                    stateSpace[successor].transitionsIn.add(transition);
                    if(onTargetPath[successor]){
                        onTargetPath[stateId] = true;
                    }
                }
            }
        }


        //Ensure all states are on target path
        logger.debug("Ensuring all states are on target path");
        ArrayList<Integer> onPath = new ArrayList<>();
        ArrayList<Integer> notOnPath = new ArrayList<>();
        for(int stateId=0; stateId<numberOfStates; stateId++){
            if(onTargetPath[stateId]){
                logger.debug(stateId+" is on a path");
                onPath.add(stateId);
            }else{
                logger.debug(stateId + " is not on a path");
                notOnPath.add(stateId);
            }
        }
        if(notOnPath.size() != 0){
            logger.debug("Adding all states to target path");
            for(int stateId : notOnPath){
                int successor = onPath.get(((int)(Math.random()*onPath.size())));
                TransitionPath transition = new TransitionPath(
                    stateId, 
                    successor, 
                    transitionRateDistribution.random());
                logger.debug("Adding state" +stateId+ " to state "+ successor);
                stateSpace[stateId].transitionsOut.add(transition);
                stateSpace[stateId].transitionsIn.add(transition);
            }
        }
    }
}

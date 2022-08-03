package imsam.mgen;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a model generator that will accept a list of target states and force all paths to converge on one of said states
 * 
 * //TODO//
 * Add support for user-defined multiple target states
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
        List<Integer> targets = generateTargetStates();

        for(int targetIndex=0; targetIndex<targets.size(); targetIndex++){
            int target = targets.get(targetIndex);
            onTargetPath[target] = true;
            int current = target;
            int initial = 0;
            int predecessor = (int)(Math.random()*numberOfStates);
            while(current != initial){
                if(predecessor != target && !targets.contains(predecessor)){
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
        }

        //Add other transitions to connect to target path
        for(int stateId = 0; stateId<numberOfStates; stateId++){
            int transitionCount = (int)transitionCountDistribution.random();
            logger.trace("Generating " + transitionCount + " transitions for state " + stateId);
            for(int transitionId = 0; transitionId<transitionCount; transitionId++){
                int successor = (int)(Math.random()*numberOfStates);
                if(stateId != successor && !targets.contains(stateId)){
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
        List<Integer> onPath = new ArrayList<>();
        List<Integer> notOnPath = new ArrayList<>();
        for(int stateId=0; stateId<numberOfStates; stateId++){
            if(onTargetPath[stateId]){
                logger.trace(stateId+" is on a path");
                onPath.add(stateId);
            }else{
                logger.trace(stateId + " is not on a path");
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
                logger.debug("Connecting state" +stateId+ " to state "+ successor);
                stateSpace[stateId].transitionsOut.add(transition);
                stateSpace[stateId].transitionsIn.add(transition);
            }
        }
    }
        /**
         * Randomly choose target states from state space, bounded at 1/3 of state space
         * @return List of target states
         */
        private List<Integer> generateTargetStates(){
        List<Integer> targets = new ArrayList<>();
        double third = 0.3333333333333333f;
        int targetBound = (int)(third*numberOfStates); //get bound targets to 1/3 of state space
        logger.info("Targetbound "+targetBound);
        int numberOfTargets = (int)((Math.random()*(targetBound-1))+1); //Randomly choose number of targets, always ensuring at least 1
        logger.info("TargetSize "+numberOfTargets);
        logger.debug(numberOfTargets+" have been selected as target states");
        for(int targetIndex = 0; targetIndex < numberOfTargets; targetIndex++){
            int pickTarget = (int)((Math.random()*(numberOfStates-1))+1); //draw targets
            targets.add(pickTarget);
            logger.debug("Picking state "+pickTarget+" as a target state");
        }
        return targets;
    }
}

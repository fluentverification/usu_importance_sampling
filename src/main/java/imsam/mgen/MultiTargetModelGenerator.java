package imsam.mgen;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.kohsuke.args4j.Option;


/**
 * This is a model generator that will accept a list of target states and force all paths to converge on one of said states
 * @author Eric Reiss
 */
public class MultiTargetModelGenerator extends MGen{
    public List<Integer> targets;
    public static final String MGEN_ID = "multi-target";
    //////////////////////////////////////////////////
    // CLI Arguments
    @Option(name="--target-list",usage="In a space-separated String, list states that all paths will converge to. default: generates random targets")
    public String targetList = "";
    // end CLI Arguments
    //////////////////////////////////////////////////


    //Set default file name
    @Override
    public String MGEN_ID(){
        return MGEN_ID;
    }

    //Parse config file options
    @Override 
    protected boolean parseSubclassConfigParam(JSONObject json, String key){
        if(key == "targetList"){
            if(targetList.isBlank()){
                targetList = json.getString(key);
            }
            return true;
        }else
            return false;
    }

    //Set default options
    @Override
    protected void initSubclassParamDefaults(){
        logger.debug("Initializing Subclass Parameter Defaults");
        if(targetList.isBlank()){
            logger.debug("target-list is blank, generating target states");
            targets = generateTargetStates();
        }else{
            logger.debug("target list is "+ targetList);
            String[] parsedTargetList = targetList.split(" ");
            targets = new ArrayList<>();
            logger.debug("Parsing target list");
            for(String targetString : parsedTargetList){
                try{
                    logger.trace("Parsing: "+targetString);
                    int targetCandidate = Integer.parseInt(targetString);
                    if(targetCandidate>numberOfStates-1 || targetCandidate==0){
                        String errMsg = "Targets must be in the state space and cannot be the initial state";
                        logger.error(errMsg);
                        System.exit(1);
                    }else{
                        logger.debug("adding "+ targetCandidate+ " to target ArrayList");
                        targets.add(targetCandidate);
                    }
                }catch(NumberFormatException e){
                    String errMsg = "The target state list must be made up of integers separated by a space. "+
                    "Ex)\"1 5 9 12\"";
                    logger.error(errMsg);
                    System.exit(1);
                }
            }
        }
    }

   

    /**
     * Generates a model by creating paths from target states to the initial state
     * It then ensures every state connects to a target state path
     * Finally it randomly adds transitions
     */
    @Override
    protected void generateModel(){
        long start = System.nanoTime();
        //initializing state space
        stateSpace = new State[numberOfStates];
        logger.debug("Initializing state space");
        for(int stateId=0; stateId < numberOfStates; stateId++){
            stateSpace[stateId] = new State(stateId);
        }

        //Create paths from initial state to target states
        logger.debug("Generating target path(s)");
        TargetPath targetPathTracker[] = new TargetPath[targets.size()];
        boolean targetPathCheck[] = new boolean[numberOfStates];
        for(int targetIdx = 0; targetIdx < targets.size(); targetIdx++){
            int currentTarget = targets.get(targetIdx);
            logger.trace("Starting path for "+ currentTarget);
            targetPathTracker[targetIdx] = new TargetPath(currentTarget);
            logger.trace("Setting targetPathCheck["+currentTarget+"] to true");
            targetPathCheck[currentTarget] = true;
            int current = currentTarget;
            int initial = 0;
            while(current != initial){
                int predecessor = (int)(Math.random()*numberOfStates);
                while(targets.contains(predecessor) || current == predecessor || targetPathTracker[targetIdx].onPath(predecessor)){
                    logger.trace(predecessor + " is an invalid predecessor. Redrawing");
                    predecessor = (int)(Math.random()*numberOfStates);
                }
                TransitionPath transition = new TransitionPath(
                    predecessor, 
                    current, 
                    transitionRateDistribution.random());
                if(!stateSpace[current].transitionInExists(transition)){
                    logger.trace("Connecting state "+predecessor+" to state "+current);
                    stateSpace[current].transitionsIn.add(transition);
                    stateSpace[predecessor].transitionsOut.add(transition);
                    logger.trace("Adding state "+predecessor+" to TargetPath "+targetPathTracker[targetIdx].targetState);
                    targetPathTracker[targetIdx].addState(predecessor);
                    logger.trace("Setting targetPathCheck["+predecessor+"] to true");
                    targetPathCheck[predecessor] = true;
                }else{
                    logger.trace("Transition between "+ predecessor+ " and "+ current+ " already exists");
                }
                current = predecessor;
            }
            logger.debug("Target Path: "+targetPathTracker[targetIdx]); //Print target path
        }
        
        //Ensure all states are on a target path
        logger.debug("Ensuring all states are on target path");
        List<Integer> notOnPath = new ArrayList<>();
        for(int stateId = 0; stateId < numberOfStates; stateId++){
            if(!targetPathCheck[stateId]){
                notOnPath.add(stateId);
                logger.trace(stateId+ " is not on a path");
            }
        }

        //Add all states that are not on a path to a target path
        if(notOnPath.size() > 0){
            logger.debug("Adding all states to a target path");
            for(int stateId : notOnPath){
                int predecessor = targetPathTracker[(int)(targets.size()*Math.random())].getRandom();
                while(targets.contains(predecessor)){
                    logger.trace(predecessor + " is not a valid predecessor. Redrawing");
                    predecessor = targetPathTracker[(int)(targets.size()*Math.random())].getRandom();
                }
                int successor = targetPathTracker[(int)(targets.size()*Math.random())].getRandom();
                //Create successor transition and add if it does not already exist
                TransitionPath successorTransition = new TransitionPath(
                    stateId, 
                    successor, 
                    transitionRateDistribution.random());
                if(!stateSpace[stateId].transitionOutExists(successorTransition)){
                    logger.trace("Connecting state " +stateId+ " to state "+ successor);
                    stateSpace[stateId].transitionsOut.add(successorTransition);
                    stateSpace[successor].transitionsIn.add(successorTransition);
                }else{
                    logger.trace(stateId+" already connects to "+ successor);
                }
                //Create predecessor transition and add if it does not already exist
                TransitionPath predTransition = new TransitionPath(
                    predecessor,
                    stateId,
                    transitionRateDistribution.random());
                if(!stateSpace[stateId].transitionInExists(predTransition)){
                    logger.trace("Connecting state " +predecessor+ " to state "+ stateId);
                    stateSpace[predecessor].transitionsOut.add(predTransition);
                    stateSpace[stateId].transitionsIn.add(predTransition);
                }else{
                    logger.trace(predecessor+" already connects to "+stateId);
                }
            }
        }else{
            logger.debug("All states are on a target path");
        }

        //Randomly generate additional transitions
        logger.debug("Randomly generating other transitions");
        for(State state : stateSpace){
            int numberOfTransitions =(int)transitionCountDistribution.random();
            logger.trace("Generating "+ numberOfTransitions+ " transitions for "+ state.stateId);
            if(!targets.contains(state.stateId)){
                for(int transitionId = 0; transitionId<numberOfTransitions; transitionId++){
                    int successor = (int)(Math.random()*numberOfStates);
                    TransitionPath transition = new TransitionPath(
                        state.stateId, 
                        successor, 
                        transitionRateDistribution.random());
                    if((!state.transitionOutExists(transition)) && (successor != state.stateId)){
                        logger.trace("Connecting state " +state.stateId+ " to state "+ successor);
                        state.transitionsOut.add(transition);
                        stateSpace[successor].transitionsIn.add(transition);
                    }else{
                        logger.trace(state.stateId+" already connects to "+successor);;
                    }
                }
            }else{
                logger.trace("Skipping target state "+ state.stateId);
            }
        }
        long end = System.nanoTime();
        logger.info(numberOfStates+"-state model generated in "+(end-start)/1000000000+ "s");
    }

    /**
     * Randomly choose target states from state space, arbitrarily bounded at 1/3 of state space
     * @return List of target states
     */
    private List<Integer> generateTargetStates(){
        StringBuilder strBld = new StringBuilder();
        List<Integer> targets = new ArrayList<>();
        double third = 0.3333333333333333f;
        int targetBound = (numberOfStates==2)?1:(int)(third*numberOfStates); //get bound targets to 1/3 of state space
        logger.info("Target Bound: "+targetBound);
        int numberOfTargets = (int)((Math.random()*(targetBound-1))+1); //Randomly choose number of targets, always ensuring at least 1
        logger.info("Target Size: "+numberOfTargets);
        logger.debug(numberOfTargets+(numberOfTargets==1?" state has ":" states have ")+"been selected as target(s)");
        for(int targetIndex = 0; targetIndex < numberOfTargets; targetIndex++){
            //draw targets
            int pickTarget = (int)((Math.random()*(numberOfStates-1))+1); 
            //redraw for unique targets
            while(targets.contains(pickTarget)){  
                pickTarget = (int)((Math.random()*(numberOfStates-1))+1);
            }
            targets.add(pickTarget);
            strBld.append(pickTarget+" ");
        }
        logger.info("Generated Target States: "+ strBld.toString());
        return targets;
    }

    /**
     * This is a class to manage target paths. 
     */
    private class TargetPath{
        private int targetState;
        private List<Integer> path;

        public TargetPath(int target){
            this.targetState = target;
            path = new ArrayList<>();
            path.add(target);
        }

        public void addState(int stateId){
            path.add(stateId);
        }
        @Override
        public String toString(){
            StringBuilder strBld = new StringBuilder();
            for(int i = path.size()-1; i >= 0; i--){
                strBld.append(path.get(i)+ " ");
            }
            return strBld.toString();
        }
        public boolean onPath(int stateId){
            return path.contains(stateId);
        }
        public int getRandom(){
            return path.get((int)(Math.random()*path.size()));
        }
    }
}

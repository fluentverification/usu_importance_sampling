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
    TargetPath targetPathTracker[];

    //////////////////////////////////////////////////
    // CLI Arguments
    @Option(name="--target-list",aliases = "-L",forbids = "--absorb",
    usage="In a comma-separated String, list states that all paths will converge to. Target State is added by default. default: generates random targets")
    public String targetList = "";
    @Option(name="--absorb", aliases="-a", forbids="--target-list",
    usage="Creates an absorbing state that all states will go to in one step")
    public boolean absorb = false;
    @Option(name="--simple", usage="generate only target paths")
    public boolean simple = false;
    // end CLI Arguments
    //////////////////////////////////////////////////


    //Set default file name
    @Override
    public String getMGenID(){
        return MGEN_ID;
    }

    //Parse config file options
    @Override 
    protected boolean parseSubclassConfigParam(JSONObject json, String key){
      switch(key){
        case "target-list": 
            targetList=json.getString(key);
            return true;
        case "simple" :
            simple = true;
            return true;
        case "absorb" : 
            absorb = true;
            return true;
        default : return false;
      }
    }

    //Set default options
    @Override
    protected void initSubclassParamDefaults(){
        logger.debug("-------------Initializing Subclass Parameter Defaults-------------");
        if(!absorb){
            if(targetList.isBlank()){
                logger.debug("-------------target-list is blank, generating target states-------------");
                targets = generateTargetStates();
            } else {
                if(targetList == Integer.toString(targetState)){
                    targets.add(targetState);
                } else {
                    logger.debug("-------------target list is "+ targetList+"-------------");
                    String[] parsedTargetList = targetList.split(",");
                    targets = new ArrayList<>();
                    logger.debug("-------------Parsing target list-------------");
                    for(String targetString : parsedTargetList){
                        try{
                            logger.trace("Parsing: "+targetString);
                            int targetCandidate = Integer.parseInt(targetString);
                            if(targetCandidate>numberOfStates-1 || targetCandidate==0){
                                String errMsg = "Targets must be in the state space and cannot be the initial state";
                                logger.error(errMsg);
                                System.exit(1);
                            } else {
                                logger.debug("-------------adding "+ targetCandidate+ " to target ArrayList-------------");
                                targets.add(targetCandidate);
                            }
                        }catch(NumberFormatException e){
                            String errMsg = "The target state list must be made up of integers separated by a space. "+
                            "Ex)\"1 5 9 12\"";
                            logger.error(errMsg);
                            System.exit(1);
                        }
                    }
                    targets.add(targetState);
                }
            }
        } else {
            //if absorb is selected then the target list will only contain the target state
            targets = new ArrayList<>();
            targets.add(targetState);
        }
    }

   

    /**
     * Generates a model by creating paths from target states to the initial state
     * It then ensures every state connects to a target state path
     * Finally it randomly adds transitions
     */
    @Override
    protected void generateModel(){
        long start = System.nanoTime(); //to calculate runtime

        //initializing state space
        stateSpace = new State[numberOfStates];
        logger.debug("-------------Initializing state space-------------");
        for(int stateId=0; stateId < numberOfStates; stateId++){
            stateSpace[stateId] = new State(stateId);
        }

        /*
         * This sections creates paths from the initial state to target states.
         * The method is to start at the target states and randomly generate predecessor states.
         * A check is done to ensure that the predecessor is not already on the path and is not a target state.
         * Predecessors are tracked using the TargetPath class.
        */
        logger.debug("-------------Generating target path(s)-------------");
        targetPathTracker = new TargetPath[targets.size()];
        boolean targetPathCheck[] = new boolean[numberOfStates];
        int initial = 0;
        for(int targetIdx = 0; targetIdx < targets.size(); targetIdx++){
            int currentTarget = targets.get(targetIdx);
            logger.trace("Starting path for "+ currentTarget);
            targetPathTracker[targetIdx] = new TargetPath(currentTarget);
            logger.trace("Setting targetPathCheck["+currentTarget+"] to true");
            targetPathCheck[currentTarget] = true;
            int current = currentTarget;
            for(int count = 1; current != initial; count++){
                int predecessor = (int)(Math.random()*numberOfStates);
                //if absorb is selected guarantee one state is not on target path
                if(absorb && count==numberOfStates-2){
                    logger.debug("-------------Forcing to initial-------------");
                    predecessor = initial;
                }
                //Check if predecessor is valid, redraw if not
                while(targets.contains(predecessor) || current == predecessor || targetPathTracker[targetIdx].onPath(predecessor)){
                    logger.trace(predecessor + " is an invalid predecessor. Redrawing");
                    predecessor = (int)(Math.random()*numberOfStates);
                }
                int rate = (int)transitionRateDistribution.random(); 
                TransitionPath transition = new TransitionPath(
                    predecessor, 
                    current, 
                    rate);
                //Make sure transition does not already exist
                if(!stateSpace[current].transitionInExists(transition)){
                    logger.trace("Connecting state "+predecessor+" to state "+current);
                    stateSpace[current].transitionsIn.add(transition);
                    stateSpace[predecessor].transitionsOut.add(transition);
                    logger.trace("Adding state "+predecessor+" to TargetPath "+targetPathTracker[targetIdx].targetState);
                    targetPathTracker[targetIdx].addState(predecessor);
                    logger.trace("Setting targetPathCheck["+predecessor+"] to true");
                    targetPathCheck[predecessor] = true;
                    //Add to adjacency matrix
                } else {
                    if(!targetPathTracker[targetIdx].onPath(predecessor)){
                        targetPathTracker[targetIdx].addState(predecessor);
                    }
                    logger.trace("Transition between "+ predecessor+ " and "+ current+ " already exists");
                }
                current = predecessor;
                count++;
            }
            logger.debug("Target Path: "+targetPathTracker[targetIdx]); //Print target path
        }
        
        /* 
         * Once all target paths have been generated a check will be run to make sure there are no disjoint states
         * If there are then those states will have a successor and a predecessor randomly drawn from a randomly 
         * selected target path. Predecessors will not be allowed to target states, and checks will be done to ensure that 
         * a transition does not already exist.
        */
        logger.debug("-------------Ensuring all states are on target path-------------");
        List<Integer> notOnPath = new ArrayList<>();
        for(int stateId = 0; stateId < numberOfStates; stateId++){
            if(!targetPathCheck[stateId]){
                notOnPath.add(stateId);
                logger.trace(stateId+ " is not on a path");
            }
        }
        //If absorb is selected then a random will state will be chosen from states not on the path
        int absorbingState=-1;
        if(absorb){
            absorbingState = notOnPath.get((int)(Math.random()*notOnPath.size()));
            notOnPath.remove(notOnPath.indexOf(absorbingState));
            logger.debug("-------------Setting absorbing state to "+ absorbingState+"-------------");
            int rate = (int) transitionRateDistribution.random();
            TransitionPath initialToAbsorb = new TransitionPath(initial, absorbingState, rate);
            stateSpace[initial].transitionsOut.add(initialToAbsorb);
            stateSpace[absorbingState].transitionsIn.add(initialToAbsorb);
        }
        //Add all states that are not on a path to a target path
        if(notOnPath.size() > 0){
            logger.debug("-------------Adding all states to a target path-------------");
            for(int stateId : notOnPath){
                int predecessor = targetPathTracker[(int)(targets.size()*Math.random())].getRandom();
                while(targets.contains(predecessor)){
                    logger.trace(predecessor + " is not a valid predecessor. Redrawing");
                    predecessor = targetPathTracker[(int)(targets.size()*Math.random())].getRandom();
                }
                int successor = absorb?absorbingState:targetPathTracker[(int)(targets.size()*Math.random())].getRandom();
                //Create successor transition and add if it does not already exist
                int rate = (int)transitionRateDistribution.random();
                TransitionPath successorTransition = new TransitionPath(
                    stateId, 
                    successor, 
                    rate);
                if(!stateSpace[stateId].transitionOutExists(successorTransition)){
                    logger.trace("Connecting state " +stateId+ " to state "+ successor);
                    stateSpace[stateId].transitionsOut.add(successorTransition);
                    stateSpace[successor].transitionsIn.add(successorTransition);
                } else {
                    logger.trace(stateId+" already connects to "+ successor);
                }
                //Create predecessor transition and add if it does not already exist
                rate = (int)transitionRateDistribution.random(); 
                TransitionPath predTransition = new TransitionPath(
                    predecessor,
                    stateId,
                    rate);
                if(!stateSpace[stateId].transitionInExists(predTransition)){
                    logger.trace("Connecting state " +predecessor+ " to state "+ stateId);
                    stateSpace[predecessor].transitionsOut.add(predTransition);
                    stateSpace[stateId].transitionsIn.add(predTransition);
                } else {
                    logger.trace(predecessor+" already connects to "+stateId);
                }
            }
        } else {
            logger.debug("-------------All states are already on a target path-------------");
        }

        /*
         * Once every state is on a target path, transitions are randomly generated to connect the model for every state except
         * the initial state and target states. The successor state is not allowed to be the current state, and if the 
         * transition already exists with a different rate then that transition is skipped. If it is a simple model 
         * then this section is skipped.
         */
        if(!simple){
            logger.debug("-------------Randomly generating other transitions-------------");
            for(State state : stateSpace){
                int numberOfTransitions =(int)transitionCountDistribution.random();
                logger.trace("Generating "+ numberOfTransitions+ " transitions for "+ state.stateId);
                if(!targets.contains(state.stateId) && state.stateId != 0 && state.stateId != absorbingState){
                    for(int transitionId = 0; transitionId<numberOfTransitions; transitionId++){
                        int successor = (int)(Math.random()*numberOfStates);
                        int rate = (int)transitionRateDistribution.random(); 
                        TransitionPath transition = new TransitionPath(
                            state.stateId, 
                            successor, 
                            rate);
                        if((!state.transitionOutExists(transition)) && (successor != state.stateId)){
                            logger.trace("Connecting state " +state.stateId+ " to state "+ successor);
                            state.transitionsOut.add(transition);
                            stateSpace[successor].transitionsIn.add(transition);
                        } else {
                            logger.trace(state.stateId+" already connects to "+successor);;
                        }
                    }
                } else {
                    logger.trace("Skipping target state "+ state.stateId);
                }
            }
        }
        //Calculate runtime
        long end = System.nanoTime();
        logger.info(numberOfStates+"-state model generated in "+(end-start)/1000000+ "ms");
    }

    /**
     * Randomly choose target states from state space, arbitrarily bounded at 1/3 of state space
     * @return List of target states
     */
    private List<Integer> generateTargetStates(){
        StringBuilder strBld = new StringBuilder();
        List<Integer> targets = new ArrayList<>();
        int targetBound = (numberOfStates==2)?1:(numberOfStates/3); //get bound targets to 1/3 of state space
        logger.info("Target Bound: "+targetBound);
        int numberOfTargets = (int)((Math.random()*(targetBound-1))+1); //Randomly choose number of targets, always ensuring at least 1
        logger.info("Target Size: "+numberOfTargets);
        logger.debug("-------------"+numberOfTargets+(numberOfTargets==1?" state has ":" states have ")+"been selected as target(s)-------------");
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
        if(!targets.contains(targetState)){
            targets.add(targetState);
            strBld.append(targetState);
        }
        logger.info("Generated Target States: "+ strBld.toString());
        return targets;
    }

    @Override
    protected void generateSeedPath() {
        for(int i = targetPathTracker.length-1; i >= 0; i--){
            if(targetPathTracker[i].targetState == targetState){
                seedPath = targetPathTracker[i].toString();
                return;
            }
        }
        logger.error("Seed Path not found");
        System.exit(1);
    }
    /**
     * This is a class to manage target paths.
     * It consists of a targetState Id and a List to track states on the target path
     * It contains methods that add a state to the target path list, print the list, check if a state is on the list, and
     * randomly select a state from the list.
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
            for(int i = path.size()-1; i >= 1; i--){
                strBld.append(path.get(i)+ ",");
            }
            strBld.append(this.targetState);
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

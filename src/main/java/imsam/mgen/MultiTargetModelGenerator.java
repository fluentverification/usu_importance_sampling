package imsam.mgen;

import java.io.FileWriter;
import java.io.IOException;
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
    public int[][] adjacencyMatrix; //Matrix to draw graph with 

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
        long start = System.nanoTime(); //to calculate runtime
        adjacencyMatrix = new int[numberOfStates][numberOfStates]; //Only used to draw graph

        //initializing state space
        stateSpace = new State[numberOfStates];
        logger.debug("Initializing state space");
        for(int stateId=0; stateId < numberOfStates; stateId++){
            stateSpace[stateId] = new State(stateId);
        }

        /*
         * This sections creates paths from the initial state to target states.
         * The method is to start at the target states and randomly generate predecessor states.
         * A check is done to ensure that the predecessor is not already on the path and is not a target state.
         * Predecessors are tracked using the TargetPath class.
        */
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
                int rate = (int)transitionRateDistribution.random(); 
                TransitionPath transition = new TransitionPath(
                    predecessor, 
                    current, 
                    rate);
                if(!stateSpace[current].transitionInExists(transition)){
                    logger.trace("Connecting state "+predecessor+" to state "+current);
                    stateSpace[current].transitionsIn.add(transition);
                    stateSpace[predecessor].transitionsOut.add(transition);
                    logger.trace("Adding state "+predecessor+" to TargetPath "+targetPathTracker[targetIdx].targetState);
                    targetPathTracker[targetIdx].addState(predecessor);
                    logger.trace("Setting targetPathCheck["+predecessor+"] to true");
                    targetPathCheck[predecessor] = true;
                    //Add to adjacency matrix
                    adjacencyMatrix[predecessor][current] = rate;
                }else{
                    logger.trace("Transition between "+ predecessor+ " and "+ current+ " already exists");
                }
                current = predecessor;
            }
            logger.debug("Target Path: "+targetPathTracker[targetIdx]); //Print target path
        }
        
        /* 
         * Once all target paths have been generated a check will be run to make sure there are no disjoint states
         * If there are then those states will have a successor and a predecessor randomly drawn from a randomly 
         * selected target path. Predecessors will not be allowed to target states, and checks will be done to ensure that 
         * a transition does not already exist.
        */
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
                int rate = (int)transitionRateDistribution.random();
                TransitionPath successorTransition = new TransitionPath(
                    stateId, 
                    successor, 
                    rate);
                if(!stateSpace[stateId].transitionOutExists(successorTransition)){
                    logger.trace("Connecting state " +stateId+ " to state "+ successor);
                    stateSpace[stateId].transitionsOut.add(successorTransition);
                    stateSpace[successor].transitionsIn.add(successorTransition);
                    //add to adjacency matrix
                    adjacencyMatrix[stateId][successor] = rate;
                }else{
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
                    //add to adjacency matrix 
                    adjacencyMatrix[predecessor][stateId] = rate;
                }else{
                    logger.trace(predecessor+" already connects to "+stateId);
                }
            }
        }else{
            logger.debug("All states are on a target path");
        }

        /*
         * Once every state is on a target path, transitions are randomly generated to connect the model for every state except
         * the initial state and target states. The successor state is not allowed to be the current state, and if the 
         * transition already exists with a different rate then that transition is skipped.
         */
        logger.debug("Randomly generating other transitions");
        for(State state : stateSpace){
            int numberOfTransitions =(int)transitionCountDistribution.random();
            logger.trace("Generating "+ numberOfTransitions+ " transitions for "+ state.stateId);
            if(!targets.contains(state.stateId) || state.stateId == 0){
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
                        //add to adjacency matrix
                        adjacencyMatrix[state.stateId][successor] = rate;
                    }else{
                        logger.trace(state.stateId+" already connects to "+successor);;
                    }
                }
            }else{
                logger.trace("Skipping target state "+ state.stateId);
            }
        }
        /*
         * Finally to help visualize the generated model, a python script is generated that will draw the graph.
         * It utilizes the networkx, matplotlib, and graphviz libraries. Total runtime is also calculated.
         */
        logger.debug("Generating drawGraph.py");
        try{
            processMatrix();
        }catch(IOException e){
            e.printStackTrace();
        }
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
     * This method generates a python script to draw the generated model.
     * It utilizes the matplotlib, networkx, and graphviz libraries.
     */
    private void processMatrix() throws IOException{
        String fileName = "drawGraph.py";
        FileWriter writer = new FileWriter(fileName);
        String imports = "from turtle import forward\nimport networkx as nx\nimport numpy as np\nimport matplotlib\nmatplotlib.use(\"Agg\")\nimport matplotlib.pyplot as plt\nimport scipy as sc\nimport tkinter as tk\nfrom networkx.drawing.nx_agraph import graphviz_layout\nfrom matplotlib import figure\n";
        writer.write(imports);
        writer.write("A = np.matrix([");
        for(int i = 0; i < adjacencyMatrix.length; i++){
            for(int j = 0; j < adjacencyMatrix[i].length; j++){
                if(j == 0){
                    writer.write("[");
                }
                writer.write(adjacencyMatrix[i][j]+",");
                if(j == (adjacencyMatrix[i].length-1)){
                    writer.write("]");
                }
            }
            if(i == adjacencyMatrix.length-1){
                writer.write("])\n");
            }else{
                writer.write(",");
            }
        }
        writer.write("G = nx.from_numpy_matrix(A,create_using=nx.DiGraph)\n");
        writer.write("f = plt.figure(figsize=(18.5,10),dpi=80)\n");
        writer.write("f.set_size_inches(18.5,10,forward=True)\n");
        writer.write("nx.draw(G, pos=graphviz_layout(G, prog=\"dot\",root=G.nodes[0]), node_size=1600, with_labels=1)\n");
        writer.write("labels=nx.get_edge_attributes(G,\"weight\")\n");
        writer.write("nx.draw_networkx_edge_labels(G,pos=graphviz_layout(G,prog=\"dot\",root=G.nodes[0]),edge_labels=labels)\n");
        writer.write("f.savefig(\"multi-target.png\")");
        writer.close();
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

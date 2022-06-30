//==============================================================================
// runPath: use PRISM to execute and analyze a specific transition path in
//          a CTMC model.
//
// Chris Winstead, Utah State University
//
// Based on PRISM API demoo "SimulateModel". The original file header is
// included below this line.
//
//==============================================================================
//	
//	Copyright (c) 2020-
//	Authors:
//	* Dave Parker <d.a.parker@cs.bham.ac.uk> (University of Birmingham)
//	
//------------------------------------------------------------------------------
//	
//	This file is part of PRISM.
//	
//	PRISM is free software; you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation; either version 2 of the License, or
//	(at your option) any later version.
//	
//	PRISM is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with PRISM; if not, write to the Free Software Foundation,
//	Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//	
//==============================================================================

package imsam;  // Importance Sampling package

import java.io.File;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import parser.Values;
import parser.ast.Expression;
import parser.ast.ModulesFile;
import prism.Prism;
import prism.PrismDevNullLog;
import prism.PrismException;
import prism.PrismLog;
import prism.PrismPrintStreamLog;
import simulator.SimulatorEngine;

/**
 * An example class demonstrating how to control PRISM programmatically,
 * through the functions exposed by the class prism.Prism.
 * 
 * This shows how to use PRISM's discrete-even simulator to generate some
 * sample paths through a model loaded in from a file.
 * 
 * See the README for how to link this to PRISM.
*/
public class runPath
{

	public static void main(String[] args)
	{
		new runPath().run();
	}

	public void run()
	{
		try {
			// Create a log for PRISM output (hidden or stdout)
			PrismLog mainLog = new PrismDevNullLog();
			//PrismLog mainLog = new PrismFileLog("stdout");

			// Initialise PRISM engine 
			Prism prism = new Prism(mainLog);
			prism.initialise();

			// Parse and load a PRISM model from a file
			ModulesFile modulesFile = prism.parseModelFile(new File("models/cycle_ssa.sm"));
			prism.loadPRISMModel(modulesFile);
						
			// Load the model into the simulator
			prism.loadModelIntoSimulator();
			SimulatorEngine sim = prism.getSimulator();
			
			FileReader fr = new FileReader("models/cycle_ssa.traces");
			BufferedReader br=new BufferedReader(fr);
			String x;
			
			x=br.readLine(); 
			

			String[] tr_st=x.split("\\s+"); 

			// Create a new path and take 3 random steps
			// (for debugging purposes, we use sim.createNewPath;
			// for greater efficiency, you could use sim.createNewOnTheFlyPath();
			sim.createNewPath();
			sim.initialisePath(null);

			int index;
			double path_probability = 1.0;
			double total_rate = 0.0;
			for (int tdx=1; tdx < tr_st.length; tdx++) {
			    index=-1;
			    total_rate = 0.0;
			    for (int idx=0; idx<sim.getNumTransitions(); idx++) {
				System.out.printf("tr %d: %s %f\n",idx,sim.getTransitionActionString(idx),sim.getTransitionProbability(idx));
				total_rate += sim.getTransitionProbability(idx);
			    }
			    for (int idx=0; idx<sim.getNumTransitions(); idx++) {
				String s1 = String.format("[%s]",tr_st[tdx]);
				String s2 = sim.getTransitionActionString(idx);
				System.out.printf("%d:%s=%s?",idx,s1,s2);
				if (s1.equalsIgnoreCase(s2)) {
				    index = idx;
				    System.out.printf(" yes.");
				    break;
				}
				System.out.printf("\n");
			    }			    
			    double transition_probability = sim.getTransitionProbability(index)/total_rate;
			    System.out.printf("\n======= tr %s (%d) %e ===========\n",tr_st[tdx],index,transition_probability);
			    path_probability *= transition_probability;
			    sim.manualTransition(index);

			}

			System.out.println(sim.getPath());
			
			sim.getPathFull().exportToLog(new PrismPrintStreamLog(System.out), true, ",", null);
			System.out.printf("Path probability: %e\n", path_probability);


			// Close down PRISM
			prism.closeDown();

		} catch (FileNotFoundException e) {
			System.out.println("Error: " + e.getMessage());
			System.exit(1);
		} catch (PrismException e) {
			System.out.println("Error: " + e.getMessage());
			System.exit(1);
		}
		catch (IOException e) {
		    System.out.println("Error: " + e.getMessage());
		    System.exit(1);
		    
		}
	}
}

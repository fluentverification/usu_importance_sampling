/*

SpecifiedDegreeDistribution Class

Author: Josh Jeppson

To use: create an ArrayList of items and their respective probabilities (must add up to 1)
and will randomly choose based on the uniformly distributed Math.random() according to desired 
probabilities.

*/
package imsam;

import java.util.ArrayList;
import java.lang.Exception;
import java.lang.Math;

/**
 * Simple pair class which contains items of two different types
 */
class Pair<T, U> {
	public T first;
	public U second;
	public Pair(T f, U s) {
		first = f;
		second = s;
	}
}

public class SpecifiedDegreeDistribution<T> {
	/**
	 * Constructor
	 *
	 * @param itemProbabilityPairs Items and probabilities they are chosen
	 * */
	public SpecifiedDegreeDistribution(ArrayList<Pair<T, Float>> itemProbabilityPairs) throws IllegalFormatException {
		this.itemProbabilityPairs = itemProbabilityPairs;
		checkItemProbabilityPairs();
	}
	
	/**
	 * Gets an item
	 *
	 * @return An item randomly chosen such that it matches the histogram
	 * */
	public T get() {
		double r = Math.random();
		ArrayList<Pair<T, Float>> pairs = this.itemProbabilityPairs;
		float total = 0;
		for (Pair<T, Float> pair : pairs) {
			total += pair.second;
			if (total >= r) {
				return pair.first;
			}
		}
		return pairs.get(pairs.size() - 1).first;
	}

	/**
	 * Checks the item probability pairs to make sure that they have valid probabilities
	 * 
	 * Throws an exception if total probability is greater than 1.0
	 */
	private void checkItemProbabilityPairs() throws IllegalFormatException {
		ArrayList<Pair<T, Float>> pairs = this.itemProbabilityPairs;
		float total = 0;
		for (Pair<T, Float> pair : pairs) {
			total += pair.second;
			if (total > 1) {
				throw new IllegalFormatException("Total probability should not be greater than 1!");
			}
		}
	}
	
	private ArrayList<Pair<T, Float>> itemProbabilityPairs;
}

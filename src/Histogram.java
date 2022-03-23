import java.util.ArrayList;
import java.lang.Exception;
import java.lang.Math;

class Pair<T, U> {
	public T first;
	public U second;
}

public class Histogram<T> {
	public Histogram(ArrayList<Pair<T, float>> itemProbabilityPairs) {
		this->itemProbabilityPairs = itemProbabilityPairs;
		checkItemProbabilityPairs();
	}

	public T get() {
		double r = Math.random();
		ArrayList<Pair<T, float>> pairs = this.itemProbabilityPairs;
		float total = 0;
		for (Pair<T, float> pair : pairs) {
			total += pair.second;
			if (total >= r) {
				return pair.first;
			}
		}

	}

	private void checkItemProbabilityPairs() throws Exception {
		ArrayList<Pair<T, float>> pairs = this.itemProbabilityPairs;
		float total = 0;
		for (Pair<T, float> pair : pairs) {
			total += pair.second;
			if (total > 1) {
				throw new Exception("Total probability should not be greater than 1!");
			}
		}
	}
	
	ArrayList<Pair<T, float>> itemProbabilityPairs;
}

/*
package imsam;

class DegreeDistributionTest {
	public static void main(String[] args) {
		// We want to choose:
		// 	1 with probability 0.2
		//	2 with probability 0.1
		// 	3 with probability 0.1
		// 	4 with probability 0.2
		// 	5 with probability 0.3
		// 	6 with probability 0.1
		ArrayList<Pair<int, Float>> itemProbabilityPairs = new ArrayList(
			{
				Pair(1, 0.1)
				, Pair(2, 0.1)
				, Pair(3, 0.1)
				, Pair(4, 0.2)
				, Pair(5, 0.3)
				, Pair(6, 0.1)
			}
		);

		SpecifiedDegreeDistribution<int> dist = new SpecifiedDegreeDistribution<int>(itemProbabilityPairs);
		int firstChoice = dist.get();
		int secondChoice = dist.get();
	}
}
*/
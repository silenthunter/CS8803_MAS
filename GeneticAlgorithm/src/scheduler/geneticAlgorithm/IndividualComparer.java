package scheduler.geneticAlgorithm;

import java.util.Comparator;

public class IndividualComparer implements Comparator<Individual> 
{
	@Override
	/**
	 * Sorts largest to smallest
	 */
	public int compare(Individual o1, Individual o2) {
		if(o1.fitness > o2.fitness) return -1;
		else if(o1.fitness == o2.fitness) return 0;
		else return 1;
	}
}

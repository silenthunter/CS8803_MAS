package scheduler.geneticAlgorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import scheduler.events.Event;

public class GeneticAlgorithm
{
	static Random rand = new Random();
	private ArrayList<Event> events = new ArrayList<Event>();
	private ArrayList<Individual> population = new ArrayList<Individual>();
	
	public GeneticAlgorithm(ArrayList<Event> events)
	{
		//Copy events
		for(Event ev : events)
		{
			Event myEv = ev.copy();
			this.events.add(myEv);
		}
	}
	
	public void compute(int populationSize, int generations)
	{
		initPopulation(populationSize);
		
		//Main loop
		for(int i = 0; i < generations; i++)
		{
			//Compute fitness for all individuals
			for(Individual ind : population)
				ind.computeFitness();
			
			//Sort based on fitness
			Collections.sort(population, new IndividualComparer());
			
			//Cull weakest
			ArrayList<Individual> leastFit = new ArrayList<Individual>();
			for(int j = populationSize / 2; j < populationSize; j++)
				leastFit.add(population.get(j));
			population.removeAll(leastFit);
			
			//Crossover
			for(int j = 0; j < populationSize / 2; j++)
			{
				int idx1 = rand.nextInt(populationSize / 2);
				int idx2 = rand.nextInt(populationSize / 2);
				Individual parent1 = population.get(idx1);
				Individual parent2 = population.get(idx2);
				Individual ind = new Individual(parent1, parent2);
				
				//Mutation
				if(rand.nextInt(100) < 5)
					ind.mutate();
				
				population.add(ind);
				
			}
		}
		
		//Get final fitness
		for(Individual ind : population)
			ind.computeFitness();
		
		Collections.sort(population, new IndividualComparer());
		population.get(0).printSchedule();
		//population.get(0).computeFitness();
	}
	
	private void initPopulation(int populationSize)
	{
		for(int i = 0; i < populationSize; i++)
		{
			Individual ind = new Individual(events);
			population.add(ind);
		}
	}
}

package scheduler.geneticAlgorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
	
	public void compute(int populationSize, int minGenerations, int maxGenerations, int goalFitness)
	{
		initPopulation(populationSize);
		
		//Main loop
		int i;
		for(i = 0; i < maxGenerations; i++)
		{
			//Compute fitness for all individuals
			boolean bestFit = false;
			for(Individual ind : population)
			{
				ind.computeFitness();
				
				//Quit good fitness if found
				if(i >= minGenerations && ind.fitness >= goalFitness) bestFit = true;
			}
			if(bestFit) break;
			
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
				//TODO: Tweak in production to avoid local minimum
				if(rand.nextInt(100) < 7)
					ind.mutate();
				
				population.add(ind);
				
			}
		}
		
		//Get final fitness
		for(Individual ind : population)
			ind.computeFitness();
		
		Collections.sort(population, new IndividualComparer());
		population.get(0).printSchedule();
		System.out.println("Generations: " + i);
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
	
	/**
	 * Returns the population of this genetic algorithm.
	 * @return ArrayList of Individuals forming a population
	 * @remark The population will be sorted from best fitness to worst if <b>compute</b> has been called beforehand.
	 */
	public ArrayList<Individual> getPopulation()
	{
		System.out.println("-TEST-");
		population.get(0).printSchedule();
		
		return population;
	}
}

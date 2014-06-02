package com.mightyknob.server.ga;

import org.uncommons.watchmaker.framework.EvolutionObserver;
import org.uncommons.watchmaker.framework.PopulationData;

public class EvolutionLogger<T> implements EvolutionObserver<T> {

	int generationCount;
	
	public EvolutionLogger(int generationCount) {
		this.generationCount = generationCount;
	}
	
	@Override
	public void populationUpdate(PopulationData<? extends T> data) {
		if (data.getGenerationNumber()==generationCount-1)
			System.out.println(data.getBestCandidateFitness());
		/* System.out.println("Generation " + data.getGenerationNumber() +
				" -> Best fitness: " + data.getBestCandidateFitness() +
				", Mean fitness: " + data.getMeanFitness()); */
	}
	
}

package com.mightyknob.server.ga;

import org.uncommons.watchmaker.framework.EvolutionObserver;
import org.uncommons.watchmaker.framework.PopulationData;

public class EvolutionLogger<T> implements EvolutionObserver<T> {

	@Override
	public void populationUpdate(PopulationData<? extends T> data) {
		System.out.println("Generation " + data.getGenerationNumber() +
				" -> Best fitness: " + data.getBestCandidateFitness() +
				", Mean fitness: " + data.getMeanFitness());
	}
	
}

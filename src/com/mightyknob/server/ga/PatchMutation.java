package com.mightyknob.server.ga;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.uncommons.watchmaker.framework.EvolutionaryOperator;


/**  
 * @author Gordan Kreković
 * @version 1.0.0
 */
public class PatchMutation implements EvolutionaryOperator<Patch> {
	float probability;
	float maxChange;
	
	public PatchMutation(float p, float w) {
		this.probability = p;
		this.maxChange = w;
	}
	
	@Override
	public List<Patch> apply(List<Patch> candidates, Random rng) {
		ArrayList<Float> parameters = new ArrayList<Float>();
		ArrayList<Float> mutatedParameters = new ArrayList<Float>();
		List<Patch> mutatedPopulation = new ArrayList<Patch>(candidates.size());
		for (Patch p : candidates) {
			parameters = p.getParameters();
			mutatedParameters.clear();
			for (float x : parameters) {
				if (rng.nextFloat() <= probability) {
					x = x + (rng.nextFloat()-0.5f)*maxChange;
					if (x < 0) x = 0;
					if (x > 1) x = 1;
				}
				mutatedParameters.add(x);
			}
			mutatedPopulation.add(new Patch(mutatedParameters));
		}
		return mutatedPopulation;
	}

}

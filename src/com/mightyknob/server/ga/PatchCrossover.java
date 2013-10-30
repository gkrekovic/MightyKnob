package com.mightyknob.server.ga;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.uncommons.maths.number.NumberGenerator;
import org.uncommons.watchmaker.framework.operators.AbstractCrossover;

/**  
 * @author Gordan Kreković
 * @version 1.0.0
 */
public class PatchCrossover extends AbstractCrossover<Patch> {

    /**
     * Single-point cross-over.
     */
	protected PatchCrossover() {
		this(1);
	}
	
	
    /**
     * Multiple-point cross-over (fixed number of points).
     * @param crossoverPoints The fixed number of cross-overs applied to each
     * pair of parents.
     */
	protected PatchCrossover(int crossoverPoints) {
		super(crossoverPoints);
	}

	
    /**
     * Multiple-point cross-over (variable number of points).
     * @param crossoverPointsVariable Provides the (possibly variable) number of
     * cross-overs applied to each pair of parents.
     */
    public PatchCrossover(NumberGenerator<Integer> crossoverPointsVariable)
    {
        super(crossoverPointsVariable);
    }
	
    
    /**
     * Applies cross-over to a pair of parents.
     *  
     * @param parent1 The first parent.
     * @param parent2 The second parent.
     * @param numberOfPoints The number of cross-overs to perform.
     * @param rng The RNG used to select the cross-over points.
     * @return A list containing a pair of offspring.
     */
	@Override
	protected List<Patch> mate(Patch parent1, Patch parent2, int numberOfPoints, Random rng) {
        int n = parent1.getSize(); 
		if (n != parent2.getSize())
        {
            throw new IllegalArgumentException("Cannot perform cross-over with different length parents.");
        }
		
		ArrayList<Float> parameters1;
		ArrayList<Float> parameters2;

		ArrayList<Float> temp1 = new ArrayList<Float>(n);
		ArrayList<Float> temp2 = new ArrayList<Float>(n);
		
		parameters1 = parent1.getParameters();
		parameters2 = parent2.getParameters();
		
		for (int i = 0; i < numberOfPoints; ++i) {
			int crossoverIndex = 1 + rng.nextInt(n-1);
			
			temp1.clear();
			temp2.clear();
			
			for (int j = 0; j < n; ++j) {
				if (j<crossoverIndex) {
					temp1.add(parameters2.get(j));
					temp2.add(parameters1.get(j));
				} else {
					temp1.add(parameters1.get(j));
					temp2.add(parameters2.get(j));
				}
			}
			
			parameters1 = new ArrayList<Float>(temp1);
			parameters2 = new ArrayList<Float>(temp2);
			
		}
		
		List<Patch> result = new ArrayList<Patch>(2);
		result.add(new Patch(parameters1));
		result.add(new Patch(parameters2));
				
		return result;
	}
	
}

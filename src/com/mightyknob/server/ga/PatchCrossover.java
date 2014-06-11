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
		
		ArrayList<Float> parameters1 = parent1.getParameters();
		ArrayList<Float> parameters2 = parent2.getParameters();

		ArrayList<Float> newParameters1 = new ArrayList<Float>(n);
		ArrayList<Float> newParameters2 = new ArrayList<Float>(n);
		
		if (numberOfPoints == 1) {
			int crossoverIndex = 1 + rng.nextInt(n-1);
			for (int i=0; i<crossoverIndex; ++i) {
				newParameters1.add(parameters1.get(i));
				newParameters2.add(parameters2.get(i));				
			}
			for (int i=crossoverIndex; i<n; ++i) {
				newParameters1.add(parameters2.get(i));
				newParameters2.add(parameters1.get(i));				
			}			
		} else {
			System.out.println("Nesto ne valja");
		}
		
		List<Patch> result = new ArrayList<Patch>(2);
		result.add(new Patch(newParameters1));
		result.add(new Patch(newParameters2));
				
		return result;
	}
	
}

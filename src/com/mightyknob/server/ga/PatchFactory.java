package com.mightyknob.server.ga;
import java.util.ArrayList;
import java.util.Random;

import org.uncommons.watchmaker.framework.factories.AbstractCandidateFactory;

public class PatchFactory extends AbstractCandidateFactory<Patch>{

	int n;
	
	public PatchFactory(int n) {
		this.n = n;
	}
	
	@Override
	public Patch generateRandomCandidate(Random rng) {
		ArrayList<Float> parameters = new ArrayList<Float>(n);
		for (int i=0; i<n; ++i) {
			parameters.add(rng.nextFloat());
		}
		return new Patch(parameters);
	}

}

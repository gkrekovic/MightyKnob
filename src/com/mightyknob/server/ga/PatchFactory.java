package com.mightyknob.server.ga;
import java.util.ArrayList;
import java.util.Random;

import org.uncommons.watchmaker.framework.factories.AbstractCandidateFactory;

import com.synthbot.audioplugin.vst.vst2.JVstHost2;

public class PatchFactory extends AbstractCandidateFactory<Patch>{

	int n;
	private JVstHost2 vst;
	
	public PatchFactory(JVstHost2 vst) {
		this.vst = vst;
		n = vst.numParameters();
	}
	
	@Override
	public Patch generateRandomCandidate(Random rng) {
		ArrayList<Float> parameters = new ArrayList<Float>(n);
		
		for (int i=0; i<n; ++i) {
			parameters.add(rng.nextFloat());
		}
		return new Patch(parameters);
	}
	
	public ArrayList<Patch> generateSeedCandidates(int populationSize) {
		ArrayList<Patch> seedCandidates = new ArrayList<Patch>();
		int numPrograms = vst.numPrograms();
		int numParameters = vst.numParameters();
		int n = Math.min(populationSize, numPrograms);
		
		for (int i = 0; i < n; ++i) {
			vst.setProgram(i);
			ArrayList<Float> parameters = new ArrayList<Float>(n);
			for (int j = 0; j < numParameters; ++j) {
				parameters.add(vst.getParameter(j));
			}
			seedCandidates.add(new Patch(parameters));
		}
		return seedCandidates;
	}

	public ArrayList<Patch> generateSeedCandidates() {
		return generateSeedCandidates(vst.numParameters());
	}
}

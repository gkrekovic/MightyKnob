package com.mightyknob.server.ga;

import java.util.List;

import org.uncommons.watchmaker.framework.FitnessEvaluator;

import com.mightyknob.server.audio.FeatureExtraction;
import com.mightyknob.server.audio.NormalizedFeatureVector;
import com.mightyknob.server.audio.StandardFeatureVector;
import com.mightyknob.server.audio.Synth;
import com.synthbot.audioplugin.vst.vst2.JVstHost2;

/**  
 * @author Gordan Kreković
 * @version 1.0.0
 */
public class PatchEvaluator implements FitnessEvaluator<Patch> {

	final static double MAX_DISTANCE = 1;
	private JVstHost2 vst;
	NormalizedFeatureVector targetVector;
	
	public PatchEvaluator(JVstHost2 vst, NormalizedFeatureVector targetVector) {
		this.vst = vst;
		this.targetVector = targetVector;
	}
	
	@Override
	public double getFitness(Patch candidate, List<? extends Patch> population) {
		int n = 132300;
		int m = 88200;
		
		int blockSize = vst.getBlockSize();
		int stepSize = blockSize;
		float sampleRate = vst.getSampleRate();
	
		float[] signal = new float[n];

		Synth synth = new Synth(vst);
		try {
			signal = synth.synthesize(candidate, n, m);
		} catch (Exception e) {
			System.err.println(candidate.name + " >> Exception: " + e.getMessage());
			return MAX_DISTANCE;
		}
			
		FeatureExtraction Extractor = new FeatureExtraction(blockSize, stepSize, sampleRate, targetVector);
		StandardFeatureVector features = new StandardFeatureVector(sampleRate);
		features = Extractor.extractFeatures(signal);
		double fitness = distance(features);
		
		return fitness;
	}

	private double distance(StandardFeatureVector candidateVector)  {
		int vectorSize = candidateVector.getSize();
		if (vectorSize != targetVector.getSize()) return MAX_DISTANCE;
		double d = 0;
		
		double[] candidateFeatures = candidateVector.getNormalizedFeatures();
		double[] targetFeatures = targetVector.getFeatures();

		int k = 0;
		for (int i = 0; i < vectorSize; ++i) {
			if (targetFeatures[i] != -1) {
				d += Math.abs(candidateFeatures[i] - targetFeatures[i]);
				k++;
			}
		}
		
		if (Double.isNaN(d)) {
			return MAX_DISTANCE;
		} else {
			return (k == 0) ? 0 : (d/k);
		}
	}
	
	@Override
	public boolean isNatural() {
		return false;
	}

}

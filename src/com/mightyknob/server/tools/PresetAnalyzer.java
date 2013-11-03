package com.mightyknob.server.tools;

import java.util.ArrayList;
import java.util.Collection;

import com.mightyknob.server.audio.FeatureExtraction;
import com.mightyknob.server.audio.NormalizedFeatureVector;
import com.mightyknob.server.audio.StandardFeatureVector;
import com.mightyknob.server.audio.Synth;
import com.mightyknob.server.ga.Patch;
import com.mightyknob.server.ga.PatchFactory;
import com.synthbot.audioplugin.vst.vst2.JVstHost2;

/**  
 * @author Gordan Kreković
 * @version 1.0.0
 */
public class PresetAnalyzer {
	JVstHost2 vst;
	
	final double MAX_DISTANCE = 1;
	
	public PresetAnalyzer(JVstHost2 vst) {
		this.vst = vst;
	}
	
	public void analyzePresets(NormalizedFeatureVector targetVector) {
		int n = 132300;
		int m = 88200;
		int blockSize = vst.getBlockSize();
		int stepSize = blockSize;
		float sampleRate = vst.getSampleRate();
		float [] signal = new float[n];
		
		PatchFactory factory = new PatchFactory(vst);
		Collection<Patch> seedCandidates = new ArrayList<Patch>();
		seedCandidates = factory.generateSeedCandidates();
		
		int i=0;
		for(Patch preset:seedCandidates) {
			Synth synth = new Synth(vst);
			try {
				signal = synth.synthesize(preset, n, m);
			} catch (Exception e) {
				System.err.println("Exception: " + e.getMessage());
			}

			FeatureExtraction Extractor = new FeatureExtraction(blockSize, stepSize, sampleRate, targetVector);
			StandardFeatureVector featureVector = Extractor.extractFeatures(signal);
			double[] normalizedFeatures = featureVector.getNormalizedFeatures();
			double[] features = featureVector.getFeatures();
			
			for (int j = 0; j < featureVector.getSize(); ++j) {
				if (features[j] != -1) System.out.print(features[j] + ", " + normalizedFeatures[j] + ", ");
			}
			System.out.print("distance: " + distance(featureVector, targetVector));
			System.out.println("preset" + i);

			new Synth(vst).preview(preset, "preset" + i +".wav");
			i++;
		}		
	}
	
	private double distance(StandardFeatureVector candidateVector, NormalizedFeatureVector targetVector)  {
		int vectorSize = candidateVector.getSize();
		if (vectorSize != targetVector.getSize()) return MAX_DISTANCE;
		double d = 0;
		
		double[] candidateFeatures = candidateVector.getNormalizedFeatures();
		double[] targetFeatures = targetVector.getFeatures();
		for (int i = 0; i < vectorSize; ++i) {
			d += Math.abs(candidateFeatures[i] - targetFeatures[i]);
		}
		
		if (Double.isNaN(d)) {
			return MAX_DISTANCE;
		} else {
			return d/vectorSize;
		}
	}
}

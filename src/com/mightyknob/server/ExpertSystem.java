package com.mightyknob.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.mightyknob.server.audio.NormalizedFeatureVector;

import net.sourceforge.jFuzzyLogic.FIS;

/**
 * Expert system implemented using jFuzzyLogic.
 * @author Gordan Kreković
 */
public class ExpertSystem {

	FIS fis;

	public ExpertSystem(String fileName) throws IOException {
		fis = FIS.load(fileName);
		
		if (fis == null) {
			throw new IOException("Cannot load file: '" + fileName + "'");
		}
	}
	
	/**
	 * Evaluates the fuzzy model based on input timbral attributes and returns a vector
	 * of normalized audio features.
	 * 
	 * @return 	Vector of normalized audio features.
	 */
	public NormalizedFeatureVector evaluate(HashMap<String, Double> inputs) {
		for (Map.Entry<String, Double> entry : inputs.entrySet()) {
			fis.setVariable(entry.getKey(), entry.getValue());
			//System.out.println("Key: " + entry.getKey() + " Value: " + entry.getValue());
		}

		fis.evaluate();
		
		NormalizedFeatureVector vector = new NormalizedFeatureVector();
		vector.setCentroidMean(fis.getVariable("centroid_mean").getLatestDefuzzifiedValue());
		vector.setCentroidStddev(fis.getVariable("centroid_stddev").getLatestDefuzzifiedValue());
		vector.setFluxMean(fis.getVariable("flux_mean").getLatestDefuzzifiedValue());
		//vector.setFluxStddev(fis.getVariable("flux_stddev").getLatestDefuzzifiedValue());
		vector.setFlatnessMean(fis.getVariable("flatness_mean").getLatestDefuzzifiedValue());
		vector.setFlatnessStddev(fis.getVariable("flatness_stddev").getLatestDefuzzifiedValue());
		vector.setAttackTime(fis.getVariable("attack_time").getLatestDefuzzifiedValue());
		vector.setSustainTime(fis.getVariable("sustain_time").getLatestDefuzzifiedValue());
		vector.setDecayTime(fis.getVariable("decay_time").getLatestDefuzzifiedValue());		
		vector.setHarmonicsOddRatio(fis.getVariable("odd_ratio").getLatestDefuzzifiedValue());		
		
		// System.out.println("Target vector: " + vector.toString());
		
		return vector;
	}
}

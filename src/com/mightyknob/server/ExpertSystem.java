package com.mightyknob.server;

import java.io.IOException;

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
	public NormalizedFeatureVector evaluate() {
		NormalizedFeatureVector vector = new NormalizedFeatureVector();
		
		fis.setVariable("bright", -1.0);
		fis.setVariable("harsh", 0.8);
		fis.setVariable("nasal", 0.6);
		fis.setVariable("compact", -1.0);
		fis.setVariable("plucked", -1.0);
		fis.setVariable("percussive", -1.0);
		fis.setVariable("varying", 0.1);		
		
		fis.evaluate();
		
		vector.setCentroidMean(fis.getVariable("centroid_mean").getLatestDefuzzifiedValue());
		vector.setCentroidStddev(fis.getVariable("centroid_stddev").getLatestDefuzzifiedValue());
		vector.setFluxMean(fis.getVariable("flux_mean").getLatestDefuzzifiedValue());
		vector.setFluxStddev(fis.getVariable("flux_stddev").getLatestDefuzzifiedValue());
		vector.setFlatnessMean(fis.getVariable("flatness_mean").getLatestDefuzzifiedValue());
		vector.setFlatnessStddev(fis.getVariable("flatness_stddev").getLatestDefuzzifiedValue());
		vector.setAttackTime(fis.getVariable("attack_time").getLatestDefuzzifiedValue());
		vector.setSustainTime(fis.getVariable("sustain_time").getLatestDefuzzifiedValue());
		vector.setDecayTime(fis.getVariable("decay_time").getLatestDefuzzifiedValue());		
		vector.setHarmonicsOddRatio(fis.getVariable("odd_ratio").getLatestDefuzzifiedValue());		
		
		System.out.println("Target vector: " + vector.toString());
		
		return vector;
	}
}

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
		
		fis.setVariable("brightness", 0.8);
		fis.setVariable("harsh", 0.2);
		
		fis.evaluate();
		vector.setCentroidMean(fis.getVariable("centroid_mean").getValue());
		vector.setCentroidStddev(fis.getVariable("centroid_stddev").getValue());
		vector.setFluxMean(fis.getVariable("flux_mean").getValue());
		vector.setFluxStddev(fis.getVariable("flux_stddev").getValue());
		vector.setFlatnessMean(fis.getVariable("flatness_mean").getValue());
		vector.setFlatnessStddev(fis.getVariable("flatness_stddev").getValue());
		
		System.out.println("Target vector: " + vector.toString());
		
		return vector;
	}
}

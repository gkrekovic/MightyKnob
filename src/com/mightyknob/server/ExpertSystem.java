package com.mightyknob.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import com.mightyknob.server.audio.FeatureVector;

import net.sourceforge.jFuzzyLogic.FIS;

public class ExpertSystem {

	FIS fis;
	double sampleRate;
	
	public ExpertSystem(Properties properties) throws IOException {
		String folder = properties.getProperty("fcl_folder");
		String fileName = properties.getProperty("fcl_name");
		sampleRate = Double.parseDouble(properties.getProperty("sample_rate"));
		fis = FIS.load(folder + fileName);
		
		if (fis == null) {
			throw new IOException("Cannot load file: '" + fileName + "'");
		}
	}
	
	public FeatureVector evaluate() {
		FeatureVector vector = new FeatureVector(sampleRate);
		
		fis.setVariable("brightness", 0.9);
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

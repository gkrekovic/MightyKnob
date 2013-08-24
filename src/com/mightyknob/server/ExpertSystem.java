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
		}	}
	
	public FeatureVector evaluate() {
		FeatureVector vector = new FeatureVector(sampleRate);
		
		fis.setVariable("brightness", 0.3);
		fis.evaluate();
		vector.setCentroidMean(fis.getVariable("centroid_mean").getLatestDefuzzifiedValue());
		vector.setCentroidStddev(fis.getVariable("centroid_stddev").getLatestDefuzzifiedValue());
		vector.setFluxMean(fis.getVariable("flux_mean").getLatestDefuzzifiedValue());
		vector.setFluxStddev(fis.getVariable("flux_stddev").getLatestDefuzzifiedValue());
		vector.setFlatnessMean(fis.getVariable("flatness_mean").getLatestDefuzzifiedValue());
		vector.setFlatnessStddev(fis.getVariable("flatness_stddev").getLatestDefuzzifiedValue());

		System.out.println(fis.getVariable("centroid_mean").getLatestDefuzzifiedValue());
		return vector;
	}
}

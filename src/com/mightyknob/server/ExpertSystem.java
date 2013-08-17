package com.mightyknob.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import net.sourceforge.jFuzzyLogic.FIS;

public class ExpertSystem {

	FIS fis;
	
	public ExpertSystem(Properties properties) throws IOException {
		String folder = properties.getProperty("fcl_folder");
		String fileName = properties.getProperty("fcl_name");
		fis = FIS.load(folder + fileName);
		
		if (fis == null) {
			throw new IOException("Cannot load file: '" + fileName + "'");
		}	}
	
	public ArrayList<Double> evaluate() {
		ArrayList<Double> vector = new ArrayList<Double>();
		
		fis.setVariable("brightness", 0.3);
		fis.evaluate();
		vector.add(fis.getVariable("centroid_mean").getLatestDefuzzifiedValue());
		System.out.println(fis.getVariable("centroid_mean").getLatestDefuzzifiedValue());
		return vector;
	}
}

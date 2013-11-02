package com.mightyknob.server.audio;

/**   
 * @author Gordan Kreković
 * @version 1.0.0
 */
public abstract class AbstractFeatureVector {
	final int NUMBER_OF_FEATURES = 9;

	double centroidMean = -1;
	double centroidStddev = -1;
	double fluxMean = -1;
	double fluxStddev = -1;
	double flatnessMean = -1;
	double flatnessStddev = -1;
	double attackTime = -1;
	double sustainTime = -1;
	double decayTime = -1;
	
	public AbstractFeatureVector() {
	}

	public abstract void setCentroidMean(double x);
	
	public abstract void setCentroidStddev(double x);
	
	public abstract void setFluxMean(double x); 
	
	public abstract void setFluxStddev(double x);
	
	public abstract void setFlatnessMean(double x);
	
	public abstract void setFlatnessStddev(double x);
	
	public abstract void setAttackTime(double x);
	
	public abstract void setSustainTime(double x);
	
	public abstract void setDecayTime(double x);
	
	public int getSize() {
		return NUMBER_OF_FEATURES;
	}
	
	public double[] getFeatures() {
		double[] features = new double[NUMBER_OF_FEATURES];
		features[0] = centroidMean;
		features[1] = centroidStddev;
		features[2] = fluxMean;
		features[3] = fluxStddev;
		features[4] = flatnessMean;
		features[5] = flatnessStddev;
		features[6] = attackTime;
		features[7] = sustainTime;
		features[8] = decayTime;
		return features;
	}
	
	public String toString() {
		StringBuilder s = new StringBuilder();
		double[] features = getFeatures();
		for (double f : features) {
			s.append(String.format("%8.4f ", f));
		}
		return s.toString();
	}
}

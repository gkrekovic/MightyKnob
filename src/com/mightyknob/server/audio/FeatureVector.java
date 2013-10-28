package com.mightyknob.server.audio;

public class FeatureVector {
	final int NUMBER_OF_FEATURES = 6;
	double sampleRate;
	
	double centroidMean;
	double centroidStddev;
	double fluxMean;
	double fluxStddev;
	double flatnessMean;
	double flatnessStddev;
	
	public FeatureVector(double sampleRate) {
		this.sampleRate = sampleRate;
		centroidMean = -1;
		centroidStddev = -1;
		fluxMean = -1;
		fluxStddev = -1;
		flatnessMean = -1;
		flatnessStddev = -1;
	}
	
	public void setCentroidMean(double x) {
		centroidMean = (Double.isNaN(x)) ? -1 : x;
	}
	
	public void setCentroidStddev(double x) {
		centroidStddev = (Double.isNaN(x)) ? -1 : x;
	}
	
	public void setFluxMean(double x) {
		fluxMean = (Double.isNaN(x)) ? -1 : x;
	}
	
	public void setFluxStddev(double x) {
		fluxStddev = (Double.isNaN(x)) ? -1 : x;
	}
	
	public void setFlatnessMean(double x) {
		flatnessMean = (Double.isNaN(x)) ? -1 : x;
	}
	
	public void setFlatnessStddev(double x) {
		flatnessStddev = (Double.isNaN(x)) ? -1 : x;
	}
	
	public int getSize() {
		return NUMBER_OF_FEATURES;
	}
	
	public double[] getNormalizedFeatures() {
		double k = 5;
		double[] features = new double[NUMBER_OF_FEATURES];
		features[0] = Math.log((centroidMean/sampleRate)*(Math.exp(k)-1)+1)/k;
		features[1] = Math.log((centroidStddev/sampleRate)*(Math.exp(k)-1)+1)/k;
		features[2] = fluxMean;
		features[3] = fluxStddev;
		features[4] =  flatnessMean; // 10*Math.log10(flatnessMean);
		features[5] = flatnessStddev;		
		return features;
	}
	
	public double[] getFeatures() {
		double[] features = new double[NUMBER_OF_FEATURES];
		features[0] = centroidMean;
		features[1] = centroidStddev;
		features[2] = fluxMean;
		features[3] = fluxStddev;
		features[4] = flatnessMean;
		features[5] = flatnessStddev;
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

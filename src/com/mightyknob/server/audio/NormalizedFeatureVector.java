package com.mightyknob.server.audio;

/**  
 * @author Gordan Kreković
 * @version 1.0.0
 */
public class NormalizedFeatureVector extends AbstractFeatureVector {

	@Override
	public void setCentroidMean(double x) {
		if (x < 0 || x > 1)
			throw new NumberFormatException("Normalized centroid mean must be between 0 and 1.");
		centroidMean = (Double.isNaN(x)) ? -1 : x;
	}

	@Override
	public void setCentroidStddev(double x) {
		if (x < 0 || x > 1)
			throw new NumberFormatException("Normalized centroid standard deviation must be between 0 and 1.");
		centroidStddev = (Double.isNaN(x)) ? -1 : x;
	}

	@Override
	public void setFluxMean(double x) {
		fluxMean = (Double.isNaN(x)) ? -1 : x;
	}

	@Override
	public void setFluxStddev(double x) {
		fluxStddev = (Double.isNaN(x)) ? -1 : x;
	}

	@Override
	public void setFlatnessMean(double x) {
		flatnessMean = (Double.isNaN(x)) ? -1 : x;
	}

	@Override
	public void setFlatnessStddev(double x) {
		flatnessStddev = (Double.isNaN(x)) ? -1 : x;
	}
}

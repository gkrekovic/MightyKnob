package com.mightyknob.server.audio;

/**  
 * @author Gordan Kreković
 * @version 1.0.0
 */
public class NormalizedFeatureVector extends AbstractFeatureVector {

	@Override
	public void setCentroidMean(double x) {
		if ((x != -1) && (x < 0 || x > 1))
			throw new NumberFormatException("Normalized centroid mean must be between 0 and 1."
					+ "The excpetion occurred, because the value was:" + x);
		centroidMean = (Double.isNaN(x)) ? -1 : x;
	}

	@Override
	public void setCentroidStddev(double x) {
		if ((x != -1) && (x < 0 || x > 1))
			throw new NumberFormatException("Normalized centroid standard deviation must be between 0 and 1."
					+ "The excpetion occurred because the value was: " + x);
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

	@Override
	public void setAttackTime(double x) {
		attackTime = (Double.isNaN(x)) ? -1 : x;
		
	}

	@Override
	public void setSustainTime(double x) {
		sustainTime = (Double.isNaN(x)) ? -1 : x;
	}

	@Override
	public void setDecayTime(double x) {
		decayTime = (Double.isNaN(x)) ? -1 : x;
	}
}

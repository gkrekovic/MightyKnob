package com.mightyknob.server.audio;

/**  
 * @author Gordan Kreković
 * @version 1.0.0
 */
public class StandardFeatureVector extends AbstractFeatureVector {
	double sampleRate;
	
	public StandardFeatureVector(double sampleRate) {
		this.sampleRate = sampleRate;
	}
	
	@Override
	public void setCentroidMean(double x) {
		if (x < 0 || (sampleRate > 0 && x > sampleRate/2))
			throw new NumberFormatException("Centroid mean out of range. It is expected to be between 0 and sampleRate/2."
					+ "The excpetion occurred, because the value was:" + x);
		centroidMean = (Double.isNaN(x)) ? -1 : x;
	}

	@Override
	public void setCentroidStddev(double x) {
		if (x < 0 || (sampleRate > 0 && x > sampleRate))
			throw new NumberFormatException("Centroid standard deviation out of range. It is expected to be between 0 and sampleRate."
					+ "The excpetion occurred, because the value was:" + x);
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
	
	public double[] getNormalizedFeatures() {
		NormalizedFeatureVector normalizedVector = new NormalizedFeatureVector();
	
		double k;
		k = 5;
		if (centroidMean != -1)
			normalizedVector.setCentroidMean(Math.log((2*centroidMean/sampleRate)*(Math.exp(k)-1)+1)/k);
		
		if (centroidStddev != -1) {
			normalizedVector.setCentroidStddev(Math.min(1, centroidStddev/centroidMean));
		}
			
		if (fluxMean != -1)
			normalizedVector.setFluxMean(Math.min(1, fluxMean));
		
		if (fluxStddev != -1)
			normalizedVector.setFluxStddev(Math.min(1, fluxStddev/fluxMean));
		
		k = 7;
		if (flatnessMean != -1)
			normalizedVector.setFlatnessMean(Math.log(flatnessMean*(Math.exp(k)-1)+1)/k);
		
		if (flatnessStddev != -1)
			normalizedVector.setFlatnessStddev(Math.min(1, flatnessStddev/flatnessMean));	
		
		k = 5;
		if (attackTime != -1)
			normalizedVector.setAttackTime(Math.log(attackTime*(Math.exp(k)-1)+1)/k);
		
		if (sustainTime != -1)
			normalizedVector.setSustainTime(Math.log(sustainTime*(Math.exp(k)-1)+1)/k);
		
		if (decayTime != -1)
			normalizedVector.setDecayTime(Math.log(decayTime*(Math.exp(k)-1)+1)/k);
		
		return normalizedVector.getFeatures();
	}
}

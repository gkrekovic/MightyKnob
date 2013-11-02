package com.mightyknob.server.audio;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;
import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D;

/**  
 * @author Gordan Kreković
 * @version 1.0.0
 */
public class FeatureExtraction {
	int blockSize;
	int stepSize;
	float sampleRate;
	boolean extractAll;
	NormalizedFeatureVector targetVector;
	
	public FeatureExtraction(int blockSize, int stepSize, float sampleRate) {
		this.blockSize = blockSize;
		this.stepSize = stepSize;
		this.sampleRate = sampleRate;
		extractAll = true;
	}
	
	public FeatureExtraction(int blockSize, int stepSize, float sampleRate, NormalizedFeatureVector targetVector) {
		this.blockSize = blockSize;
		this.stepSize = stepSize;
		this.sampleRate = sampleRate;
		this.targetVector = targetVector;
		extractAll = false;
	}

	/**
	 * Calculates audio features.
	 * <p>
	 * If the target vector is given in a constructor call, only features which does not have the value -1 in the target vector
	 * will be calculated here. The calculated features are not normalized.
	 * 
	 * @param signal - samples of the audio signal ranging from -1 to 1
	 * @return A vector of calculated audio features.
	 */
	public StandardFeatureVector extractFeatures(float signal[]) {	
		// For spectral-based features ending zeros in the signal are ignored. For that reason
		// we need to calculate effective length of the signal and the number of blocks.
		int effectiveLength = lastIndexBeforeZeros(signal);
		int effectiveNumberOfBlocks = (effectiveLength-blockSize)/stepSize+1;
		
		double[][] spectrum = new double[effectiveNumberOfBlocks][blockSize/2];
		double[] centroid = new double[effectiveNumberOfBlocks];
		double[] flux = new double[effectiveNumberOfBlocks];
		double[] flatness = new double[effectiveNumberOfBlocks];
		StandardFeatureVector vector = new StandardFeatureVector(sampleRate);
		
		spectrum = powerSpectrum(signal, effectiveLength);
		
		// Calculate spectral centroid and its statistical features
		if (extractAll || targetVector.centroidMean >= 0 || targetVector.centroidStddev >= 0) {
			centroid = spectralCentroid(spectrum);
			if (extractAll || targetVector.centroidMean >= 0) vector.setCentroidMean(calcMean(centroid));
			if (extractAll || targetVector.centroidStddev >= 0) vector.setCentroidStddev(calcStdDev(centroid));
		}

		// Calculate spectral flux and its statistical features
		if (extractAll || targetVector.fluxMean >= 0 || targetVector.fluxStddev >= 0) {
			flux = spectralFlux(spectrum);
			if (extractAll || targetVector.fluxMean >= 0)	vector.setFluxMean(calcMean(flux));
			if (extractAll || targetVector.fluxStddev >= 0) vector.setFluxStddev(calcStdDev(flux));
		}

		// Calculate spectral flatness and ist statistical features	
		if (extractAll || targetVector.fluxMean >= 0 || targetVector.fluxStddev >= 0) {
			flatness = spectralFlatness(spectrum);
			if (extractAll || targetVector.flatnessMean >= 0) vector.setFlatnessMean(calcMean(flatness));
			if (extractAll || targetVector.flatnessStddev >= 0) vector.setFlatnessStddev(calcStdDev(flatness));
		}
			
		
		// Calculate temporal features
		if (extractAll || targetVector.attackTime >= 0 || targetVector.sustainTime >= 0  || targetVector.decayTime >= 0) {
			double[] envelope = new double[signal.length];
			envelope = amplitudeEnvelope(signal);
			int attackTime = calculateAttackTime(envelope);
			int sustainTime = calculateSustainTime(envelope, attackTime);
			int decayTime = calculateDecayTime(envelope, attackTime+sustainTime);
			if (extractAll || targetVector.attackTime >= 0) vector.setAttackTime((double) attackTime / signal.length);			
			if (extractAll || targetVector.sustainTime >= 0) vector.setSustainTime((double) sustainTime / signal.length);			
			if (extractAll || targetVector.decayTime >= 0) vector.setDecayTime((double) decayTime / signal.length);						
		}	
		return vector;
	}

	/** Determine which is the last non-zero sample in the signal */
	private int lastIndexBeforeZeros(float signal[]) {
		int n = signal.length-1;
		while (signal[n] < 0.08 && n>0) n--;
		return n+1;
	}

	/** Calculate the power spectrum which is necessary for calculating spectral-based features */
	private double[][] powerSpectrum(float signal[], int effectiveLength) {
		int numberOfBlocks = (effectiveLength-blockSize)/stepSize+1;
		
		DoubleFFT_1D FFTObj = new DoubleFFT_1D(blockSize);
		double[][] spectrum = new double[numberOfBlocks][blockSize/2];
		
		double[] block = new double[blockSize];

		int counter=0;
		
		for (int i=0; (i + blockSize) <= effectiveLength; i+=stepSize) {
			for (int j=0; j<blockSize; ++j) {
				block[j] = signal[i+j];
			}
			FFTObj.realForward(block);
			
			spectrum[counter][0] = block[0] * block[0];
			for (int j=1; j<blockSize/2; ++j) {
				spectrum[counter][j] = block[j*2]*block[j*2]+block[j*2+1]*block[j*2+1];
			}
			counter++;
		}
		return spectrum;
	}

	/** Calculate the spectral centroid for all blocks of an audio signal */
	private double[] spectralCentroid(double spectrum[][]) {
		int numberOfBlocks = spectrum.length;
		double total = 0;
		double weightedTotal = 0;
		double[] centroid = new double[numberOfBlocks];
		
		for (int i=0; i<numberOfBlocks; ++i) {
			total = 0;
			weightedTotal = 0;
			for (int j=0; j<blockSize/2; ++j) {
				total += spectrum[i][j];
				weightedTotal += (sampleRate/blockSize)*j*spectrum[i][j];
			}
			
			if (total != 0) {
				centroid[i] = weightedTotal/total;
			} else {
				centroid[i] = 0;
			}
		}
		return centroid;
	}

	/** Calculate the spectral flux for all blocks of an audio signal */
	private double[] spectralFlux(double spectrum[][]) {
		int numberOfBlocks = spectrum.length;
		double[] flux = new double[numberOfBlocks];
		flux[0] = 0.0;
		
		for (int i=1; i<numberOfBlocks; ++i) {
			double sum = 0;
			double difference;
			double maxSpec1 = 0;
			double maxSpec2 = 0;
			
			for (int j=0; j<blockSize/2; ++j) {
				if (spectrum[i-1][j] > maxSpec1) maxSpec1 = spectrum[i-1][j];
				if (spectrum[i][j] > maxSpec2) maxSpec2 = spectrum[i][j];
			}
			
			if (maxSpec1 == 0) maxSpec1 = 1;
			if (maxSpec2 == 0) maxSpec2 = 1;
			
			for (int j=0; j<blockSize/2; ++j) {
				difference = (spectrum[i][j]/maxSpec2) - (spectrum[i-1][j]/maxSpec1);
				sum += (difference*difference);
			}
			flux[i] = Math.sqrt(sum);
		}
		
		return flux;
	}
	
	/** Calculate the spectral flatness for all blocks of an audio signal */
	private double[] spectralFlatness(double spectrum[][]) {
		int numberOfBlocks = spectrum.length;
		double[] flatness = new double[numberOfBlocks];
		for (int i=0; i<numberOfBlocks; ++i) {
			double mean = calcMean(spectrum[i]);
			double gmean = calcGMean(spectrum[i]);
			if (mean == 0) {
				flatness[i] = 1;
			} else {
				flatness[i] = gmean / mean;
			}
		}
		return flatness;
	}
	
	/** Calculate the amplitude envelope by convolving the audio signal with the Gaussian window.
	 *  Amplitude envelope is necessary for calculating temporal features.
	 */
	private double[] amplitudeEnvelope(float signal[]) {
		int n = signal.length;
		
		// Generate the Gaussian window in the time domain
		double sigma = 0.2; // sigma must be <= 0.5
		double[] window = new double[n];
		for (int i = 0; i < n; ++i) {
			double innerTerm = (i-(n-1)/2)/(sigma*(n-1)/2);
			window[i] = Math.exp(-0.5*innerTerm*innerTerm);
		}
		
		// Transform the window and the audio sginal to the frequency domain
		DoubleFFT_1D FFTObjDouble = new DoubleFFT_1D(n);	
		FFTObjDouble.realForward(window);
		FloatFFT_1D FFTObjFloat = new FloatFFT_1D(n);	
		FFTObjFloat.realForward(signal);
		
		// Multiplication in the frequency domain
		double[] smoothed = new double[n];
		for (int i = 0; i < n-1; i+=2) {
			smoothed[i] = window[i]*signal[i]-window[i+1]*signal[i+1];
			smoothed[i+1] = window[i]*signal[i+1]-window[i+1]*signal[i];
		}
		
		// Transform back to the time domain
		FFTObjDouble.realInverse(smoothed, true);
		
		return smoothed;
	}
	
	/** Returns the attack time in number of samples */
	private int calculateAttackTime(double[] envelope) {
		for (int i = 0; i < envelope.length; ++i) {
			if (envelope[i] > 0.99) return i;
		}
		return envelope.length;
	}
	
	/** Returns the sustain time in number of samples */
	private int calculateSustainTime(double[] envelope, int start) {
		for (int i = start; i < envelope.length; ++i) {
			if (envelope[i] < 0.7) return i-start;
		}
		return envelope.length-start;
	}

	/** Returns the decay time in number of samples */
	private int calculateDecayTime(double[] envelope, int start) {
		for (int i = start; i < envelope.length; ++i) {
			if (envelope[i] < 0.05) return i-start;
		}
		return envelope.length-start;
	}
	
	/** Calculate arithmetic mean */
	private double calcMean(double[] array) {
		double sum = 0;
		for (double a : array) {
			sum += a;
		}
		return sum/array.length;
	}
	
	/** Calculate geometric mean */
	private double calcGMean(double[] array)  {
		double sum = 0;
		int n = array.length;
		for (int i = 0; i < n; ++i) {
			sum += Math.log(array[i]);
		}
		return Math.exp(sum/n);
	}
	
	/** Calculate variance */
	private double calcVariance(double[] array) {
		double mean = calcMean(array);
		double temp = 0;
		for (double a : array) {
			temp += (mean-a)*(mean-a);
		}
		return temp/array.length;
	}
	
	/** Calculate standard deviation */
	private double calcStdDev(double[] array) {
		return Math.sqrt(calcVariance(array));
	}
}

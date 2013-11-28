package com.mightyknob.server.audio;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

/**  
 * @author Gordan Kreković
 * @version 1.0.0
 */
public class FeatureExtraction {
	int blockSize;
	int stepSize;
	float sampleRate;
	boolean extractAll;
	final int numberOfHarmonics = 10;
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
			double centroidMean = calcMean(centroid);
			if (extractAll || targetVector.centroidMean >= 0) vector.setCentroidMean(centroidMean);
			if (extractAll || targetVector.centroidStddev >= 0) vector.setCentroidStddev(calcStdDev(centroid)/centroidMean);
		}

		// Calculate spectral flux and its statistical features
		if (extractAll || targetVector.fluxMean >= 0 || targetVector.fluxStddev >= 0) {
			flux = spectralFlux(spectrum);
			double fluxMean = calcMean(flux);
			if (extractAll || targetVector.fluxMean >= 0)	vector.setFluxMean(fluxMean);
			if (extractAll || targetVector.fluxStddev >= 0) vector.setFluxStddev(calcStdDev(flux)/fluxMean);
		}

		// Calculate spectral flatness and its statistical features	
		if (extractAll || targetVector.flatnessMean >= 0 || targetVector.flatnessStddev >= 0) {
			flatness = spectralFlatness(spectrum);
			double flatnessMean = calcMean(flatness);
			if (extractAll || targetVector.flatnessMean >= 0) vector.setFlatnessMean(flatnessMean);
			if (extractAll || targetVector.flatnessStddev >= 0) vector.setFlatnessStddev(calcStdDev(flatness)/flatnessMean);
		}
			
		
		// Calculate temporal features
		if (extractAll || targetVector.attackTime >= 0 || targetVector.sustainTime >= 0  || targetVector.decayTime >= 0) {
			double[] envelope = new double[signal.length];
			envelope = amplitudeEnvelope(signal);
			int attackTime = calculateAttackTime(envelope);
			int sustainTime = calculateSustainTime(envelope, attackTime);
			int decayTime = calculateDecayTime(envelope, attackTime+sustainTime);
			double l =  (2*(double) envelope.length/3);
			if (extractAll || targetVector.attackTime >= 0) vector.setAttackTime(attackTime / l);			
			if (extractAll || targetVector.sustainTime >= 0) vector.setSustainTime(sustainTime / l);
			if (extractAll || targetVector.decayTime >= 0) vector.setDecayTime(decayTime / l);						
		}
		
		// Calculate pitch and harmonic ratios
		if (extractAll || targetVector.pitchMean >=0 || targetVector.pitchStddev >=0 
				|| targetVector.harmonicsEvenRatio >= 0 || targetVector.harmonicsOddRatio >= 0) {
			double[] pitch = new double[effectiveNumberOfBlocks];
			pitch = estimatePitch(signal, effectiveLength);
			if (extractAll || targetVector.pitchMean >= 0) vector.setPitchMean(calcMean(pitch));
			if (extractAll || targetVector.pitchStddev >= 0) vector.setPitchStddev(calcStdDev(pitch));
			if (targetVector.harmonicsEvenRatio >= 0 || targetVector.harmonicsOddRatio >= 0) {
				double[] ratios = new double[2]; 
				ratios = harmonicRatios(harmonics(spectrum, pitch));
				vector.setHarmonicsEvenRatio(ratios[0]);
				vector.setHarmonicsOddRatio(ratios[1]);
			}
		}
		return vector;
	}

	/** Determine which is the last non-zero sample in the signal */
	private int lastIndexBeforeZeros(float signal[]) {
		int n = signal.length-1;
		while (signal[n] < 0.02 && n>0) n--;
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
	
	/** Estimate the fundamental frequency using the YIN algorithm */
	private double[] estimatePitch(float signal[], int effectiveLength) {
		int numberOfBlocks = (effectiveLength-blockSize)/stepSize+1;
		double[] result = new double[numberOfBlocks];
		float [] block = new float[blockSize];
		FastYin yinObj = new FastYin(sampleRate, blockSize);
		
		int k = 0;
		for (int i=0; (i + blockSize) <= effectiveLength; i+=stepSize) {
			for (int j=0; j<blockSize; ++j) {
				block[j] = signal[i+j];
			}
			result[k++] = yinObj.getPitch(block);
		}
		return result;
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
	private double[] amplitudeEnvelope(float s[]) {
		int n = s.length;
		double[] signal = new double[n];	
		for (int i = 0; i < n; ++i) {
			signal[i] = Math.abs(s[i]);
		}
		
		// Generate the Gaussian window in the time domain
		GaussianWindow window = new GaussianWindow(2*blockSize, 0.4);
		
		// Convolve
		Convolution convolution = new Convolution(signal, window.window);
		int l = convolution.getFrameSize();
		double[] result = new double[l];
		convolution.computeResult(result);	
		
		// Normalize
		double maxR = 0;
		for (int i = 0; i < l; ++i) if (result[i] > maxR) maxR = result[i];
		for (int i = 0; i < l; ++i) result[i] = result[i]/maxR;
		
		return result;
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
	
	/** Returns the amplitudes of harmonics */
	private double[][] harmonics(double[][] spectrum, double[] pitch) {
		// GaussianWindow window = new GaussianWindow(20, 0.4);
		double[][] harmonics = new double[pitch.length][numberOfHarmonics];
		for (int i = 0; i < pitch.length; ++i) {
			for (int j = 0; j < numberOfHarmonics; ++j) {
				if (pitch[i] < 0) {
					harmonics[i][j] = -1;
				} else {
					int f = (int) Math.round((pitch[i]*(j+1)*blockSize/(2*sampleRate)));
					/* System.out.println("f = " + f + " pitch = " + pitch[i] + " bsize = " + blockSize);
					if (f+10 < blockSize/2) {
						double sum = 0;
						for (int k = 0; k < 20; ++k)
							sum += spectrum[i][f+k-10] * window.window[k];
						harmonics[i][j] = sum;
					} else {
						harmonics[i][j] = 0;
					} */
					if (f < blockSize/2) harmonics[i][j] = spectrum[i][f];
					else harmonics[i][j] = 0;
				}
			}
		}
		return harmonics;
	}
	
	/** Returns the even and odd harmonic ratios */
	private double[] harmonicRatios(double[][] harmonics) {
		double oddHarmonics = 0;
		double evenHarmonics = 0;

		double evenRatio = 0;
		double oddRatio = 0;
		int pitchedBlocks = 0;
		
		for (int i = 0; i < harmonics.length; ++i) {
			if (harmonics[i][0] >= 0) {
				oddHarmonics = 0;
				evenHarmonics = 0;
				for (int j = 0; j < numberOfHarmonics; ++j) {
					if (j % 2 == 1) oddHarmonics += harmonics[i][j];
					else evenHarmonics += harmonics[i][j];
				}
				oddRatio += oddHarmonics/(evenHarmonics+oddHarmonics);
				evenRatio += evenHarmonics/(evenHarmonics+oddHarmonics);
				pitchedBlocks++;
			}
		}
		
		double[] result = new double[2];
		if (pitchedBlocks > 2*harmonics.length/3) {
			result[0] = evenRatio/pitchedBlocks;
			result[1] = oddRatio/pitchedBlocks;
		} else {
			result[0] = 2;
			result[1] = 2;
		}
		
		return result;
	}	
	
	
	/** Calculate tristimulus 1, 2 and 3 */
	@SuppressWarnings("unused")
	private double[] tristimulus(double[][] harmonics) {
		double tristimulus1 = 0;
		double tristimulus2 = 0;
		double tristimulus3 = 0;
		int pitchedBlocks = 0;
		
		for (int i = 0; i < harmonics.length; ++i) {
			if (harmonics[i][0] >= 0) {
				double sum = 0;
				double temp = 0;
				for (int j = 0; j < numberOfHarmonics; ++j) {
					sum += harmonics[i][j];
					if (j > 3) temp += harmonics[i][j];
				}
				tristimulus1 += harmonics[i][0] / sum;
				tristimulus2 += (harmonics[i][1] + harmonics[i][2] + harmonics[i][3]) / sum;
				tristimulus3 += temp / sum;
				pitchedBlocks++;
			}
		}
		double[] result = new double[3];
		result[0] = tristimulus1/pitchedBlocks;
		result[1] = tristimulus2/pitchedBlocks;
		result[2] = tristimulus3/pitchedBlocks;
		
		return result;
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

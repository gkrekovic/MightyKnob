package com.mightyknob.server.audio;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

public class FeatureExtraction {
	
	int blockSize;
	int stepSize;
	float sampleRate;
	boolean extractAll;
	StandardFeatureVector targetVector;
	
	public FeatureExtraction(int blockSize, int stepSize, float sampleRate) {
		this.blockSize = blockSize;
		this.stepSize = stepSize;
		this.sampleRate = sampleRate;
		extractAll = true;
	}
	
	
	public FeatureExtraction(int blockSize, int stepSize, float sampleRate, StandardFeatureVector targetVector) {
		this.blockSize = blockSize;
		this.stepSize = stepSize;
		this.sampleRate = sampleRate;
		this.targetVector = targetVector;
		extractAll = false;
	}
	
	/**
	 * @param signal - samples of the audio signal ranging from -1 to 1
	 * @return Values of audio features extracted from the signal.
	 */
	public StandardFeatureVector extractFeatures(float signal[]) {
		// int numberOfBlocks = (signal.length-blockSize)/stepSize+1;
		
		// For spectral-based features ending zeros in the signal are ignored. For that reason
		// we need to calculate effective length of the signal and the number of blocks.
		int effectiveLength = lastIndexBeforeZeros(signal);
		int effectiveNumberOfBlocks = (effectiveLength-blockSize)/stepSize+1;
		
		double[][] spectrum = new double[effectiveNumberOfBlocks][blockSize/2];
		double[] centroid = new double[effectiveNumberOfBlocks];
		double[] flux = new double[effectiveNumberOfBlocks];
		double[] flatness = new double[effectiveNumberOfBlocks];
		
		spectrum = powerSpectrum(signal, effectiveLength);
		centroid = spectralCentroid(spectrum);
		flux = spectralFlux(spectrum);
		flatness = spectralFlatness(spectrum);
		
		StandardFeatureVector vector = new StandardFeatureVector(sampleRate);
		
		if (extractAll || targetVector.centroidMean != -1)
			vector.setCentroidMean(calcMean(centroid));
			
		if (extractAll || targetVector.centroidStddev != -1)
			vector.setCentroidStddev(calcStdDev(centroid));
		
		if (extractAll || targetVector.fluxMean != -1)
			vector.setFluxMean(calcMean(flux));
		
		if (extractAll || targetVector.fluxStddev != -1)
			vector.setFluxStddev(calcStdDev(flux));
		
		if (extractAll || targetVector.flatnessMean != -1)
			vector.setFlatnessMean(calcMean(flatness));
		
		if (extractAll || targetVector.flatnessStddev != -1)
			vector.setFlatnessStddev(calcStdDev(flatness));
		
		return vector;
	}
	
	private int lastIndexBeforeZeros(float signal[]) {
		int n = signal.length-1;
		while (signal[n] < 0.08 && n>0) n--;
		return n+1;
	}
	
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
			
			for (int j=0; j<blockSize/2; ++j) {
				spectrum[counter][j] = block[j*2]*block[j*2]+block[j*2+1]*block[j*2+1];
			}
			counter++;
		}
		return spectrum;
	}
	
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
				weightedTotal += (2*sampleRate/blockSize)*j*spectrum[i][j];
			}
			
			if (total != 0) {
				centroid[i] = weightedTotal/total;
			} else {
				centroid[i] = 0;
			}
		}
		return centroid;
	}
	
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
			flux[i] = sum;
		}
		
		return flux;
	}
	
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
			// if (mean == 0) System.out.println("i = " + i + " numberOfBlocks = " + numberOfBlocks);
		}
		return flatness;
	}
	
	private double calcMean(double[] array) {
		double sum = 0;
		for (double a : array) {
			sum += a;
		}
		return sum/array.length;
	}
	
	private double calcGMean(double[] array)  {
		double sum = 0;
		int n = array.length;
		for (int i = 0; i < n; ++i) {
			sum += Math.log(array[i]);
		}
		return Math.exp(sum/n);
	}
	
	private double calcVariance(double[] array) {
		double mean = calcMean(array);
		double temp = 0;
		for (double a : array) {
			temp += (mean-a)*(mean-a);
		}
		return temp/array.length;
	}
	
	private double calcStdDev(double[] array) {
		return Math.sqrt(calcVariance(array));
	}
}

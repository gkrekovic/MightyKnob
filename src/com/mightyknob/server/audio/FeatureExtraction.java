package com.mightyknob.server.audio;

import java.util.ArrayList;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;



public class FeatureExtraction {
	
	int blockSize;
	int stepSize;
	float sampleRate;
	
	public FeatureExtraction(int blockSize, int stepSize, float sampleRate) {
		this.blockSize = blockSize;
		this.stepSize = stepSize;
		this.sampleRate = sampleRate;		
	}
	
	public ArrayList<Double> extractFeatures(float signal[]) {
		int effectiveLength = lastIndexBeforeZeros(signal);
		
		int numberOfBlocks = (signal.length-blockSize)/stepSize+1;
		int effectiveNumberOfBlocks = (effectiveLength-blockSize)/stepSize+1;
		
		// System.out.println("Eln = " + effectiveLength + " Rln = " + signal.length + " ENb = " + effectiveNumberOfBlocks + " RalNumB = " + numberOfBlocks);
		
		double[][] spectrum = new double[effectiveNumberOfBlocks][blockSize/2];
		double[] centroid = new double[effectiveNumberOfBlocks];
		double[] flux = new double[effectiveNumberOfBlocks];
		double[] flatness = new double[effectiveNumberOfBlocks];
		
		spectrum = powerSpectrum(signal, effectiveLength);
		centroid = spectralCentroid(spectrum);
		flux = spectralFlux(spectrum);
		flatness = spectralFlatness(spectrum);
		
		ArrayList<Double> result = new ArrayList<Double>();
		
		result.add(calcMean(centroid));
		result.add(calcStdDev(centroid));
		result.add(calcMean(flux));
		result.add(calcStdDev(flux));
		result.add(calcMean(flatness));
		result.add(calcStdDev(flatness));
	
		return result;
	}
	
	private int lastIndexBeforeZeros(float signal[]) {
		int n = signal.length-1;
		while (signal[n] == 0 && n>0) n--;
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
			for (int j=0; j<blockSize/2; ++j) {
				double difference;
				difference = spectrum[i][j] - spectrum[i-1][j];
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
			flatness[i] = gmean / mean;
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
		for (double a : array) {
			sum += Math.log(a);
		}
		return Math.exp(sum/array.length);
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

package com.mightyknob.server.audio;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D;



public class FeatureExtraction {
	
	int blockSize;
	int stepSize;
	float sampleRate;
	
	public FeatureExtraction(int blockSize, int stepSize, float sampleRate) {
		this.blockSize = blockSize;
		this.stepSize = stepSize;
		this.sampleRate = sampleRate;
		
	}
	
	public float extractFeatures(float signal[]) {
		int numberOfBlocks = (signal.length-blockSize)/stepSize+1;
		float[][] spectrum = new float[numberOfBlocks][blockSize/2];
		float[] centroid = new float[numberOfBlocks];
		float[] flux = new float[numberOfBlocks];
		
		spectrum = powerSpectrum(signal);
		centroid = spectralCentroid(spectrum);
		flux = spectralFlux(spectrum);
		
		return calcMean(centroid);
	}
	
	public float[][] powerSpectrum(float signal[]) {
		int numberOfBlocks = (signal.length-blockSize)/stepSize+1;
		FloatFFT_1D FFTObj = new FloatFFT_1D(blockSize);
		float[][] spectrum = new float[numberOfBlocks][blockSize/2];
		
		int signalSize = signal.length;
		float[] block = new float[blockSize];

		int counter=0;
		
		for (int i=0; (i + blockSize) < signalSize; i+=stepSize) {
			for (int j=0; j<blockSize; ++j) {
				block[j] = signal[i+j];
				// if (counter==2) System.out.println(block[j]);
			}
			FFTObj.realForward(block);
			
			for (int j=0; j<blockSize/2; ++j) {
				spectrum[counter][j] = block[j*2]*block[j*2]+block[j*2+1]*block[j*2+1];
			}
			counter++;
		}
		return spectrum;
	}
	
	public float[] spectralCentroid(float spectrum[][]) {
		int numberOfBlocks = spectrum.length;
		float total = 0;
		float weightedTotal = 0;
		float[] centroid = new float[numberOfBlocks];
		
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
	
	private float[] spectralFlux(float spectrum[][]) {
		int numberOfBlocks = spectrum.length;
		float[] flux = new float [numberOfBlocks];
		flux[0] = 0;
		
		for (int i=1; i<numberOfBlocks; ++i) {
			float sum = 0;
			for (int j=0; j<blockSize/2; ++j) {
				float difference;
				difference = spectrum[i][j] - spectrum[i-1][j];
				sum += (difference*difference);
			}
			flux[i] = sum;
		}
		
		return flux;
	}
	
	private float calcMean(float[] array) {
		float sum = 0;
		for (float a : array) {
			sum += a;
		}
		return sum/array.length;
	}
	
	
	private float calcVariance(float[] array) {
		float mean = calcMean(array);
		float temp = 0;
		for (float a : array) {
			temp += (mean-a)*(mean-a);
		}
		return temp/array.length;
	}
	
	private float calcStdDev(float[] array) {
		return (float) Math.sqrt(calcVariance(array));
	}
}

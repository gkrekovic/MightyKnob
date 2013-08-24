package com.mightyknob.server.ga;

import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

import org.uncommons.watchmaker.framework.FitnessEvaluator;

import com.mightyknob.server.audio.FeatureExtraction;
import com.mightyknob.server.audio.FeatureVector;
import com.mightyknob.server.audio.Synth;
import com.synthbot.audioplugin.vst.vst2.JVstHost2;

public class PatchEvaluator implements FitnessEvaluator<Patch> {

	final static double MAX_DISTANCE = 20000.0;
	private JVstHost2 vst;
	FeatureVector targetVector;
	
	public PatchEvaluator(JVstHost2 vst, FeatureVector targetVector) {
		this.vst = vst;
		this.targetVector = targetVector;
	}
	
	@Override
	public double getFitness(Patch candidate, List<? extends Patch> population) {
		int n = 88200;
		
		int blockSize = vst.getBlockSize();
		int stepSize = blockSize;
		float sampleRate = vst.getSampleRate();
	
		float [] signal = new float[n];

		Synth synth = new Synth(vst);
		try {
			signal = synth.synthesize(candidate, n);
		} catch (Exception e) {
			System.err.println("Exception: " + e.getMessage());
			return 20000;
		}
			
		FeatureExtraction Extractor = new FeatureExtraction(blockSize, stepSize, sampleRate);
		FeatureVector features = new FeatureVector(sampleRate);
		features = Extractor.extractFeatures(signal);
		return distance(features);
	}

	private double distance(FeatureVector candidateVector)  {
		int vectorSize = candidateVector.getSize();
		if (vectorSize != targetVector.getSize()) return MAX_DISTANCE;
		double d = 0;
		
		double[] candidateFeatures = candidateVector.getNormalizedFeatures();
		double[] targetFeatures = targetVector.getNormalizedFeatures();
		for (int i = 0; i < vectorSize; ++i) {
			d += Math.abs(candidateFeatures[i] - targetFeatures[i]);
		}
		return d/vectorSize;
	}
	
	@Override
	public boolean isNatural() {
		return false;
	}

}

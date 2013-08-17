package com.mightyknob.server.ga;

import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

import org.uncommons.watchmaker.framework.FitnessEvaluator;

import com.mightyknob.server.audio.FeatureExtraction;
import com.mightyknob.server.audio.Synth;
import com.synthbot.audioplugin.vst.vst2.JVstHost2;

public class PatchEvaluator implements FitnessEvaluator<Patch> {

	final static double MAX_DISTANCE = 20000.0;
	private JVstHost2 vst;
	ArrayList<Double> targetVector;
	
	public PatchEvaluator(JVstHost2 vst, ArrayList<Double> targetVector) {
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
		ArrayList<Double> features = Extractor.extractFeatures(signal);
		return distance(features);
	}

	private double distance(ArrayList<Double> candidateVector)  {
		if (candidateVector.size() != targetVector.size()) return MAX_DISTANCE;
		double d = 0;
		for (int i = 0; i < candidateVector.size(); ++i) {
			d += Math.abs(candidateVector.get(i) - targetVector.get(i));
		}
		return d;
	}
	
	@Override
	public boolean isNatural() {
		return false;
	}

}

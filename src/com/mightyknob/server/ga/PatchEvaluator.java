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

	private JVstHost2 vst;
	
	public PatchEvaluator(JVstHost2 vst) {
		this.vst = vst;
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
		float centroid;
		centroid = Extractor.extractFeatures(signal);
		/* System.out.println("Centroid: " + centroid);
		
		if (Float.isNaN(centroid)) {
			for (float p : parameters) {
				System.out.println(p);
			}			
		} */
		
		return Math.abs(4800-centroid);
	}

	@Override
	public boolean isNatural() {
		return false;
	}

}

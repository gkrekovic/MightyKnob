package com.mightyknob.server.tools;

import java.util.ArrayList;
import java.util.Collection;

import com.mightyknob.server.audio.FeatureExtraction;
import com.mightyknob.server.audio.FeatureVector;
import com.mightyknob.server.audio.Synth;
import com.mightyknob.server.ga.Patch;
import com.mightyknob.server.ga.PatchFactory;
import com.synthbot.audioplugin.vst.vst2.JVstHost2;

public class PresetAnalyzer {

	JVstHost2 vst;
	
	public PresetAnalyzer(JVstHost2 vst) {
		this.vst = vst;
	}
	
	public void analyzePresets() {
		int n = 88200;
		int blockSize = vst.getBlockSize();
		int stepSize = blockSize;
		float sampleRate = vst.getSampleRate();
		float [] signal = new float[n];
		
		PatchFactory factory = new PatchFactory(vst);
		Collection<Patch> seedCandidates = new ArrayList<Patch>();
		seedCandidates = factory.generateSeedCandidates();

		
		int i=0;
		for(Patch preset:seedCandidates) {
			Synth synth = new Synth(vst);
			try {
				signal = synth.synthesize(preset, n);
			} catch (Exception e) {
				System.err.println("Exception: " + e.getMessage());
			}

			FeatureExtraction Extractor = new FeatureExtraction(blockSize, stepSize, sampleRate);
			double[] features = Extractor.extractFeatures(signal).getNormalizedFeatures();
			
			for (double feature : features) {
				System.out.print(feature + ", ");
			}
			System.out.println("preset" + i);

			new Synth(vst).preview(preset, "preset" + i +".wav");
			i++;
		}
	}
}

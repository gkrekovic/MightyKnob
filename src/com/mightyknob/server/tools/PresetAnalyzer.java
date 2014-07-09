package com.mightyknob.server.tools;

import java.util.ArrayList;
import java.util.Collection;

import com.mightyknob.server.audio.FeatureExtraction;
import com.mightyknob.server.audio.NormalizedFeatureVector;
import com.mightyknob.server.audio.StandardFeatureVector;
import com.mightyknob.server.audio.Synth;
import com.mightyknob.server.ga.Patch;
import com.synthbot.audioplugin.vst.vst2.JVstHost2;

/**  
 * @author Gordan Kreković
 * @version 1.0.0
 */
public class PresetAnalyzer {
	JVstHost2 vst;
	
	final double MAX_DISTANCE = 1;
	
	public PresetAnalyzer(JVstHost2 vst) {
		this.vst = vst;
	}
	
	public Collection<NormalizedFeatureVector> extractFeaturesFromPresets() {
		int n = 132300;
		int m = 88200;
		int blockSize = vst.getBlockSize();
		int stepSize = blockSize;
		float sampleRate = vst.getSampleRate();
		float [] signal = new float[n];
		
		Collection<Patch> seedCandidates = new ArrayList<Patch>();
		seedCandidates = generatePresets();
		
		Collection<NormalizedFeatureVector> featureVectors = new ArrayList<NormalizedFeatureVector>();
		
		for(Patch preset:seedCandidates) {
			Synth synth = new Synth(vst);
			try {
				signal = synth.synthesize(preset, n, m);
			} catch (Exception e) {
				System.err.println("Exception: " + e.getMessage());
			}

			NormalizedFeatureVector targetVector = new NormalizedFeatureVector();
			// define which features have to be calculated
			targetVector.setCentroidMean(0);
			targetVector.setCentroidStddev(0);
			targetVector.setFlatnessMean(0);
			targetVector.setFlatnessMean(0);
			targetVector.setFluxMean(0);
			targetVector.setAttackTime(0);
			targetVector.setDecayTime(0);
			targetVector.setSustainTime(0);
			targetVector.setHarmonicsOddRatio(0);
			
			FeatureExtraction Extractor = new FeatureExtraction(blockSize, stepSize, sampleRate, targetVector);
			StandardFeatureVector featureVector = Extractor.extractFeatures(signal);
			
			featureVectors.add(featureVector.getNormalizedFeatureVector());
		}
		return featureVectors;
	}
	
	public void analyzePresets() {
		Collection<NormalizedFeatureVector> featureVectors = extractFeaturesFromPresets();
		for (NormalizedFeatureVector featureVector : featureVectors) {
			double[] features = featureVector.getFeatures();
			for (int j = 0; j < featureVector.getSize(); ++j) {
				if (features[j] != -1) System.out.print(features[j] + ", ");
			}
			System.out.println();
		}
	}
	
	public ArrayList<Patch> generatePresets() {
		ArrayList<Patch> seedCandidates = new ArrayList<Patch>();
		int numPrograms = vst.numPrograms();
		int numParameters = vst.numParameters();

		for (int i = 0; i < numPrograms; ++i) {
			vst.setProgram(i);
			ArrayList<Float> parameters = new ArrayList<Float>(numPrograms);
			for (int j = 0; j < numParameters; ++j) {
				parameters.add(vst.getParameter(j));
			}
			seedCandidates.add(new Patch(parameters, "preset" + i));
		}
		return seedCandidates;
	}
}

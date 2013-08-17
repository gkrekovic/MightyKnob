package com.mightyknob.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Properties;

import javax.sound.midi.ShortMessage;

import com.mightyknob.server.audio.FeatureExtraction;
import com.synthbot.audioio.vst.JVstAudioThread;
import com.synthbot.audioplugin.vst.JVstLoadException;
import com.synthbot.audioplugin.vst.vst2.JVstHost2;


/* import jAudioFeatureExtractor.Aggregators.Aggregator;
import jAudioFeatureExtractor.Aggregators.AggregatorContainer;
import jAudioFeatureExtractor.AudioFeatures.*; */

public class Test {

	static JVstHost2 vst;
	static float sampleRate;
	static int blockSize; 
	static int stepSize;
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		Properties properties = new Properties();
		FileInputStream fis = new FileInputStream("properties"); 
		properties.load(fis);
		
		initVst(properties);
		
		stepSize = blockSize;
		int n = 88200;
		
		float[][] signalInput = new float[2][blockSize];
		float[][] signalOutput = new float[2][blockSize];
		
		vst.setProgram(0);
				
		ShortMessage midiMessage = new ShortMessage();
		midiMessage.setMessage(ShortMessage.NOTE_ON, 0, 60, 100);
		vst.queueMidiMessage(midiMessage);
		
		float [] signal = new float[n];

		int numberOfBlocks = (n-blockSize)/stepSize+1;
		// System.out.println(numberOfBlocks);
		for (int i=0; i<numberOfBlocks; ++i) {
			
			for (int j=0; j<blockSize; ++j) {
				signalInput[0][j] = 0;
				signalOutput[0][j] = 0;
				signalInput[1][j] = 0;
				signalOutput[1][j] = 0;	
			}
			
			vst.processReplacing(signalInput, signalOutput, blockSize);
		
			// Stereo to mono
			float maxSignal = 0;
			for (int j=0; j<blockSize; ++j) {
				int k = i*blockSize+j;
				signal[k] = signalOutput[0][j]+signalOutput[1][j];
				if (Math.abs(signal[k]) > maxSignal) maxSignal = Math.abs(signal[k]);
			}

			// Normalize
			for (int j=0; j<blockSize; ++j) {
				int k = i*blockSize+j;
				signal[k] = signal[k]/maxSignal;
			}

		}
		
		FeatureExtraction Extractor = new FeatureExtraction(blockSize, stepSize, sampleRate);
		
		ArrayList<Double> features = Extractor.extractFeatures(signal);
		double centroid;
		centroid = features.get(0);
		System.out.println(centroid);
		
	}
	
    private static void initVst(Properties properties) {
    	JVstAudioThread audioThread;
    	vst = null;
    	
		String vstFolder = properties.getProperty("vst_folder");
		String vstName = properties.getProperty("vst_name");
		File vstFile = new File(vstFolder+vstName);
		
		sampleRate = Integer.parseInt(properties.getProperty("sample_rate"));
		blockSize = Integer.parseInt(properties.getProperty("block_size"));
		final String AUDIO_THREAD = "Audio Thread";
				
		try {
			vst = JVstHost2.newInstance(vstFile, sampleRate, blockSize);
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace(System.err);
		} catch (JVstLoadException jvle) {
			jvle.printStackTrace(System.err);
		}
		
		// start the audio thread
	    audioThread = new JVstAudioThread(vst);
	    Thread thread = new Thread(audioThread);
	    thread.setName(AUDIO_THREAD); // for easy debugging
	    thread.setDaemon(true); // allows the JVM to exit normally
	    // thread.setPriority(Thread.MAX_PRIORITY);
	    thread.start();
    }

}

package com.mightyknob.server;

import java.io.File;
import java.io.FileNotFoundException;

import com.synthbot.audioplugin.vst.JVstLoadException;
import com.synthbot.audioplugin.vst.vst2.JVstHost2;

public class VstInitializer {
	/** 
	 * Initializes a VST synth using JVstHost2
	 * <p>
	 * Parameters are defined in the properties file. In this implementation the audio thread
	 * is not started, because it uses CPU time unnecessarily.
	 * */
	public JVstHost2 initialize(String vstFileName, float sampleRate, int blockSize) {
		JVstHost2 vst = null;
		File vstFile = new File(vstFileName);
		try {
			vst = JVstHost2.newInstance(vstFile, sampleRate, blockSize);
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace(System.err);
		} catch (JVstLoadException jvle) {
			jvle.printStackTrace(System.err);
		}
		return vst;
	}
}

package com.mightyknob.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import com.mightyknob.server.ga.GeneticAlgorithm;
import com.mightyknob.server.tools.PresetAnalyzer;
// import com.synthbot.audioio.vst.JVstAudioThread;
import com.synthbot.audioplugin.vst.JVstLoadException;
import com.synthbot.audioplugin.vst.vst2.JVstHost2;

public class MightyKnobServer {

	// private static final long serialVersionUID = 1L;
	static JVstHost2 vst;
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		Properties properties = new Properties();
		FileInputStream fis = new FileInputStream("properties"); 
		properties.load(fis);

		initVst(properties);
		
		GeneticAlgorithm ga = new GeneticAlgorithm(properties, vst);
		
		String fclFolder = properties.getProperty("fcl_folder");
		String fclFileName = properties.getProperty("fcl_name");
		ExpertSystem es = new ExpertSystem(fclFolder + fclFileName);
		
		try {
			ga.evolvePatch(es.evaluate());
		} catch (Exception e) {
			System.err.println(e);
		}

		if (properties.getProperty("preset_analyzer", "off").compareToIgnoreCase("on") == 0) {
			PresetAnalyzer analyzer = new PresetAnalyzer(vst);
			analyzer.analyzePresets(es.evaluate());
		}
	}
	
    private static void initVst(Properties properties) {
    	vst = null;
		String vstFolder = properties.getProperty("vst_folder");
		String vstName = properties.getProperty("vst_name");
		File vstFile = new File(vstFolder+vstName);
		
		final float SAMPLE_RATE = Integer.parseInt(properties.getProperty("sample_rate"));
		final int BLOCK_SIZE = Integer.parseInt(properties.getProperty("block_size"));
				
		try {
			vst = JVstHost2.newInstance(vstFile, SAMPLE_RATE, BLOCK_SIZE);
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace(System.err);
		} catch (JVstLoadException jvle) {
			jvle.printStackTrace(System.err);
		}
		
		// start the audio thread
	    /* 
	    JVstAudioThread audioThread;
	    final String AUDIO_THREAD = "Audio Thread";
	    audioThread = new JVstAudioThread(vst);
	    Thread thread = new Thread(audioThread);
	    thread.setName(AUDIO_THREAD); // for easy debugging
	    thread.setDaemon(true); // allows the JVM to exit normally
	    
	    thread.setPriority(Thread.MAX_PRIORITY);
	    thread.start(); */
    }

}

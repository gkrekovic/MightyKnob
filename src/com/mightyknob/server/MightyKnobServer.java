package com.mightyknob.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import com.mightyknob.server.ga.GeneticAlgorithm;
import com.synthbot.audioio.vst.JVstAudioThread;
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
		// ExpertSystem es = new ExpertSystem(properties);
		// runAlgorithm(ga, es);
		ga.evolvePatch();
	}
	
    /*
	private static void runAlgorithm(GeneticAlgorithm ga, ExpertSystem es) {
		
	} */
	
    private static void initVst(Properties properties) {
    	JVstAudioThread audioThread;
    	vst = null;
    	
		String vstFolder = properties.getProperty("vst_folder");
		String vstName = properties.getProperty("vst_name");
		File vstFile = new File(vstFolder+vstName);
		
		final float SAMPLE_RATE = Integer.parseInt(properties.getProperty("sample_rate"));
		final int BLOCK_SIZE = Integer.parseInt(properties.getProperty("block_size"));
		final String AUDIO_THREAD = "Audio Thread";
				
		try {
			vst = JVstHost2.newInstance(vstFile, SAMPLE_RATE, BLOCK_SIZE);
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
	    
	    int n = vst.numParameters();
	    for (int i = 1; i < n; ++i) {
	    	System.out.println(vst.getParameterName(i));
	    }
    }

}

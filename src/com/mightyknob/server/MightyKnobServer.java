package com.mightyknob.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import com.mightyknob.server.ga.GeneticAlgorithm;
import com.mightyknob.server.tools.PresetAnalyzer;
import com.synthbot.audioplugin.vst.vst2.JVstHost2;

/**  
 * @author Gordan Kreković
 * @version 1.0.0
 */
public class MightyKnobServer {
	static JVstHost2 vst;
	
	/**
	 * Initializes and starts the algorithm. 
	 * <p>
	 * This is the main method which reads the properties file, Initializes a VST synth,
	 * genetic algorithm, and expert system. The properties file name can be passed as a
	 * program argument.
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// Initialize properties
		Properties properties = new Properties();
		String propertiesFileName;
		if (args.length > 0) {
			propertiesFileName = args[0];
		} else {
			propertiesFileName = "properties";
		}
		FileInputStream fis = new FileInputStream(propertiesFileName); 
		properties.load(fis);
		
		// Initialize the VST synth
		String vstFolder = properties.getProperty("vst_folder");
		String vstName = properties.getProperty("vst_name");
		float sampleRate = Integer.parseInt(properties.getProperty("sample_rate"));
		int blockSize = Integer.parseInt(properties.getProperty("block_size"));
		vst = new VstInitializer().initialize(vstFolder+vstName, sampleRate, blockSize);
		
		// Initialize the genetic algorithm
		float mutationProbability = Float.parseFloat(properties.getProperty("mutation_probability", "0.2"));
		float maxMutation = Float.parseFloat(properties.getProperty("max_mutation", "0.2"));
		int populationSize = Integer.parseInt(properties.getProperty("population_size", "30"));
		int eliteCount = Integer.parseInt(properties.getProperty("elite_count", "5"));
		GeneticAlgorithm ga = new GeneticAlgorithm(mutationProbability, maxMutation, populationSize, eliteCount);
		
		// Initialize the expert system
		String fclFolder = properties.getProperty("fcl_folder");
		String fclFileName = properties.getProperty("fcl_name");
		ExpertSystem es = new ExpertSystem(fclFolder + fclFileName);
		
		// Initialize the algorithm
		ProcessInput algorithm = new ProcessInput(es, ga, vst);
		
		// Start the algorithm
		String inputFileFolder = properties.getProperty("input_file_folder");
		String inputFileName = properties.getProperty("input_file_name");
		algorithm.start(inputFileFolder + inputFileName);

		if (properties.getProperty("preset_analyzer", "off").compareToIgnoreCase("on") == 0) {
			PresetAnalyzer analyzer = new PresetAnalyzer(vst);
			analyzer.analyzePresets();
		}
	}
	

}

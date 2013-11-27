package com.mightyknob.server;

import com.mightyknob.server.ga.GeneticAlgorithm;
import com.synthbot.audioplugin.vst.vst2.JVstHost2;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class ProcessInput {
	ExpertSystem es;
	GeneticAlgorithm ga;
	JVstHost2 vst;
	
	public ProcessInput(ExpertSystem es, GeneticAlgorithm ga, JVstHost2 vst) {
		this.es = es;
		this.ga = ga;
		this.vst = vst;
	}
	
	public void start(String inputFile) {
		BufferedReader br = null;
		String line;
		String separator = ",";
		HashMap<String, Double> inputs = new HashMap<String, Double>();
		try {
			br = new BufferedReader(new FileReader(inputFile));
			line = br.readLine();
			String[] attributes = line.split(separator);
			int j=0;
			while ((line = br.readLine()) != null) {
				String[] values = line.split(separator);
				for (int i=0; i < values.length; ++i) {
					Double value;
					if (values[i].isEmpty() == true) value = -1.0;
					else value = Double.parseDouble(values[i]);
					inputs.put(attributes[i], value);
				}
				
				// Start the algorithm
				try {
					vst = new VstInitializer().initialize(vst.getPluginPath(), vst.getSampleRate(), vst.getBlockSize());
					ga.evolvePatch(es.evaluate(inputs), vst, String.format("sound%02d.wav", ++j));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}

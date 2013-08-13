package com.mightyknob.server.ga;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.mightyknob.server.audio.Synth;
import com.synthbot.audioplugin.vst.vst2.JVstHost2;

import org.uncommons.watchmaker.framework.*;
import org.uncommons.watchmaker.framework.operators.EvolutionPipeline;
import org.uncommons.watchmaker.framework.selection.RouletteWheelSelection;
import org.uncommons.watchmaker.framework.termination.Stagnation;
import org.uncommons.maths.random.MersenneTwisterRNG;

public class GeneticAlgorithm {
	private JVstHost2 vst;	
	private float mutationProbability;
	private float maxMutation;
	int populationSize;
	int eliteCount;
	
	public GeneticAlgorithm(Properties properties, JVstHost2 vst) {
		this.vst = vst;
		
		mutationProbability = Float.parseFloat(properties.getProperty("mutation_probability", "0.2"));
		maxMutation = Float.parseFloat(properties.getProperty("max_mutation", "0.2"));
		populationSize = Integer.parseInt(properties.getProperty("population_size", "30"));
		eliteCount = Integer.parseInt(properties.getProperty("elite_count", "5"));		
	}
	
	public void evolvePatch() {
		PatchFactory factory = new PatchFactory(vst.numParameters());
		List<EvolutionaryOperator<Patch>> operators = new ArrayList<EvolutionaryOperator<Patch>>(2);
		operators.add(new PatchMutation(mutationProbability, maxMutation));
		operators.add(new PatchCrossover());
		EvolutionaryOperator<Patch> pipeline = new EvolutionPipeline<Patch>(operators);
		
		EvolutionEngine<Patch> engine = new GenerationalEvolutionEngine<Patch>(factory,
				pipeline, new PatchEvaluator(vst), new RouletteWheelSelection(), new MersenneTwisterRNG());
		
		Patch p = new Patch();		
		p = engine.evolve(populationSize, eliteCount, new Stagnation(5, false));
		Synth synth = new Synth(vst);
		synth.preview(p);
		
	}
}

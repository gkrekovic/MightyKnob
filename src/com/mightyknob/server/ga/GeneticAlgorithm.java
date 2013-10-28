package com.mightyknob.server.ga;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import com.mightyknob.server.audio.FeatureVector;
import com.mightyknob.server.audio.Synth;
import com.synthbot.audioplugin.vst.vst2.JVstHost2;

import org.uncommons.watchmaker.framework.*;
import org.uncommons.watchmaker.framework.operators.EvolutionPipeline;
import org.uncommons.watchmaker.framework.selection.RouletteWheelSelection;
import org.uncommons.watchmaker.framework.termination.GenerationCount;
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
	
	public void evolvePatch(FeatureVector targetVector) {
		PatchFactory factory = new PatchFactory(vst);
		Collection<Patch> seedCandidates = new ArrayList<Patch>();
		seedCandidates = factory.generateSeedCandidates(populationSize);
		
		List<EvolutionaryOperator<Patch>> operators = new ArrayList<EvolutionaryOperator<Patch>>(2);
		operators.add(new PatchMutation(mutationProbability, maxMutation));
		operators.add(new PatchCrossover());
		EvolutionaryOperator<Patch> pipeline = new EvolutionPipeline<Patch>(operators);
		
		GenerationalEvolutionEngine<Patch> engine = new GenerationalEvolutionEngine<Patch>(factory,
				pipeline, new PatchEvaluator(vst, targetVector), new RouletteWheelSelection(), new MersenneTwisterRNG());
		
		engine.setSingleThreaded(true);
		
		engine.addEvolutionObserver(new EvolutionLogger<Patch>());
		System.out.println("EC = " + eliteCount);

		// Collection<EvaluatedCandidate<Patch>> results = new ArrayList<EvaluatedCandidate<Patch>>();
		// results = engine.evolvePopulation(populationSize, eliteCount, /* seedCandidates, */ new GenerationCount(10));
		
		Patch p = new Patch();
		p = engine.evolve(populationSize, eliteCount, seedCandidates, new GenerationCount(15));
		
		Synth synth = new Synth(vst);
		synth.preview(p);
	}
}

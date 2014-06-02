package com.mightyknob.server.ga;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import com.mightyknob.server.audio.NormalizedFeatureVector;
import com.mightyknob.server.audio.Synth;
import com.synthbot.audioplugin.vst.vst2.JVstHost2;
import org.uncommons.watchmaker.framework.*;
import org.uncommons.watchmaker.framework.operators.EvolutionPipeline;
import org.uncommons.watchmaker.framework.selection.RouletteWheelSelection;
import org.uncommons.watchmaker.framework.termination.GenerationCount;
import org.uncommons.maths.random.MersenneTwisterRNG;

/**  
 * @author Gordan Kreković
 * @version 1.0.0
 */
public class GeneticAlgorithm {
	private float mutationProbability;
	private float maxMutation;
	int populationSize;
	int eliteCount;
	int generationCount;

	public GeneticAlgorithm(float mutationProbability, float maxMutation, int populationSize, int eliteCount, int generationCount) {
		this.mutationProbability = mutationProbability;
		this.maxMutation = maxMutation;
		this.populationSize = populationSize;
		this.eliteCount = eliteCount;
		this.generationCount = generationCount;
	}
	
	/** Run the genetic algorithm
	 * 
	 * @param targetVector - target normalized values of audio features
	 */
	public void evolvePatch(NormalizedFeatureVector targetVector, JVstHost2 vst, String soundName) {
		// TODO Reset VST to have original presets in the initial population
		PatchFactory factory = new PatchFactory(vst);

		// Prepare seed candidates which will be existing presets in the VST synth
		Collection<Patch> seedCandidates = new ArrayList<Patch>();
		seedCandidates = factory.generateSeedCandidates(populationSize);
		
		// Prepare evolutionary operators
		List<EvolutionaryOperator<Patch>> operators = new ArrayList<EvolutionaryOperator<Patch>>(2);
		operators.add(new PatchMutation(mutationProbability, maxMutation));
		operators.add(new PatchCrossover());
		EvolutionaryOperator<Patch> pipeline = new EvolutionPipeline<Patch>(operators);
		
		// Prepare and evolve
		GenerationalEvolutionEngine<Patch> engine = new GenerationalEvolutionEngine<Patch>(factory,
				pipeline, new PatchEvaluator(vst, targetVector), new RouletteWheelSelection(), new MersenneTwisterRNG());
		engine.setSingleThreaded(true);
		engine.addEvolutionObserver(new EvolutionLogger<Patch>(generationCount));		
		Patch p = new Patch();
		p = engine.evolve(populationSize, eliteCount, seedCandidates, new GenerationCount(generationCount));
		
		// Synthesize the best candidate
		// Synth synth = new Synth(vst);
		// synth.preview(p, soundName);
	}
	
	public void evolvePatch(NormalizedFeatureVector targetVector, JVstHost2 vst) {
		evolvePatch(targetVector, vst, "temp.wav");
	}
}

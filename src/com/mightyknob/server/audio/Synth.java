package com.mightyknob.server.audio;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

import com.mightyknob.server.ga.Patch;
import com.synthbot.audioplugin.vst.vst2.JVstHost2;

/**  
 * @author Gordan Kreković
 * @version 1.0.0
 */
public class Synth {
	JVstHost2 vst;
	
	public Synth(JVstHost2 vst) {
		this.vst = vst;
	}
	
	/**
	 * Synthesizes <i>n</i> audio samples for the <i>candidate</i> patch. All <i>n</i>
	 * samples represent the part of the signal when the note is pressed.
	 * 
	 * @param candidate
	 * @param n - the total number of synthesized samples
	 * @return
	 * @throws Exception
	 */
	public float[] synthesize(Patch candidate, int n) throws Exception {
		return synthesize(candidate, n, n);
	}
	
	/**
	 * Synthesizes <i>n</i> audio samples for the <i>candidate</i> patch. The first
	 * <i>m</i> samples represent the part of the signal when the note is pressed. After
	 * that period, the note is released, so the sound generally decays.
	 * 
	 * @param candidate
	 * @param n - the total number of synthesized samples
	 * @param m - the number of samples when the note is pressed
	 * @return
	 * @throws Exception
	 */
	public float[] synthesize(Patch candidate, int n, int m) throws NumberFormatException {
		if (m > n) throw new IllegalArgumentException("The total number n should be greater or equal than the number m.");
		
		// Set parameters to the VST synth
		ArrayList<Float> parameters = candidate.getParameters();
		int counter=0;
		for (float p : parameters) {
			vst.setParameter(counter++, p);
		}
		
		// Prepare arrays and limits
		int blockSize = vst.getBlockSize();
		int totalNumberOfBlocks = n/blockSize;
		int noteNumberOfBlocks = m/blockSize;
		float[] signal = new float[n];
		
		// These two arrays are used for the processReplacing method
		float[][] signalInput = new float[2][blockSize];
		float[][] signalOutput = new float[2][blockSize];

		// Queue the note
		int noteNumber = 50;
		queueNote(noteNumber, ShortMessage.NOTE_ON);
		boolean noteOn = true;
		
		// This will be used for normalization
		float maxSignal = 0;

		// Process block by block
		for (int i=0; i<totalNumberOfBlocks; ++i) {	
			
			// TODO: Is there a better way to clean the array?
			for (int j=0; j<blockSize; ++j) {
				signalInput[0][j] = 0;
				signalOutput[0][j] = 0;
				signalInput[1][j] = 0;
				signalOutput[1][j] = 0;	
			}
			vst.processReplacing(signalInput, signalOutput, blockSize);
			
			// Note off if the first m samples are finished
			if (noteOn && (i >= noteNumberOfBlocks)) {
				noteOn = false;
				queueNote(noteNumber, ShortMessage.NOTE_OFF);
			}
		
			// Stereo to mono
			for (int j=0; j<blockSize; ++j) {
				int k = i*blockSize+j;
				signal[k] = signalOutput[0][j]+signalOutput[1][j];
				if (Float.isNaN(signal[k])) {
					throw new NumberFormatException("NaN value occurred among samples. Index: " + k);
				}
				if (Math.abs(signal[k]) > maxSignal) maxSignal = Math.abs(signal[k]);
			}
		}

		// Normalize
		for (int i=0; i<signal.length; ++i) {
			signal[i] = signal[i]/maxSignal;
		}
		
		return signal;
	}
	
	private void queueNote(int midiNoteNumber, int command) {
		ShortMessage midiMessage = new ShortMessage();
		try {
			midiMessage.setMessage(command, 0, midiNoteNumber, 100);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
		vst.queueMidiMessage(midiMessage);
	}
	
	/**
	 * Synthesized the audio signal and saves it in a wav file. The total duration of the audio
	 * is 3 seconds, whilst the note is pressed for the first 2 seconds.
	 * 
	 * @param candidate
	 * @param fileName
	 */
	public void preview(Patch candidate, String fileName) {
		int n = 132300;
		int m = 88200;
		float [] signal = new float[n];

		Synth synth = new Synth(vst);
		try {
			signal = synth.synthesize(candidate, n, m);
		} catch (Exception e) {
			System.err.println("Exception: " + e.getMessage());
			return;
		}
		saveToFile(signal, fileName);	
	}

	/**
	 * Synthesized the audio signal and saves it as temp.wav. The total duration of the audio
	 * is 3 seconds, whilst the note is pressed for the first 2 seconds.
	 * 
	 * @param candidate
	 * @param fileName
	 */
	public void preview(Patch candidate) {
		preview(candidate, "temp.wav");
	}
	
	private void saveToFile(float[] signal, String fileName) {
		short myFormat = 1;
		short myChannels = 1;
		short myBitsPerSample = 16;
		int mySampleRate = (int) vst.getSampleRate();
		int myByteRate = mySampleRate * myChannels * myBitsPerSample/8;
		short myBlockAlign = (short) (myChannels * myBitsPerSample/8);
		
		int myDataSize = 2 * signal.length;
		int mySubChunk1Size = 16;
		int myChunkSize = 20 + mySubChunk1Size + myDataSize;
		
		byte[] myData = new byte[myDataSize];	
		myData = floatArrayToByteArray(signal);
		
		try {
			DataOutputStream outFile = new DataOutputStream(new FileOutputStream(fileName));
            outFile.writeBytes("RIFF");                 // 00 - RIFF
            outFile.write(intToByteArray((int) myChunkSize), 0, 4);     // 04 - how big is the rest of this file?
            outFile.writeBytes("WAVE");                 // 08 - WAVE
            outFile.writeBytes("fmt ");                 // 12 - fmt
            outFile.write(intToByteArray((int) mySubChunk1Size), 0, 4); // 16 - size of this chunk
            outFile.write(shortToByteArray((short) myFormat), 0, 2);        // 20 - what is the audio format? 1 for PCM = Pulse Code Modulation
            outFile.write(shortToByteArray((short) myChannels), 0, 2);  // 22 - mono or stereo? 1 or 2?  (or 5 or ???)
            outFile.write(intToByteArray((int) mySampleRate), 0, 4);        // 24 - samples per second (numbers per second)
            outFile.write(intToByteArray((int) myByteRate), 0, 4);      // 28 - bytes per second
            outFile.write(shortToByteArray((short) myBlockAlign), 0, 2);    // 32 - # of bytes in one sample, for all channels
            outFile.write(shortToByteArray((short) myBitsPerSample), 0, 2); // 34 - how many bits in a sample(number)?  usually 16 or 24
            outFile.writeBytes("data");                 // 36 - data
            outFile.write(intToByteArray((int) myDataSize), 0, 4);      // 40 - how big is this data chunk
            outFile.write(myData);                      // 44 - the actual data itself - just a long string of numbers
            outFile.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

    // Big-endian
    public static byte[] shortToByteArray(short value) {
        return new byte[]{(byte) (value & 0xff), (byte) ((value >>> 8) & 0xff)};
    }
    
    // Big-endian
    public static byte[] intToByteArray(int value) {
        return new byte[] {
                (byte)(value  & 0xff),
                (byte)((value >>> 8) & 0xff),
                (byte)((value >>> 16)  & 0xff),
                (byte)((value >>> 24)  & 0xff)};	
    }
	
    // Little-endian
	private byte[] floatArrayToByteArray(float signal[]) {
		byte[] bytes = new byte[2*signal.length];
		int i = 0;
		for (float sample:signal) {
			sample = Math.min(1.0F, Math.max(-1.0F, sample));
			// int nSample = Math.round(sample * 32767.0F);
			int nSample = Math.round(sample * 32767.0F);
			bytes[i++] = (byte) (nSample & 0xFF);
			bytes[i++] = (byte) ((nSample >> 8) & 0xFF);
			
		}
		return bytes;
	}
}

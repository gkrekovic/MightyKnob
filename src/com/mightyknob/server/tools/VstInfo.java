package com.mightyknob.server.tools;

import com.synthbot.audioplugin.vst.vst2.JVstHost2;

/**  
 * @author Gordan Kreković
 * @version 1.0.0
 */
public class VstInfo {
	public void printPrograms(JVstHost2 vst) {
	    int n = vst.numParameters();
	    for (int i = 1; i < n; ++i) {
	    	System.out.println(vst.getParameterName(i));
	    }
	}
}

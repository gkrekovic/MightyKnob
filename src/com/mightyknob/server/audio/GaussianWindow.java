package com.mightyknob.server.audio;

public class GaussianWindow {
	double[] window;
	int size;
	
	public GaussianWindow(int m, double sigma) {
		window = new double[m];
		size = m;
		for (int i = 0; i < m; ++i) {
			double innerTerm = (i-(m-1)/2)/(sigma*(m-1)/2);
			window[i] = Math.exp(-0.5*innerTerm*innerTerm);
		}
	}
}

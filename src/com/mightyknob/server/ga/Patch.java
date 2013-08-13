package com.mightyknob.server.ga;

import java.util.ArrayList;

public class Patch {
	private ArrayList<Float> parameters;
	
	public Patch()  {
		this.parameters = null;
	}
	
	public Patch(ArrayList<Float> parameters) {
		this.parameters = parameters;
	}

	public int getSize() {
		return parameters.size();
	}
	
	public ArrayList<Float> getParameters() {
		return parameters;
	}

	public void setParameters(ArrayList<Float> parameters) {
		this.parameters = parameters;
	}
}

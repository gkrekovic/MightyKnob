package com.mightyknob.server.tools;

import javax.swing.JFrame;

import org.math.plot.*;


public class Plotter {
	public Plotter(String name, double[] y) {
		Plot2DPanel plot = new Plot2DPanel();
		plot.addScatterPlot(name, y);
		JFrame frame = new JFrame("Plot");
		frame.setSize(400, 300);
		frame.setContentPane(plot);
		frame.setVisible(true);
	}
}

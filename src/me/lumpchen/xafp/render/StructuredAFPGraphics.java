package me.lumpchen.xafp.render;

import me.lumpchen.xafp.render.AFPGraphics;

public interface StructuredAFPGraphics extends AFPGraphics {
	
	public void beginText();
	public void endText();
	
	public void beginGraphics();
	public void endGraphics();
	
	public void beginImage();
	public void endImage();
}

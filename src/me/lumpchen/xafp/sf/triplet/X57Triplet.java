package me.lumpchen.xafp.sf.triplet;

import java.io.IOException;

import me.lumpchen.xafp.AFPInputStream;

public class X57Triplet extends Triplet {

	public static final int ID = 0x57;
	
	public X57Triplet() {
		super();
		this.identifier = ID;
		this.name = "Object Byte Extent";
	}
	
	@Override
	protected void readContents(AFPInputStream in) throws IOException {
		while (remain > 0) {
			in.readBytes(remain);
			remain = 0;
		}
	}

}

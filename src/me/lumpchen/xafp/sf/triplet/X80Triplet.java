package me.lumpchen.xafp.sf.triplet;

import java.io.IOException;

import me.lumpchen.xafp.AFPInputStream;

public class X80Triplet extends Triplet {

	public static final int ID = 0x80;
	
	public X80Triplet() {
		super();
		this.identifier = ID;
		this.name = "Attribute Qualifier";
	}
	
	@Override
	protected void readContents(AFPInputStream in) throws IOException {
		while (remain > 0) {
			in.readBytes(remain);
			remain = 0;
		}
	}

}


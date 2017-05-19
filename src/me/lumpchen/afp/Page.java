package me.lumpchen.afp;

import java.util.ArrayList;
import java.util.List;

import me.lumpchen.afp.func.Function;
import me.lumpchen.afp.render.AFPGraphics;
import me.lumpchen.afp.render.ResourceManager;
import me.lumpchen.afp.sf.Identifier.Tag;
import me.lumpchen.afp.sf.StructureField;

public class Page extends AFPContainer {

	private ActiveEnvironmentGroup aeg;
	private List<PresentationTextObject> ptxList;
	
	private double scaleRatio;
	
	private double hRes;
	private double vRes;
	
	private double width;
	private double height;
	
	public Page(StructureField structField) {
		super(structField);
		this.structField = structField;
		if (this.structField != null) {
			this.nameStr = this.structField.getNameStr();
		}
		this.ptxList = new ArrayList<PresentationTextObject>();
	}
	
	public double getPageWidth() {
		return this.width;
	}
	
	public double getPageHeight() {
		return this.height;
	}
	
	public double getHorResolution() {
		return this.hRes;
	}
	
	public double getVerResolution() {
		return this.vRes;
	}
	
	@Override
	public void collect() {
		for (AFPObject child : this.children) {
			if (child instanceof ActiveEnvironmentGroup) {
				this.aeg = (ActiveEnvironmentGroup) child;
			} else if (child instanceof PresentationTextObject) {
				this.ptxList.add((PresentationTextObject) child);
			}
		}
		if (this.aeg != null && this.aeg.getPageDescriptor() != null) {
			PageDescriptor pgd = this.aeg.getPageDescriptor();
			
			this.hRes = ((double) pgd.getXpgUnits()) / pgd.getXpgBase();
			this.vRes = ((double) pgd.getXpgUnits()) / pgd.getXpgBase();
			
			this.scaleRatio = pgd.getXpgBase() / pgd.getXpgUnits();
			this.width = this.unit2Inch(pgd.getXpgSize());
			this.height = this.unit2Inch(pgd.getYpgSize());
		}
	}
	
	public MapCodedFontFormat2.Attribute getMapCodedFont(int ResLID) {
		return this.aeg.getMapCodedFontFormat2().getResourceAttributes(ResLID);
	}
	
	public void render(AFPGraphics graphics, ResourceManager resourceManager) {
		for (PresentationTextObject ptx : this.ptxList) {
			graphics.beginText();
			List<Function> cs = ptx.getPTX().getControlSequence();
			for (Function func : cs) {
				func.render(this, graphics, resourceManager);
			}
			graphics.endText();
		}
	}
	
	public double unit2Inch(int val) {
		double inch = ((double) val) * this.scaleRatio;
		return inch;
	}
	
	public double unit2Point(int val) {
		double pt = ((double) val) * this.scaleRatio * 72;
		return pt;
	}
	
	@Override
	public boolean isBegin() {
		if (Tag.BPG == this.structField.getStructureTag()) {
			return true;
		} else if (Tag.EPG == this.structField.getStructureTag()) {
			return false;
		}
		return false;
	}

}
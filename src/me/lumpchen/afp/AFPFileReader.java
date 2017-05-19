package me.lumpchen.afp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import me.lumpchen.afp.sf.Identifier.Tag;
import me.lumpchen.afp.sf.StructureField;

public class AFPFileReader {

	private AFPInputStream input;
	private Stack<AFPObject> objStack;
	private PrintFile printFile;
	
	public AFPFileReader() {
		this.objStack = new Stack<AFPObject>();
		this.printFile = new PrintFile();
	}
	
	public PrintFile getPrintFile() {
		return this.printFile;
	}
	
	public void read(File file) throws IOException {
		this.input = new AFPInputStream(new FileInputStream(file));
		
		this.objStack.push(this.printFile);
		
		while (true) {
			if (this.input.available() <= 0) {
				break;
			}
			byte first = this.input.readByte();
			if (AFPConst.Carriage_Control_Character != first) {
				break;
			}
			StructureField next = this.readNext();
			
			AFPObject obj = this.createObject(next);
			if (obj instanceof AFPContainer) {
				AFPContainer container = (AFPContainer) obj;
				if (container.isBegin()) {
					this.objStack.push(obj);
				} else {
					if (this.objStack.isEmpty()) {
						throw new AFPException("Not matched structure: " + next.getStructureTag());
					}
					AFPObject last = this.objStack.peek();
					if (this.isMatchedStructure(last, obj)) {
						last = this.objStack.pop();
						((AFPContainer) last).collect();
						AFPObject parent = this.objStack.peek();
						this.addToParent(parent, last);
					} else {
						throw new AFPException("Not matched structure: " + next.getStructureTag());
					}
				}
			} else {
				AFPObject parent = this.objStack.peek();
				this.addToParent(parent, obj);
			}
			
//			String xx = obj instanceof AFPContainer ? ((AFPContainer) obj).getNameStr() : "###";
//			System.out.println(next.getStructureTag().getDesc() + ": " + xx);
		}
		this.printFile.collect();
		this.objStack.pop();
	}
	
	private void addToParent(AFPObject parent, AFPObject child) {
		if (parent instanceof AFPContainer) {
			((AFPContainer) parent).addChild(child);
		} else {
			throw new AFPException("Not matched structure.");
		}
	}
	
	private StructureField readNext() throws IOException {
		StructureField sf = StructureFieldReader.read(this.input);
		return sf;
	}
	
	public static final Map<Tag, Tag> pairedStructureField = new HashMap<Tag, Tag>();
	static {
		pairedStructureField.put(Tag.BRG, Tag.ERG);
		pairedStructureField.put(Tag.BRS, Tag.ERS);
		pairedStructureField.put(Tag.BOC, Tag.EOC);
		pairedStructureField.put(Tag.BDT, Tag.EDT);
		pairedStructureField.put(Tag.BPG, Tag.EPG);
		pairedStructureField.put(Tag.BAG, Tag.EAG);
		pairedStructureField.put(Tag.BPT, Tag.EPT);
		pairedStructureField.put(Tag.BFN, Tag.EFN);
		pairedStructureField.put(Tag.BCP, Tag.ECP);
		pairedStructureField.put(Tag.BNG, Tag.ENG);
	}
	
	private boolean isMatchedStructure(AFPObject begin, AFPObject end) {
		if (pairedStructureField.containsKey(begin.getStructureTag())) {
			if (end.getStructureTag() == pairedStructureField.get(begin.getStructureTag())) {
				return true;
			}
		}
		return false;
	}
	
	public void close() throws IOException {
		if (this.input != null) {
			this.input.close();
		}
	}
	
	private AFPObject createObject(StructureField sf) throws IOException {
		Tag tag = sf.getStructureTag();
		AFPObject obj = null;
		if (Tag.BRG == tag || Tag.ERG == tag) {
			obj = new ResourceGroup(sf);	
		} else if (Tag.BRS == tag || Tag.ERS == tag) {
			obj = new Resource(sf);
		} else if (Tag.BOC == tag || Tag.EOC == tag) {
			obj = new ObjectContainer(sf);
		} else if (Tag.BDT == tag || Tag.EDT == tag) {
			obj = new Document(sf);
		} else if (Tag.BNG == tag || Tag.ENG == tag) {
			obj = new NamedPageGroup(sf);
		} else if (Tag.BPG == tag || Tag.EPG == tag) {
			obj = new Page(sf);
		} else if (Tag.BAG == tag || Tag.EAG == tag) {
			obj = new ActiveEnvironmentGroup(sf);
		} else if (Tag.BPT == tag || Tag.EPT == tag) {
			obj = new PresentationTextObject(sf);
		} else if (Tag.BFN == tag || Tag.EFN == tag) {
			obj = new Font(sf);
		} else if (Tag.BCP == tag || Tag.ECP == tag) {
			obj = new CodePage(sf);
		} else if (Tag.BIM == tag || Tag.EIM == tag) {
			obj = new ImageObject(sf);
		} else if (Tag.BOG == tag || Tag.EOG == tag) {
			obj = new ObjectEnvironmentGroup(sf);
		} else {
			if (Tag.OCD == tag) {
				obj = new ObjectContainerData(sf);
			} else if (Tag.PGD == tag) {
				obj = new PageDescriptor(sf);
			} else if (Tag.PTD == tag) {
				obj = new PresentationTextDescriptor(sf);
			} else if (Tag.PTX == tag) {
				obj = new PresentationTextData(sf);
			} else if (Tag.IOB == tag) {
				obj = new IncludeObject(sf);
			} else if (Tag.CPD == tag) {
				obj = new CodePageDescriptor(sf);
			} else if (Tag.CPI == tag) {
				obj = new CodePageIndex(sf);
			} else if (Tag.CPC == tag) {
				obj = new CodePageControl(sf);
			} else if (Tag.NOP == tag) {
				obj = new NoOperation(sf);
			} else if (Tag.FND == tag) {
				obj = new FontDescriptor(sf);
			} else if (Tag.FNC == tag) {
				obj = new FontControl(sf);
			} else if (Tag.FNO == tag) {
				obj = new FontOrientation(sf);
			} else if (Tag.FNP == tag) {
				obj = new FontPosition(sf);
			} else if (Tag.FNI == tag) {
				obj = new FontIndex(sf);
			} else if (Tag.FNN == tag) {
				obj = new FontNameMap(sf);
			} else if (Tag.FNG == tag) {
				obj = new FontPatterns(sf);
			} else if (Tag.MCF == tag) {
				obj = new MapCodedFontFormat2(sf);
			} else if (Tag.OBD == tag) {
				obj = new ObjectAreaDescriptor(sf);
			} else if (Tag.OBP == tag) {
				obj = new ObjectAreaPosition(sf);
			} else if (Tag.MIO == tag) {
				obj = new MapImageObject(sf);
			} else if (Tag.IDD == tag) {
				obj = new ImageDataDescriptor(sf);
			} else if (Tag.IPD == tag) {
				obj = new ImagePictureData(sf);
			}
			
		}
		
		return obj;
	}
	
	
}
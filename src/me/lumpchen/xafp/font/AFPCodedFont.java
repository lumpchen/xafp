package me.lumpchen.xafp.font;

import java.awt.geom.GeneralPath;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.fontbox.type1.Type1Font;
import org.apache.fontbox.util.BoundingBox;

import me.lumpchen.xafp.CodePage;
import me.lumpchen.xafp.Font;
import me.lumpchen.xafp.FontPatterns;
import me.lumpchen.xafp.GCGIDDatabase;
import me.lumpchen.xafp.FontPatterns.PatTech;

public class AFPCodedFont implements AFPFont {

	private String name;
	private CodePage codePage;
	private Font charset;
	
	private Encoding encoding;
	private BaseFont baseFont;
	
	private Map<String, String> nameMap;
	
	public AFPCodedFont(CodePage codePage, Font charset) {
		this.codePage = codePage;
		this.charset = charset;
		this.initEncoding(codePage);
		try {
			this.initBaseFont();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.nameMap = this.charset.getNameMap();
	}
	
	private void initBaseFont() throws IOException {
		FontPatterns patterns = this.charset.getFontPatterns();
		PatTech patTech = patterns.getPatTech();
		if (patTech == PatTech.PFB_Type1) {
			byte[] fdata = patterns.getFontData();
			Type1Font type1 = Type1Font.createWithPFB(fdata);
			this.baseFont = new BaseFont(type1);
		}
	}
	
	private void initEncoding(final CodePage codePage) {
		this.encoding = new Encoding() {
			@Override
			public int getMaxCodePoint() {
				return 255;
			}

			@Override
			public int getMinCodePoint() {
				return 0;
			}

			@Override
			public int getCodePoint(int unicode) {
				Integer codepoint = (Integer) codePage.unicode2CodePointMap().get(new Integer(unicode));
				if (codepoint != null) {
					return codepoint.intValue();
				}
				return 0xFFFF;
			}

			@Override
			public String getCharacterName(int codepoint) {
				Integer unicode = (Integer) codePage.codePoint2UnicodeMap().get(new Integer(codepoint));
				if (unicode != null) {
					return GCGIDDatabase.getGCGID(unicode.intValue());
				}
				return null;
			}

			@Override
			public int getUnicode(int codepoint) {
				Integer unicode = (Integer) codePage.codePoint2UnicodeMap().get(new Integer(codepoint));
				if (unicode != null) {
					return unicode.intValue();
				}
				return 0xFFFF;
			}

			@Override
			public boolean isDefinedCodePoint(int codepoint) {
				return codePage.codePoint2UnicodeMap().containsKey(codepoint);
			}};
	}

	@Override
	public String getName() {
		return this.charset.getTypefaceStr();
	}

	@Override
	public BoundingBox getFontBBox() throws IOException {
		return this.baseFont.getFontBBox();
	}

	@Override
	public List<Number> getFontMatrix() throws IOException {
		return this.baseFont.getFontMatrix();
	}

	@Override
	public GeneralPath getPath(String name) throws IOException {
		String techSpecName = this.getTechSpecName(name);
		if (techSpecName == null) {
			return null;
		}
		return this.baseFont.getPath(techSpecName);
	}

	@Override
	public float getWidth(String name) throws IOException {
		String techSpecName = this.getTechSpecName(name);
		if (techSpecName == null) {
			return 0;
		}
		return this.baseFont.getWidth(techSpecName);
	}

	@Override
	public boolean hasGlyph(String name) throws IOException {
		return this.baseFont.hasGlyph(name);
	}

	@Override
	public Encoding getEncoding() {
		return this.encoding;
	}
	
	public String getTechSpecName(String gcgid) {
		return this.nameMap.get(gcgid);
	}
}
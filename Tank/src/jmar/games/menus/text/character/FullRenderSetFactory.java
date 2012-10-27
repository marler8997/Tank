package jmar.games.menus.text.character;

import jmar.games.menus.text.CharacterRenderer;
import jmar.games.menus.text.FullCharacterRendererSet;

public class FullRenderSetFactory {

	public static FullCharacterRendererSet makeSimpleRenderSet(float segmentWidth) {
		FullCharacterRendererSet font = new FullCharacterRendererSet();		
		CharacterRenderer[] renderers = font.renderers;
		renderers['A'] = new EightSegRenderer((byte) (EightSegRenderer.Top | EightSegRenderer.TopRight | EightSegRenderer.BottomRight | EightSegRenderer.BottomLeft | EightSegRenderer.TopLeft | EightSegRenderer.HorzCenter), segmentWidth);
		renderers['B'] = new EightSegRenderer((byte) (EightSegRenderer.Top | EightSegRenderer.TopRight | EightSegRenderer.BottomRight | EightSegRenderer.Bottom | EightSegRenderer.BottomLeft | EightSegRenderer.TopLeft | EightSegRenderer.HorzCenter), segmentWidth);
		renderers['C'] = new EightSegRenderer((byte) (EightSegRenderer.Top | EightSegRenderer.Bottom | EightSegRenderer.BottomLeft | EightSegRenderer.TopLeft), segmentWidth);
		renderers['D'] = new EightSegRenderer((byte) (EightSegRenderer.Top | EightSegRenderer.TopRight | EightSegRenderer.BottomRight | EightSegRenderer.Bottom | EightSegRenderer.BottomLeft | EightSegRenderer.TopLeft), segmentWidth);
		renderers['E'] = new EightSegRenderer((byte) (EightSegRenderer.Top | EightSegRenderer.Bottom | EightSegRenderer.BottomLeft | EightSegRenderer.TopLeft | EightSegRenderer.HorzCenter), segmentWidth);
		renderers['F'] = new EightSegRenderer((byte) (EightSegRenderer.Top | EightSegRenderer.BottomLeft | EightSegRenderer.TopLeft | EightSegRenderer.HorzCenter), segmentWidth);
		renderers['G'] = new EightSegRenderer((byte) (EightSegRenderer.Top | EightSegRenderer.BottomRight | EightSegRenderer.Bottom | EightSegRenderer.BottomLeft | EightSegRenderer.TopLeft), segmentWidth);
		renderers['H'] = new EightSegRenderer((byte) (EightSegRenderer.TopRight | EightSegRenderer.BottomRight | EightSegRenderer.BottomLeft | EightSegRenderer.TopLeft | EightSegRenderer.HorzCenter), segmentWidth);
		renderers['I'] = new EightSegRenderer((byte) (EightSegRenderer.Top | EightSegRenderer.Bottom | EightSegRenderer.VertCenter), segmentWidth);
		renderers['J'] = new EightSegRenderer((byte) (EightSegRenderer.TopRight | EightSegRenderer.BottomRight | EightSegRenderer.Bottom), segmentWidth);
		renderers['K'] = new EightSegRenderer((byte) (EightSegRenderer.TopRight | EightSegRenderer.BottomRight | EightSegRenderer.BottomLeft | EightSegRenderer.TopLeft | EightSegRenderer.HorzCenter), segmentWidth);
		renderers['L'] = new EightSegRenderer((byte) (EightSegRenderer.Bottom | EightSegRenderer.BottomLeft | EightSegRenderer.TopLeft), segmentWidth);		

		renderers['N'] = new SpecialRenderer.NRenderer(segmentWidth);
		renderers['O'] = new EightSegRenderer((byte) (EightSegRenderer.Top | EightSegRenderer.TopRight | EightSegRenderer.BottomRight | EightSegRenderer.Bottom | EightSegRenderer.BottomLeft | EightSegRenderer.TopLeft), segmentWidth);
		renderers['P'] = new EightSegRenderer((byte) (EightSegRenderer.Top | EightSegRenderer.TopRight | EightSegRenderer.BottomLeft | EightSegRenderer.TopLeft | EightSegRenderer.HorzCenter), segmentWidth);		
		//...
		renderers['R'] = new SpecialRenderer.RRenderer(segmentWidth);
		renderers['S'] = new EightSegRenderer((byte) (EightSegRenderer.Top | EightSegRenderer.TopLeft | EightSegRenderer.HorzCenter | EightSegRenderer.BottomRight | EightSegRenderer.Bottom), segmentWidth);
		renderers['T'] = new EightSegRenderer((byte) (EightSegRenderer.Top | EightSegRenderer.VertCenter), segmentWidth);
		renderers['U'] = new EightSegRenderer((byte) (EightSegRenderer.TopRight | EightSegRenderer.BottomRight | EightSegRenderer.Bottom | EightSegRenderer.BottomLeft | EightSegRenderer.TopLeft), segmentWidth);
		renderers['V'] = new SpecialRenderer.VRenderer(segmentWidth);
		
		renderers['Y'] = new SpecialRenderer.YRenderer(segmentWidth);
		
		// Set lower case to have same renderer has upper case
		for(char c = 'a'; c <= 'z'; c++) {
			renderers[c] = renderers[c + ('A' - 'a')];
		}


		renderers['0'] = renderers['O'];
		renderers['1'] = new EightSegRenderer((byte) (EightSegRenderer.VertCenter), segmentWidth);
		renderers['2'] = new EightSegRenderer((byte) (EightSegRenderer.Top | EightSegRenderer.TopRight | EightSegRenderer.HorzCenter | EightSegRenderer.BottomLeft | EightSegRenderer.Bottom), segmentWidth);
		renderers['3'] = new EightSegRenderer((byte) (EightSegRenderer.Top | EightSegRenderer.TopRight | EightSegRenderer.HorzCenter | EightSegRenderer.BottomRight | EightSegRenderer.Bottom), segmentWidth);
		renderers['4'] = new EightSegRenderer((byte) (EightSegRenderer.TopLeft | EightSegRenderer.HorzCenter | EightSegRenderer.TopRight | EightSegRenderer.BottomRight), segmentWidth);
		renderers['5'] = renderers['S'];
		renderers['6'] = new EightSegRenderer((byte) (EightSegRenderer.TopLeft | EightSegRenderer.BottomLeft | EightSegRenderer.Bottom | EightSegRenderer.BottomRight | EightSegRenderer.HorzCenter), segmentWidth);
		renderers['7'] = new EightSegRenderer((byte) (EightSegRenderer.Top | EightSegRenderer.TopRight | EightSegRenderer.BottomRight), segmentWidth);
		renderers['8'] = renderers['B'];
		renderers['9'] = new EightSegRenderer((byte) (EightSegRenderer.Top | EightSegRenderer.TopRight | EightSegRenderer.BottomRight | EightSegRenderer.Bottom | EightSegRenderer.TopLeft | EightSegRenderer.HorzCenter), segmentWidth);
		
		renderers['.'] = new SpecialRenderer.PeriodRenderer(segmentWidth);
		return font;
	}
}

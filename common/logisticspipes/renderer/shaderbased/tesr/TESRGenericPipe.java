package logisticspipes.renderer.shaderbased.tesr;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

import com.google.common.collect.Maps;

import logisticspipes.renderer.shaderbased.NewGLRenderer;
import logisticspipes.renderer.shaderbased.model.OBJRoot;

public class TESRGenericPipe extends TileEntitySpecialRenderer<> {

	private final OBJRoot pipeModel;
	private final NewGLRenderer render;

	public TESRGenericPipe(){
		pipeModel =
		render = new NewGLRenderer(pipeModel, Maps.newHashMap());
	}
}

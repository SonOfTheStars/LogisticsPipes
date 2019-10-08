/**
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.gui.modules;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.IInventory;

import org.lwjgl.opengl.GL11;

import logisticspipes.modules.ModuleAdvancedExtractor;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.module.AdvancedExtractorIncludePacket;
import logisticspipes.network.packets.module.AdvancedExtractorSneakyGuiPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.GuiStringHandlerButton;
import logisticspipes.utils.gui.LogisticsBaseGuiScreen;

public class GuiAdvancedExtractor extends ModuleBaseGui {

	private final ModuleAdvancedExtractor _advancedExtractor;

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		//Default item toggle:
		buttonList.clear();
		buttonList.add(new GuiStringHandlerButton(0, width / 2 + 20, height / 2 - 34, 60, 20, () -> _advancedExtractor.areItemsIncluded() ? "Included" : "Excluded"));

		buttonList.add(new GuiButton(1, width / 2 - 25, height / 2 - 34, 40, 20, "Sneaky"));
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		switch (guibutton.id) {
			case 0:
				_advancedExtractor.setItemsIncluded(!_advancedExtractor.areItemsIncluded());
				MainProxy.sendPacketToServer(PacketHandler.getPacket(AdvancedExtractorIncludePacket.class).setModulePos(_advancedExtractor));
				break;
			case 1:
				MainProxy.sendPacketToServer(PacketHandler.getPacket(AdvancedExtractorSneakyGuiPacket.class).setModulePos(_advancedExtractor));
				break;
		}

	}

	public GuiAdvancedExtractor(IInventory playerInventory, ModuleAdvancedExtractor advancedExtractor) {
		super(null, advancedExtractor);
		_advancedExtractor = advancedExtractor;
		DummyContainer dummy = new DummyContainer(playerInventory, _advancedExtractor.getFilterInventory());
		dummy.addNormalSlotsForPlayerInventory(8, 60);

		//Pipe slots
		for (int pipeSlot = 0; pipeSlot < 9; pipeSlot++) {
			dummy.addDummySlot(pipeSlot, 8 + pipeSlot * 18, 18);
		}

		inventorySlots = dummy;
		xSize = 175;
		ySize = 142;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		mc.fontRenderer.drawString(_advancedExtractor.getFilterInventory().getName(), 8, 6, 0x404040);
		mc.fontRenderer.drawString("Inventory", 8, ySize - 92, 0x404040);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(LogisticsBaseGuiScreen.ITEMSINK);
		int j = guiLeft;
		int k = guiTop;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
	}

	public void setInclude(boolean flag) {
		_advancedExtractor.setItemsIncluded(flag);
	}
}

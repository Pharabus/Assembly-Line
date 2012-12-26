package assemblyline.client.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import universalelectricity.core.vector.Vector3;
import assemblyline.common.AssemblyLine;
import assemblyline.common.machine.ContainerRejector;

public class GuiStamper extends GuiContainer
{
	private int containerWidth;
	private int containerHeight;

	public GuiStamper(InventoryPlayer par1InventoryPlayer, World worldObj, Vector3 position)
	{
		super(new ContainerRejector(par1InventoryPlayer, worldObj, position));
	}

	/**
	 * Draw the foreground layer for the GuiContainer (everything in front of the items)
	 */
	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2)
	{
		this.fontRenderer.drawString(AssemblyLine.translateLocal("assemblyline.gui.stamper"), 55, 6, 4210752);
	}

	/**
	 * Draw the background layer for the GuiContainer (everything behind the items)
	 */
	@Override
	protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
	{
		int var4 = this.mc.renderEngine.getTexture(AssemblyLine.TEXTURE_PATH + "gui_ejector.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture(var4);
		containerWidth = (this.width - this.xSize) / 2;
		containerHeight = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(containerWidth, containerHeight, 0, 0, this.xSize, this.ySize);

		// GUI button changes
		for (int i = 1; i < this.tileEntity.guiButtons.length; i++)
		{
			this.drawTexturedModalRect(containerWidth + 17 + i * 18, containerHeight + 17, 176, +(tileEntity.guiButtons[i] ? 12 : 0), 12, 12);
		}
		this.fontRenderer.drawString((tileEntity.guiButtons[0] ? "Inv" : "Other"), containerWidth + 108, containerHeight + 22, 4210752);
	}
}
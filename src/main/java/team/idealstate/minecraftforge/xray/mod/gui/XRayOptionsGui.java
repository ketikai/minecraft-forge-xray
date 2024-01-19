/*
 *     <one line to give the program's name and a brief idea of what it does.>
 *     Copyright (C) 2024/01/19 ideal-state
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package team.idealstate.minecraftforge.xray.mod.gui;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;
import team.idealstate.minecraftforge.xray.common.gui.AbstractGuiScreen;
import team.idealstate.minecraftforge.xray.mod.config.XRayOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>XRayOptionsGui</p>
 *
 * <p>Created on 2024/1/18 12:58</p>
 *
 * @author ketikai
 * @since 1.0.0
 */
public class XRayOptionsGui extends AbstractGuiScreen {

    private XRayBlockListGui blockListGui;
    private XRayBlockListGui excludeBlockListGui;
    private GuiTextField searchBox;
    private GuiButton doneButton;
    private final AtomicBoolean hasUpdate = new AtomicBoolean(false);

    public XRayOptionsGui(GuiScreen parent) {
        super(parent, "gui.xray.options.title");
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        this.doneButton = new GuiButton(
                6,
                width - 120 - margin, height - 20 - margin,
                120, 20,
                I18n.format("gui.done")
        );
        buttonList.add(doneButton);
        this.searchBox = new GuiTextField(getFontRenderer(), margin, doneButton.yPosition,
                width - margin - margin - margin - doneButton.width, doneButton.height);
        searchBox.setFocused(false);
        searchBox.setCanLoseFocus(true);
        searchBox.setMaxStringLength(128);
        Set<String> blockRegistryKeys = Block.blockRegistry.getKeys();
        List<String> blocks = new ArrayList<>(blockRegistryKeys.size());
        for (String key : blockRegistryKeys) {
            Block block = (Block) Block.blockRegistry.getObject(key);
            if (block.getMaterial() == Material.air) {
                continue;
            }
            blocks.add(block.delegate.name());
        }
        final int blockListGuiWidth = (width - margin * 2) / 5 * 2;
        this.blockListGui = new XRayBlockListGui(this, "gui.xray.blocks.title", "gui.xray.blocks.sub_title", blocks, blockListGuiWidth, margin);
        this.excludeBlockListGui = new XRayBlockListGui(this, "gui.xray.excludes.title", "gui.xray.excludes.sub_title", XRayOptions.getInstance().getExcludeBlockIdentifiers(), blockListGuiWidth, width - blockListGuiWidth - margin);
        blockListGui.addElementListener((self, index, doubleClick) -> {
            if (doubleClick) {
                String element = self.getElement(index);
                if (element != null) {
                    if (excludeBlockListGui.addElement(element)) {
                        excludeBlockListGui.slideToBottom();
                        hasUpdate.set(true);
                    }
                }
            }
        });
        excludeBlockListGui.addElementListener((self, index, doubleClick) -> {
            if (doubleClick) {
                if (excludeBlockListGui.removeElement(index)) {
                    hasUpdate.set(true);
                }
            }
        });
        XRayBlockListGui.ElementFilter elementFilter = (blockIdentifier, blockName) -> {
            String text = searchBox.getText();
            if (text == null || text.isEmpty()) {
                return true;
            }
            return blockIdentifier.contains(text) || blockName.contains(text);
        };
        blockListGui.addIncludeElementFilters(elementFilter);
        excludeBlockListGui.addIncludeElementFilters(elementFilter);
        blockListGui.addExcludeElementFilters((blockIdentifier, blockName) -> excludeBlockListGui.containsElement(blockIdentifier));
        super.initGui();
    }

    @Override
    @SuppressWarnings({"all"})
    protected void actionPerformed(GuiButton button) {
        if (button.enabled) {
            switch (button.id) {
                case 6:
                    hasUpdate.set(false);
                    XRayOptions.getInstance().setExcludeBlockIdentifiers(excludeBlockListGui.getElements());
                    break;
            }
        }
        super.actionPerformed(button);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (searchBox.isFocused()) {
            if (keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_RETURN) {
                searchBox.setFocused(false);
                return;
            }
            searchBox.textboxKeyTyped(typedChar, keyCode);
        }
        if (keyCode == Keyboard.KEY_RETURN) {
            if (doneButton.enabled) {
                this.actionPerformed(doneButton);
            }
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        searchBox.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void updateScreen() {
        searchBox.updateCursorCounter();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawBackground(0);
        blockListGui.drawScreen(mouseX, mouseY, partialTicks);
        excludeBlockListGui.drawScreen(mouseX, mouseY, partialTicks);
        searchBox.drawTextBox();
        doneButton.enabled = hasUpdate.get();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}

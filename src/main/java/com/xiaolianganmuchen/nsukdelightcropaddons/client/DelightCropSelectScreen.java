package com.xiaolianganmuchen.nsukdelightcropaddons.client;

import client.cn.kafei.simukraft.client.ui.SimuKraftUiTheme;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import com.lowdragmc.lowdraglib2.gui.texture.ColorBorderTexture;
import com.lowdragmc.lowdraglib2.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.ScrollerMode;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import com.xiaolianganmuchen.nsukdelightcropaddons.config.CropSelectUiStorage;
import com.xiaolianganmuchen.nsukdelightcropaddons.crop.CropCatalog;
import com.xiaolianganmuchen.nsukdelightcropaddons.crop.CropDisplayNames;
import com.xiaolianganmuchen.nsukdelightcropaddons.crop.CropLangIndex;
import com.xiaolianganmuchen.nsukdelightcropaddons.crop.ResolvedCrop;
import common.cn.kafei.simukraft.farmland.FarmCrop;
import common.cn.kafei.simukraft.network.farmland.FarmlandBoxOpenRequestPacket;
import common.cn.kafei.simukraft.network.farmland.FarmlandBoxOpenResponsePacket;
import common.cn.kafei.simukraft.network.farmland.FarmlandBoxSetCropPacket;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.TaffyPosition;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

@OnlyIn(Dist.CLIENT)
public final class DelightCropSelectScreen {
   private static final int ITEM_HEIGHT = 24;
   private static final int HEADER_HEIGHT = 14;
   private static final int HEADER_COLOR = -658016;
   private static final int ICON_SIZE = 18;
   private static final int PANEL_MAX_WIDTH = 320;
   private static final int PANEL_HEIGHT = 236;
   private static final int SEARCH_HEIGHT = 22;

   private DelightCropSelectScreen() {
   }

   public static void open(FarmlandBoxOpenResponsePacket packet) {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft != null) {
         CropLangIndex.refresh();
         minecraft.execute(() -> minecraft.setScreen(new DelightCropSelectScreen.CropScreen(createUi(packet), Component.empty())));
      }
   }

   private static ModularUI createUi(FarmlandBoxOpenResponsePacket packet) {
      int screenWidth = Math.max(320, Minecraft.getInstance().getWindow().getGuiScaledWidth());
      int screenHeight = Math.max(240, Minecraft.getInstance().getWindow().getGuiScaledHeight());
      UIElement root = new UIElement().layout(layout -> {
         layout.widthPercent(100.0F);
         layout.heightPercent(100.0F);
         layout.alignItems(AlignItems.CENTER);
         layout.justifyContent(AlignContent.CENTER);
         layout.paddingAll(8.0F);
      });
      root.addChild(SimuKraftUiTheme.createShellPanel(screenWidth, screenHeight));
      root.addChild(topButton("gui.button.back", () -> back(packet.boxPos())));
      UIElement panel = new UIElement().layout(layout -> {
         layout.widthPercent(90.0F);
         layout.maxWidth(320.0F);
         layout.height(Math.min(236, screenHeight - 48));
         layout.flexDirection(FlexDirection.COLUMN);
         layout.alignItems(AlignItems.STRETCH);
         layout.paddingAll(10.0F);
         layout.gapAll(6.0F);
      }).addClass("simukraft_panel");
      panel.addChild(label(Component.translatable("gui.simukraft.farmland_box.select_crop_title"), Horizontal.CENTER, -1, 16));
      panel.addChild(label(Component.translatable("gui.nsukdelightcropaddons.select_crop.hint"), Horizontal.CENTER, -4342339, 12));
      UIElement list = new UIElement().layout(layout -> {
         layout.widthPercent(100.0F);
         layout.flexDirection(FlexDirection.COLUMN);
         layout.alignItems(AlignItems.STRETCH);
         layout.gapAll(4.0F);
      });
      DelightCropSelectScreen.CropListState state = new DelightCropSelectScreen.CropListState(packet, list);
      panel.addChild(searchBox(state));
      ScrollerView scroller = new ScrollerView();
      scroller.scrollerStyle(style -> style.mode(ScrollerMode.VERTICAL));
      scroller.layout(layout -> {
         layout.widthPercent(100.0F);
         layout.flex(1.0F);
         layout.minHeight(80.0F);
      });
      scroller.addScrollViewChild(list);
      panel.addChild(scroller);
      state.rebuild();
      root.addChild(panel);
      return new ModularUI(SimuKraftUiTheme.createUi(root)).shouldCloseOnEsc(true).shouldCloseOnKeyInventory(false);
   }

   private static UIElement searchBox(DelightCropSelectScreen.CropListState state) {
      UIElement wrap = new UIElement().layout(layout -> {
         layout.widthPercent(100.0F);
         layout.height(22.0F);
         layout.minHeight(22.0F);
         layout.maxHeight(22.0F);
         layout.flexShrink(0.0F);
      }).style(style -> style.backgroundTexture(new GuiTextureGroup(new IGuiTexture[]{new ColorRectTexture(-15461352), new ColorBorderTexture(1, -7697808)})));
      TextField field = new TextField();
      field.setAnyString();
      field.setTextResponder(state::setQuery);
      field.layout(layout -> {
         layout.widthPercent(100.0F);
         layout.height(22.0F);
         layout.paddingLeft(6.0F);
         layout.paddingRight(6.0F);
         layout.paddingTop(5.0F);
         layout.paddingBottom(5.0F);
      });
      field.style(style -> style.backgroundTexture(IGuiTexture.EMPTY));
      field.textFieldStyle(
         style -> style.fontSize(9.0F)
            .textColor(-1)
            .cursorColor(-1)
            .textShadow(false)
            .placeholder(Component.translatable("gui.nsukdelightcropaddons.select_crop.search"))
            .focusOverlay(IGuiTexture.EMPTY)
      );
      wrap.addChild(field);
      return wrap;
   }

   private static UIElement vanillaButton(FarmlandBoxOpenResponsePacket packet, FarmCrop crop) {
      boolean selected = crop.id().equals(packet.cropId());
      Component name = Component.translatable(crop.translationKey());
      return cropRow(packet.boxPos(), crop.id(), new ItemStack(crop.seed()), name, selected);
   }

   private static UIElement delightButton(FarmlandBoxOpenResponsePacket packet, ResolvedCrop crop) {
      boolean selected = crop.id().equals(packet.cropId());
      return cropRow(packet.boxPos(), crop.id(), new ItemStack(crop.seed()), CropDisplayNames.of(crop), selected);
   }

   private static UIElement cropRow(BlockPos boxPos, String cropId, ItemStack icon, Component name, boolean selected) {
      UIElement row = new UIElement().layout(layout -> {
         layout.widthPercent(100.0F);
         layout.height(24.0F);
         layout.flexDirection(FlexDirection.ROW);
         layout.alignItems(AlignItems.CENTER);
         layout.gapAll(6.0F);
      });
      row.addChild(new UIElement().layout(layout -> {
         layout.width(18.0F);
         layout.height(18.0F);
         layout.flexShrink(0.0F);
      }).style(style -> style.backgroundTexture(new ItemStackTexture(new ItemStack[]{icon}))));
      Button button = new Button();
      Component text = (Component)(selected ? Component.translatable("gui.simukraft.farmland_box.crop_selected", new Object[]{name}) : name);
      button.setText(text);
      button.setOnClick(event -> select(boxPos, cropId));
      button.layout(layout -> {
         layout.height(24.0F);
         layout.flex(1.0F);
      });
      row.addChild(button);
      return row;
   }

   private static UIElement sectionTitle(Component text) {
      return label(text, Horizontal.LEFT, -658016, 14);
   }

   private static UIElement foldHeader(String modId, boolean expanded, Runnable toggle) {
      String key = expanded ? "gui.nsukdelightcropaddons.select_crop.group_expanded" : "gui.nsukdelightcropaddons.select_crop.group_collapsed";
      Button header = new Button().noText();
      header.buttonStyle(style -> style.baseTexture(IGuiTexture.EMPTY).hoverTexture(new ColorRectTexture(586544544)).pressedTexture(IGuiTexture.EMPTY));
      header.setOnClick(event -> toggle.run());
      header.layout(layout -> {
         layout.widthPercent(100.0F);
         layout.height(14.0F);
         layout.flexDirection(FlexDirection.ROW);
         layout.alignItems(AlignItems.CENTER);
      });
      header.addChild(label(Component.translatable(key, new Object[]{CropDisplayNames.source(modId)}), Horizontal.LEFT, -658016, 14));
      return header;
   }

   private static boolean keepOpen(String modId, int totalCount) {
      return "farmersdelight".equals(modId) || totalCount <= 1;
   }

   private static int sourceRank(String modId, int totalCount) {
      if ("farmersdelight".equals(modId)) {
         return 0;
      } else if (totalCount <= 1) {
         return 1;
      } else {
         return "custom".equals(modId) ? 3 : 2;
      }
   }

   private static List<Entry<String, List<ResolvedCrop>>> orderedSources(Map<String, List<ResolvedCrop>> grouped) {
      List<Entry<String, List<ResolvedCrop>>> entries = new ArrayList<>(grouped.entrySet());
      entries.sort(
         Comparator.<Entry<String, List<ResolvedCrop>>>comparingInt(entry -> sourceRank(entry.getKey(), entry.getValue().size()))
            .thenComparing(entry -> CropDisplayNames.source(entry.getKey()).getString())
      );
      return entries;
   }

   private static Button topButton(String key, Runnable action) {
      Button button = new Button();
      button.setText(Component.translatable(key));
      button.setOnClick(event -> action.run());
      button.layout(layout -> {
         layout.positionType(TaffyPosition.ABSOLUTE);
         layout.left(5.0F);
         layout.top(5.0F);
         layout.width(50.0F);
         layout.height(22.0F);
      });
      return button;
   }

   private static Label label(Component text, Horizontal horizontal, int color, int height) {
      Label label = new Label();
      label.setText(text);
      label.layout(layout -> {
         layout.widthPercent(100.0F);
         layout.height(height);
      });
      label.textStyle(style -> style.textColor(color).textShadow(true).textAlignHorizontal(horizontal).textAlignVertical(Vertical.CENTER));
      return label;
   }

   private static void select(BlockPos boxPos, String cropId) {
      PacketDistributor.sendToServer(new FarmlandBoxSetCropPacket(boxPos, cropId), new CustomPacketPayload[0]);
   }

   private static void back(BlockPos boxPos) {
      PacketDistributor.sendToServer(new FarmlandBoxOpenRequestPacket(boxPos), new CustomPacketPayload[0]);
   }

   private static final class CropListState {
      private final FarmlandBoxOpenResponsePacket packet;
      private final UIElement list;
      private String query = "";

      private CropListState(FarmlandBoxOpenResponsePacket packet, UIElement list) {
         this.packet = packet;
         this.list = list;
      }

      private void setQuery(String text) {
         String next = text == null ? "" : text.trim();
         if (!next.equals(this.query)) {
            this.query = next;
            this.rebuild();
         }
      }

      private void toggle(String modId) {
         CropSelectUiStorage.toggle(modId);
         this.rebuild();
      }

      private void rebuild() {
         this.list.clearAllChildren();
         boolean searching = !this.query.isBlank();
         boolean any = false;
         boolean vanillaHeader = false;

         for (FarmCrop crop : FarmCrop.values()) {
            if (CropDisplayNames.matches(this.query, crop)) {
               if (!vanillaHeader) {
                  this.list.addChild(DelightCropSelectScreen.sectionTitle(Component.translatable("source.nsukdelightcropaddons.vanilla")));
                  vanillaHeader = true;
               }

               this.list.addChild(DelightCropSelectScreen.vanillaButton(this.packet, crop));
               any = true;
            }
         }

         Map<String, List<ResolvedCrop>> grouped = CropCatalog.selectableBySource();
         if (grouped.isEmpty() && !searching) {
            this.list
               .addChild(DelightCropSelectScreen.label(Component.translatable("gui.nsukdelightcropaddons.select_crop.empty"), Horizontal.LEFT, -4342339, 14));
         } else {
            for (Entry<String, List<ResolvedCrop>> entry : DelightCropSelectScreen.orderedSources(grouped)) {
               String modId = entry.getKey();
               List<ResolvedCrop> all = entry.getValue();
               List<ResolvedCrop> matched = new ArrayList<>();

               for (ResolvedCrop cropx : all) {
                  if (CropDisplayNames.matches(this.query, cropx)) {
                     matched.add(cropx);
                  }
               }

               if (!matched.isEmpty()) {
                  boolean open = searching || DelightCropSelectScreen.keepOpen(modId, all.size()) || CropSelectUiStorage.isExpanded(modId);
                  if (!DelightCropSelectScreen.keepOpen(modId, all.size()) && !searching) {
                     this.list.addChild(DelightCropSelectScreen.foldHeader(modId, open, () -> this.toggle(modId)));
                  } else {
                     this.list.addChild(DelightCropSelectScreen.sectionTitle(CropDisplayNames.source(modId)));
                  }

                  if (open) {
                     for (ResolvedCrop cropxx : matched) {
                        this.list.addChild(DelightCropSelectScreen.delightButton(this.packet, cropxx));
                     }
                  }

                  any = true;
               }
            }

            if (!any) {
               this.list
                  .addChild(
                     DelightCropSelectScreen.label(Component.translatable("gui.nsukdelightcropaddons.select_crop.no_match"), Horizontal.LEFT, -4342339, 14)
                  );
            }
         }
      }
   }

   private static final class CropScreen extends ModularUIScreen {
      private CropScreen(ModularUI modularUI, Component title) {
         super(modularUI, title);
      }
   }
}

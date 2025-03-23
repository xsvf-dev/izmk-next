package net.optifine;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.optifine.render.Blender;
import net.optifine.util.PropertiesOrdered;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class CustomSky {
    private static final Logger log = LogManager.getLogger(CustomSky.class);
    private static CustomSkyLayer[][] worldSkyLayers = null;

    public static void reset() {
        worldSkyLayers = null;
    }

    public static void update() {
        reset();
        if (ovo.xsvf.izmk.module.impl.CustomSky.INSTANCE.getEnabled()) {
            worldSkyLayers = readCustomSkies();
        }
    }

    private static CustomSkyLayer[][] readCustomSkies() {
        CustomSkyLayer[][] acustomskylayer = new CustomSkyLayer[10][0];
        String s = "optifine/sky/world";
        int i = -1;

        for (int j = 0; j < acustomskylayer.length; ++j) {
            String s1 = s + j;
            List<CustomSkyLayer> list = new ArrayList<>();

            int l = 0;
            for (int k = 0; k < 1000; ++k) {
                String s2 = s1 + "/sky" + k + ".properties";

                try {
                    ResourceLocation resourcelocation = ResourceLocation.tryParse(s2);
                    InputStream inputstream = Config.getResourceStream(resourcelocation);
                    if (l > 10) {
                        break;
                    }

                    Properties properties = new PropertiesOrdered();
                    properties.load(inputstream);
                    inputstream.close();

                    CustomSky.log.debug("CustomSky properties: {}", s2);
                    String s3 = k + ".png";
                    CustomSkyLayer customskylayer = new CustomSkyLayer(properties, s3);
                    if (customskylayer.isValid(s2)) {
                        String s4 = addSuffixCheck(customskylayer.source, ".png");
                        ResourceLocation resourcelocation1 = ResourceLocation.tryParse(s4);
                        AbstractTexture abstracttexture = Config.getTextureManager().getTexture(resourcelocation1);
                        customskylayer.textureId = abstracttexture.getId();
                        list.add(customskylayer);
                        inputstream.close();
                    }
                } catch (FileNotFoundException filenotfoundexception) {
                    ++l;
                } catch (IOException ioexception) {
                    ioexception.printStackTrace();
                }
            }

            if (list.size() > 0) {
                CustomSkyLayer[] acustomskylayer2 = list.toArray(new CustomSkyLayer[list.size()]);
                acustomskylayer[j] = acustomskylayer2;
                i = j;
            }
        }

        if (i < 0) {
            return null;
        } else {
            int i1 = i + 1;
            CustomSkyLayer[][] acustomskylayer1 = new CustomSkyLayer[i1][0];

            System.arraycopy(acustomskylayer, 0, acustomskylayer1, 0, acustomskylayer1.length);

            return acustomskylayer1;
        }
    }

    public static void renderSky(Level world, PoseStack matrixStackIn, float partialTicks) {
        if (worldSkyLayers != null) {
//            if (Config.isShaders()) {
//                Shaders.setRenderStage(RenderStage.CUSTOM_SKY);
//            }

            int i = getDimensionId(world);
            if (i >= 0 && i < worldSkyLayers.length) {
                CustomSkyLayer[] acustomskylayer = worldSkyLayers[i];
                if (acustomskylayer != null) {
                    long j = world.getDayTime();
                    int k = (int) (j % 24000L);
                    float f = world.getTimeOfDay(partialTicks);
                    float f1 = world.getRainLevel(partialTicks);
                    float f2 = world.getThunderLevel(partialTicks);
                    if (f1 > 0.0F) {
                        f2 /= f1;
                    }

                    for (int l = 0; l < acustomskylayer.length; ++l) {
                        CustomSkyLayer customskylayer = acustomskylayer[l];
                        if (customskylayer.isActive(world, k)) {
                            customskylayer.render(world, matrixStackIn, k, f, f1, f2);
                        }
                    }

                    float f3 = 1.0F - f1;
                    Blender.clearBlend(f3);
                }
            }
        }
    }

    public static boolean hasSkyLayers(Level world) {
        if (worldSkyLayers == null) {
            return false;
        } else {
            int i = getDimensionId(world);
            if (i >= 0 && i < worldSkyLayers.length) {
                CustomSkyLayer[] acustomskylayer = worldSkyLayers[i];
                if (acustomskylayer == null) {
                    return false;
                } else {
                    return acustomskylayer.length > 0;
                }
            } else {
                return false;
            }
        }
    }

    public static String addSuffixCheck(String str, String suffix) {
        if (str != null && suffix != null) {
            return str.endsWith(suffix) ? str : str + suffix;
        } else {
            return str;
        }
    }

    public static int getDimensionId(ResourceKey<Level> dimension) {
        if (dimension == Level.NETHER) {
            return -1;
        } else if (dimension == Level.OVERWORLD) {
            return 0;
        } else {
            return dimension == Level.END ? 1 : 0;
        }
    }

    public static int getDimensionId(Level world) {
        return world == null ? 0 : getDimensionId(world.dimension());
    }
}

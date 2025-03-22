package net.optifine;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.*;

public class Config {
    private static final Minecraft minecraft = Minecraft.getInstance();

    public static TextureManager getTextureManager() {
        return minecraft.getTextureManager();
    }

    public static ResourceManager getResourceManager() {
        return minecraft.getResourceManager();
    }

    public static InputStream getResourceStream(ResourceLocation location) throws IOException {
        return getResourceStream(minecraft.getResourceManager(), location);
    }

    public static InputStream getResourceStream(ResourceManager resourceManager, ResourceLocation location) throws IOException {
        Resource resource = resourceManager.getResourceOrThrow(location);
        return resource.open();
    }

    public static Resource getResource(ResourceLocation location) throws IOException {
        return minecraft.getResourceManager().getResourceOrThrow(location);
    }

    public static boolean hasResource(ResourceLocation location) {
        if (location == null) {
            return false;
        } else {
            PackResources packresources = getDefiningResourcePack(location);
            return packresources != null;
        }
    }

    public static boolean hasResource(ResourceManager resourceManager, ResourceLocation location) {
        try {
            Resource resource = resourceManager.getResourceOrThrow(location);
            return resource != null;
        } catch (IOException ioexception) {
            return false;
        }
    }

    public static boolean hasResource(PackResources rp, ResourceLocation loc) {
        if (rp != null && loc != null) {
            IoSupplier<InputStream> iosupplier = rp.getResource(PackType.CLIENT_RESOURCES, loc);
            return iosupplier != null;
        } else {
            return false;
        }
    }

    public static PackResources getDefiningResourcePack(ResourceLocation location) {
        PackRepository packrepository = minecraft.getResourcePackRepository();
        Collection<Pack> collection = packrepository.getSelectedPacks();
        List<Pack> list = (List) collection;

        for (int i = list.size() - 1; i >= 0; --i) {
            Pack pack = list.get(i);
            PackResources packresources = pack.open();
            if (packresources.getResource(PackType.CLIENT_RESOURCES, location) != null) {
                return packresources;
            }
        }

        return null;
    }

    public static boolean equalsOne(int val, int[] vals) {
        for (int j : vals) {
            if (j == val) {
                return true;
            }
        }

        return false;
    }

    public static int[] addIntToArray(int[] intArray, int intValue) {
        return addIntsToArray(intArray, new int[]{intValue});
    }

    public static int[] addIntsToArray(int[] intArray, int[] copyFrom) {
        if (intArray != null && copyFrom != null) {
            int i = intArray.length;
            int j = i + copyFrom.length;
            int[] aint = new int[j];
            System.arraycopy(intArray, 0, aint, 0, i);

            System.arraycopy(copyFrom, 0, aint, i, copyFrom.length);

            return aint;
        } else {
            throw new NullPointerException("The given array is NULL");
        }
    }

    public static String arrayToString(boolean[] arr, String separator) {
        if (arr == null) {
            return "";
        } else {
            StringBuilder stringbuffer = new StringBuilder(arr.length * 5);

            for (int i = 0; i < arr.length; ++i) {
                boolean flag = arr[i];
                if (i > 0) {
                    stringbuffer.append(separator);
                }

                stringbuffer.append(flag);
            }

            return stringbuffer.toString();
        }
    }

    public static String arrayToString(int[] arr) {
        return arrayToString(arr, ", ");
    }

    public static String arrayToString(int[] arr, String separator) {
        if (arr == null) {
            return "";
        } else {
            StringBuilder stringbuffer = new StringBuilder(arr.length * 5);

            for (int i = 0; i < arr.length; ++i) {
                int j = arr[i];
                if (i > 0) {
                    stringbuffer.append(separator);
                }

                stringbuffer.append(j);
            }

            return stringbuffer.toString();
        }
    }

    public static String[] tokenize(String str, String delim) {
        StringTokenizer stringtokenizer = new StringTokenizer(str, delim);
        List list = new ArrayList();

        while (stringtokenizer.hasMoreTokens()) {
            String s = stringtokenizer.nextToken();
            list.add(s);
        }

        String[] astring = (String[]) list.toArray(new String[list.size()]);
        return astring;
    }

    public static int parseInt(String str, int defVal) {
        try {
            if (str == null) {
                return defVal;
            } else {
                str = str.trim();
                return Integer.parseInt(str);
            }
        } catch (NumberFormatException numberformatexception) {
            return defVal;
        }
    }

    public static Object[] addObjectToArray(Object[] arr, Object obj) {
        if (arr == null) {
            throw new NullPointerException("The given array is NULL");
        } else {
            int i = arr.length;
            int j = i + 1;
            Object[] aobject = (Object[]) Array.newInstance(arr.getClass().getComponentType(), j);
            System.arraycopy(arr, 0, aobject, 0, i);
            aobject[i] = obj;
            return aobject;
        }
    }

    public static Object[] addObjectToArray(Object[] arr, Object obj, int index) {
        List<Object> list = new ArrayList<>(Arrays.asList(arr));
        list.add(index, obj);
        Object[] aobject = (Object[]) Array.newInstance(arr.getClass().getComponentType(), list.size());
        return list.toArray(aobject);
    }

    public static Object[] addObjectsToArray(Object[] arr, Object[] objs) {
        if (arr == null) {
            throw new NullPointerException("The given array is NULL");
        } else if (objs.length == 0) {
            return arr;
        } else {
            int i = arr.length;
            int j = i + objs.length;
            Object[] aobject = (Object[]) Array.newInstance(arr.getClass().getComponentType(), j);
            System.arraycopy(arr, 0, aobject, 0, i);
            System.arraycopy(objs, 0, aobject, i, objs.length);
            return aobject;
        }
    }

    public static int[] toPrimitive(Integer[] arr) {
        if (arr == null) {
            return null;
        } else if (arr.length == 0) {
            return new int[0];
        } else {
            int[] aint = new int[arr.length];

            for (int i = 0; i < aint.length; ++i) {
                aint[i] = arr[i];
            }

            return aint;
        }
    }

    public static float parseFloat(String str, float defVal) {
        try {
            if (str == null) {
                return defVal;
            } else {
                str = str.trim();
                return Float.parseFloat(str);
            }
        } catch (NumberFormatException numberformatexception) {
            return defVal;
        }
    }

    public static float limit(float val, float min, float max) {
        if (val < min) {
            return min;
        } else {
            return Math.min(val, max);
        }
    }

    public static boolean parseBoolean(String str, boolean defVal) {
        try {
            if (str == null) {
                return defVal;
            } else {
                str = str.trim();
                return Boolean.parseBoolean(str);
            }
        } catch (NumberFormatException numberformatexception) {
            return defVal;
        }
    }

    public static Boolean parseBoolean(String str, Boolean defVal) {
        try {
            if (str == null) {
                return defVal;
            } else {
                str = str.trim().toLowerCase();
                if (str.equals("true")) {
                    return Boolean.TRUE;
                } else {
                    return str.equals("false") ? Boolean.FALSE : defVal;
                }
            }
        } catch (NumberFormatException numberformatexception) {
            return defVal;
        }
    }

    public static boolean isFalse(Boolean val) {
        return val != null && !val;
    }

    public static String arrayToString(Object[] arr) {
        return arrayToString(arr, ", ");
    }

    public static String arrayToString(Object[] arr, String separator) {
        if (arr == null) {
            return "";
        } else {
            StringBuffer stringbuffer = new StringBuffer(arr.length * 5);

            for (int i = 0; i < arr.length; ++i) {
                Object object = arr[i];
                if (i > 0) {
                    stringbuffer.append(separator);
                }

                stringbuffer.append(object);
            }

            return stringbuffer.toString();
        }
    }
}

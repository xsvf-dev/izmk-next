package ovo.xsvf.izmk.graphics;

public class GlStateHelper {

    private static GlStateHelper INSTANCE;

    public static GlStateHelper getInstance() {
        if (INSTANCE == null) INSTANCE = new GlStateHelper();
        return INSTANCE;
    }

    private GlStateHelper() {
    }



}

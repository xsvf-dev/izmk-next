package net.optifine.config;

import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.optifine.Config;
import net.optifine.util.BiomeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.regex.Pattern;

public class ConnectedParser {
    private static final Logger log = LogManager.getLogger(ConnectedParser.class);
    private static final Pattern PATTERN_RANGE_SEPARATOR = Pattern.compile("(\\d|\\))-(\\d|\\()");
    private static Map<ResourceLocation, BiomeId> MAP_BIOMES_COMPACT = null;

    private static String stripBrackets(String str) {
        return str.startsWith("(") && str.endsWith(")") ? str.substring(1, str.length() - 1) : str;
    }

    public RangeListInt parseRangeListInt(String str) {
        if (str == null) {
            return null;
        } else {
            RangeListInt rangelistint = new RangeListInt();
            String[] astring = Config.tokenize(str, " ,");

            for (String s : astring) {
                RangeInt rangeint = this.parseRangeInt(s);
                if (rangeint == null) {
                    return null;
                }

                rangelistint.addRange(rangeint);
            }

            return rangelistint;
        }
    }

    private RangeInt parseRangeInt(String str) {
        if (str == null) {
            return null;
        } else if (str.indexOf(45) >= 0) {
            String[] astring = Config.tokenize(str, "-");
            if (astring.length != 2) {
                log.warn("Invalid range: {}", str);
                return null;
            } else {
                int j = Config.parseInt(astring[0], -1);
                int k = Config.parseInt(astring[1], -1);
                if (j >= 0 && k >= 0) {
                    return new RangeInt(j, k);
                } else {
                    log.warn("Invalid range: {}", str);
                    return null;
                }
            }
        } else {
            int i = Config.parseInt(str, -1);
            if (i < 0) {
                log.warn("Invalid integer: {}", str);
                return null;
            } else {
                return new RangeInt(i, i);
            }
        }
    }

    public int parseInt(String str, int defVal) {
        if (str == null) {
            return defVal;
        } else {
            str = str.trim();
            int i = Config.parseInt(str, -1);
            if (i < 0) {
                log.warn("Invalid number: {}", str);
                return defVal;
            } else {
                return i;
            }
        }
    }

    private ResourceLocation makeResourceLocation(String str) {
        try {
            return ResourceLocation.tryParse(str);
        } catch (ResourceLocationException resourcelocationexception) {
            log.warn("Invalid resource location: " + resourcelocationexception.getMessage());
            return null;
        }
    }

    private ResourceLocation makeResourceLocation(String namespace, String path) {
        try {
            return ResourceLocation.fromNamespaceAndPath(namespace, path);
        } catch (ResourceLocationException resourcelocationexception) {
            log.warn("Invalid resource location: " + resourcelocationexception.getMessage());
            return null;
        }
    }

    public BiomeId getBiomeId(String biomeName) {
        biomeName = biomeName.toLowerCase();
        ResourceLocation resourcelocation = this.makeResourceLocation(biomeName);
        if (resourcelocation != null) {
            BiomeId biomeid = BiomeUtils.getBiomeId(resourcelocation);
            if (biomeid != null) {
                return biomeid;
            }
        }

        String s1 = biomeName.replace(" ", "").replace("_", "");
        ResourceLocation resourcelocation1 = this.makeResourceLocation(s1);
        if (MAP_BIOMES_COMPACT == null) {
            MAP_BIOMES_COMPACT = new HashMap<>();

            for (ResourceLocation resourcelocation2 : BiomeUtils.getLocations()) {
                BiomeId biomeid1 = BiomeUtils.getBiomeId(resourcelocation2);
                if (biomeid1 != null) {
                    String s = resourcelocation2.getPath().replace(" ", "").replace("_", "").toLowerCase();
                    ResourceLocation resourcelocation3 = this.makeResourceLocation(resourcelocation2.getNamespace(), s);
                    if (resourcelocation3 != null) {
                        MAP_BIOMES_COMPACT.put(resourcelocation3, biomeid1);
                    }
                }
            }
        }

        return MAP_BIOMES_COMPACT.get(resourcelocation1);
    }

    public BiomeId[] parseBiomes(String str) {
        if (str == null) {
            return null;
        } else {
            str = str.trim();
            boolean flag = false;
            if (str.startsWith("!")) {
                flag = true;
                str = str.substring(1);
            }

            String[] astring = Config.tokenize(str, " ");
            List<BiomeId> list = new ArrayList<>();

            for (String s : astring) {
                BiomeId biomeid = this.getBiomeId(s);
                if (biomeid == null) {
                    log.warn("Biome not found: {}", s);
                } else {
                    list.add(biomeid);
                }
            }

            if (flag) {
                Set<ResourceLocation> set = new HashSet<>(BiomeUtils.getLocations());

                for (BiomeId biomeid1 : list) {
                    set.remove(biomeid1.getResourceLocation());
                }

                list = BiomeUtils.getBiomeIds(set);
            }

            return list.toArray(new BiomeId[0]);
        }
    }

    public RangeListInt parseRangeListIntNeg(String str) {
        if (str == null) {
            return null;
        } else {
            RangeListInt rangelistint = new RangeListInt();
            String[] astring = Config.tokenize(str, " ,");

            for (String s : astring) {
                RangeInt rangeint = this.parseRangeIntNeg(s);
                if (rangeint == null) {
                    return null;
                }

                rangelistint.addRange(rangeint);
            }

            return rangelistint;
        }
    }

    private RangeInt parseRangeIntNeg(String str) {
        if (str == null) {
            return null;
        } else if (str.contains("=")) {
            log.warn("Invalid range: " + str);
            return null;
        } else {
            String s = PATTERN_RANGE_SEPARATOR.matcher(str).replaceAll("$1=$2");
            if (s.indexOf(61) >= 0) {
                String[] astring = Config.tokenize(s, "=");
                if (astring.length != 2) {
                    log.warn("Invalid range: " + str);
                    return null;
                } else {
                    int j = Config.parseInt(stripBrackets(astring[0]), Integer.MIN_VALUE);
                    int k = Config.parseInt(stripBrackets(astring[1]), Integer.MIN_VALUE);
                    if (j != Integer.MIN_VALUE && k != Integer.MIN_VALUE) {
                        return new RangeInt(j, k);
                    } else {
                        log.warn("Invalid range: " + str);
                        return null;
                    }
                }
            } else {
                int i = Config.parseInt(stripBrackets(str), Integer.MIN_VALUE);
                if (i == Integer.MIN_VALUE) {
                    log.warn("Invalid integer: " + str);
                    return null;
                } else {
                    return new RangeInt(i, i);
                }
            }
        }
    }
}

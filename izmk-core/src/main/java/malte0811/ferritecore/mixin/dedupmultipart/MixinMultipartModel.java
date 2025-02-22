package malte0811.ferritecore.mixin.dedupmultipart;

// 我看不懂
// TODO: 研究一下


///**
// * The map implementation used for {@link MultiPartBakedModel#selectorCache} ({@link Object2ObjectOpenCustomHashMap})
// * is not thread-safe, but getQuads is called in parallel in vanilla (and even more so in Forge with
// * alwaysSetupTerrainOffThread=true). The only reason this works for vanilla is that the cache will never contain more
// * than a single blockstate, since a new instance is created for each blockstate (this is probably unintentional, a map
// * would be a weird choice for this scenario). {@link MixinMultipartBuilder} re-uses the equivalent models, so the cache
// * can grow beyond a single element (as is probably intended). If a put-call causes the backing array to be resized
// * concurrent get-calls can (and will) crash, so we need to synchronize them.<br>
// * It is not clear if this implementation (naive synchronization on the cache) is optimal w.r.t.
// * runtime/parallelization, in my experience this part of the code is not runtime-critical enough to put significant
// * effort into fancy parallelization solutions (may change in the future).<br>
// * The increased priority takes care of compatibility with sodium's prio 1000 overwrite. Some versions of sodium come
// * with their own synchronization, but disabling the FC synchronization when sodium's is present is near-impossible. Any
// * decent JIT should be able to remove the inner synchronization since both are on the same final field, so performance
// * should not be an issue.
// */
//// Unresolved reference: Forge adds parameters to getQuads, so the usual remapping process breaks and I need to specify
//// SRG and intermediary names directly, which confuses the MCDev IntelliJ plugin
//// Sync on local/parameter: The parameter is actually always a final field, but which one it is depends on whether
//// sodium is installed or not.
//@SuppressWarnings({"UnresolvedMixinReference", "SynchronizationOnLocalVariableOrMethodParameter"})
//@Mixin(value = MultiPartBakedModel.class, priority = 1100)
//public class MixinMultipartModel {
//    @Redirect(
//            method = {
//                    "getQuads", // Mapped name in MCP, Moj and Yarn
//                    "method_4707", // Intermediary
//                    "emitBlockQuads", // Added by FRAPI, also needs to be synchronized
//                    "getSelectors" // Forge moves the logic into its own method
//            },
//            at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"),
//            remap = false
//    )
//    public <K, V> V redirectCacheGet(Map<K, V> map, K key) {
//        synchronized (map) {
//            return map.get(key);
//        }
//    }
//
//    @Redirect(
//            method = {"getQuads", "method_4707", "emitBlockQuads", "getSelectors"},
//            at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"),
//            remap = false
//    )
//    public <K, V> V redirectCachePut(Map<K, V> map, K key, V value) {
//        synchronized (map) {
//            return map.put(key, value);
//        }
//    }
//}

package ovo.xsvf.izmk;

import kotlin.Unit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ovo.xsvf.common.status.Status;
import ovo.xsvf.common.status.StatusReporter;
import ovo.xsvf.izmk.event.impl.EntryEvent;
import ovo.xsvf.izmk.injection.patch.MinecraftPatch;
import ovo.xsvf.izmk.misc.ClassUtil;
import ovo.xsvf.patchify.Mapping;
import ovo.xsvf.patchify.PatchLoader;
import ovo.xsvf.patchify.asm.MethodWrapper;
import ovo.xsvf.patchify.asm.ReflectionUtil;

import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Entry {
    private static final Logger log = LogManager.getLogger(Entry.class);
    private static final List<Class<?>> PATCHES = new ArrayList<>();

    public static void entry(Instrumentation inst, boolean obfuscated, Map<String, byte[]> classes) throws Throwable {
        // 初始化状态报告器，使用从启动参数中传递的端口号
        try {
            initStatusReporter();
            IZMK.INSTANCE.setStatusReporter(status -> {
                StatusReporter.report(status);
                return Unit.INSTANCE;
            });
        } catch (Exception e) {
            log.warn("无法初始化状态报告器: {}", e.getMessage());
            // 继续执行，不因状态报告器初始化失败而中断
            IZMK.INSTANCE.setStatusReporter(status -> Unit.INSTANCE);
        }

        log.info("Initializing IZMK...");
        ClassUtil.init(inst);
        IZMK.INSTANCE.setClasses(classes);
        IZMK.INSTANCE.setObfuscated(obfuscated);
        if (obfuscated) {
            IZMK.statusReporter.invoke(Status.CORE_SETUP_MAPPING);
            log.info("IZMK running in obfuscated mode");
            InputStream resource = Entry.class.getResourceAsStream("/assets/izmk/mapping.srg");
            if (resource == null) {
                log.error("Mapping file not found, please provide mapping.srg in the root directory of the jar file");
                reportError(Status.ERROR_GENERAL);
                throw new RuntimeException("Mapping file not found");
            }
            Mapping mapping0 = new Mapping(resource.readAllBytes());
            log.info("Mapping loaded: Methods Size: {}, Fields Size: {}",
                    mapping0.methodsMapping.size(), mapping0.fieldMapping.size());
            PatchLoader.mapping = mapping0;
            MethodWrapper.mapping = mapping0;
            ReflectionUtil.mapping = mapping0;
            resource.close();
        }

        PatchLoader.INSTANCE.loadPatch(MinecraftPatch.class, ClassUtil::getClassBytes, ClassUtil::redefineClass);
        IZMK.statusReporter.invoke(Status.CORE_WAIT_INIT_CALLBACK);

        new EntryEvent().post();
    }

    /**
     * 初始化状态报告器
     */
    private static void initStatusReporter() throws Exception {
        // 尝试从系统属性中读取端口
        String portStr = System.getProperty("izmk.status.port");
        if (portStr != null && !portStr.isEmpty()) {
            try {
                int port = Integer.parseInt(portStr);
                StatusReporter.init(port);
                log.info("Status reporter initialized on port {}", port);
            } catch (NumberFormatException e) {
                log.warn("Invalid port number: {}", portStr);
                throw e;
            }
        } else throw new Exception("Izmk status port not found in system properties");
    }

    /**
     * 报告错误状态
     */
    private static void reportError(Status errorStatus) {
        try {
            StatusReporter.report(errorStatus);
        } catch (Exception e) {
            log.warn("报告错误状态时发生异常: {}", e.getMessage());
        }
    }
}

package ovo.xsvf.common.status;

/**
 * 注入过程状态码枚举
 * 定义所有可能的注入过程状态
 */
public enum Status {
    // ===== Loader阶段状态码 (1xxx) =====
    /* ServiceMain Phase */
    LOADER_INIT(1000, "初始化加载器"),
    LOADER_SEARCH_MINECRAFT(1100, "搜索 Minecraft 进程"),
    LOADER_FOUND_MINECRAFT(1101, "已找到 Minecraft 进程"),
    LOADER_CONNECT_PROCESS(1300, "连接目标进程"),

    /* Bootstrap Phase */
    LOADER_FINDING_CLASSLOADER(1200, "等待客户端线程启动"),
    LOADER_EXTRACTING_RESOURCES(1201, "提取资源"),
    LOADER_SETUP_CLASSLOADER(1202, "配置类加载器"),
    LOADER_START_CORE(1400, "调用启动函数"),

    // ===== Core阶段状态码 (2xxx) =====
    CORE_SETUP_MAPPING(2002, "设置映射表"),
    CORE_WAIT_INIT_CALLBACK(2005, "等待 Hook 回调"),
    CORE_PATCH(2003, "处理 Mixin"),

    // ===== 特殊状态码 (9xxx) =====
    SUCCESS(9000, "操作成功完成"),
    ERROR_GENERAL(9900, "发生错误"),
    ERROR_CONNECTION(9901, "无法连接到游戏进程"),
    ERROR_INJECTION(9902, "注入失败");

    private final int code;
    private final String description;

    /**
     * 构造函数
     *
     * @param code        状态码数值
     * @param description 状态描述
     */
    Status(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 从数字状态码获取枚举实例
     */
    public static Status fromCode(int code) {
        for (Status status : values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid status code: " + code);
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
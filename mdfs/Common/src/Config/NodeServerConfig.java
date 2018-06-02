package Config;

public class NodeServerConfig {
    public static String blockMappingPersistFile = "blockMapping.json";
    public static long backupCheckCycle = 5 * 1000; // in ms
    public static long backupInitWaitTime = 10 * 1000; // in ms
}

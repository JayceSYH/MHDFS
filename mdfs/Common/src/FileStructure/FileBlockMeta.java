package FileStructure;

public interface FileBlockMeta {
    // get block no
    int getBlockNo();

    // get file name
    String getFileName();

    // get total block num
    int getTotalBlockNum();

    // is block data available
    boolean isAvailable();
}

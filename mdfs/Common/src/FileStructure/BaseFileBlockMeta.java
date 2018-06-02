package FileStructure;

public class BaseFileBlockMeta implements FileBlockMeta {

    private String fileName;
    private String absPath;
    private int totalBlockNum;
    private int blockNo;
    private int blockSize;

    public BaseFileBlockMeta(String fileName, String absPath, int blockSize, int totalBlockNum, int blockNo) {
        this.fileName = fileName;
        this.totalBlockNum = totalBlockNum;
        this.blockNo = blockNo;
        this.blockSize = blockSize;
        this.absPath = absPath;
    }

    public BaseFileBlockMeta() {

    }

    @Override
    public int getBlockNo() {
        return this.blockNo;
    }

    @Override
    public int getTotalBlockNum() {
        return this.totalBlockNum;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public String getAbsPath() {
        return absPath;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setAbsPath(String absPath) {
        this.absPath = absPath;
    }

    public void setTotalBlockNum(int totalBlockNum) {
        this.totalBlockNum = totalBlockNum;
    }

    public void setBlockNo(int blockNo) {
        this.blockNo = blockNo;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }
}

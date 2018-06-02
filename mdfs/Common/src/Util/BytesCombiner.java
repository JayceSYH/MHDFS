package Util;

import java.util.List;

public class BytesCombiner {
    public static byte[] concatContentParts(List<byte[]> parts) {
        int totalLength = 0;
        for (byte[] array : parts) {
            totalLength += array.length;
        }
        byte[] result = new byte[totalLength];
        int offset = 0;
        for (byte[] array : parts) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }
}

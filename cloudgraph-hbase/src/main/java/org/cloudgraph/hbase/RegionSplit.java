package org.cloudgraph.hbase;

import org.apache.commons.lang.ArrayUtils;
import org.apache.hadoop.hbase.util.Bytes;

public class RegionSplit {
	public static void main(String[] args) {
		int numRegions = 32;
		int keylength = 1;
		byte[] end = new byte[keylength];
		end[0] = (byte) 0xFF;

		byte[][] split = split(ArrayUtils.EMPTY_BYTE_ARRAY, end, numRegions);
		for (byte[] cs : split) {
			System.out.println(Bytes.toStringBinary(cs));
		}
	}

	public static byte[][] split(byte[] firstRowBytes, byte[] lastRowBytes,
			int numRegions) {
		byte[][] splitKeysPlusEndpoints = Bytes.split(firstRowBytes,
				lastRowBytes, numRegions - 1);
		byte[][] splitAtKeys = new byte[splitKeysPlusEndpoints.length - 2][];
		System.arraycopy(splitKeysPlusEndpoints, 1, splitAtKeys, 0,
				splitKeysPlusEndpoints.length - 2);
		return splitAtKeys;
	}

}

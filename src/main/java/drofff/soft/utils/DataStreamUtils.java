package drofff.soft.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.stream.Collectors;

public class DataStreamUtils {

	private static final long SLEEP_TIME_MILLIS = 300;

	private static final String EMPTY_STRING = "";

	private static final String ARRAY_START = "[";
	private static final String ARRAY_DELIMITER = ":";
	private static final String ARRAY_END = "]";

	private DataStreamUtils() {}

	public static int[] readIntArrayFromDataStream(DataInputStream dataInputStream) throws IOException {
		String intArrayStr = dataInputStream.readUTF();
		return parseIntArrayFromStr(intArrayStr);
	}

	private static int[] parseIntArrayFromStr(String intArrayStr) {
		String arrayStr = intArrayStr.replace(ARRAY_START, EMPTY_STRING)
				.replace(ARRAY_END, EMPTY_STRING);
		String[] arrayElementsStr = arrayStr.split(ARRAY_DELIMITER);
		return Arrays.stream(arrayElementsStr)
				.map(Integer::parseInt)
				.mapToInt(x -> x)
				.toArray();
	}

	public static void writeIntArrayToDataStream(int[] array, DataOutputStream dataOutputStream) throws IOException {
		String intArrayStr = Arrays.stream(array)
				.boxed()
				.map(Object::toString)
				.collect(Collectors.joining(ARRAY_DELIMITER));
		String message = ARRAY_START + intArrayStr + ARRAY_END;
		dataOutputStream.writeUTF(message);
	}

	public static void waitTillHasAvailableBytes(DataInputStream inputStream) throws IOException {
		while(hasNoAvailableBytes(inputStream)) {
			CommunicationUtils.sleep(SLEEP_TIME_MILLIS);
		}
	}

	private static boolean hasNoAvailableBytes(InputStream inputStream) throws IOException {
		return inputStream.available() == 0;
	}

}

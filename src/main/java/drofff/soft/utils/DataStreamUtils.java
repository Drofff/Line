package drofff.soft.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class DataStreamUtils {

	private static final long SLEEP_TIME_MILLIS = 300;

	private DataStreamUtils() {}

	public static int[] readIntArrayFromDataStream(DataInputStream dataInputStream) throws IOException {
		List<Integer> result = new ArrayList<>();
		while(dataInputStream.available() > 0) {
			int nextInt = dataInputStream.readInt();
			result.add(nextInt);
		}
		return result.stream()
				.mapToInt(x -> x)
				.toArray();
	}

	public static void writeIntArrayToDataStream(int[] array, DataOutputStream dataOutputStream) throws IOException {
		for(int elem : array) {
			dataOutputStream.write(elem);
		}
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

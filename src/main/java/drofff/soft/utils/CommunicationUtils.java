package drofff.soft.utils;

import java.util.Arrays;
import java.util.Scanner;

public class CommunicationUtils {

	private static final String ANSWERS_DELIMITER = "/";
	private static final String ANSWERS_PREFIX = "[";
	private static final String ANSWERS_SUFFIX = "]";

	private static final String IP_ADDRESS_V4_PATTERN = "^([0-9]{1,3}\\.){3}[0-9]{1,3}$";
	private static final String IP_ADDRESS_V6_PATTERN = "^([0-9a-fA-F]{1,4}){7}[0-9a-fA-F]{1,4}$";

	private static final Scanner SCANNER = new Scanner(System.in);

	private CommunicationUtils() {}

	public static String askQuestion(String question, String ... possibleAnswers) {
		String answers = answersToStr(possibleAnswers);
		System.out.println(question + " " + answers);
		String answer = SCANNER.nextLine();
		while(!isValidAnswer(answer, possibleAnswers)) {
			System.out.println("Invalid answer. Please, use some of these answers " + answers);
			answer = SCANNER.nextLine();
		}
		return answer;
	}

	private static boolean isValidAnswer(String answer, String ... possibleAnswers) {
		return Arrays.asList(possibleAnswers).contains(answer);
	}

	private static String answersToStr(String ... answers) {
		return ANSWERS_PREFIX + String.join(ANSWERS_DELIMITER, answers) + ANSWERS_SUFFIX;
	}

	public static String askForIpAddress(String text) {
		System.out.println(text);
		String ipAddress = SCANNER.nextLine();
		while(!isValidIpAddress(ipAddress)) {
			System.out.println("Invalid IP address format");
			ipAddress = SCANNER.nextLine();
		}
		return ipAddress;
	}

	private static boolean isValidIpAddress(String ipAddress) {
		return ipAddress.matches(IP_ADDRESS_V4_PATTERN) || ipAddress.matches(IP_ADDRESS_V6_PATTERN);
	}

	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch(InterruptedException e) {
			System.out.println("Unexpected interrupt: " + e.getMessage());
		}
	}

}

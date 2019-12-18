package drofff.soft.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import drofff.soft.exception.LineException;

public class Properties {

	private static final String PROPERTIES_FILENAME = "application.properties";

	private java.util.Properties applicationProperties;

	public Properties() {
		try {
			readPropertiesFromFile();
		} catch(IOException e) {
			throw new LineException("Error while opening properties file:\n" + e.getMessage());
		}
	}

	private void readPropertiesFromFile() throws IOException {
		InputStream inputStream = Properties.class.getClassLoader().getResourceAsStream(PROPERTIES_FILENAME);
		applicationProperties = new java.util.Properties();
		applicationProperties.load(inputStream);
	}

	public String getPropertyByKey(String key) {
		String property = applicationProperties.getProperty(key);
		return Optional.ofNullable(property)
				.orElseThrow(() -> new LineException("Property with such key do not exists"));
	}

	public void setProperty(String key, String value) {
		applicationProperties.setProperty(key, value);
	}

}

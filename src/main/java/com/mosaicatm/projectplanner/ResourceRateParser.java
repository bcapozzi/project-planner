package com.mosaicatm.projectplanner;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class ResourceRateParser {

	
	public static Map<String, Integer> parseResourceRates(String filePath) throws Exception {
		Map<String, Integer> resourceRatesPerHour = new HashMap<String, Integer>();
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		String nextLine = reader.readLine(); // skip header
		while ((nextLine = reader.readLine()) != null) {
			String [] tokens = nextLine.split(",");
			if (tokens.length != 2) {
				reader.close();
				throw new RuntimeException("Formatting error: expected 2 tokens per line, but got " + tokens);
			}
			
			String resource = tokens[0].trim();
			int dollars = Integer.parseInt(tokens[1].trim());
			resourceRatesPerHour.put(resource, dollars);
		}
		reader.close();
		return resourceRatesPerHour;
	}
}

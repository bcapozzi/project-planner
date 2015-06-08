package com.mosaicatm.projectplanner.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.mosaicatm.projectplanner.ProjectPlanner;
import com.mosaicatm.projectplanner.ResourceRateParser;

public class ProjectPlannerTests {

	@Test public void canCreateMainWindow() throws Exception {
		
		Map<String,Integer> resourceRates = ResourceRateParser.parseResourceRates("src/main/resources/resource_costs.csv");
		ProjectPlanner planner = ProjectPlanner.createAndShowGui(resourceRates);
		//Thread.sleep(100*1000);
	}

	/*private Map<String, Integer> parseResourceRates(String filePath) throws Exception {
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
	*/
}

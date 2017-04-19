package io.github.agentsoz.bushfire.matsimjill;

/*
 * #%L
 * BDI-ABM Integration Package
 * %%
 * Copyright (C) 2014 - 2015 by its authors. See AUTHORS file.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import io.github.agentsoz.bushfire.datamodels.Location;
import io.github.agentsoz.bushfire.datamodels.Region;
import io.github.agentsoz.bushfire.datamodels.ReliefCentre;
import io.github.agentsoz.bushfire.datamodels.Route;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * 
 * Utility class to hold configuration information
 *
 */
public class SimpleConfig {

	private static final Logger logger = LoggerFactory.getLogger("io.github.agentsoz.bushfire");

	private static String configFile = null;

	private static String matSimFile = null;
	private static String geographyFile = null;
	private static String fireFile = null;

	private static int numBDIAgents = 1;

	private static double proportionWithKids = 0.0;
	private static double proportionWithRelatives = 0.0;
	private static int maxDistanceToRelatives = 1000;
	
	private static double[] evacDelay = new double[] { 0.0, 0.0 };
	private static HashMap<String, Location> locations = new HashMap<String, Location>();
	private static HashMap<String, Region> regions = new HashMap<String, Region>();
	private static HashMap<String, Route> routes = new HashMap<String, Route>();
	public static HashMap<String, ReliefCentre> reliefCentres = new HashMap<String, ReliefCentre>();
	private static String fireCoordinateSystem = "longlat";
	private static String fireFileFormat = "custom";
	private static String geographyCoordinateSystem = "longlat";

	private static final String eSimulation = "simulation";
	private static final String eMATSimFile = "matsimfile";
	private static final String eFireFile = "firefile";
	private static final String eGeographyFile = "geographyfile";
	private static final String eNumBDI = "bdiagents";
	private static final String eName = "name";
	private static final String eCoordinates = "coordinates";
	private static final String eFormat = "format";
	private static final String eTrafficBehaviour = "trafficBehaviour";
	private static final String ePreEvacDetour = "preEvacDetour";
	private static final String eProportion = "proportion";
	private static final String eRadiusInMtrs = "radiusInMtrs";
	private static final String eGeography = "geography";
	private static final String eCoordinateSystem = "coordinateSystem";
	private static final String eDestinations = "destinations";
	private static final String eLocation = "location";
	
	public static String getMatSimFile() {
		return matSimFile;
	}

	public static String getFireFile() {
		return fireFile;
	}

	public static int getNumBDIAgents() {
		return numBDIAgents;
	}

	public static double getProportionWithKids() {
		return proportionWithKids;
	}

	public static double getProportionWithRelatives() {
		return proportionWithRelatives;
	}

	public static int getMaxDistanceToRelatives() {
		return maxDistanceToRelatives;
	}

	public static double[] getEvacDelay() {
		return evacDelay;
	}

	public static String getGeographyCoordinateSystem() {
		return geographyCoordinateSystem;
	}

	public static String getFireFireCoordinateSystem() {
		return fireCoordinateSystem;
	}

	public static String getFireFireFormat() {
		return fireFileFormat;
	}

	public static Location getLocation(String name) {
		return locations.get(name);
	}

	private static Region getRegion(String name) {
		return regions.get(name);
	}

	private static Set<String> getRegionsByName() {
		return regions.keySet();
	}

	private static Collection<Region> getRegions() {
		return regions.values();
	}

	private static Route getRoute(String name) {
		return routes.get(name);
	}

	private static Set<String> getRoutes() {
		return routes.keySet();
	}

	private static ReliefCentre getReliefCentre(String name) {
		return reliefCentres.get(name);
	}

	private static Set<String> getReliefCentres() {
		return reliefCentres.keySet();
	}

	public static String getConfigFile() {
		return configFile;
	}

	public static void setConfigFile(String string) {
		configFile = string;
	}

	/**
	 * Pick one of the listed evac points
	 */
	public static Location getRandomEvacLocation() {
		int n = new Random().nextInt(locations.size());
		Location rc = (Location) locations.values().toArray()[n];
		return rc;
	}

	public static void readConfig() throws Exception {
		if (configFile == null) {
			throw new Exception("No configuration file given");
		}
		
		logger.info("Loading configuration from '" + configFile + "'");
		
		// Validate the XML against the schema first
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = factory.newSchema(); // schema specified in file
		Validator validator = schema.newValidator();
		validator.validate(new StreamSource(new File(configFile)));
		
		// Now we can start reading; we don't need to add many checks since
		// the XML is at this point already validated
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(new FileInputStream(configFile));
		
	    // Normalisation is optional, but recommended
	    // see http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
	    doc.getDocumentElement().normalize();
	    
	    // Get the root element
		Element root = (Element)doc.getElementsByTagName(eSimulation).item(0);
		
		// Get MATSim config file name
		matSimFile = doc.getElementsByTagName(eMATSimFile).item(0).getTextContent().replaceAll("\n", "").trim();
		
		// Get the fire data
		Element element = (Element)root.getElementsByTagName(eFireFile).item(0);
		fireFile = element.getElementsByTagName(eName).item(0).getTextContent().replaceAll("\n", "").trim();
		fireCoordinateSystem = element.getElementsByTagName(eCoordinates).item(0).getTextContent().replaceAll("\n", "").trim();
		fireFileFormat = element.getElementsByTagName(eFormat).item(0).getTextContent();
		
		// Get the geography data
		element = (Element)root.getElementsByTagName(eGeographyFile).item(0);
		geographyFile = element.getElementsByTagName(eName).item(0).getTextContent().replaceAll("\n", "").trim();
		
		// Get the number of BDI agents
		numBDIAgents = Integer.parseInt(root.getElementsByTagName(eNumBDI).item(0).getTextContent().replaceAll("\n", "").trim());
		
		// Get the traffic detour behaviour
		element = (Element)root.getElementsByTagName(eTrafficBehaviour).item(0);
		element = (Element)element.getElementsByTagName(ePreEvacDetour).item(0);
		proportionWithRelatives = Double.parseDouble(root.getElementsByTagName(eProportion).item(0).getTextContent().replaceAll("\n", "").trim());
		maxDistanceToRelatives = Integer.parseInt(root.getElementsByTagName(eRadiusInMtrs).item(0).getTextContent().replaceAll("\n", "").trim());

		// Now read the geography file
		readGeography();
	}

	private static void readGeography() throws Exception {
		if (geographyFile == null) {
			throw new Exception("No geography file given");
		}
		
		logger.info("Loading geography from '" + geographyFile + "'");
		
		// Validate the XML against the schema first
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = factory.newSchema(); // schema specified in file
		Validator validator = schema.newValidator();
		validator.validate(new StreamSource(new File(geographyFile)));
		
		// Now we can start reading; we don't need to add many checks since
		// the XML is at this point already validated
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(new FileInputStream(geographyFile));
		
	    // Normalisation is optional, but recommended
	    // see http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
	    doc.getDocumentElement().normalize();

	    // Get the root element
		Element root = (Element)doc.getElementsByTagName(eGeography).item(0);
		
		// Get the coordinate system
		geographyCoordinateSystem = root.getElementsByTagName(eCoordinateSystem).item(0).getTextContent().replaceAll("\n", "").trim();

		// Get the locations
		Element dests = (Element)root.getElementsByTagName(eDestinations).item(0);
		NodeList nl = dests.getElementsByTagName(eLocation);
		for (int i = 0; i < nl.getLength(); i++) {
			Element location = (Element)nl.item(i);
			String name = location.getElementsByTagName(eName).item(0).getTextContent().replaceAll("\n", "").trim();
			String s = location.getElementsByTagName(eCoordinates).item(0).getTextContent().replaceAll("\n", "").trim();
			String[] sCoords = s.split(",");
			double x = Double.parseDouble(sCoords[0]);
			double y = Double.parseDouble(sCoords[1]);
			locations.put(name, new Location(name, x, y));
		}
	}
}
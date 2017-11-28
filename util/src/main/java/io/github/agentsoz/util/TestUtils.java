package io.github.agentsoz.util;

/*
 * #%L
 * BDI-ABM Integration Package
 * %%
 * Copyright (C) 2014 - 2017 by its authors. See AUTHORS file.
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

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.VehicleEntersTrafficEvent;
import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.events.handler.VehicleEntersTrafficEventHandler;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsManagerImpl;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.utils.io.UncheckedIOException;
import org.matsim.core.utils.misc.CRCChecksum;
import org.matsim.utils.eventsfilecomparison.EventsFileComparator;
import org.matsim.utils.eventsfilecomparison.EventsFileComparator.Result;

/**
 * There is already a MatsimTestUtils.  So it might seem logical to add the methods here to that class.  However, 
 * that class is _not_ a collection of static methods, but rather something that becomes instantiated.  So
 * I am putting these here into a separate class.
 * 
 * @author kainagel
 *
 */
public final class TestUtils {
	private static final Logger log = Logger.getLogger( TestUtils.class ) ;
	
	private TestUtils(){}  // do not instantiate

	public static void compareEventsWithSlack(SortedMap<Id<Person>, List<Double>> arrivalsExpected,
			SortedMap<Id<Person>, List<Double>> arrivalsActual, double slack) {
		Assert.assertEquals( arrivalsExpected.size(), arrivalsActual.size() ) ;
		Iterator<Entry<Id<Person>, List<Double>>> itActual = arrivalsActual.entrySet().iterator() ;
		Iterator<Entry<Id<Person>, List<Double>>> itExpected = arrivalsExpected.entrySet().iterator() ;
		double differencesSum = 0. ;
		long differencesCnt = 0 ;
		while ( itActual.hasNext() ) {
			Entry<Id<Person>, List<Double>> actual = itActual.next() ;
			Entry<Id<Person>, List<Double>> expected = itExpected.next() ;
			Assert.assertEquals( "finding a different personId: ", expected.getKey(), actual.getKey() );
			Assert.assertEquals( "person has different number of arrivals: ", expected.getValue().size(), actual.getValue().size() ) ;
			Iterator<Double> itExpectedArrivals = expected.getValue().iterator() ; 
			Iterator<Double> itActualArrivals = actual.getValue().iterator() ;
			while ( itExpectedArrivals.hasNext() ) {
				double expectedArrival = itExpectedArrivals.next() ;
				double actualArrival = itActualArrivals.next() ;
				if ( expectedArrival != actualArrival ) {
					final double difference = actualArrival-expectedArrival;
					differencesSum += difference ;
					differencesCnt ++ ;
					log.warn("personId=" + expected.getKey()
					+ ";\texpectedTime=" + expectedArrival
					+ ";\tactualTime=" + actualArrival
					+ ";\tdifference=" + difference );
				}
				Assert.assertEquals(expectedArrival, actualArrival,slack);
			}
		}
		if ( differencesCnt > 0 ) {
			log.warn( "differencesSum=" + differencesSum + ";\tdifferencesAv=" + differencesSum/differencesCnt );
		}
	}

	public static SortedMap<Id<Person>, List<Double>> collectArrivals(final String filename) {
		SortedMap<Id<Person>,List<Double>> actualArrivals = new TreeMap<>() ;
		EventsManager events = new EventsManagerImpl() ;
		events.addHandler(new PersonArrivalEventHandler(){
			@Override public void handleEvent(PersonArrivalEvent event) {
				Id<Person> personId = event.getPersonId() ;
				if ( !actualArrivals.containsKey(personId) ) {
					actualArrivals.put(personId, new ArrayList<Double>() ) ;
				}
				List<Double> list = actualArrivals.get( personId ) ;
				list.add( event.getTime() ) ;
			}
		});
		new MatsimEventsReader(events).readFile(filename);
		return actualArrivals ;
	}

	public static SortedMap<Id<Person>, List<Double>> collectDepartures(final String filename) {
		SortedMap<Id<Person>,List<Double>> actualDepartures = new TreeMap<>() ;
		EventsManager events = new EventsManagerImpl() ;
		events.addHandler(new PersonDepartureEventHandler(){
			@Override public void handleEvent(PersonDepartureEvent event) {
				Id<Person> personId = event.getPersonId() ;
				if ( !actualDepartures.containsKey(personId) ) {
					actualDepartures.put(personId, new ArrayList<Double>() ) ;
				}
				List<Double> list = actualDepartures.get( personId ) ;
				list.add( event.getTime() ) ;
			}
		});
		new MatsimEventsReader(events).readFile(filename);
		return actualDepartures ;
	}

	public static SortedMap<Id<Person>, List<Double>> collectEnterTraffic(final String filename) {
		SortedMap<Id<Person>,List<Double>> actualDepartures = new TreeMap<>() ;
		EventsManager events = new EventsManagerImpl() ;
		events.addHandler(new VehicleEntersTrafficEventHandler(){
			@Override public void handleEvent(VehicleEntersTrafficEvent event) {
				Id<Person> personId = event.getPersonId() ;
				if ( !actualDepartures.containsKey(personId) ) {
					actualDepartures.put(personId, new ArrayList<Double>() ) ;
				}
				List<Double> list = actualDepartures.get( personId ) ;
				list.add( event.getTime() ) ;
			}
		});
		new MatsimEventsReader(events).readFile(filename);
		return actualDepartures ;
	}

	public static List<Long> checkSeveralFiles(final String cmpFileName, String baseDir ) {
		List<Long> expecteds = new ArrayList<>() ;
	
		try {
			Files.walkFileTree(new File(baseDir).toPath(), new SimpleFileVisitor<Path>() {
				@Override public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					final String filename = dir + "/" + cmpFileName;
					if ( Files.exists( new File( filename).toPath() ) ) {
						log.info( "checking against " + filename );
						long crc = CRCChecksum.getCRCFromFile( filename ) ; 
						expecteds.add(crc) ;
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			throw new UncheckedIOException(e.getMessage(), e);
		}
	
		return expecteds ;
	}

	public static void checkSeveral(List<Long> expecteds, long actualEvents) {
		long [] expectedsArray = new long[expecteds.size()] ;
		for ( int ii=0 ; ii<expecteds.size() ; ii++ ) {
			expectedsArray[ii] = expecteds.get(ii) ;
		}
		TestUtils.checkSeveral( expectedsArray, actualEvents ) ;
	}

	public static void checkSeveral(long[] expectedEvents, long actualEvents) {
		boolean found = false ;
		for ( int ii=0 ; ii<expectedEvents.length ; ii++ ) {
			final boolean b = actualEvents==expectedEvents[ii];
			log.info("checking if " + actualEvents + "==" + expectedEvents[ii] + " ? " + b);
			if ( b ) {
				found = true ;
				break ;
			}
		}
		if ( !found ) {
			Assert.fail(); 
		}
	}

	static void comparingArrivals(final String primaryExpectedEventsFilename, String actualEventsFilename) {
		log.info("Comparing arrivals:");
		SortedMap<Id<Person>, List<Double>> arrivalsExpected = 
				collectArrivals(primaryExpectedEventsFilename) ;
		SortedMap<Id<Person>, List<Double>> arrivalsActual = 
				collectArrivals(actualEventsFilename) ;
		compareEventsWithSlack(arrivalsExpected, arrivalsActual, 20.);
		log.info("Arrivals: Comparison with slack: passed.");
		log.info("") ;
	
		Assert.assertEquals(arrivalsExpected, arrivalsActual);
		log.info("Arrivals: Exact comparison: passed.");
	}

	static void comparingEnterTraffic(final String primaryExpectedEventsFilename, String actualEventsFilename) {
		log.info("Comparing enterTraffic:");
		SortedMap<Id<Person>, List<Double>> enterTrafficExpected = 
				collectEnterTraffic(primaryExpectedEventsFilename) ;
		SortedMap<Id<Person>, List<Double>> enterTrafficActual = 
				collectEnterTraffic(actualEventsFilename) ;
		compareEventsWithSlack(enterTrafficExpected, enterTrafficActual, 20.);
		log.info("EnterTraffic: Comparison with slack: passed.");
		log.info("") ;
	}

	static void comparingDepartures(final String primaryExpectedEventsFilename, String actualEventsFilename) {
		log.info("Comparing departures:");
		SortedMap<Id<Person>, List<Double>> departuresExpected = 
				collectDepartures(primaryExpectedEventsFilename) ;
		SortedMap<Id<Person>, List<Double>> departuresActual = 
				collectDepartures(actualEventsFilename) ;
		compareEventsWithSlack(departuresExpected, departuresActual, 20.);
		log.info("Departures: Comparison with slack: passed.");
		log.info("") ;
	}

	static void compareFullEvents(final String primaryExpectedEventsFilename, String actualEventsFilename) {
		Result result = EventsFileComparator.compare(primaryExpectedEventsFilename, actualEventsFilename) ;
		log.info("Full events file: comparison: result=" + result.name() );
		Assert.assertEquals(Result.FILES_ARE_EQUAL, result);
	}

}
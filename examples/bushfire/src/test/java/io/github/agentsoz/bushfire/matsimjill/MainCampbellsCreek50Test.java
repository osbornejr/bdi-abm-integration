/**
 * 
 */
package io.github.agentsoz.bushfire.matsimjill;

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

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
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
import org.matsim.testcases.MatsimTestUtils;
/**
 * @author dsingh
 *
 */
public class MainCampbellsCreek50Test {
	// have tests in separate classes so that they run, at least under maven, in separate JVMs.  kai, nov'17

	@Rule public MatsimTestUtils utils = new MatsimTestUtils() ;

	@Test
	public void testCampbellsCreek50() {

		String [] args = {
				"--config",  "scenarios/campbells-creek/scenario_main.xml", 
				"--logfile", "scenarios/campbells-creek/scenario.log",
				"--loglevel", "INFO",
				//	                "--plan-selection-policy", "FIRST", // ensures it is deterministic, as default is RANDOM
				"--seed", "12345",
				"--safeline-output-file-pattern", "scenarios/campbells-creek/safeline.%d%.out",
				"--matsim-output-directory", utils.getOutputDirectory(),
				"--jillconfig", "--config={"+
						"agents:[{classname:io.github.agentsoz.bushfire.matsimjill.agents.Resident, args:null, count:50}],"+
						"logLevel: WARN,"+
						"logFile: \"scenarios/campbells-creek/jill.log\","+
						"programOutputFile: \"scenarios/campbells-creek/jill.out\","+
						"randomSeed: 12345,"+ // jill random seed
						"numThreads: 1"+ // run jill in single-threaded mode so logs are deterministic
		"}"};

		Main.main(args);
		

		// first print out the actuals so we have them even if it fails afterwards
		final String filenameActuals = utils.getOutputDirectory() + "/output_events.xml.gz";
		long actualEvents = CRCChecksum.getCRCFromFile( filenameActuals ) ;
		System.err.println("actual(events)="+actualEvents) ;
		
		long actualPlans = CRCChecksum.getCRCFromFile( utils.getOutputDirectory() + "/output_plans.xml.gz" ) ;
		System.err.println("actual(plans)="+actualPlans) ;

		SortedMap<Id<Person>, List<Double>> arrivalsActual = collectArrivals(filenameActuals);

		final String filenameExpected = utils.getInputDirectory() + "/output_events.xml.gz" ;
		SortedMap<Id<Person>, List<Double>> arrivalsExpected = collectArrivals(filenameExpected);
		
//		Assert.assertEquals( arrivalsExpected, arrivalsActual ) ;
		// if I see this correctly, both the assert implementation uses equals in the correct way, and
		// the collections implement it in the way we need it. kai, nov'17
		// yyyyyy but we want to give it some slack!!
		
		compareEventsWithSlack(arrivalsExpected, arrivalsActual, 5.);

		// now do the comparisons:
		{
			List<Long> expecteds = checkSeveralFiles("output_events.xml.gz", utils.getInputDirectory() ) ;
			expecteds.add(3114851905L) ; // travis
			checkSeveral(expecteds, actualEvents);
		}
		{
			List<Long> expecteds = checkSeveralFiles("output_plans.xml.gz", utils.getInputDirectory() ) ;
			expecteds.add(2234597649L) ; // travis
			checkSeveral(expecteds, actualPlans);
		}

	}

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
			Assert.assertEquals( expected.getKey(), actual.getKey() );
			Assert.assertEquals( expected.getValue().size(), actual.getValue().size() ) ;
			Iterator<Double> itExpectedArrivals = expected.getValue().iterator() ; 
			Iterator<Double> itActualArrivals = actual.getValue().iterator() ;
			while ( itExpectedArrivals.hasNext() ) {
				double expectedArrival = itExpectedArrivals.next() ;
				double actualArrival = itActualArrivals.next() ;
				if ( expectedArrival != actualArrival ) {
					final double difference = actualArrival-expectedArrival;
					differencesSum += difference ;
					differencesCnt ++ ;
					System.err.println( "personId=" + expected.getKey()
					+ ";\texpectedTime=" + expectedArrival
					+ ";\tactualTime=" + actualArrival
					+ ";\tdifference=" + difference );
				}
				Assert.assertEquals(expectedArrival, actualArrival,slack);
			}
		}
		if ( differencesCnt > 0 ) {
			System.err.println( "differencesSum=" + differencesSum + ";\tdifferencesAv=" + differencesSum/differencesCnt );
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
						System.err.println( "checking against " + filename );
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
		checkSeveral( expectedsArray, actualEvents ) ;
	}
	public static void checkSeveral(long[] expectedEvents, long actualEvents) {
		boolean found = false ;
		for ( int ii=0 ; ii<expectedEvents.length ; ii++ ) {
			final boolean b = actualEvents==expectedEvents[ii];
			System.err.println("checking if " + actualEvents + "==" + expectedEvents[ii] + " ? " + b);
			if ( b ) {
				found = true ;
				break ;
			}
		}
		if ( !found ) {
			Assert.fail(); 
		}
	}

}

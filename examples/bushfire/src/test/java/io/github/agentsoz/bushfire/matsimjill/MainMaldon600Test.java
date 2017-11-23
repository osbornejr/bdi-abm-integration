/**
 * 
 */
package io.github.agentsoz.bushfire.matsimjill;

import java.util.List;
import java.util.SortedMap;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.utils.misc.CRCChecksum;
import org.matsim.testcases.MatsimTestUtils;
import org.matsim.utils.eventsfilecomparison.EventsFileComparator;
import org.matsim.utils.eventsfilecomparison.EventsFileComparator.Result;
/**
 * @author dsingh
 *
 */
public class MainMaldon600Test {
	// have tests in separate classes so that they run, at least under maven, in separate JVMs.  kai, nov'17

	@Rule public MatsimTestUtils utils = new MatsimTestUtils() ;


	@SuppressWarnings("static-method")
	@Test
	public void testMaldon600() {

		/*
--config /var/www/data/user-data/2017-10-23-ds-maldon/scenario/scenario_main.xml 
--logfile /var/www/data/user-data/2017-10-23-ds-maldon/scenario/scenario.log 
--loglevel TRACE 
--jillconfig "--config={agents:[{classname:io.github.agentsoz.bushfire.matsimjill.agents.Resident, args:null, count:600}],logLevel: WARN,logFile: \"/var/www/data/user-data/2017-10-23-ds-maldon/scenario/jill.log\",programOutputFile: \"/var/www/data/user-data/2017-10-23-ds-maldon/scenario/jill.out\"}"
		 */
		String [] args = {
				"--config",  "scenarios/maldon-2017-11-01/scenario_main.xml", 
				"--logfile", "scenarios/maldon-2017-11-01/scenario.log",
				"--loglevel", "INFO",
				//				"--plan-selection-policy", "FIRST", // ensures it is deterministic, as default is RANDOM
				"--seed", "12345",
				"--safeline-output-file-pattern", "scenarios/campbells-creek/safeline.%d%.out",
				"--matsim-output-directory", utils.getOutputDirectory(),
				"--jillconfig", "--config={"+
						"agents:[{classname:io.github.agentsoz.bushfire.matsimjill.agents.Resident, args:null, count:600}],"+
						"logLevel: WARN,"+
						"logFile: \"scenarios/maldon-2017-11-01/jill.log\","+
						"programOutputFile: \"scenarios/maldon-2017-11-01/jill.out\","+
						"randomSeed: 12345,"+ // jill random seed
						"numThreads: 1"+ // run jill in single-threaded mode so logs are deterministic
						"}"
		};

		boolean playback = false ;
		if ( !playback) {
			Main.main(args);
		}

		final String primaryExpectedEventsFilename = utils.getInputDirectory() + "/0/output_events.xml.gz";
		long [] expectedEvents = new long [] {
				CRCChecksum.getCRCFromFile( primaryExpectedEventsFilename ), 
				//CRCChecksum.getCRCFromFile( utils.getInputDirectory() + "/1/output_events.xml.gz" ), 
				//CRCChecksum.getCRCFromFile( utils.getInputDirectory() + "/1/output_events.xml.gz" ), 
				// 3214464728 mvn console, eclipse single
				//CRCChecksum.getCRCFromFile( utils.getInputDirectory() + "/2/output_events.xml.gz" ), 
				// 1316710466 eclipse in context
				//              CRCChecksum.getCRCFromFile( utils.getInputDirectory() + "/3/output_events.xml.gz" ) 
				4211426679L // travis
		} ;

		long [] expectedPlans = new long [] {
				CRCChecksum.getCRCFromFile( utils.getInputDirectory() + "/0/output_plans.xml.gz" ), 
				//CRCChecksum.getCRCFromFile( utils.getInputDirectory() + "/1/output_plans.xml.gz" ), 
				// 1884178769 mvn console, eclipse single
				//CRCChecksum.getCRCFromFile( utils.getInputDirectory() + "/2/output_plans.xml.gz" ),
				// eclipse in context
				//              CRCChecksum.getCRCFromFile( utils.getInputDirectory() + "/3/output_plans.xml.gz" )
				3715899067L // travis
		} ;

		String actualEventsFilename = utils.getOutputDirectory() + "/output_events.xml.gz";
		if ( playback ) {
			actualEventsFilename = utils.getOutputDirectory() + "../bak/output_events.xml.gz";
		}
		System.err.println( "actualEventsFilename=" + actualEventsFilename );
		long actualEvents = CRCChecksum.getCRCFromFile( actualEventsFilename ) ;
		System.err.println("actual(events)="+actualEvents) ;

		long actualPlans = CRCChecksum.getCRCFromFile( utils.getOutputDirectory() + "/output_plans.xml.gz" ) ;
		if ( playback ) {
			actualPlans = CRCChecksum.getCRCFromFile( utils.getOutputDirectory() + "../bak/output_plans.xml.gz" ) ;
		}
		System.err.println("actual(plans)="+actualPlans) ;

		System.err.println() ;
		{
			System.err.println("Comparing departures:");
			SortedMap<Id<Person>, List<Double>> departuresExpected = 
					MainCampbellsCreek50Test.collectDepartures(primaryExpectedEventsFilename) ;
			SortedMap<Id<Person>, List<Double>> departuresActual = 
					MainCampbellsCreek50Test.collectDepartures(actualEventsFilename) ;
			MainCampbellsCreek50Test.compareEventsWithSlack(departuresExpected, departuresActual, 20.);
			System.err.println("Departures: Comparison with slack: passed.");
			System.err.println() ;
		}
		{
			System.err.println("Comparing enterTraffic:");
			SortedMap<Id<Person>, List<Double>> enterTrafficExpected = 
					MainCampbellsCreek50Test.collectEnterTraffic(primaryExpectedEventsFilename) ;
			SortedMap<Id<Person>, List<Double>> enterTrafficActual = 
					MainCampbellsCreek50Test.collectEnterTraffic(actualEventsFilename) ;
			MainCampbellsCreek50Test.compareEventsWithSlack(enterTrafficExpected, enterTrafficActual, 20.);
			System.err.println("EnterTraffic: Comparison with slack: passed.");
			System.err.println() ;
		}
		{
			System.err.println("Comparing arrivals:");
			SortedMap<Id<Person>, List<Double>> arrivalsExpected = 
					MainCampbellsCreek50Test.collectArrivals(primaryExpectedEventsFilename) ;
			SortedMap<Id<Person>, List<Double>> arrivalsActual = 
					MainCampbellsCreek50Test.collectArrivals(actualEventsFilename) ;
			MainCampbellsCreek50Test.compareEventsWithSlack(arrivalsExpected, arrivalsActual, 20.);
			System.err.println("Arrivals: Comparison with slack: passed.");
			System.err.println() ;

			Assert.assertEquals(arrivalsExpected, arrivalsActual);
			System.err.println("Arrivals: Exact comparison: passed.");
		}

		Result result = EventsFileComparator.compare(primaryExpectedEventsFilename, actualEventsFilename) ;
		System.err.println("Full events file: comparison: result=" + result.name() );
		Assert.assertEquals(Result.FILES_ARE_EQUAL, result);

		MainCampbellsCreek50Test.checkSeveral(expectedEvents, actualEvents);
		MainCampbellsCreek50Test.checkSeveral(expectedPlans, actualPlans);

		// FIXME: still small differences in jill log. dsingh, 06/Nov/17
		// expectedCRC = CRCChecksum.getCRCFromFile( utils.getInputDirectory() + "/jill.out" ) ;
		// actualCRC = CRCChecksum.getCRCFromFile( "scenarios/maldon-2017-11-01/jill.out" ) ;
		// Assert.assertEquals (expectedCRC, actualCRC); 

	}
}

/**
 * 
 */
package io.github.agentsoz.ees;

import io.github.agentsoz.bdimatsim.EvacConfig;
import io.github.agentsoz.bdimatsim.MATSimModel;
import io.github.agentsoz.util.TestUtils;
import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.matsim.core.utils.misc.CRCChecksum;
import org.matsim.testcases.MatsimTestUtils;

/**
 * @author dsingh
 *
 */
public class FireAreaMaldon600Test {
	// have tests in separate classes so that they run, at least under maven, in separate JVMs.  kai, nov'17
	
	private static final Logger log = Logger.getLogger(FireAreaMaldon600Test.class) ;

	@Rule public MatsimTestUtils utils = new MatsimTestUtils() ;


	@SuppressWarnings("static-method")
	@Test
	@Ignore // I don't think that this test is contributing anything useful beyond the other tests that we have.  kai, dec'17
	public void testMaldon600() {

		/*
--config /var/www/data/user-data/2017-10-23-ds-maldon/scenario/scenario_main.xml 
--logfile /var/www/data/user-data/2017-10-23-ds-maldon/scenario/scenario.log 
--loglevel TRACE 
--jillconfig "--config={agents:[{classname:Resident, args:null, count:600}],logLevel: WARN,logFile: \"/var/www/data/user-data/2017-10-23-ds-maldon/scenario/jill.log\",programOutputFile: \"/var/www/data/user-data/2017-10-23-ds-maldon/scenario/jill.out\"}"
		 */
		String [] args = {
				"--config",  "scenarios/maldon-2017-11-01/scenario_main.xml", 
				"--logfile", "scenarios/maldon-2017-11-01/scenario.log",
				"--loglevel", "INFO",
				//				"--plan-selection-policy", "FIRST", // ensures it is deterministic, as default is RANDOM
				"--seed", "12345",
				"--safeline-output-file-pattern", "scenarios/campbells-creek/safeline.%d%.out",
				MATSimModel.MATSIM_OUTPUT_DIRECTORY_CONFIG_INDICATOR, utils.getOutputDirectory(),
				"--jillconfig", "--config={"+
						"agents:[{classname:io.github.agentsoz.ees.agents.Resident, args:null, count:600}],"+
						"logLevel: WARN,"+
						"logFile: \"scenarios/maldon-2017-11-01/jill.log\","+
						"programOutputFile: \"scenarios/maldon-2017-11-01/jill.out\","+
						"randomSeed: 12345"+ // jill random seed
						//"numThreads: 1"+ // run jill in single-threaded mode so logs are deterministic
						"}"
		};

		Main.main(args);

		// print these so that we have them:
		final String actualEventsFilename = utils.getOutputDirectory() + "/output_events.xml.gz";
		long actualEventsCRC = CRCChecksum.getCRCFromFile( actualEventsFilename ) ;
		log.warn("actual(events)="+actualEventsCRC) ;

		long actualPlansCRC = CRCChecksum.getCRCFromFile( utils.getOutputDirectory() + "/output_plans.xml.gz" ) ;
		log.warn("actual(plans)="+actualPlansCRC) ;
		
		// return early since we don't have results yet against which to compare:
		if ( true ) {
			return;
		}
		
		// look into events:

		final String primaryExpectedEventsFilename = utils.getInputDirectory() + "/output_events.xml.gz";
		
		TestUtils.comparingDepartures(primaryExpectedEventsFilename,actualEventsFilename,1.);
		TestUtils.comparingArrivals(primaryExpectedEventsFilename,actualEventsFilename,1.);
		TestUtils.comparingActivityStarts(primaryExpectedEventsFilename,actualEventsFilename, 1.);
		TestUtils.compareFullEvents(primaryExpectedEventsFilename,actualEventsFilename, true);

		
		// comparing the plain files, but maybe more than one:
//		long [] expectedEvents = new long [] {
//				CRCChecksum.getCRCFromFile( primaryExpectedEventsFilename )
//		} ;
//		TestUtils.checkSeveral(expectedEvents, actualEventsCRC);

		// look into plans:

		long [] expectedPlans = new long [] {
				CRCChecksum.getCRCFromFile( utils.getInputDirectory() + "/output_plans.xml.gz" )
		} ;
		
		TestUtils.checkSeveral(expectedPlans, actualPlansCRC);



		// FIXME: still small differences in jill log. dsingh, 06/Nov/17
		// expectedCRC = CRCChecksum.getCRCFromFile( utils.getInputDirectory() + "/jill.out" ) ;
		// actualCRC = CRCChecksum.getCRCFromFile( "scenarios/maldon-2017-11-01/jill.out" ) ;
		// Assert.assertEquals (expectedCRC, actualCRC); 

	}
}

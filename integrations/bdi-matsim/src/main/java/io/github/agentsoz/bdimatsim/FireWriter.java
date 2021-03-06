package io.github.agentsoz.bdimatsim;

/*-
 * #%L
 * BDI-ABM Integration Package
 * %%
 * Copyright (C) 2014 - 2018 by its authors. See AUTHORS file.
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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.matsim.core.config.Config;
import org.matsim.core.utils.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;

class FireWriter {
	private static final Logger log = LoggerFactory.getLogger(FireWriter.class);
	
	private final Config config;
	private PrintStream fireWriter ;
	FireWriter(Config config1 ) {
		config = config1 ;
	}
	void write(double now, Geometry fire) {
		if (fireWriter == null) {
			final String filename = config.controler().getOutputDirectory() + "/output_fireCoords.txt.gz";
			log.warn("writing fire data to " + filename);
			fireWriter = IOUtils.getPrintStream(filename);
			fireWriter.println("time\tx\ty");
		}
		// can't do this earlier since the output directories are not yet prepared by the controler.  kai, dec'17
		
		Envelope env = new Envelope();
		env.expandToInclude(fire.getEnvelopeInternal());
		double gridsize = 100.;
		boolean atLeastOnePixel = false;
		for (double yy = env.getMinY(); yy <= env.getMaxY(); yy += gridsize) {
			for (double xx = env.getMinX(); xx <= env.getMaxX(); xx += gridsize) {
				if (fire.contains(new GeometryFactory().createPoint(new Coordinate(xx, yy)))) {
					final String str = now + "\t" + xx + "\t" + yy;
//					log.debug(str);
					fireWriter.println(str);
					atLeastOnePixel = true;
				}
			}
		}
		if (!atLeastOnePixel) {
			final String str = now + "\t" + fire.getCentroid().getX() + "\t" + fire.getCentroid().getY();
			fireWriter.println(str);
		}
	}
	
	public void close() {
		if ( fireWriter != null ) {
			fireWriter.close();
		}
	}
}

package io.github.agentsoz.conservation.jill.plans;

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

import io.github.agentsoz.conservation.ConservationUtils;
import io.github.agentsoz.conservation.Log;
import io.github.agentsoz.conservation.LandholderHistory.BidResult;
import io.github.agentsoz.conservation.jill.agents.Landholder;
import io.github.agentsoz.conservation.jill.goals.UpdateConservationEthicGoal;
import io.github.agentsoz.jill.lang.Agent;
import io.github.agentsoz.jill.lang.Goal;
import io.github.agentsoz.jill.lang.Plan;
import io.github.agentsoz.jill.lang.PlanStep;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Landholder's C is decreased according to the highest profit obtained by
 * winners.
 * 
 * This plan will be triggered for all land holders who have low C and have not
 * participated in auction. The land holders C is updated proportional to the
 * highest profit obtained by any winner. The factor,
 * ConservationUtils.decreaseFactorForCNotParticipatedLowC is used to decrease
 * land holder's C.
 * 
 * @author Sewwandi Perera
 */
public class NotParticipatedLowC extends Plan {
	Landholder landholder;
	UpdateConservationEthicGoal updateConservationEthicGoal;

	public NotParticipatedLowC(Agent agent, Goal goal, String name) {
		super(agent, goal, name);
		landholder = (Landholder) getAgent();
		updateConservationEthicGoal = (UpdateConservationEthicGoal) getGoal();
		body = steps;
	}

	@Override
	public boolean context() {
		return !landholder.getCurrentAuctionRound().isParticipated()
				&& !landholder.isConservationEthicHigh();
	}

	@Override
	public void setPlanVariables(HashMap<String, Object> vars) {

	}

	PlanStep[] steps = { new PlanStep() {
		public void step() {
			ArrayList<BidResult> winningBids = updateConservationEthicGoal
					.getMyResults().getWinnersInfo();

			// This is used only for logging purposes
			ArrayList<Double> profits = new ArrayList<Double>();

			if (null != winningBids) {
				double highestProfit = 0;
				double winningPrice = 0;

				for (int i = 0; i < winningBids.size(); i++) {
					BidResult bid = (BidResult) winningBids.get(i);
					winningPrice = bid.getBidPrice();

					if (winningPrice > 0) { // If somebody has won the package
						double tempProfit = ((winningPrice - bid
								.getOpportunityCost()) / bid
								.getOpportunityCost()) * 100;
						profits.add(tempProfit);

						if (highestProfit < tempProfit) {
							highestProfit = tempProfit;
						}
					}
				}

				Collections.sort(profits);
				Log.debug("Agent " + landholder.getName()
						+ "- highest profit which changes agents C is "
						+ highestProfit + ", all profits:" + profits);

				double currentC = landholder.getConservationEthicBarometer();
				double newC;
				if (highestProfit > 0) {
					Log.debug("Agent " + landholder.getName()
							+ " Decrease agent's C since his highest profit ("
							+ highestProfit + "%) is greater than 0.");

					newC = currentC
							* (1 - (Math.abs(highestProfit) / 100)
									* ConservationUtils
											.getConservationEthicModifier());
					updateConsrvationEthicBarometer(newC, currentC);
				} else {
					Log.debug("Agent "
							+ landholder.getName()
							+ " Did not change agent's C since his highest profit ("
							+ highestProfit + "%) is not greater than 0.");
				}
			}
		}
	} };

	public void updateConsrvationEthicBarometer(double newC, double currentC) {
		newC = landholder.setConservationEthicBarometer(newC);
		landholder.setConservationEthicHigh(landholder
				.isConservationEthicHigh(newC));
		String newStatus = (landholder.isConservationEthicHigh()) ? "high"
				: "low";
		Log.debug("Agent " + landholder.getName() + " updated his C from "
				+ currentC + " to :" + newC + ", which is " + newStatus);
	}

}
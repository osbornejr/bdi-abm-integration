# Conservation Ethics modelling

Combined work with Sarah Bekessy, Ascelin Gordon, Fiona Fidler, 
Lin Padgham, Dhirendra Singh, Sayed Iftekhar, Graeme Doole.

See ./doc/cjae13-ihl.pdf for background work that this model is based on. 
In this work, we extend the landholder model so that it (1) is implemented
as a BDI agent (in JACK), and (2) models the notion of conservation ethics.
The ABM is modelled in GAMS.

For more information on the BDI-ABM integration project, 
see the top level README.


## Dependencies


This program depends on the following libraries:

* BDI-ABM-Integration (`/integrations/bdi-abm`) 
* BDI-GAMS-Integration (`/integrations/bdi-gams`) 
* ABM-JACK-Integration (`/integrations/abm-jack`)

See the respective README files for information on how to build these 
libraries.


## How to Compile

### Command Line
  
1.  Build the bdi-abm-integration layer: In the source repository `/`, do 
    `mvn clean install -N`
2.  Build the BDI-ABM library: See `/integrations/bdi-abm/README.md`
    for instructions
3.  Build the BDI-GAMS library: See `/integrations/bdi-gams/README.md`
    for instructions
4.  Build the ABM-JILL library: See `/integrations/abm-jill/README.md`
    for instructions
5.  Build the Bushfire application: In `/examples/conservation`, do
    `mvn clean install`

### Eclipse

Ensure that you have the corrent version of Eclipse installed. See 
`../../README.md` for details. Then import and build the following 
Eclipse projects:

*  `/integrations/bdi-abm` 
*  `/integrations/abm-jack` 
*  `/integrations/bdi-gams` 
*  `/examples/conservation` 



## How to Run


To run from the command line:

        > ./test/run.sh



## License


BDI-ABM Integration Library
Copyright (C) 2014, 2015 by its authors. See AUTHORS file.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

For contact information, see AUTHORS file.

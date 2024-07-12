package org.matsim.prepare.network;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.api.*;

import java.nio.file.Paths;

public class Create_U5_Network_V1 {
    private static final NetworkFactory networkFactory = NetworkUtils.createNetwork().getFactory();
    private static final TransitScheduleFactory scheduleFactory = ScenarioUtils.createScenario(ConfigUtils.createConfig()).getTransitSchedule().getFactory();

    public static void main(String[] args) {

        // read in network and create scenario
        var root = Paths.get("C:\\Users\\Julius Guizetti\\Desktop\\UNI\\Master\\MATSim\\HA 2");
        var network = NetworkUtils.readNetwork(root.resolve("hamburg-v3.0-network-with-pt.xml.gz").toString());
        var scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());

        // create pt nodes and links --> adapt network
        var Bramfeld = network.getFactory().createNode(Id.createNodeId("Bramfeld"), new Coord(571219, 5941120));
        network.addNode(Bramfeld);

        var Bramfeldlink = createLink("BramfeldLink", Bramfeld, Bramfeld);
        network.addLink(Bramfeldlink);

        // create TransitStopFacility
        var BramfeldStopFacility = scheduleFactory.createTransitStopFacility(Id.create("BramfeldStopFacility", TransitStopFacility.class), Bramfeld.getCoord(), false);
        BramfeldStopFacility.setLinkId(Bramfeldlink.getId());
        scenario.getTransitSchedule().addStopFacility(BramfeldStopFacility);

        // create TransitRouteStop
        var BramfeldStop1 = scheduleFactory.createTransitRouteStop(BramfeldStopFacility, 0, 30);

        var Steilshoop = network.getFactory.createNode(Id.createNodeId("Steilshoop"), new Coord(570058, 5940701));
        network.addNode(Steilshoop);

        var Barmbek_Nord = network.getFactory.createNode(Id.createNodeId("Barmbek_Nord"), new Coord(568759, 5940450));
        network.addNode(Barmbek_Nord);

        var Sengelmannstrasse = network.getFactory.createNode(Id.createNodeId("Sengelmannstrasse"), new Coord(567616 , 5940552));
        network.addNode(Sengelmannstrasse);

        var City_Nord = network.getFactory.createNode(Id.createNodeId("City_Nord"), new Coord(567314, 5939747));
        network.addNode(City_Nord);

        var Borgweg = network.getFactory.createNode(Id.createNodeId("Borgweg"), new Coord(567169 , 5938476));
        network.addNode(Borgweg);

        var Jarrestrasse = network.getFactory.createNode(Id.createNodeId("Jarrestrasse"), new Coord(567453, 5937734));
        network.addNode(Jarrestrasse);

        var Beethovenstrasse = network.getFactory.createNode(Id.createNodeId("Beethovenstrasse"), new Coord(567583, 5936810));
        network.addNode(Beethovenstrasse);

        var Uhlenhorst = network.getFactory.createNode(Id.createNodeId("Uhlenhorst"), new Coord(567701, 5935870));
        network.addNode(Uhlenhorst);

        var St_Georg = network.getFactory.createNode(Id.createNodeId("St_Georg"), new Coord(567192, 5935020));
        network.addNode(St_Georg);

        var Hauptbahnhof_Nord = network.getFactory.createNode(Id.createNodeId("Hauptbahnhof_Nord"), new Coord(566654, 5934389));
        network.addNode(Hauptbahnhof_Nord);

        var Jungfernstieg = network.getFactory.createNode(Id.createNodeId("Jungfernstieg"), new Coord(565765, 5934277));
        network.addNode(Jungfernstieg);

        var Stephansplatz_Dammtor = network.getFactory.createNode(Id.createNodeId("Stephansplatz_Dammtor"), new Coord(565531, 5934885));
        network.addNode(Stephansplatz_Dammtor);

        var Universitaet = network.getFactory.createNode(Id.createNodeId("Universitaet"), new Coord(564995, 5935768));
        network.addNode(Universitaet);

        var Grindelberg = network.getFactory.createNode(Id.createNodeId("Grindelberg"), new Coord(564799, 5936354));
        network.addNode(Grindelberg);

        var Hoheluftbruecke = network.getFactory.createNode(Id.createNodeId("Hoheluftbruecke"), new Coord(564670, 5937011));
        network.addNode(Hoheluftbruecke);

        var Gaertnerstrasse = network.getFactory.createNode(Id.createNodeId("Gaertnerstrasse"), new Coord(564151, 5937787));
        network.addNode(Gaertnerstrasse);

        var UKE = network.getFactory.createNode(Id.createNodeId("UKE"), new Coord(564499, 5938723));
        network.addNode(UKE);

        var Behrmannplatz = network.getFactory.createNode(Id.createNodeId("Behrmannplatz"), new Coord(563375, 5939353));
        network.addNode(Behrmannplatz);

        var Hagenbecks_Tierpark = network.getFactory.createNode(Id.createNodeId("Hagenbecks_Tierpark"), new Coord(562477, 5938643));
        network.addNode(Hagenbecks_Tierpark);

        var Sportplatzring = network.getFactory.createNode(Id.createNodeId("Sportplatzring"), new Coord(561681, 5938588));
        network.addNode(Sportplatzring);

        var Stellingen_Arenen = network.getFactory.createNode(Id.createNodeId("Stellingen_Arenen"), new Coord(560879, 5938410));
        network.addNode(Stellingen_Arenen);

        var Arenen_Volkspark = network.getFactory.createNode(Id.createNodeId("Arenen_Volkspark"), new Coord(559558, 5938583));
        network.addNode(Arenen_Volkspark);

        // create connecting links in the end
        var Bramfeld_Steilshoop = createLink("Bramfeld_Steilshoop", Bramfeld, Steilshoop);
        network.addLink(Bramfeld_Steilshoop);

    }
}





// create pt nodes and links --> adapt network
var NodeName = network.getFactory().createNode(Id.createNodeId("NodeID"), new Coord(x, y));
        network.addNode(NodeName);

var LinkName = createLink("LinkID", Bramfeld, Bramfeld);
        network.addLink(LinkName);

// create TransitStopFacility
var StopFacilityName = scheduleFactory.createTransitStopFacility(Id.create("StopFacilityName", TransitStopFacility.class), NodeName.getCoord(), false);
        StopFacilityName.setLinkId(LinkID.getId());
        scenario.getTransitSchedule().addStopFacility(StopFacilityName);

// create TransitRouteStop
var StopName = scheduleFactory.createTransitRouteStop(StopFacilityName, arrivalOffset, departureOffset);
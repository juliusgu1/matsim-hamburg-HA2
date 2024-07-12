package org.matsim.prepare.network;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.network.Link;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;
import org.matsim.pt.transitSchedule.api.TransitRouteStop;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt.transitSchedule.api.TransitScheduleFactory;

import java.util.List;
import java.util.Map;

public class Create_U5_Network_V2 {

    public void createTransitNetwork(Network network, TransitSchedule schedule, List<Map<String, String>> rows) {
        TransitScheduleFactory scheduleFactory = schedule.getFactory();

        for (Map<String, String> row : rows) {
            // Extract values from the row
            String station = row.get("Station");
            String nodeNameStr = row.get("NodeNamen");
            String nodeIdStr = row.get("NodeID");
            String linkNameStr = row.get("LinkName");
            String linkIdStr = row.get("LinkID");
            String stopFacilityNameStr = row.get("StopFacility");
            String stopNameStr = row.get("StopName");

            // Replace with actual coordinates
            double x = 0.0;
            double y = 0.0;

            // Create Node
            Node node = network.getFactory().createNode(Id.createNodeId(nodeIdStr), new Coord(x, y));
            network.addNode(node);

            // Create Link (assuming createLink is a custom method)
            Link link = createLink(linkIdStr, node, node);
            network.addLink(link);

            // Create TransitStopFacility
            TransitStopFacility stopFacility = scheduleFactory.createTransitStopFacility(
                    Id.create(stopFacilityNameStr, TransitStopFacility.class),
                    node.getCoord(),
                    false
            );
            stopFacility.setLinkId(link.getId());
            schedule.addStopFacility(stopFacility);

            // Create TransitRouteStop
            TransitRouteStop stop = scheduleFactory.createTransitRouteStop(stopFacility, null, null);
            // Add the stop to your transit route here
        }
    }

    private Link createLink(String linkId, Node fromNode, Node toNode) {
        // Implement link creation logic here
        return null;
    }

    public static void main(String[] args) {
        // Example usage
        Network network = null; // Initialize your network here
        TransitSchedule schedule = null; // Initialize your transit schedule here

        List<Map<String, String>> rows = List.of(
                Map.of("Station", "Bramfeld", "NodeNamen", "Bramfeld", "NodeID", "Bramfeld",
                        "LinkName", "BramfeldLink", "LinkID", "BramfeldLink",
                        "StopFacility", "BramfeldStopFacility", "StopName", "BramfeldStop1"),
                Map.of("Station", "Steilshoop", "NodeNamen", "Steilshoop", "NodeID", "Steilshoop",
                        "LinkName", "SteilshoopLink", "LinkID", "SteilshoopLink",
                        "StopFacility", "SteilshoopStopFacility", "StopName", "SteilshoopStop1")
        );

        TransitNetworkBuilder builder = new TransitNetworkBuilder();
        builder.createTransitNetwork(network, schedule, rows);
    }
}

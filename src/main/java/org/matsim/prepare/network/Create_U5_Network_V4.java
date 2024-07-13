package org.matsim.prepare.network;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.network.Link;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;
import org.matsim.pt.transitSchedule.api.TransitRouteStop;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt.transitSchedule.api.TransitScheduleFactory;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Set;

public class Create_U5_Network_V4 {

    public void createTransitNetwork(Network network, TransitSchedule schedule, String csvFilePath) throws IOException {
        TransitScheduleFactory scheduleFactory = schedule.getFactory();

        try (CSVParser parser = CSVFormat.DEFAULT
                .withHeader("Station", "NodeName", "NodeID", "LinkName", "LinkID", "StopFacilityName", "StopName", "arrivalOffset", "departureOffset")
                .withSkipHeaderRecord()
                .parse(new FileReader(csvFilePath))) {

            for (CSVRecord record : parser) {
                // Extract values from the record
                String station = record.get("Station");
                String nodeNameStr = record.get("NodeNamen");
                String nodeIdStr = record.get("NodeID");
                String linkNameStr = record.get("LinkName");
                String linkIdStr = record.get("LinkID");
                String stopFacilityNameStr = record.get("StopFacility");
                String stopNameStr = record.get("StopName");
                double x = record.get("x");
                double y = record.get("y");
                String arrivalOffset = record.get("arrivalOffset");
                String departureOffset = record.get("departureOffset");


                // Create Node
                Node node = network.getFactory().createNode(Id.createNodeId(nodeIdStr), new Coord(x, y));
                network.addNode(node);

                // Create Link
                Link link = createLink(linkIdStr, node, node);
                network.addLink(link);

                // Create TransitStopFacility
                TransitStopFacility stopFacility = scheduleFactory.createTransitStopFacility(
                        Id.create(stopFacilityNameStr, TransitStopFacility.class), node.getCoord(), false);
                stopFacility.setLinkId(link.getId());
                schedule.addStopFacility(stopFacility);

                // Create TransitRouteStop
                TransitRouteStop stop = scheduleFactory.createTransitRouteStop(stopFacility, arrivalOffset, departureOffset);
                // Add the stop to your transit route here
            }
        }
    }

    private Link createLink(String linkId, Node fromNode, Node toNode) {
        var link = networkFactory.createLink(Id.createLinkId(id), from, to);
        link.setAllowedModes(Set.of(TransportMode.pt));
        link.setFreespeed(100);
        link.setCapacity(10000);
        return link;
    }

    public static void main(String[] args) {
        // Example usage
        Network network = null; // Initialize your network here
        TransitSchedule schedule = null; // Initialize your transit schedule here

        String csvFilePath = "path/to/your/stationen.csv"; // Path to your CSV file

        TransitNetworkBuilder builder = new TransitNetworkBuilder();
        try {
            builder.createTransitNetwork(network, schedule, csvFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

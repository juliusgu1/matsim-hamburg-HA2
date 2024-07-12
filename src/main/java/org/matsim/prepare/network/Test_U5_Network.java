import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.*;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.utils.geometry.CoordUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Test_U5_Network {

    public static void main(String[] args) {
        // Pfad zur existierenden Netzwerkdatei
        String networkFilePath = "C:\\Users\\Julius Guizetti\\Desktop\\UNI\\Master\\MATSim\\HA 2\\hamburg-v3.0-network-with-pt.xml";
        // Pfad zur CSV-Datei
        String csvFilePath = "C:\\Users\\Julius Guizetti\\Desktop\\UNI\\Master\\MATSim\\HA 2\\U5_Haltestellen.csv";
        // Pfad zur Ausgabe der aktualisierten Netzwerkdatei
        String outputNetworkFilePath = "C:\\Users\\Julius Guizetti\\Desktop\\UNI\\Master\\MATSim\\HA 2\\hamburg-v3.0-network-with-pt_TEST.xml";

        // Netzwerk laden
        Network network = NetworkUtils.createNetwork();
        new MatsimNetworkReader(network).readFile(networkFilePath);

        NetworkFactory factory = network.getFactory();

        // CSV-Datei lesen und Knoten erstellen
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            Node previousNode = null;
            boolean header = true;

            while ((line = br.readLine()) != null) {
                if (header) {
                    header = false;
                    continue;
                }

                String[] values = line.split(",");
                String name = values[0];
                double lat = Double.parseDouble(values[1]);
                double lon = Double.parseDouble(values[2]);
                String time = values[3];

                Id<Node> nodeId = Id.createNodeId(time.isEmpty() ? name : time);
                Node node = factory.createNode(nodeId, CoordUtils.createCoord(lon, lat));
                network.addNode(node);

                // Knoten mit Kanten verbinden
                if (previousNode != null) {
                    Id<Link> linkId = Id.createLinkId(previousNode.getId() + "-" + node.getId());
                    Link link = factory.createLink(linkId, previousNode, node);
                    network.addLink(link);
                }

                previousNode = node;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Aktualisiertes Netzwerk speichern
        new NetworkWriter(network).write(outputNetworkFilePath);

        System.out.println("Netzwerk aktualisiert und gespeichert mit " + network.getNodes().size() + " Knoten und " + network.getLinks().size() + " Kanten.");
    }
}
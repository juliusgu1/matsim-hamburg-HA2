package org.matsim.run;

import ch.sbb.matsim.config.SwissRailRaptorConfigGroup;
import ch.sbb.matsim.routing.pt.raptor.RaptorIntermodalAccessEgress;
import org.apache.log4j.Logger;
import org.matsim.analysis.TransportPlanningMainModeIdentifier;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.drt.optimizer.insertion.ExtensiveInsertionSearchParams;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.drt.run.DrtConfigs;
import org.matsim.contrib.drt.run.MultiModeDrtConfigGroup;
import org.matsim.contrib.drt.run.MultiModeDrtModule;
import org.matsim.contrib.drt.speedup.DrtSpeedUpParams;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.contrib.dvrp.run.DvrpModule;
import org.matsim.contrib.dvrp.run.DvrpQSimComponents;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.network.algorithms.MultimodalNetworkCleaner;
import org.matsim.core.router.AnalysisMainModeIdentifier;
import org.matsim.core.router.MainModeIdentifier;
import org.matsim.extensions.pt.fare.intermodalTripFareCompensator.IntermodalTripFareCompensatorsConfigGroup;
import org.matsim.extensions.pt.fare.intermodalTripFareCompensator.IntermodalTripFareCompensatorsModule;
import org.matsim.extensions.pt.routing.EnhancedRaptorIntermodalAccessEgress;
import org.matsim.extensions.pt.routing.ptRoutingModes.PtIntermodalRoutingModesConfigGroup;
import org.matsim.extensions.pt.routing.ptRoutingModes.PtIntermodalRoutingModesModule;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author zmeng
 */
public class RunRealLabHH2030Scenario {

    private static final Logger log = Logger.getLogger(RunRealLabHH2030Scenario.class);

    private static final String DRT_ACCESS_EGRESS_TO_PT_STOP_FILTER_ATTRIBUTE = "drtStopFilter";
    private static final String DRT_ACCESS_EGRESS_TO_PT_STOP_FILTER_VALUE = "HVV_switch_drtServiceArea";
    private static final String DRT_FEEDER_SERVICE_AREA = "D:/svn/shared-svn/projects/realLabHH/data/drt-feeder-potential-areas/Potentielle Bediengebiete - Angebotssegment A/Potentielle Bediengebiete - Angebotssegment A.shp";



    public static void main(String[] args) throws ParseException, IOException {

        for (String arg : args) {
            log.info(arg);
        }

        if (args.length == 0) {
            args = new String[] {"scenarios/input/hamburg-v1.1-1pct.config.xml"};
        }

        RunRealLabHH2030Scenario realLabHH2030 = new RunRealLabHH2030Scenario();
        realLabHH2030.run(args);
    }

    private void run(String[] args) throws IOException {

        Config config = prepareConfig(args);
        Scenario scenario = prepareScenario(config);

        Controler controler = prepareControler(scenario);

        controler.run();
        log.info("Done.");
    }

    public static Controler prepareControler(Scenario scenario) {
        Controler controler =  RunBaseCaseHamburgScenario.prepareControler(scenario);



//        // drt + dvrp module
        controler.addOverridingModule(new MultiModeDrtModule());
        controler.addOverridingModule(new DvrpModule());
        controler.configureQSimComponents(DvrpQSimComponents.activateAllModes(MultiModeDrtConfigGroup.get(controler.getConfig())));

        controler.addOverridingModule(new AbstractModule() {

            @Override
            public void install() {
                // use a main mode identifier which knows how to handle intermodal trips generated by the used sbb pt raptor router
                // the SwissRailRaptor already binds its IntermodalAwareRouterModeIdentifier, however drt obviously replaces it
                // with its own implementation
                // So we need our own main mode indentifier which replaces both :-(
                //TODO: write our hamburg mainModeIdentifier,which can deal with all the pt+x(s)

                //HamburgFreightMainModeIdentifier was already bound
//                bind(MainModeIdentifier.class).toInstance(new TransportPlanningMainModeIdentifier());
//                bind(AnalysisMainModeIdentifier.class).to(HamburgFreightMainModeIdentifier.class);

                //need to bind this in another overriding module than in the module where we install the SwissRailRaptorModule
                bind(RaptorIntermodalAccessEgress.class).to(EnhancedRaptorIntermodalAccessEgress.class);
            }
        });

        // TODO: 02.06.21 report bugs
        controler.addOverridingModule(new IntermodalTripFareCompensatorsModule());
        //todo: write our hamburg PtIntermodalRoutingModesModule,which can deal with all the pt+x(s). Ask Gregor why not multi-routingmode in config?
        controler.addOverridingModule(new PtIntermodalRoutingModesModule());

        return controler;
    }

    public static Config prepareConfig(String[] args, ConfigGroup... customModules) {
        ConfigGroup[] customModulesToAdd = new ConfigGroup[] { new DvrpConfigGroup(), new MultiModeDrtConfigGroup(),
                new SwissRailRaptorConfigGroup(), new IntermodalTripFareCompensatorsConfigGroup(),
                new PtIntermodalRoutingModesConfigGroup()};
        ConfigGroup[] customModulesAll = new ConfigGroup[customModules.length + customModulesToAdd.length];

        int counter = 0;
        for (ConfigGroup customModule : customModules) {
            customModulesAll[counter] = customModule;
            counter++;
        }

        for (ConfigGroup customModule : customModulesToAdd) {
            customModulesAll[counter] = customModule;
            counter++;
        }
        Config config = RunBaseCaseHamburgScenario.prepareConfig(args, customModulesAll);

        //configure DRT feeder system and intermodal pt router
        configureDRTFeeder(config);

        return config;
    }

    public static Config configureDRTFeeder(Config config){
        //when simulating dvrp, we need/should simulate from the start to the end
        config.qsim().setSimStarttimeInterpretation(QSimConfigGroup.StarttimeInterpretation.onlyUseStarttime);
        config.qsim().setSimEndtimeInterpretation(QSimConfigGroup.EndtimeInterpretation.onlyUseEndtime);

        String drtFeederMode = "drt_feeder";

//        DvrpConfigGroup dvrpCfg = ConfigUtils.addOrGetModule(config, DvrpConfigGroup.class);
//        dvrpCfg.setNetworkModes(ImmutableSet.<String>builder()
//                .add("drt_feeder")
//                .build());
        //TODO potentially further configure dvrp. For example, travelTimeMatrix cell size etc.

        MultiModeDrtConfigGroup multiModeDrtCfg = ConfigUtils.addOrGetModule(config, MultiModeDrtConfigGroup.class);

        DrtConfigGroup drtFeederCfg = new DrtConfigGroup();
        drtFeederCfg.setMode(drtFeederMode);
//        drtFeederCfg.setOperationalScheme(DrtConfigGroup.OperationalScheme.serviceAreaBased);
        drtFeederCfg.setUseModeFilteredSubnetwork(false); //vehicles should be able to drive from one area to the other
        drtFeederCfg.setMaxWaitTime(300); //5 minutes as in the ReallabHH goals
        drtFeederCfg.setRejectRequestIfMaxWaitOrTravelTimeViolated(false); //no rejections please

        //set some standard values
        drtFeederCfg.setMaxTravelTimeAlpha(1.7);
        drtFeederCfg.setMaxTravelTimeBeta(120);
        drtFeederCfg.setStopDuration(60);

        drtFeederCfg.addDrtInsertionSearchParams(new ExtensiveInsertionSearchParams());

        multiModeDrtCfg.addDrtConfig(drtFeederCfg);

        //configure drt speed-up params
        for (DrtConfigGroup drtCfg : multiModeDrtCfg.getModalElements()) {
            if (drtCfg.getDrtSpeedUpParams().isEmpty()) {
                drtCfg.addParameterSet(new DrtSpeedUpParams());
            }
        }

        //add drt stage activities
        DrtConfigs.adjustMultiModeDrtConfig(multiModeDrtCfg, config.planCalcScore(), config.plansCalcRoute());

        //configure intermodal pt
        SwissRailRaptorConfigGroup swissRailRaptorConfigGroup = ConfigUtils.addOrGetModule(config,SwissRailRaptorConfigGroup.class);
        swissRailRaptorConfigGroup.setUseIntermodalAccessEgress(true);
        SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet params = new SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet();
        params.setMode(drtFeederMode);
        //TODO these values were recommended by GL based on his experiences for Berlin
        params.setInitialSearchRadius(3_000);
        params.setSearchExtensionRadius(1_000);
        params.setMaxRadius(20_000);
        swissRailRaptorConfigGroup.addIntermodalAccessEgress(params);

        return config;
    }

    public static Scenario prepareScenario(Config config) throws IOException {
        Scenario scenario = RunBaseCaseHamburgScenario.prepareScenario(config);
        HamburgExperimentalConfigGroup hamburgExperimentalConfigGroup = ConfigUtils.addOrGetModule(config, HamburgExperimentalConfigGroup.class);
        for (DrtConfigGroup drtCfg : MultiModeDrtConfigGroup.get(config).getModalElements()) {

            String drtServiceAreaShapeFile = drtCfg.getDrtServiceAreaShapeFile();
            if (drtServiceAreaShapeFile != null && !drtServiceAreaShapeFile.equals("") && !drtServiceAreaShapeFile.equals("null")) {

                // Michal says restricting drt to a drt network roughly the size of the service area helps to speed up.
                // This is even more true since drt started to route on a freespeed TT matrix (Nov '20).
                // A buffer of 10km to the service area Berlin includes the A10 on some useful stretches outside Berlin.
                if(hamburgExperimentalConfigGroup.getTagDrtLinksBufferAroundServiceAreaShp() >= 0.0) {
                    addDRTmode(scenario, drtCfg.getMode(), drtServiceAreaShapeFile, hamburgExperimentalConfigGroup.getTagDrtLinksBufferAroundServiceAreaShp());
                }

                //tag pt stops that are to be used for intermodal access and egress
                //TODO restrict to the actual stations that we want to use and do not use generic solution here....
                tagTransitStopsInServiceArea(scenario.getTransitSchedule(),
                        DRT_ACCESS_EGRESS_TO_PT_STOP_FILTER_ATTRIBUTE, DRT_ACCESS_EGRESS_TO_PT_STOP_FILTER_VALUE,
                        drtServiceAreaShapeFile,
                        "stopFilter", "station_S/U/RE/RB",
                        200.0); //
            }
        }
        return scenario;
    }

    public static void addDRTmode(Scenario scenario, String drtNetworkMode, String drtServiceAreaShapeFile, double buffer) {

        log.info("Adjusting network...");

        HamburgShpUtils shpUtils = new HamburgShpUtils( drtServiceAreaShapeFile );

        int counter = 0;
        int counterInside = 0;
        int counterOutside = 0;
        for (Link link : scenario.getNetwork().getLinks().values()) {
            if (counter % 10000 == 0)
                log.info("link #" + counter);
            counter++;
            if (link.getAllowedModes().contains(TransportMode.car)) {
                if (shpUtils.isCoordInDrtServiceAreaWithBuffer(link.getFromNode().getCoord(), buffer)
                        || shpUtils.isCoordInDrtServiceAreaWithBuffer(link.getToNode().getCoord(), buffer)) {
                    Set<String> allowedModes = new HashSet<>(link.getAllowedModes());

                    allowedModes.add(drtNetworkMode);

                    link.setAllowedModes(allowedModes);
                    counterInside++;
                } else {
                    counterOutside++;
                }

            } else if (link.getAllowedModes().contains(TransportMode.pt)) {
                // skip pt links
            } else {
                throw new RuntimeException("Aborting...");
            }
        }

        log.info("Total links: " + counter);
        log.info("Total links inside service area: " + counterInside);
        log.info("Total links outside service area: " + counterOutside);

        Set<String> modes = new HashSet<>();
        modes.add(drtNetworkMode);
        new MultimodalNetworkCleaner(scenario.getNetwork()).run(modes);
    }

    private static void tagTransitStopsInServiceArea(TransitSchedule transitSchedule,
                                                     String newAttributeName, String newAttributeValue,
                                                     String drtServiceAreaShapeFile,
                                                     String oldFilterAttribute, String oldFilterValue,
                                                     double bufferAroundServiceArea) {
        log.info("Tagging pt stops marked for intermodal access/egress in the service area.");
        HamburgShpUtils shpUtils = new HamburgShpUtils( drtServiceAreaShapeFile );
        for (TransitStopFacility stop: transitSchedule.getFacilities().values()) {
            if (stop.getAttributes().getAttribute(oldFilterAttribute) != null) {
                if (stop.getAttributes().getAttribute(oldFilterAttribute).equals(oldFilterValue)) {
                    if (shpUtils.isCoordInDrtServiceAreaWithBuffer(stop.getCoord(), bufferAroundServiceArea)) {
                        stop.getAttributes().putAttribute(newAttributeName, newAttributeValue);
                    }
                }
            }
        }
    }
}

package org.ipea.r5r.Utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.conveyal.r5.api.util.LegMode;
import com.conveyal.r5.api.util.TransitModes;
import com.conveyal.r5.common.GeometryUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;

public class Utils {

    static public boolean verbose = true;
    static public boolean benchmark = false;
    static public boolean progress = true;

    static public boolean detailedItinerariesV2 = true;

    static public boolean saveOutputToCsv = false;
    static public String outputCsvFolder = "";

    public static EnumSet<LegMode> setLegModes(String modes) {
        EnumSet<LegMode> legModes = EnumSet.noneOf(LegMode.class);

        String[] modesArray = modes.split(";");
        if (!modes.equals("") & modesArray.length > 0) {
            for (String mode : modesArray) {
                legModes.add(LegMode.valueOf(mode));
            }
        }

        return legModes;
    }

    public static EnumSet<TransitModes> setTransitModes(String modes) {
        EnumSet<TransitModes> transitModes = EnumSet.noneOf(TransitModes.class);

        String[] modesArray = modes.split(";");
        if (!modes.equals("") & modesArray.length > 0) {
            for (String mode : modesArray) {
                transitModes.add(TransitModes.valueOf(mode));
            }
        }

        return transitModes;
    }

    public static int getSecondsFromMidnight(String departureTime) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date reference = dateFormat.parse("00:00:00");
        Date date = dateFormat.parse(departureTime);
        return (int) ((date.getTime() - reference.getTime()) / 1000L);
    }

    public static String getTimeFromSeconds(int secondsFromMidnight) {
        int sec = secondsFromMidnight % 60;
        int min = (secondsFromMidnight / 60)%60;
        int hours = (secondsFromMidnight/60)/60;

        String strSec=(sec<10)?"0"+ sec :Integer.toString(sec);
        String strMin=(min<10)?"0"+ min :Integer.toString(min);
        String strHours=(hours<10)?"0"+ hours :Integer.toString(hours);

        return (strHours + ":" + strMin + ":" + strSec);
    }

    public static void setlogProgress(boolean progress){
        Utils.progress = progress;

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger;
        logger = loggerContext.getLogger("org.ipea.r5r.Process.R5RProcess");

        if (Utils.verbose) {
            logger.setLevel(Level.ALL);
        } else if (progress) {
            logger.setLevel(Level.INFO);
        } else {
            logger.setLevel(Level.ERROR);
        }
    }

    public static void setLogMode(String mode, boolean verbose) {
        Utils.verbose = verbose;

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        Logger logger = loggerContext.getLogger("com.conveyal.r5");
        logger.setLevel(Level.valueOf(mode));

        logger = loggerContext.getLogger("com.conveyal.osmlib");
        logger.setLevel(Level.valueOf(mode));

        logger = loggerContext.getLogger("com.conveyal.gtfs");
        logger.setLevel(Level.valueOf(mode));

        logger = loggerContext.getLogger("com.conveyal.r5.profile.ExecutionTimer");
        logger.setLevel(Level.valueOf(mode));

        logger = loggerContext.getLogger("graphql.GraphQL");
        logger.setLevel(Level.valueOf(mode));

        logger = loggerContext.getLogger("org.mongodb.driver.connection");
        logger.setLevel(Level.valueOf(mode));

        logger = loggerContext.getLogger("org.eclipse.jetty");
        logger.setLevel(Level.valueOf(mode));

        logger = loggerContext.getLogger("org.eclipse.jetty");
        logger.setLevel(Level.valueOf(mode));

        logger = loggerContext.getLogger("com.conveyal.r5.profile.FastRaptorWorker");
        logger.setLevel(Level.valueOf(mode));

        logger = loggerContext.getLogger("org.hsqldb.persist.Logger");
        logger.setLevel(Level.valueOf(mode));

        logger = loggerContext.getLogger("org.ipea.r5r.R5RCore");
        logger.setLevel(Level.valueOf(mode));

        logger = loggerContext.getLogger("org.ipea.r5r.Process.PathOptionsTable");
        logger.setLevel(Level.valueOf(mode));

        logger = loggerContext.getLogger("org.ipea.r5r.R5.R5TravelTimeComputer");
        logger.setLevel(Level.valueOf(mode));

        logger = loggerContext.getLogger("org.ipea.r5r.Network.TransitLayerWithShapes");
        logger.setLevel(Level.valueOf(mode));

        logger = loggerContext.getLogger("org.ipea.r5r.Network.NetworkChecker");
        logger.setLevel(Level.valueOf(mode));

        logger = loggerContext.getLogger("org.ipea.r5r.R5.R5ParetoServer");
        logger.setLevel(Level.valueOf(mode));

        logger = loggerContext.getLogger("org.ipea.r5r.Planner.TripPlanner");
        logger.setLevel(Level.valueOf(mode));

        logger = loggerContext.getLogger("org.ipea.r5r.Planner.Trip");
        logger.setLevel(Level.valueOf(mode));

        logger = loggerContext.getLogger("org.ipea.r5r.Planner.TripLeg");
        logger.setLevel(Level.valueOf(mode));

        logger = loggerContext.getLogger("org.ipea.r5r.Utils.SpeedSetter");
        logger.setLevel(Level.valueOf(mode));
    }

    public static int getLinestringLength(LineString geometry) {
        Coordinate previousCoordinate = null;
        double accDistance = 0;

        for (Coordinate coordinate : geometry.getCoordinates()) {
            if (previousCoordinate != null) {
                accDistance += GeometryUtils.distance(previousCoordinate.y, previousCoordinate.x, coordinate.y, coordinate.x);
            }

            previousCoordinate = coordinate;
        }

        return (int) Math.round(accDistance);
    }

    public static double roundTo1Place(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

}

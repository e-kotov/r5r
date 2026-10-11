package org.ipea.r5r.Utils;

import com.conveyal.osmlib.OSM;
import com.conveyal.r5.analyst.cluster.PathResult;
import com.conveyal.r5.streets.LinkedPointSet;
import com.conveyal.r5.profile.FastRaptorWorker;
import com.conveyal.r5.profile.PerTargetPropagater;
import com.conveyal.r5.streets.PointSetTimes;
import com.conveyal.r5.streets.Split;
import com.conveyal.r5.transit.path.StopSequence;

import java.io.File;
import java.lang.reflect.Method;

/** API differences between the pinned R5 7.5 and seed-supporting R5 7.6 fork. */
public final class R5Compatibility {
    private R5Compatibility() { }

    public static OSM openOsmDatabase(String path) {
        try {
            try {
                return OSM.class.getConstructor(String.class).newInstance(path);
            } catch (NoSuchMethodException e) {
                if (path == null) {
                    return (OSM) OSM.class.getMethod("newWritableInMemory").invoke(null);
                }
                return (OSM) OSM.class.getMethod("newWritableFile", File.class)
                        .invoke(null, new File(path));
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot create the R5 OSM database.", e);
        }
    }

    private static final Method EVAL = evalMethod();
    private static Method evalMethod() {
        try {
            try {
                return LinkedPointSet.class.getMethod("eval", LinkedPointSet.CostToVertexFunction.class,
                        Integer.class, int.class, Split.class);
            } catch (NoSuchMethodException e) {
                return LinkedPointSet.class.getMethod("eval", LinkedPointSet.CostToVertexFunction.class,
                        int.class, int.class, Split.class);
            }
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Unsupported R5 point set API.", e);
        }
    }

    public static PointSetTimes eval(LinkedPointSet points, LinkedPointSet.CostToVertexFunction times,
                                     int onStreetSpeed, int offStreetSpeed, Split origin) {
        try {
            return (PointSetTimes) EVAL.invoke(points, times, onStreetSpeed, offStreetSpeed, origin);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot evaluate R5 point set travel times.", e);
        }
    }

    public static void copyDepartureTimes(FastRaptorWorker worker, PerTargetPropagater propagater) {
        // R5 7.6 requires the caller to supply departure timestamps for saved paths.
        try {
            Object times = FastRaptorWorker.class.getField("departureTimeForIteration").get(worker);
            PerTargetPropagater.class.getField("departureTimeForIteration").set(propagater, times);
        } catch (NoSuchFieldException e) {
            // R5 7.5 reconstructs timestamps within the propagater.
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot supply R5 path departure times.", e);
        }
    }

    private static final Method TRANSFER_TIME = transferTimeMethod();
    private static Method transferTimeMethod() {
        try {
            try {
                return StopSequence.class.getMethod("totalTransferTimeSeconds");
            } catch (NoSuchMethodException e) {
                return StopSequence.class.getMethod("transferTime", PathResult.Iteration.class);
            }
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Unsupported R5 transfer time API.", e);
        }
    }

    public static int transferTime(StopSequence sequence, PathResult.Iteration iteration) {
        try {
            return (Integer) (TRANSFER_TIME.getParameterCount() == 0
                    ? TRANSFER_TIME.invoke(sequence) : TRANSFER_TIME.invoke(sequence, iteration));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot read R5 transfer time.", e);
        }
    }
}

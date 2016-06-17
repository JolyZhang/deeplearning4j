package org.deeplearning4j.spark.impl.vanilla.stats;

import org.deeplearning4j.spark.api.stats.SparkTrainingStats;
import org.deeplearning4j.spark.impl.vanilla.VanillaTrainingMaster;
import org.nd4j.linalg.util.ArrayUtil;

import java.util.*;

/**
 * Created by Alex on 17/06/2016.
 */
public class VanillaTrainingWorkerStats implements SparkTrainingStats {

    private int[] vanillaWorkerBroadcastGetValueTimeMs;
    private int[] vanillaWorkerInitTimeMs;
    private int[] vanillaWorkerFitTimesMs;

    private static Set<String> columnNames = Collections.unmodifiableSet(
            new LinkedHashSet<>(Arrays.asList(
                    "VanillaWorkerBroadcastGetValueTimeMs",
                    "VanillaWorkerInitTimeMs",
                    "VanillaWorkerFitTimesMs"
            )));

    public VanillaTrainingWorkerStats(int vanillaWorkerBroadcastGetValueTimeMs, int vanillaWorkerInitTimeMs, int[] vanillaWorkerFitTimesMs){
        this.vanillaWorkerBroadcastGetValueTimeMs = new int[]{vanillaWorkerBroadcastGetValueTimeMs};
        this.vanillaWorkerInitTimeMs = new int[]{vanillaWorkerInitTimeMs};
        this.vanillaWorkerFitTimesMs = vanillaWorkerFitTimesMs;
    }

    @Override
    public Set<String> getKeySet() {
        return columnNames;
    }

    @Override
    public Object getValue(String key) {
        switch(key){
            case "VanillaWorkerBroadcastGetValueTimeMs":
                return vanillaWorkerBroadcastGetValueTimeMs;
            case "VanillaWorkerInitTimeMs":
                return vanillaWorkerInitTimeMs;
            case "VanillaWorkerFitTimesMs":
                return vanillaWorkerFitTimesMs;
            default:
                throw new IllegalArgumentException("Unknown key: \"" + key + "\"");
        }
    }

    @Override
    public void addOtherTrainingStats(SparkTrainingStats other) {
        if(!(other instanceof VanillaTrainingWorkerStats)) throw new IllegalArgumentException("Cannot merge VanillaTrainingWorkerStats with " + (other != null ? other.getClass() : null));

        VanillaTrainingWorkerStats o = (VanillaTrainingWorkerStats)other;

        this.vanillaWorkerBroadcastGetValueTimeMs = ArrayUtil.combine(vanillaWorkerBroadcastGetValueTimeMs,o.vanillaWorkerBroadcastGetValueTimeMs);
        this.vanillaWorkerInitTimeMs = ArrayUtil.combine(vanillaWorkerInitTimeMs, o.vanillaWorkerInitTimeMs);
        this.vanillaWorkerFitTimesMs = ArrayUtil.combine(vanillaWorkerFitTimesMs, o.vanillaWorkerFitTimesMs);

    }

    public static class VanillaTrainingWorkerStatsHelper {
        private long broadcastStartTime;
        private long broadcastEndTime;
        private long initStartTime;
        private long initEndTime;
        private long lastFitStartTime;
        //TODO replace with fast int collection (no boxing)
        private List<Integer> fitTimes = new ArrayList<>();


        public void logBroadcastGetValueStart(){
            broadcastStartTime = System.currentTimeMillis();
        }

        public void logBroadcastGetValueEnd(){
            broadcastEndTime = System.currentTimeMillis();
        }

        public void logInitEnd(){
            initStartTime = System.currentTimeMillis();
        }

        public void logFitStart(){
            lastFitStartTime = System.currentTimeMillis();
        }

        public void logFitEnd(){
            long now = System.currentTimeMillis();
            fitTimes.add((int)(now - lastFitStartTime));
        }

        public VanillaTrainingWorkerStats build(){
            int bcast = (int)(broadcastEndTime - broadcastStartTime);
            int init = (int)(initEndTime - initStartTime);
            int[] fitTimesArr = new int[fitTimes.size()];
            for( int i=0; i<fitTimesArr.length; i++ ) fitTimesArr[i] = fitTimes.get(i);
            return new VanillaTrainingWorkerStats(bcast, init, fitTimesArr);
        }
    }
}

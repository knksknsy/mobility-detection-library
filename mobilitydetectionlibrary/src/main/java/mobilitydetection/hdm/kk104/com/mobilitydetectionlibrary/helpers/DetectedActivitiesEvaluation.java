package mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.helpers;

import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.DetectedActivities;

public class DetectedActivitiesEvaluation {

    public static class Acceleration {
        public static class Still {
            public final static double average = 19.8;
            public final static double median = 15.0;
            public final static double std_deviation = 14.3;
            public final static double mean_deviation = 11.6;
            public final static double median_deviation = 7.0;
            public final static double lower_quartile = 10.0;
            public final static double upper_quartile = 29.0;
            public final static double quartile_distance = 19.0;
            public final static double semi_quartile_distance = 9.5;
        }

        public static class InVehicle {
            public final static double average = 23.7;
            public final static double median = 19.0;
            public final static double std_deviation = 16.0;
            public final static double mean_deviation = 13.0;
            public final static double median_deviation = 9.0;
            public final static double lower_quartile = 11.0;
            public final static double upper_quartile = 31.0;
            public final static double quartile_distance = 20.0;
            public final static double semi_quartile_distance = 10.0;
        }
    }

    public static class InVehicleMotion {
        public static class Still {
            public final static double average = 16.5;
            public final static double median = 10.0;
            public final static double std_deviation = 18.1;
            public final static double mean_deviation = 14.0;
            public final static double median_deviation = 9.0;
            public final static double lower_quartile = 4.0;
            public final static double upper_quartile = 24.0;
            public final static double quartile_distance = 20.0;
            public final static double semi_quartile_distance = 10.0;
        }

        public static class InVehicle {
            public final static double average = 40.6;
            public final static double median = 30.0;
            public final static double std_deviation = 30.6;
            public final static double mean_deviation = 27.0;
            public final static double median_deviation = 20.0;
            public final static double lower_quartile = 13.0;
            public final static double upper_quartile = 69.0;
            public final static double quartile_distance = 56.0;
            public final static double semi_quartile_distance = 28.0;
        }
    }

    public static class Deceleration {
        public static class Still {
            public final static double average = 27.7;
            public final static double median = 25.0;
            public final static double std_deviation = 16.3;
            public final static double mean_deviation = 12.9;
            public final static double median_deviation = 10.0;
            public final static double lower_quartile = 16.0;
            public final static double upper_quartile = 38.0;
            public final static double quartile_distance = 22.0;
            public final static double semi_quartile_distance = 11.0;
        }

        public static class InVehicle {
            public final static double average = 19.7;
            public final static double median = 15.0;
            public final static double std_deviation = 12.4;
            public final static double mean_deviation = 9.5;
            public final static double median_deviation = 5.0;
            public final static double lower_quartile = 12.0;
            public final static double upper_quartile = 25.0;
            public final static double quartile_distance = 13.0;
            public final static double semi_quartile_distance = 6.5;

        }
    }

    public static void evaluate(final DetectedActivities detectedActivities) {
        if (detectedActivities.getProbableActivities().IN_VEHICLE >= Acceleration.InVehicle.average - Acceleration.InVehicle.std_deviation / 2
                && detectedActivities.getProbableActivities().IN_VEHICLE >= Acceleration.InVehicle.average + Acceleration.InVehicle.std_deviation / 2) {

        } else if (detectedActivities.getProbableActivities().IN_VEHICLE >= Acceleration.InVehicle.average - Acceleration.InVehicle.mean_deviation / 2
                && detectedActivities.getProbableActivities().IN_VEHICLE >= Acceleration.InVehicle.average + Acceleration.InVehicle.mean_deviation / 2) {

        } else if (detectedActivities.getProbableActivities().IN_VEHICLE >= Acceleration.InVehicle.median - Acceleration.InVehicle.median_deviation / 2
                && detectedActivities.getProbableActivities().IN_VEHICLE >= Acceleration.InVehicle.median + Acceleration.InVehicle.median_deviation / 2) {

        } else if (detectedActivities.getProbableActivities().IN_VEHICLE >= Acceleration.InVehicle.quartile_distance - Acceleration.InVehicle.semi_quartile_distance / 2
                && detectedActivities.getProbableActivities().IN_VEHICLE >= Acceleration.InVehicle.quartile_distance + Acceleration.InVehicle.semi_quartile_distance / 2) {

        }

    }

    public static void evaluateAcceleration(final DetectedActivities detectedActivities) {

    }

    public static void evaluateInVehicleMotion(final DetectedActivities detectedActivities) {

    }

    public static void evaluateDeceleration(final DetectedActivities detectedActivities) {

    }

    public static void evaluateWalking(final DetectedActivities detectedActivities) {

    }

    public static void evaluateRunning(final DetectedActivities detectedActivities) {

    }

    public static void evaluateOnBicycle(final DetectedActivities detectedActivities) {

    }

    public static void evaluateStill(final DetectedActivities detectedActivities) {

    }
}

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
            public final static double quartile_distance = upper_quartile - lower_quartile;
            public final static double semi_quartile_distance = quartile_distance / 2;
        }

        public static class InVehicle {
            public final static double average = 23.7;
            public final static double median = 19.0;
            public final static double std_deviation = 16.0;
            public final static double mean_deviation = 13.0;
            public final static double median_deviation = 9.0;
            public final static double lower_quartile = 11.0;
            public final static double upper_quartile = 31.0;
            public final static double quartile_distance = upper_quartile - lower_quartile;
            public final static double semi_quartile_distance = quartile_distance / 2;
        }

        public static boolean checkState(final int IN_VEHICLE_CONFIDENCE, final int STILL_CONFIDENCE) {
            return (IN_VEHICLE_CONFIDENCE >= InVehicle.average - InVehicle.std_deviation / 2
                    && IN_VEHICLE_CONFIDENCE <= InVehicle.average + InVehicle.std_deviation / 2
                    && STILL_CONFIDENCE >= Still.average - Still.std_deviation / 2
                    && STILL_CONFIDENCE <= Still.average + Still.std_deviation / 2)

                    || (IN_VEHICLE_CONFIDENCE >= InVehicle.average - InVehicle.mean_deviation / 2
                    && IN_VEHICLE_CONFIDENCE <= InVehicle.average + InVehicle.mean_deviation / 2
                    && STILL_CONFIDENCE >= Still.average - Still.mean_deviation / 2
                    && STILL_CONFIDENCE <= Still.average + Still.mean_deviation / 2)

                    || (IN_VEHICLE_CONFIDENCE >= InVehicle.median - InVehicle.median_deviation / 2
                    && IN_VEHICLE_CONFIDENCE <= InVehicle.median + InVehicle.median_deviation / 2
                    && STILL_CONFIDENCE >= Still.median - Still.median_deviation / 2
                    && STILL_CONFIDENCE <= Still.median + Still.median_deviation / 2)

                    || (IN_VEHICLE_CONFIDENCE >= InVehicle.quartile_distance - InVehicle.mean_deviation / 2
                    && IN_VEHICLE_CONFIDENCE <= InVehicle.quartile_distance + InVehicle.mean_deviation / 2
                    && STILL_CONFIDENCE >= Still.quartile_distance - Still.mean_deviation / 2
                    && STILL_CONFIDENCE <= Still.quartile_distance + Still.mean_deviation / 2)

                    || (IN_VEHICLE_CONFIDENCE >= InVehicle.quartile_distance - InVehicle.median_deviation / 2
                    && IN_VEHICLE_CONFIDENCE <= InVehicle.quartile_distance + InVehicle.median_deviation / 2
                    && STILL_CONFIDENCE >= Still.quartile_distance - Still.median_deviation / 2
                    && STILL_CONFIDENCE <= Still.quartile_distance + Still.median_deviation / 2)

                    || (IN_VEHICLE_CONFIDENCE >= InVehicle.median - InVehicle.semi_quartile_distance / 2
                    && IN_VEHICLE_CONFIDENCE <= InVehicle.median + InVehicle.semi_quartile_distance / 2
                    && STILL_CONFIDENCE >= Still.median - Still.semi_quartile_distance / 2
                    && STILL_CONFIDENCE <= Still.median + Still.semi_quartile_distance / 2);
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
            public final static double quartile_distance = upper_quartile - lower_quartile;
            public final static double semi_quartile_distance = quartile_distance / 2;
        }

        public static class InVehicle {
            public final static double average = 40.6;
            public final static double median = 30.0;
            public final static double std_deviation = 30.6;
            public final static double mean_deviation = 27.0;
            public final static double median_deviation = 20.0;
            public final static double lower_quartile = 13.0;
            public final static double upper_quartile = 69.0;
            public final static double quartile_distance = upper_quartile - lower_quartile;
            public final static double semi_quartile_distance = quartile_distance / 2;
        }

        public static boolean checkState(final int IN_VEHICLE_CONFIDENCE, final int STILL_CONFIDENCE) {
            return (IN_VEHICLE_CONFIDENCE >= InVehicle.average - InVehicle.std_deviation / 2
                    && IN_VEHICLE_CONFIDENCE <= InVehicle.average + InVehicle.std_deviation / 2
                    && STILL_CONFIDENCE >= Still.average - Still.std_deviation / 2
                    && STILL_CONFIDENCE <= Still.average + Still.std_deviation / 2)

                    || (IN_VEHICLE_CONFIDENCE >= InVehicle.average - InVehicle.mean_deviation / 2
                    && IN_VEHICLE_CONFIDENCE <= InVehicle.average + InVehicle.mean_deviation / 2
                    && STILL_CONFIDENCE >= Still.average - Still.mean_deviation / 2
                    && STILL_CONFIDENCE <= Still.average + Still.mean_deviation / 2)

                    || (IN_VEHICLE_CONFIDENCE >= InVehicle.median - InVehicle.median_deviation / 2
                    && IN_VEHICLE_CONFIDENCE <= InVehicle.median + InVehicle.median_deviation / 2
                    && STILL_CONFIDENCE >= Still.median - Still.median_deviation / 2
                    && STILL_CONFIDENCE <= Still.median + Still.median_deviation / 2)

                    || (IN_VEHICLE_CONFIDENCE >= InVehicle.quartile_distance - InVehicle.mean_deviation / 2
                    && IN_VEHICLE_CONFIDENCE <= InVehicle.quartile_distance + InVehicle.mean_deviation / 2
                    && STILL_CONFIDENCE >= Still.quartile_distance - Still.mean_deviation / 2
                    && STILL_CONFIDENCE <= Still.quartile_distance + Still.mean_deviation / 2)

                    || (IN_VEHICLE_CONFIDENCE >= InVehicle.quartile_distance - InVehicle.median_deviation / 2
                    && IN_VEHICLE_CONFIDENCE <= InVehicle.quartile_distance + InVehicle.median_deviation / 2
                    && STILL_CONFIDENCE >= Still.quartile_distance - Still.median_deviation / 2
                    && STILL_CONFIDENCE <= Still.quartile_distance + Still.median_deviation / 2)

                    || (IN_VEHICLE_CONFIDENCE >= InVehicle.median - InVehicle.semi_quartile_distance / 2
                    && IN_VEHICLE_CONFIDENCE <= InVehicle.median + InVehicle.semi_quartile_distance / 2
                    && STILL_CONFIDENCE >= Still.median - Still.semi_quartile_distance / 2
                    && STILL_CONFIDENCE <= Still.median + Still.semi_quartile_distance / 2);
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
            public final static double quartile_distance = upper_quartile - lower_quartile;
            public final static double semi_quartile_distance = quartile_distance / 2;
        }

        public static class InVehicle {
            public final static double average = 19.7;
            public final static double median = 15.0;
            public final static double std_deviation = 12.4;
            public final static double mean_deviation = 9.5;
            public final static double median_deviation = 5.0;
            public final static double lower_quartile = 12.0;
            public final static double upper_quartile = 25.0;
            public final static double quartile_distance = upper_quartile - lower_quartile;
            public final static double semi_quartile_distance = quartile_distance / 2;
        }

        public static boolean checkState(final int IN_VEHICLE_CONFIDENCE, final int STILL_CONFIDENCE) {
            return (IN_VEHICLE_CONFIDENCE >= InVehicle.average - InVehicle.std_deviation / 2
                    && IN_VEHICLE_CONFIDENCE <= InVehicle.average + InVehicle.std_deviation / 2
                    && STILL_CONFIDENCE >= Still.average - Still.std_deviation / 2
                    && STILL_CONFIDENCE <= Still.average + Still.std_deviation / 2)

                    || (IN_VEHICLE_CONFIDENCE >= InVehicle.average - InVehicle.mean_deviation / 2
                    && IN_VEHICLE_CONFIDENCE <= InVehicle.average + InVehicle.mean_deviation / 2
                    && STILL_CONFIDENCE >= Still.average - Still.mean_deviation / 2
                    && STILL_CONFIDENCE <= Still.average + Still.mean_deviation / 2)

                    || (IN_VEHICLE_CONFIDENCE >= InVehicle.median - InVehicle.median_deviation / 2
                    && IN_VEHICLE_CONFIDENCE <= InVehicle.median + InVehicle.median_deviation / 2
                    && STILL_CONFIDENCE >= Still.median - Still.median_deviation / 2
                    && STILL_CONFIDENCE <= Still.median + Still.median_deviation / 2)

                    || (IN_VEHICLE_CONFIDENCE >= InVehicle.quartile_distance - InVehicle.mean_deviation / 2
                    && IN_VEHICLE_CONFIDENCE <= InVehicle.quartile_distance + InVehicle.mean_deviation / 2
                    && STILL_CONFIDENCE >= Still.quartile_distance - Still.mean_deviation / 2
                    && STILL_CONFIDENCE <= Still.quartile_distance + Still.mean_deviation / 2)

                    || (IN_VEHICLE_CONFIDENCE >= InVehicle.quartile_distance - InVehicle.median_deviation / 2
                    && IN_VEHICLE_CONFIDENCE <= InVehicle.quartile_distance + InVehicle.median_deviation / 2
                    && STILL_CONFIDENCE >= Still.quartile_distance - Still.median_deviation / 2
                    && STILL_CONFIDENCE <= Still.quartile_distance + Still.median_deviation / 2)

                    || (IN_VEHICLE_CONFIDENCE >= InVehicle.median - InVehicle.semi_quartile_distance / 2
                    && IN_VEHICLE_CONFIDENCE <= InVehicle.median + InVehicle.semi_quartile_distance / 2
                    && STILL_CONFIDENCE >= Still.median - Still.semi_quartile_distance / 2
                    && STILL_CONFIDENCE <= Still.median + Still.semi_quartile_distance / 2);
        }
    }
}

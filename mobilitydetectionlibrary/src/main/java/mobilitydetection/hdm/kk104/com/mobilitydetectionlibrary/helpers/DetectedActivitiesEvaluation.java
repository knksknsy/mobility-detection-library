package mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.helpers;

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
            return (IN_VEHICLE_CONFIDENCE >= InVehicle.average - InVehicle.mean_deviation / 2 // 17.2
                    && IN_VEHICLE_CONFIDENCE <= InVehicle.average + InVehicle.mean_deviation / 2 // 30.2
                    && STILL_CONFIDENCE >= Still.average - Still.mean_deviation / 2 // 14
                    && STILL_CONFIDENCE <= Still.average + Still.mean_deviation / 2) // 25.6

                    || (IN_VEHICLE_CONFIDENCE >= InVehicle.average - InVehicle.std_deviation / 2 // 15.7
                    && IN_VEHICLE_CONFIDENCE <= InVehicle.average + InVehicle.std_deviation / 2 // 31.7
                    && STILL_CONFIDENCE >= Still.average - Still.std_deviation / 2 // 12.65
                    && STILL_CONFIDENCE <= Still.average + Still.std_deviation / 2) // 26.95

                    || (IN_VEHICLE_CONFIDENCE >= InVehicle.quartile_distance - InVehicle.median_deviation / 2 // 15.5
                    && IN_VEHICLE_CONFIDENCE <= InVehicle.quartile_distance + InVehicle.median_deviation / 2 // 24.5
                    && STILL_CONFIDENCE >= Still.quartile_distance - Still.median_deviation / 2 // 15.5
                    && STILL_CONFIDENCE <= Still.quartile_distance + Still.median_deviation / 2) // 22.5

                    || (IN_VEHICLE_CONFIDENCE >= InVehicle.median - InVehicle.median_deviation / 2 // 14.5
                    && IN_VEHICLE_CONFIDENCE <= InVehicle.median + InVehicle.median_deviation / 2 // 23.5
                    && STILL_CONFIDENCE >= Still.median - Still.median_deviation / 2 // 11.5
                    && STILL_CONFIDENCE <= Still.median + Still.median_deviation / 2) // 18.5

                    || (IN_VEHICLE_CONFIDENCE >= InVehicle.median - InVehicle.semi_quartile_distance / 2 // 14
                    && IN_VEHICLE_CONFIDENCE <= InVehicle.median + InVehicle.semi_quartile_distance / 2 // 24
                    && STILL_CONFIDENCE >= Still.median - Still.semi_quartile_distance / 2 // 10.25
                    && STILL_CONFIDENCE <= Still.median + Still.semi_quartile_distance / 2) // 19.75

                    || (IN_VEHICLE_CONFIDENCE >= InVehicle.quartile_distance - InVehicle.mean_deviation / 2 // 13.5
                    && IN_VEHICLE_CONFIDENCE <= InVehicle.quartile_distance + InVehicle.mean_deviation / 2 // 26.5
                    && STILL_CONFIDENCE >= Still.quartile_distance - Still.mean_deviation / 2 // 13.2
                    && STILL_CONFIDENCE <= Still.quartile_distance + Still.mean_deviation / 2); // 24.8
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
            return (IN_VEHICLE_CONFIDENCE >= InVehicle.quartile_distance - InVehicle.median_deviation / 2 // 46
                    && IN_VEHICLE_CONFIDENCE <= InVehicle.quartile_distance + InVehicle.median_deviation / 2 // 66
                    && STILL_CONFIDENCE >= Still.quartile_distance - Still.median_deviation / 2 // 15.5
                    && STILL_CONFIDENCE <= Still.quartile_distance + Still.median_deviation / 2) // 29

                    || (IN_VEHICLE_CONFIDENCE >= InVehicle.quartile_distance - InVehicle.mean_deviation / 2 // 42.5
                    && IN_VEHICLE_CONFIDENCE <= InVehicle.quartile_distance + InVehicle.mean_deviation / 2 // 69.5
                    && STILL_CONFIDENCE >= Still.quartile_distance - Still.mean_deviation / 2 // 13
                    && STILL_CONFIDENCE <= Still.quartile_distance + Still.mean_deviation / 2) // 27

                    || (IN_VEHICLE_CONFIDENCE >= InVehicle.average - InVehicle.mean_deviation / 2 // 27.1
                    && IN_VEHICLE_CONFIDENCE <= InVehicle.average + InVehicle.mean_deviation / 2 // 54.1
                    && STILL_CONFIDENCE >= Still.average - Still.mean_deviation / 2 // 9.5
                    && STILL_CONFIDENCE <= Still.average + Still.mean_deviation / 2) // 23.5

                    || (IN_VEHICLE_CONFIDENCE >= InVehicle.average - InVehicle.std_deviation / 2 // 25.3
                    && IN_VEHICLE_CONFIDENCE <= InVehicle.average + InVehicle.std_deviation / 2 // 55.9
                    && STILL_CONFIDENCE >= Still.average - Still.std_deviation / 2 // 7.45
                    && STILL_CONFIDENCE <= Still.average + Still.std_deviation / 2) // 22.55

                    || (IN_VEHICLE_CONFIDENCE >= InVehicle.median - InVehicle.median_deviation / 2 // 20
                    && IN_VEHICLE_CONFIDENCE <= InVehicle.median + InVehicle.median_deviation / 2 // 40
                    && STILL_CONFIDENCE >= Still.median - Still.median_deviation / 2 // 5.5
                    && STILL_CONFIDENCE <= Still.median + Still.median_deviation / 2) // 14.5

                    || (IN_VEHICLE_CONFIDENCE >= InVehicle.median - InVehicle.semi_quartile_distance / 2 // 16
                    && IN_VEHICLE_CONFIDENCE <= InVehicle.median + InVehicle.semi_quartile_distance / 2 // 44
                    && STILL_CONFIDENCE >= Still.median - Still.semi_quartile_distance / 2 // 5
                    && STILL_CONFIDENCE <= Still.median + Still.semi_quartile_distance / 2); // 15
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
            return (STILL_CONFIDENCE >= Still.average - Still.mean_deviation / 2 // 21.25
                    && STILL_CONFIDENCE <= Still.average + Still.mean_deviation / 2 // 34.15
                    && IN_VEHICLE_CONFIDENCE >= InVehicle.average - InVehicle.mean_deviation / 2 // 14.95
                    && IN_VEHICLE_CONFIDENCE <= InVehicle.average + InVehicle.mean_deviation / 2) // 24.45

                    || (STILL_CONFIDENCE >= Still.median - Still.median_deviation / 2 // 20
                    && STILL_CONFIDENCE <= Still.median + Still.median_deviation / 2 // 30
                    && IN_VEHICLE_CONFIDENCE >= InVehicle.median - InVehicle.median_deviation / 2 // 12.5
                    && IN_VEHICLE_CONFIDENCE <= InVehicle.median + InVehicle.median_deviation / 2) // 17.5

                    || (STILL_CONFIDENCE >= Still.average - Still.std_deviation / 2 // 19.55
                    && STILL_CONFIDENCE <= Still.average + Still.std_deviation / 2 // 35.85
                    && IN_VEHICLE_CONFIDENCE >= InVehicle.average - InVehicle.std_deviation / 2 // 13.5
                    && IN_VEHICLE_CONFIDENCE <= InVehicle.average + InVehicle.std_deviation / 2) // 25.9

                    || (STILL_CONFIDENCE >= Still.median - Still.semi_quartile_distance / 2 // 19.5
                    && STILL_CONFIDENCE <= Still.median + Still.semi_quartile_distance / 2 // 30.5
                    && IN_VEHICLE_CONFIDENCE >= InVehicle.median - InVehicle.semi_quartile_distance / 2 // 11.75
                    && IN_VEHICLE_CONFIDENCE <= InVehicle.median + InVehicle.semi_quartile_distance / 2) // 18.25

                    || (STILL_CONFIDENCE >= Still.quartile_distance - Still.median_deviation / 2 // 17
                    && STILL_CONFIDENCE <= Still.quartile_distance + Still.median_deviation / 2 // 27
                    && IN_VEHICLE_CONFIDENCE >= InVehicle.quartile_distance - InVehicle.median_deviation / 2 // 10.5
                    && IN_VEHICLE_CONFIDENCE <= InVehicle.quartile_distance + InVehicle.median_deviation / 2) // 15.5

                    || (STILL_CONFIDENCE >= Still.quartile_distance - Still.mean_deviation / 2 // 15.55
                    && STILL_CONFIDENCE <= Still.quartile_distance + Still.mean_deviation / 2 // 28.45
                    && IN_VEHICLE_CONFIDENCE >= InVehicle.quartile_distance - InVehicle.mean_deviation / 2 // 8.25
                    && IN_VEHICLE_CONFIDENCE <= InVehicle.quartile_distance + InVehicle.mean_deviation / 2); // 17.75
        }
    }
}

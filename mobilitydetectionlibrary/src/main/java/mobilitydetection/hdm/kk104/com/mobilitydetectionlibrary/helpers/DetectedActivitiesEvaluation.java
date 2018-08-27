package mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.helpers;

import mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.ProbableActivities;

/**
 * Class containing static classes for estimating vehicle based activities.
 */
public class DetectedActivitiesEvaluation {

    /**
     * Class containing data ranges which are used to estimate if a vehicle accelerates.
     */
    public static class Acceleration {
        /**
         * Class containing percentages of a STILL activity.
         *
         * @see mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.ProbableActivities
         */
        private static class Still {
            private final static double average = 19.8;
            private final static double median = 15.0;
            private final static double std_deviation = 14.3;
            private final static double mean_deviation = 11.6;
            private final static double median_deviation = 7.0;
            private final static double lower_quartile = 10.0;
            private final static double upper_quartile = 29.0;
            private final static double quartile_distance = upper_quartile - lower_quartile;
            private final static double semi_quartile_distance = quartile_distance / 2;
        }

        /**
         * Class containing percentages of a IN_VEHICLE activity.
         *
         * @see mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.ProbableActivities
         */
        private static class InVehicle {
            private final static double average = 23.7;
            private final static double median = 19.0;
            private final static double std_deviation = 16.0;
            private final static double mean_deviation = 13.0;
            private final static double median_deviation = 9.0;
            private final static double lower_quartile = 11.0;
            private final static double upper_quartile = 31.0;
            private final static double quartile_distance = upper_quartile - lower_quartile;
            private final static double semi_quartile_distance = quartile_distance / 2;
        }

        /**
         * Class containing percentages of a WALKING activity.
         *
         * @see mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.ProbableActivities
         */
        private static class Walking {
            private final static double average = 10.5;
            private final static double median = 10.0;
            private final static double std_deviation = 6.7;
            private final static double mean_deviation = 4.5;
            private final static double median_deviation = 3.0;
            private final static double lower_quartile = 6.0;
            private final static double upper_quartile = 12.0;
            private final static double quartile_distance = upper_quartile - lower_quartile;
            private final static double semi_quartile_distance = quartile_distance / 2;
        }

        /**
         * Return if the activity is accelerating
         *
         * @param probableActivities ProbableActivities object containing the probabilities of each activity.
         * @return state of if activity is accelerating
         * @see mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.ProbableActivities
         */
        public static boolean checkState(final ProbableActivities probableActivities) {
            return (probableActivities.IN_VEHICLE >= InVehicle.average - InVehicle.mean_deviation / 2 // 17.2
                    && probableActivities.IN_VEHICLE <= InVehicle.average + InVehicle.mean_deviation / 2 // 30.2
                    && probableActivities.STILL >= Still.average - Still.mean_deviation / 2 // 14
                    && probableActivities.STILL <= Still.average + Still.mean_deviation / 2) // 25.6
                    /*&& probableActivities.WALKING >= Walking.average - Walking.mean_deviation / 2
                    && probableActivities.WALKING <= Walking.average + Walking.mean_deviation / 2)*/

                    || (probableActivities.IN_VEHICLE >= InVehicle.average - InVehicle.std_deviation / 2 // 15.7
                    && probableActivities.IN_VEHICLE <= InVehicle.average + InVehicle.std_deviation / 2 // 31.7
                    && probableActivities.STILL >= Still.average - Still.std_deviation / 2 // 12.65
                    && probableActivities.STILL <= Still.average + Still.std_deviation / 2) // 26.95
                    /*&& probableActivities.WALKING >= Walking.average - Walking.std_deviation / 2
                    && probableActivities.WALKING <= Walking.average + Walking.std_deviation / 2)*/

                    || (probableActivities.IN_VEHICLE >= InVehicle.quartile_distance - InVehicle.median_deviation / 2 // 15.5
                    && probableActivities.IN_VEHICLE <= InVehicle.quartile_distance + InVehicle.median_deviation / 2 // 24.5
                    && probableActivities.STILL >= Still.quartile_distance - Still.median_deviation / 2 // 15.5
                    && probableActivities.STILL <= Still.quartile_distance + Still.median_deviation / 2) // 22.5
                    /*&& probableActivities.WALKING >= Walking.quartile_distance - Walking.mean_deviation / 2
                    && probableActivities.WALKING <= Walking.quartile_distance + Walking.mean_deviation / 2)*/

                    || (probableActivities.IN_VEHICLE >= InVehicle.median - InVehicle.median_deviation / 2 // 14.5
                    && probableActivities.IN_VEHICLE <= InVehicle.median + InVehicle.median_deviation / 2 // 23.5
                    && probableActivities.STILL >= Still.median - Still.median_deviation / 2 // 11.5
                    && probableActivities.STILL <= Still.median + Still.median_deviation / 2) // 18.5
                    /*&& probableActivities.WALKING >= Walking.median - Walking.median_deviation / 2
                    && probableActivities.WALKING <= Walking.median + Walking.median_deviation / 2)*/

                    || (probableActivities.IN_VEHICLE >= InVehicle.median - InVehicle.semi_quartile_distance / 2 // 14
                    && probableActivities.IN_VEHICLE <= InVehicle.median + InVehicle.semi_quartile_distance / 2 // 24
                    && probableActivities.STILL >= Still.median - Still.semi_quartile_distance / 2 // 10.25
                    && probableActivities.STILL <= Still.median + Still.semi_quartile_distance / 2) // 19.75
                    /*&& probableActivities.WALKING >= Walking.median - Walking.semi_quartile_distance / 2
                    && probableActivities.WALKING <= Walking.median + Walking.semi_quartile_distance / 2)*/

                    || (probableActivities.IN_VEHICLE >= InVehicle.quartile_distance - InVehicle.mean_deviation / 2 // 13.5
                    && probableActivities.IN_VEHICLE <= InVehicle.quartile_distance + InVehicle.mean_deviation / 2 // 26.5
                    && probableActivities.STILL >= Still.quartile_distance - Still.mean_deviation / 2 // 13.2
                    && probableActivities.STILL <= Still.quartile_distance + Still.mean_deviation / 2); // 24.8
                    /*&& probableActivities.WALKING >= Walking.quartile_distance - Walking.mean_deviation / 2
                    && probableActivities.WALKING <= Walking.quartile_distance + Walking.mean_deviation / 2);*/
        }
    }

    /**
     * Class containing data ranges which are used to estimate if a vehicle is in constant motion
     */
    public static class InVehicleMotion {
        /**
         * Class containing percentages of a STILL activity.
         *
         * @see mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.ProbableActivities
         */
        private static class Still {
            private final static double average = 16.5;
            private final static double median = 10.0;
            private final static double std_deviation = 18.1;
            private final static double mean_deviation = 14.0;
            private final static double median_deviation = 9.0;
            private final static double lower_quartile = 4.0;
            private final static double upper_quartile = 24.0;
            private final static double quartile_distance = upper_quartile - lower_quartile;
            private final static double semi_quartile_distance = quartile_distance / 2;
        }

        /**
         * Class containing percentages of a IN_VEHICLE activity.
         *
         * @see mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.ProbableActivities
         */
        private static class InVehicle {
            private final static double average = 40.6;
            private final static double median = 30.0;
            private final static double std_deviation = 30.6;
            private final static double mean_deviation = 27.0;
            private final static double median_deviation = 20.0;
            private final static double lower_quartile = 13.0;
            private final static double upper_quartile = 69.0;
            private final static double quartile_distance = upper_quartile - lower_quartile;
            private final static double semi_quartile_distance = quartile_distance / 2;
        }

        /**
         * Class containing percentages of a WALKING activity.
         *
         * @see mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.ProbableActivities
         */
        private static class Walking {
            private final static double average = 8.4;
            private final static double median = 7.0;
            private final static double std_deviation = 10.1;
            private final static double mean_deviation = 6.0;
            private final static double median_deviation = 4.0;
            private final static double lower_quartile = 2.0;
            private final static double upper_quartile = 10.0;
            private final static double quartile_distance = upper_quartile - lower_quartile;
            private final static double semi_quartile_distance = quartile_distance / 2;
        }

        /**
         * Return if the activity is a vehicle in a constant motion
         *
         * @param probableActivities ProbableActivities object containing the probabilities of each activity.
         * @return state of if activity is in constant motion
         * @see mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.ProbableActivities
         */
        public static boolean checkState(final ProbableActivities probableActivities) {
            return (probableActivities.IN_VEHICLE >= InVehicle.quartile_distance - InVehicle.median_deviation / 2 // 46
                    && probableActivities.IN_VEHICLE <= InVehicle.quartile_distance + InVehicle.median_deviation / 2 // 66
                    && probableActivities.STILL >= Still.quartile_distance - Still.median_deviation / 2 // 15.5
                    && probableActivities.STILL <= Still.quartile_distance + Still.median_deviation / 2) // 29
                    /*&& probableActivities.WALKING >= Walking.quartile_distance - Walking.median_deviation / 2
                    && probableActivities.WALKING <= Walking.quartile_distance + Walking.median_deviation / 2)*/

                    || (probableActivities.IN_VEHICLE >= InVehicle.quartile_distance - InVehicle.mean_deviation / 2 // 42.5
                    && probableActivities.IN_VEHICLE <= InVehicle.quartile_distance + InVehicle.mean_deviation / 2 // 69.5
                    && probableActivities.STILL >= Still.quartile_distance - Still.mean_deviation / 2 // 13
                    && probableActivities.STILL <= Still.quartile_distance + Still.mean_deviation / 2) // 27
                    /*&& probableActivities.WALKING >= Walking.quartile_distance - Walking.mean_deviation / 2
                    && probableActivities.WALKING <= Walking.quartile_distance + Walking.mean_deviation / 2)*/

                    || (probableActivities.IN_VEHICLE >= InVehicle.average - InVehicle.mean_deviation / 2 // 27.1
                    && probableActivities.IN_VEHICLE <= InVehicle.average + InVehicle.mean_deviation / 2 // 54.1
                    && probableActivities.STILL >= Still.average - Still.mean_deviation / 2 // 9.5
                    && probableActivities.STILL <= Still.average + Still.mean_deviation / 2) // 23.5
                    /*&& probableActivities.WALKING >= Walking.average - Walking.mean_deviation / 2
                    && probableActivities.WALKING <= Walking.average + Walking.mean_deviation / 2)*/

                    || (probableActivities.IN_VEHICLE >= InVehicle.average - InVehicle.std_deviation / 2 // 25.3
                    && probableActivities.IN_VEHICLE <= InVehicle.average + InVehicle.std_deviation / 2 // 55.9
                    && probableActivities.STILL >= Still.average - Still.std_deviation / 2 // 7.45
                    && probableActivities.STILL <= Still.average + Still.std_deviation / 2) // 22.55
                    /*&& probableActivities.WALKING >= Walking.average - Walking.std_deviation / 2
                    && probableActivities.WALKING <= Walking.average + Walking.std_deviation / 2)*/

                    || (probableActivities.IN_VEHICLE >= InVehicle.median - InVehicle.median_deviation / 2 // 20
                    && probableActivities.IN_VEHICLE <= InVehicle.median + InVehicle.median_deviation / 2 // 40
                    && probableActivities.STILL >= Still.median - Still.median_deviation / 2 // 5.5
                    && probableActivities.STILL <= Still.median + Still.median_deviation / 2) // 14.5
                    /*&& probableActivities.WALKING >= Walking.median - Walking.median_deviation / 2
                    && probableActivities.WALKING <= Walking.median + Walking.median_deviation / 2)*/

                    || (probableActivities.IN_VEHICLE >= InVehicle.median - InVehicle.semi_quartile_distance / 2 // 16
                    && probableActivities.IN_VEHICLE <= InVehicle.median + InVehicle.semi_quartile_distance / 2 // 44
                    && probableActivities.STILL >= Still.median - Still.semi_quartile_distance / 2 // 5
                    && probableActivities.STILL <= Still.median + Still.semi_quartile_distance / 2); // 15
                    /*&& probableActivities.WALKING >= Walking.median - Walking.semi_quartile_distance / 2
                    && probableActivities.WALKING <= Walking.median + Walking.semi_quartile_distance / 2);*/
        }
    }

    /**
     * Class containing data ranges which are used to estimate if a vehicle decelerates
     */
    public static class Deceleration {
        /**
         * Class containing percentages of a STILL activity.
         *
         * @see mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.ProbableActivities
         */
        private static class Still {
            private final static double average = 27.7;
            private final static double median = 25.0;
            private final static double std_deviation = 16.3;
            private final static double mean_deviation = 12.9;
            private final static double median_deviation = 10.0;
            private final static double lower_quartile = 16.0;
            private final static double upper_quartile = 38.0;
            private final static double quartile_distance = upper_quartile - lower_quartile;
            private final static double semi_quartile_distance = quartile_distance / 2;
        }

        /**
         * Class containing percentages of a IN_VEHICLE activity.
         *
         * @see mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.ProbableActivities
         */
        private static class InVehicle {
            private final static double average = 19.7;
            private final static double median = 15.0;
            private final static double std_deviation = 12.4;
            private final static double mean_deviation = 9.5;
            private final static double median_deviation = 5.0;
            private final static double lower_quartile = 12.0;
            private final static double upper_quartile = 25.0;
            private final static double quartile_distance = upper_quartile - lower_quartile;
            private final static double semi_quartile_distance = quartile_distance / 2;
        }

        /**
         * Class containing percentages of a WALKING activity.
         *
         * @see mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.ProbableActivities
         */
        private static class Walking {
            private final static double average = 12.0;
            private final static double median = 10.0;
            private final static double std_deviation = 9.4;
            private final static double mean_deviation = 5.1;
            private final static double median_deviation = 3.0;
            private final static double lower_quartile = 8.0;
            private final static double upper_quartile = 13.0;
            private final static double quartile_distance = upper_quartile - lower_quartile;
            private final static double semi_quartile_distance = quartile_distance / 2;
        }

        /**
         * Return if the activity is decelerating
         *
         * @param probableActivities ProbableActivities object containing the probabilities of each activity.
         * @return state of if activity is decelerating
         * @see mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.models.ProbableActivities
         */
        public static boolean checkState(final ProbableActivities probableActivities) {
            return (probableActivities.STILL >= Still.average - Still.mean_deviation / 2 // 21.25
                    && probableActivities.STILL <= Still.average + Still.mean_deviation / 2 // 34.15
                    && probableActivities.IN_VEHICLE >= InVehicle.average - InVehicle.mean_deviation / 2 // 14.95
                    && probableActivities.IN_VEHICLE <= InVehicle.average + InVehicle.mean_deviation / 2) // 24.45
                    /*&& probableActivities.WALKING >= Walking.average - Walking.mean_deviation / 2
                    && probableActivities.WALKING <= Walking.average + Walking.mean_deviation / 2)*/

                    || (probableActivities.STILL >= Still.median - Still.median_deviation / 2 // 20
                    && probableActivities.STILL <= Still.median + Still.median_deviation / 2 // 30
                    && probableActivities.IN_VEHICLE >= InVehicle.median - InVehicle.median_deviation / 2 // 12.5
                    && probableActivities.IN_VEHICLE <= InVehicle.median + InVehicle.median_deviation / 2) // 17.5
                    /*&& probableActivities.WALKING >= Walking.median - Walking.median_deviation / 2
                    && probableActivities.WALKING <= Walking.median + Walking.median_deviation / 2)*/

                    || (probableActivities.STILL >= Still.average - Still.std_deviation / 2 // 19.55
                    && probableActivities.STILL <= Still.average + Still.std_deviation / 2 // 35.85
                    && probableActivities.IN_VEHICLE >= InVehicle.average - InVehicle.std_deviation / 2 // 13.5
                    && probableActivities.IN_VEHICLE <= InVehicle.average + InVehicle.std_deviation / 2) // 25.9
                    /*&& probableActivities.WALKING >= Walking.average - Walking.std_deviation / 2
                    && probableActivities.WALKING <= Walking.average + Walking.std_deviation / 2)*/

                    || (probableActivities.STILL >= Still.median - Still.semi_quartile_distance / 2 // 19.5
                    && probableActivities.STILL <= Still.median + Still.semi_quartile_distance / 2 // 30.5
                    && probableActivities.IN_VEHICLE >= InVehicle.median - InVehicle.semi_quartile_distance / 2 // 11.75
                    && probableActivities.IN_VEHICLE <= InVehicle.median + InVehicle.semi_quartile_distance / 2) // 18.25
                    /*&& probableActivities.WALKING >= Walking.median - Walking.semi_quartile_distance / 2
                    && probableActivities.WALKING <= Walking.median + Walking.semi_quartile_distance / 2)*/

                    || (probableActivities.STILL >= Still.quartile_distance - Still.median_deviation / 2 // 17
                    && probableActivities.STILL <= Still.quartile_distance + Still.median_deviation / 2 // 27
                    && probableActivities.IN_VEHICLE >= InVehicle.quartile_distance - InVehicle.median_deviation / 2 // 10.5
                    && probableActivities.IN_VEHICLE <= InVehicle.quartile_distance + InVehicle.median_deviation / 2) // 15.5
                    /*&& probableActivities.WALKING >= Walking.quartile_distance - Walking.median_deviation / 2
                    && probableActivities.WALKING <= Walking.quartile_distance + Walking.median_deviation / 2)*/

                    || (probableActivities.STILL >= Still.quartile_distance - Still.mean_deviation / 2 // 15.55
                    && probableActivities.STILL <= Still.quartile_distance + Still.mean_deviation / 2 // 28.45
                    && probableActivities.IN_VEHICLE >= InVehicle.quartile_distance - InVehicle.mean_deviation / 2 // 8.25
                    && probableActivities.IN_VEHICLE <= InVehicle.quartile_distance + InVehicle.mean_deviation / 2); // 17.75
                    /*&& probableActivities.WALKING >= Walking.quartile_distance - Walking.mean_deviation / 2
                    && probableActivities.WALKING <= Walking.quartile_distance + Walking.mean_deviation / 2);*/
        }
    }
}

package geocaching;

import map.test.myapplication3.app.R;

public class Utils {
    public static int getMarkerResId(GeoCacheType type, GeoCacheStatus status) {
        switch (type) {
            case TRADITIONAL:
                switch (status) {
                    case VALID:
                        return R.drawable.ic_cache_default_traditional_valid;
                    case NOT_VALID:
                        return R.drawable.ic_cache_default_traditional_not_valid;
                    case NOT_CONFIRMED:
                        return R.drawable.ic_cache_default_traditional_not_confirmed;
                }
                break;

            case STEP_BY_STEP_TRADITIONAL:
                switch (status) {
                    case VALID:
                        return R.drawable.ic_cache_default_stepbystep_valid;
                    case NOT_VALID:
                        return R.drawable.ic_cache_default_stepbystep_not_valid;
                    case NOT_CONFIRMED:
                        return R.drawable.ic_cache_default_stepbystep_not_confirmed;
                }
                break;
            case VIRTUAL:
                switch (status) {
                    case VALID:
                        return R.drawable.ic_cache_default_virtual_valid;
                    case NOT_VALID:
                        return R.drawable.ic_cache_default_virtual_not_valid;
                    case NOT_CONFIRMED:
                        return R.drawable.ic_cache_default_virtual_not_confirmed;
                }
                break;
            case EVENT:
                switch (status) {
                    case VALID:
                        return R.drawable.ic_cache_default_event_valid;
                    case NOT_VALID:
                        return R.drawable.ic_cache_default_event_not_valid;
                    case NOT_CONFIRMED:
                        return R.drawable.ic_cache_default_event_not_confirmed;
                }
                break;
            case WEBCAM:
                switch (status) {
                    case VALID:
                        return R.drawable.ic_cache_default_virtual_valid;
                    case NOT_VALID:
                        return R.drawable.ic_cache_default_virtual_not_valid;
                    case NOT_CONFIRMED:
                        return R.drawable.ic_cache_default_virtual_not_confirmed;
                }
                break;
            case EXTREME:
                switch (status) {
                    case VALID:
                        return R.drawable.ic_cache_default_extreme_valid;
                    case NOT_VALID:
                        return R.drawable.ic_cache_default_extreme_not_valid;
                    case NOT_CONFIRMED:
                        return R.drawable.ic_cache_default_extreme_not_confirmed;
                }
                break;
            case STEP_BY_STEP_VIRTUAL:
                switch (status) {
                    case VALID:
                        return R.drawable.ic_cache_default_virtual_stepbystep_valid;
                    case NOT_VALID:
                        return R.drawable.ic_cache_default_virtual_stepbystep_not_valid;
                    case NOT_CONFIRMED:
                        return R.drawable.ic_cache_default_virtual_stepbystep_not_confirmed;
                }
                break;
            case CONTEST:
                switch (status) {
                    case VALID:
                        return R.drawable.ic_cache_default_competition_valid;
                    case NOT_VALID:
                        return R.drawable.ic_cache_default_competition_not_valid;
                    case NOT_CONFIRMED:
                        return R.drawable.ic_cache_default_competition_not_confirmed;
                }
                break;
            case GROUP:
                return R.drawable.ic_cache_default_group;
        }
        return -1;
    }

    public static GeoCacheType numberToType(int type) {
        switch (type) {
            case 1:
                return GeoCacheType.TRADITIONAL;
            case 2:
                return GeoCacheType.STEP_BY_STEP_TRADITIONAL;
            case 3:
                return GeoCacheType.VIRTUAL;
            case 4:
                return GeoCacheType.EVENT;
            case 5:
                return GeoCacheType.WEBCAM;
            case 6:
                return GeoCacheType.EXTREME;
            case 7:
                return GeoCacheType.STEP_BY_STEP_VIRTUAL;
            case 8:
                return GeoCacheType.CONTEST;
            default:
                return GeoCacheType.TRADITIONAL;
        }
    }

    public static GeoCacheStatus numberToStatus(int number) {
        switch (number) {
            case 1:
                return GeoCacheStatus.VALID;
            case 2:
                return GeoCacheStatus.NOT_VALID;
            case 3:
                return GeoCacheStatus.NOT_CONFIRMED;
            default:
                return GeoCacheStatus.VALID;
        }
    }
}

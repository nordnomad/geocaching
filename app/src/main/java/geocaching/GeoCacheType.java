package geocaching;

public enum GeoCacheType {
    TRADITIONAL("Традиционный"),
    STEP_BY_STEP_TRADITIONAL("Традиционный пошаговый"),
    STEP_BY_STEP_VIRTUAL("Виртуальный пошаговый"),
    VIRTUAL("Виртуальный"),
    EVENT(""), WEBCAM(""), EXTREME(""), CONTEST(""), GROUP(""), CHECKPOINT("");

    public String title;

    GeoCacheType(String title) {
        this.title = title;
    }
}

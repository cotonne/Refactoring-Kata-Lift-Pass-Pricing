package dojo.liftpasspricing.domain;

public class Age {
    private final Integer value;

    private Age(Integer value) {
        this.value = value;
    }

    public boolean isSet() {
        return value != null;
    }

    public static Age from(Integer value) {
        return new Age(value);
    }

    public boolean isChild() {
        return isSet() && value < 6;
    }

    public boolean isOld() {
        return isSet() && value > 64;
    }

    public boolean isTeenager() {
        return isSet() && value < 15;
    }
}

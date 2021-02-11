package dojo.liftpasspricing.domain;

enum LiftPassTypes {
    NIGHT_LIFT_PASS("night");

    private final String name;

    LiftPassTypes(String name) {
        this.name = name;
    }
}

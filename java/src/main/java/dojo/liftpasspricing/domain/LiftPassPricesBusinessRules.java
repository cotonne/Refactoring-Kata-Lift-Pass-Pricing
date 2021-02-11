package dojo.liftpasspricing.domain;

import dojo.liftpasspricing.Prices;
import dojo.liftpasspricing.domain.Age;

import java.util.function.BiFunction;

enum LiftPassPricesBusinessRules {
    FREE_FOR_CHILD((age, type) -> age.isChild(), (baseCost, reduction) -> 0.),
    NIGHT_DEFAULT_PRICE_WITHOUT_AGE((age, type) -> Prices.NIGHT_LIFT_PASS.equals(type) && !age.isSet(), (baseCost, reduction) -> 0.),
    NIGHT_PRICE_FOR_OLD((age, type) -> Prices.NIGHT_LIFT_PASS.equals(type) && age.isOld(), (baseCost, reduction) -> baseCost * .4),
    NIGHT_DEFAULT_PRICE((age, type) -> Prices.NIGHT_LIFT_PASS.equals(type) && !age.isSet(), (baseCost, reduction) -> baseCost),
    TEENAGER_PRICE((age, type) -> age.isTeenager(), (baseCost, reduction) -> baseCost * .7),
    DEFAULT_PRICE_WITHOUT_AGE((age, type) -> !age.isSet(), (baseCost, reduction) -> baseCost * (1 - reduction / 100.0)),
    DEFAULT_PRICE_FOR_OLD((age, type) -> age.isOld(), (baseCost, reduction) -> baseCost * .75 * (1 - reduction / 100.0)),
    DEFAULT_PRICE((age, type) -> true, (baseCost, reduction) -> baseCost * (1 - reduction / 100.0));
    final BiFunction<Age, String, Boolean> condition;
    final BiFunction<Double, Double, Double> finalPrice;

    LiftPassPricesBusinessRules(
            BiFunction<Age, String, Boolean> condition,
            BiFunction<Double, Double, Double> finalPrice) {
        this.condition = condition;
        this.finalPrice = finalPrice;
    }
}

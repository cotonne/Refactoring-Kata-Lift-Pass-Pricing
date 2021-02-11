package dojo.liftpasspricing.domain;

import java.util.stream.Stream;

class QueryParam<T> {
    final T value;

    public QueryParam(T value) {
        this.value = value;
    }

    public boolean isSet() {
        return value != null;
    }
}

public class PriceQuery {
    private final String type;
    private final double reduction;
    private final Age a;

    public PriceQuery(String type, Age a, double reduction) {
        this.type = type;
        this.reduction = reduction;
        this.a = a;
    }

    public double calculateReduction(double baseCost) {
        return Stream.of(LiftPassPricesBusinessRules.values())
                .filter((rule) -> rule.condition.apply(a, type))
                .findFirst()
                .map((rule) -> rule.finalPrice.apply(baseCost, reduction))
                .orElseThrow(() -> new IllegalStateException("Unexpected case"));
    }
}


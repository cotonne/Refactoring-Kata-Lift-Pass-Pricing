package dojo.liftpasspricing;

import dojo.liftpasspricing.domain.Age;
import dojo.liftpasspricing.domain.Holidays;
import dojo.liftpasspricing.domain.PriceQuery;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static spark.Spark.after;
import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.put;

public class Prices {

    public static final String NIGHT_LIFT_PASS = "night";

    public static Connection createApp() throws SQLException {

        final Connection connection = DriverManager
                .getConnection(Settings.JDBC_CONNECTION_URL, Settings.USERNAME, Settings.PASSWORD);

        final Optional<Holidays> allHolidays = getHolidays(connection);

        final Function<String, Optional<Double>> findBaseCost = getBaseCost(connection);

        port(4567);

        put("/prices", (req, res) -> {
            int liftPassCost = Integer.parseInt(req.queryParams("cost"));
            String liftPassType = req.queryParams("type");

            try (PreparedStatement stmt = connection.prepareStatement( //
                    "INSERT INTO base_price (type, cost) VALUES (?, ?) " + //
                            "ON DUPLICATE KEY UPDATE cost = ?")) {
                stmt.setString(1, liftPassType);
                stmt.setInt(2, liftPassCost);
                stmt.setInt(3, liftPassCost);
                stmt.execute();
            }

            return "";
        });

        get("/prices", (req, res) -> {
            final String type = req.queryParams("type");
            final String date = req.queryParams("date");
            final String age1 = req.queryParams("age");
            final Integer age = age1 != null ? Integer.valueOf(age1) : null;

            Optional<Date> queriedDate = Optional.ofNullable(date)
                    .flatMap(SafeDateParser::parse);

            final double reduction = allHolidays
                    .flatMap((holidays) -> queriedDate.map(holidays::calculateReductionForMondayBusinessDay))
                    .orElse(0);

            final Age queriedAge = Age.from(age);
            final PriceQuery priceQuery = new PriceQuery(type, queriedAge, reduction);

            return findBaseCost.apply(type)
                    .map(priceQuery::calculateReduction)
                    .map(JsonResponse::build)
                    .orElseThrow(IllegalStateException::new);
        });

        after((req, res) -> res.type("application/json"));

        return connection;
    }

    private static Optional<Holidays> getHolidays(Connection connection) {
        final Set<Date> allHolidays = new HashSet<>();
        try (PreparedStatement holidayStmt = connection.prepareStatement( //
                "SELECT * FROM holidays")) {
            try (ResultSet holidays = holidayStmt.executeQuery()) {
                while (holidays.next()) {
                    Date holiday = holidays.getDate("holiday");
                    allHolidays.add(holiday);
                }
            }
            return Optional.of(new Holidays(allHolidays));
        } catch (SQLException e) {
            return Optional.empty();
        }
    }

    private static Function<String, Optional<Double>> getBaseCost(Connection connection) {
        return (type) -> {
            final double baseCost;
            try (PreparedStatement costStmt = connection.prepareStatement( //
                    "SELECT cost FROM base_price " + //
                            "WHERE type = ?")) {
                costStmt.setString(1, type);

                try (ResultSet result = costStmt.executeQuery()) {
                    result.next();
                    baseCost = result.getDouble("cost");
                }
                return Optional.of(baseCost);
            } catch (SQLException e) {
                return Optional.empty();
            }
        };
    }


}

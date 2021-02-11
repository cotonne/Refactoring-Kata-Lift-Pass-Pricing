package dojo.liftpasspricing;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import spark.Spark;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PricesTest {

    private static Connection connection;

    @BeforeAll
    public static void createPrices() throws SQLException {
        Settings.JDBC_CONNECTION_URL = "jdbc:h2:mem:myDb;DB_CLOSE_DELAY=-1;INIT=RUNSCRIPT FROM '../database/initDatabase.sql';";
        Settings.USERNAME = "sa";
        Settings.PASSWORD = "sa";
        connection = Prices.createApp();
    }

    @AfterAll
    public static void stopApplication() throws SQLException {
        Spark.stop();
        connection.close();
    }

    @Test
    public void getCostForNightLiftPass() {
        Map<String, String> params = Map.of(
                "type", "night"
        );
        int cost = when_I_get_the_cost(params);
        assertEquals(0, cost);
    }

    @Test
    public void getCostForNightLiftPassDEfault() {
        Map<String, String> params = Map.of(
                "type", "night",
                "age", "64"
        );
        int cost = when_I_get_the_cost(params);
        assertEquals(19, cost);
    }

    @Test
    public void get60ReducedCostForNightLiftPassWhenStrictlyMoreThan64years() {
        Map<String, String> params = Map.of(
                "type", "night",
                "age", "65"
        );
        int cost = when_I_get_the_cost(params);
        assertEquals(8, cost);
    }

    @Test
    public void getCostFor1jourLiftPassWithoutDate() {
        Map<String, String> params = Map.of(
                "type", "1jour"
        );
        int cost = when_I_get_the_cost(params);
        assertEquals(35, cost);
    }

    @Test
    public void getCostFor1jourAtHolidays() {
        Map<String, String> params = Map.of(
                "type", "1jour",
                "date", "2019-02-18"
        );
        int cost = when_I_get_the_cost(params);
        assertEquals(35, cost);
    }

    @Test
    public void get35ReductionOnCostFor1jourLiftPassOutsideHolidaysEveryTuesday() {
        Map<String, String> params = Map.of(
                "type", "1jour",
                "date", "2019-02-11"
        );
        int cost = when_I_get_the_cost(params);
        assertEquals(23, cost);
    }

    @Test
    public void get30ReducedPriceFor1jourLiftPassForTeenagersStrictlyLessThan15y() {
        Map<String, String> params = Map.of(
                "type", "1jour",
                "age", "14"
        );
        int cost = when_I_get_the_cost(params);
        assertEquals(25, cost);
    }

    @Test
    public void getFreeLiftPassWhenStrictlyLessThan6Year() {
        Map<String, String> params = Map.of(
                "type", "1jour",
                "age", "5"
        );
        int cost = when_I_get_the_cost(params);
        assertEquals(0, cost);
    }

    @Test
    public void getXXReducedCostFor1jourLiftPassWhenStrictlyMoreThan64() {
        Map<String, String> params = Map.of(
                "type", "1jour",
                "age", "65"
        );
        int cost = when_I_get_the_cost(params);
        assertEquals(27, cost);
    }

    @Test
    public void getXXReducedCostFor1jourLiftPassDefault() {
        Map<String, String> params = Map.of(
                "type", "1jour",
                "age", "64"
        );
        int cost = when_I_get_the_cost(params);
        assertEquals(35, cost);
    }

    private int when_I_get_the_cost(Map<String, String> params) {
        String query = params.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
        JsonPath response = RestAssured.
                given().
                port(4567).
                when().
                // construct some proper url parameters
                        get("/prices?" + query).
                        then().
                        assertThat().
                        statusCode(200).
                        assertThat().
                        contentType("application/json").

                        extract().jsonPath();

        return response.getInt("cost");
    }
}

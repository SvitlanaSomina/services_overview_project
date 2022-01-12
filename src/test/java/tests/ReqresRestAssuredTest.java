package tests;

import model.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;

public class ReqresRestAssuredTest {
    private final static String URL = "https://reqres.in/";

    @Test
    public void checkAvatarAndId() {
        Specification.installSpecification(Specification.requestSpec(URL), Specification.responseSpecOK200());
        List<UserData> users = given()
                .when()
                .get("api/users?page=2")
                .then().log().all()
                .extract().body().jsonPath().getList("data", UserData.class);
        users.forEach(x -> Assert.assertTrue(x.getAvatar().contains(x.getId().toString())));
    }

    @Test
    public void checkEmailEnd() {
        String expectedEmailEnd = "reqres.in";
        Specification.installSpecification(Specification.requestSpec(URL), Specification.responseSpecOK200());
        List<UserData> users = given()
                .when()
                .get("api/users?page=2")
                .then().log().all()
                .extract().body().jsonPath().getList("data", UserData.class);
        Assert.assertTrue(users.stream().allMatch(x -> x.getEmail().endsWith(expectedEmailEnd)));
    }

    @Test
    public void checkSuccessRegistration() {
        Integer expectedId = 4;
        String expectedToken = "QpwL5tke4Pnpja7X4";
        String email = "eve.holt@reqres.in";
        String password = "pistol";
        Specification.installSpecification(Specification.requestSpec(URL), Specification.responseSpecOK200());
        Register user = new Register(email, password);
        SuccessRegister successRegister = given()
                .body(user)
                .when()
                .post("api/register")
                .then().log().all()
                .extract().as(SuccessRegister.class);
        Assert.assertEquals(expectedId, successRegister.getId());
        Assert.assertEquals(expectedToken, successRegister.getToken());
    }

    @Test
    public void checkUnSuccessRegistration() {
        String expectedError = "Missing password";
        String email = "sydney@fife";
        String password = "";
        Specification.installSpecification(Specification.requestSpec(URL), Specification.responseSpecError400());
        Register user = new Register(email, password);
        UnSuccessRegister unSuccessRegister = given()
                .body(user)
                .when()
                .post("api/register")
                .then().log().all()
                .extract().as(UnSuccessRegister.class);
        Assert.assertEquals(expectedError, unSuccessRegister.getError());
    }

    @Test
    public void checkSuccessLogin() {
        String expectedToken = "QpwL5tke4Pnpja7X4";
        String email = "eve.holt@reqres.in";
        String password = "cityslicka";
        Specification.installSpecification(Specification.requestSpec(URL), Specification.responseSpecOK200());
        Login user = new Login(email, password);
        SuccessLogin successLogin = given()
                .body(user)
                .when()
                .post("api/login")
                .then().log().all()
                .extract().as(SuccessLogin.class);
        Assert.assertEquals(expectedToken, successLogin.getToken());
    }

    @Test
    public void checkSortedYears() {
        Specification.installSpecification(Specification.requestSpec(URL), Specification.responseSpecOK200());
        List<ColorData> colors = given()
                .when()
                .get("api/unknown")
                .then().log().all()
                .extract().body().jsonPath().getList("data", ColorData.class);
        List<Integer> years = colors.stream().map(ColorData::getYear).collect(Collectors.toList());
        List<Integer> sortedYears = years.stream().sorted().collect(Collectors.toList());
        Assert.assertEquals(sortedYears, years);
    }

    @Test
    public void checkDeleteUser() {
        int statusCode = 204;
        Specification.installSpecification(Specification.requestSpec(URL), Specification.responseSpecUnique(statusCode));
        given()
                .when()
                .delete("api/users/2")
                .then().log().all();
    }
}

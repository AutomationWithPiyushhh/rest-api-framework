package anvnacare_apis;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/**
 * Lecture 04 Demo:
 * - Proper Query Parameters (.queryParam())
 * - Response Extraction (.extract().response() and JsonPath)
 * - Dynamic API Chaining (POST -> GET -> PUT -> DELETE -> GET 404 verification)
 */
public class UserApiChainingTest {

	private RequestSpecification reqSpec;
	private int userId;
	private String userEmail;
	private final String defaultPassword = "password@123";

	@BeforeClass
	public void setup() {
		reqSpec = new RequestSpecBuilder()
				.setBaseUri("https://anvnacare.alwaysdata.net")
				.setContentType(ContentType.JSON)
				.build();

		// Unique email for every test execution run
		userEmail = "qa_user_" + System.currentTimeMillis() + "@anvnacare.com";
	}

	// Step 1: Create User and extract dynamic ID
	@Test(priority = 1)
	public void testCreateUser() {
		Map<String, Object> payload = new HashMap<>();
		payload.put("name", "Piyush Automation");
		payload.put("email", userEmail);
		payload.put("phone", "9876543210");
		payload.put("password", defaultPassword);
		payload.put("role", "user");

		Response response = given()
			.spec(reqSpec)
			.body(payload)
		.when()
			.post("/api/users.php")
		.then()
			.statusCode(201)
			.body("success", equalTo(true))
			.body("message", containsString("User created successfully"))
			.body("user.email", equalTo(userEmail))
			.extract()
			.response();

		JsonPath json = response.jsonPath();
		userId = json.getInt("user.id");
		System.out.println("✅ Generated User ID: " + userId);
	}

	// Step 2: GET user by extracted ID
	@Test(priority = 2, dependsOnMethods = "testCreateUser")
	public void testGetUserById() {
		given()
			.spec(reqSpec)
			.queryParam("id", userId) // Clean query parameter handling
		.when()
			.get("/api/users.php")
		.then()
			.statusCode(200)
			.body("success", equalTo(true))
			.body("user.id", equalTo(userId))
			.body("user.email", equalTo(userEmail))
			.body("user.name", equalTo("Piyush Automation"));
	}

	// Step 3: PUT user to update details
	@Test(priority = 3, dependsOnMethods = "testGetUserById")
	public void testUpdateUser() {
		Map<String, Object> updatePayload = new HashMap<>();
		updatePayload.put("name", "Piyush Automation Updated");
		updatePayload.put("email", userEmail);
		updatePayload.put("phone", "9123456780");
		updatePayload.put("password", defaultPassword);
		updatePayload.put("role", "user");

		given()
			.spec(reqSpec)
			.queryParam("id", userId)
			.body(updatePayload)
		.when()
			.put("/api/users.php")
		.then()
			.statusCode(200)
			.body("success", equalTo(true))
			.body("message", containsString("User updated successfully"))
			.body("user.name", equalTo("Piyush Automation Updated"))
			.body("user.phone", equalTo("9123456780"));
	}

	// Step 4: DELETE user cleanly
	@Test(priority = 4, dependsOnMethods = "testUpdateUser")
	public void testDeleteUser() {
		Map<String, Object> deletePayload = new HashMap<>();
		deletePayload.put("id", userId);
		deletePayload.put("password", defaultPassword);

		given()
			.spec(reqSpec)
			.queryParam("id", userId)
			.body(deletePayload)
		.when()
			.delete("/api/users.php")
		.then()
			.statusCode(200)
			.body("success", equalTo(true))
			.body("message", containsString("User deleted successfully"));
	}

	// Step 5: Verify user is 404 Not Found after deletion
	@Test(priority = 5, dependsOnMethods = "testDeleteUser")
	public void testVerifyUserDeleted() {
		given()
			.spec(reqSpec)
			.queryParam("id", userId)
		.when()
			.get("/api/users.php")
		.then()
			.statusCode(404)
			.body("success", equalTo(false))
			.body("message", containsString("User not found"));
	}
}

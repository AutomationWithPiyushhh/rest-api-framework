package learning_deserialization;

import static io.restassured.RestAssured.given;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/**
 * Lecture 05 Demo:
 * - JSON Deserialization into Java Objects (POJOs) using Jackson Databind
 * - response.as(Class) for Full Response Deserialization
 * - response.jsonPath().getObject("path", Class) for Sub-object Deserialization
 * - response.jsonPath().getList("path", Class) for JSON Array to List<POJO>
 * - End-to-End Comparison: Request POJO vs Response POJO
 */
public class DeserializationTest {

	private RequestSpecification reqSpec;

	@BeforeClass
	public void setup() {
		reqSpec = new RequestSpecBuilder()
				.setBaseUri("https://anvnacare.alwaysdata.net")
				.setContentType(ContentType.JSON)
				.build();
	}

	// 1> Full Response Deserialization into Nested POJO (SingleUserResponse)
	@Test
	public void testDeserializeSingleUser() {
		SingleUserResponse responseObj = given()
				.spec(reqSpec)
				.queryParam("id", 1)
			.when()
				.get("/api/users.php")
			.then()
				.statusCode(200)
				.extract()
				.as(SingleUserResponse.class); // <-- Deserialization happens here!

		// Type-safe assertions via getters
		Assert.assertTrue(responseObj.isSuccess(), "Success flag should be true");
		Assert.assertNotNull(responseObj.getUser(), "User object should not be null");
		Assert.assertEquals(responseObj.getUser().getId(), 1);
		Assert.assertEquals(responseObj.getUser().getRole(), "admin");

		System.out.println("✅ Full Response Deserialized: " + responseObj.getUser().getName());
	}

	// 2> Partial Node Deserialization directly to User POJO using getObject()
	@Test
	public void testDeserializeSubObjectDirectly() {
		Response response = given()
				.spec(reqSpec)
				.queryParam("id", 1)
			.when()
				.get("/api/users.php")
			.then()
				.statusCode(200)
				.extract()
				.response();

		// Extract ONLY the "user" JSON block directly into our User POJO
		User user = response.jsonPath().getObject("user", User.class);

		Assert.assertNotNull(user);
		Assert.assertEquals(user.getId(), 1);
		System.out.println("✅ Partial Object Deserialized: " + user.getEmail());
	}

	// 3> Deserializing JSON Array into List of POJOs (AllUsersResponse)
	@Test
	public void testDeserializeAllUsers() {
		AllUsersResponse allUsers = given()
				.spec(reqSpec)
			.when()
				.get("/api/users.php")
			.then()
				.statusCode(200)
				.extract()
				.as(AllUsersResponse.class);

		Assert.assertTrue(allUsers.isSuccess());
		Assert.assertTrue(allUsers.getCount() > 0);
		Assert.assertFalse(allUsers.getUsers().isEmpty());

		System.out.println("✅ Total Users Deserialized: " + allUsers.getUsers().size());
		System.out.println("First User: " + allUsers.getUsers().get(0).getName());
	}

	// 4> Extracting Array directly to List<User> via jsonPath().getList()
	@Test
	public void testDeserializeListDirectly() {
		Response response = given()
				.spec(reqSpec)
			.when()
				.get("/api/users.php")
			.then()
				.statusCode(200)
				.extract()
				.response();

		List<User> userList = response.jsonPath().getList("users", User.class);

		Assert.assertNotNull(userList);
		Assert.assertTrue(userList.size() > 0);
		System.out.println("✅ Extracted List<User> directly, count = " + userList.size());
	}

	// 5> Complete Circle: Request POJO (Serialization) vs Response POJO (Deserialization)
	@Test
	public void testSerializationAndDeserializationCircle() {
		String dynamicEmail = "circle_test_" + System.currentTimeMillis() + "@anvnacare.com";

		// Step A: Serialization (Java POJO -> JSON Request)
		User requestUser = new User("Circle Test", dynamicEmail, "9876543210", "pass@123", "user");

		SingleUserResponse responseBody = given()
				.spec(reqSpec)
				.body(requestUser) // Jackson serializes Java object -> JSON
			.when()
				.post("/api/users.php")
			.then()
				.statusCode(201)
				.extract()
				.as(SingleUserResponse.class); // Jackson deserializes JSON -> Java object

		// Step B: Direct Object Assertions
		Assert.assertTrue(responseBody.isSuccess());
		Assert.assertEquals(responseBody.getMessage(), "User created successfully.");
		Assert.assertEquals(responseBody.getUser().getName(), requestUser.getName());
		Assert.assertEquals(responseBody.getUser().getEmail(), requestUser.getEmail());
		Assert.assertEquals(responseBody.getUser().getRole(), requestUser.getRole());

		System.out.println("✅ Round-trip Verified! Generated User ID: " + responseBody.getUser().getId());

		// Cleanup
		int createdId = responseBody.getUser().getId();
		given()
			.spec(reqSpec)
			.queryParam("id", createdId)
			.body("{\"id\":" + createdId + ",\"password\":\"pass@123\"}")
		.when()
			.delete("/api/users.php")
		.then()
			.statusCode(200);

		System.out.println("✅ Cleanup completed for User ID: " + createdId);
	}
}

package anvnacare_apis;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import generic_utility.JavaUtility;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;

public class CrudOpsTest {
	
	RequestSpecification reqSpec = new RequestSpecBuilder()
										.setBaseUri("https://anvnacare.alwaysdata.net/")
										.setContentType(ContentType.JSON)
										.build();
	
//	RestAssured.baseuri = "https://anvnacare.alwaysdata.net";
	private String name = "Ansh_" + JavaUtility.generateRanNum();
	private String password = "Password@21";
	private int userID ;

	@Test
	public void createUser() {
		HashMap<String, Object> data = new HashMap<>();
		data.put("name", name);
		data.put("email", name + "@anvnacare.com");
		data.put("phone", "0987654321");
		data.put("password", password);
		data.put("role", "user");
	
		JsonPath path = given() // arrange
			.spec(reqSpec)
			.body(data)
		.when() // act
			.post("/api/users.php")
		.then()
			.statusCode(201)
			.body("success", equalTo(true))
			.body("message", containsString("User created successfully"))
			.body("user.name", equalTo(name))
//			.log().all(); // assert
			.extract().jsonPath();
		
		userID = path.getInt("user.id");
	}
	
//	path and query	
	
	@Test(dependsOnMethods = "createUser")
	public void getUserByID() {

	    given() // Arrange
	    	.spec(reqSpec)
	        .queryParam("id", userID)

	    .when() // Act
	        .get("/api/users.php")

	    .then() // Assert
	        .log().all();
	}


	@Test
	public void updateUser() {		
		
		HashMap<String, Object> data = new HashMap<>();
		data.put("name", name);
		data.put("email", name + "@anvnacare.com");
		data.put("phone", "1234567890");
		data.put("password", password);
		data.put("role", "user");
		given() // arrange
    		.spec(reqSpec)
    		.body(data)
			.queryParam("id", userID)
		.when() // act
			.put("/api/users.php")
		.then()
			.log().all();// assert
	}

	@Test(dependsOnMethods = "getUserByID")
	public void deleteUser() {
		Map<String, Object> data = new HashMap<>();
		data.put("id", userID);
		data.put("password", password);		
/*Get a JsonPath view of the response body. 
 *This will let you use the JsonPath syntax to get values from the response.
*/		
		JsonPath path = given() // arrange
	    	.spec(reqSpec)
	    	.body(data)
			.queryParam("id", userID)
		.when() // act
			.delete("/api/users.php")
		.then()
			.extract().jsonPath();
		String msg = path.getString("message");
		boolean success = path.getBoolean("success");
		System.out.println(success + " " + msg);
	}

}

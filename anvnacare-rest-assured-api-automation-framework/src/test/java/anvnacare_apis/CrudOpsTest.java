package anvnacare_apis;

import org.testng.annotations.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class CrudOpsTest {
	
//	RestAssured.baseuri = "https://anvnacare.alwaysdata.net";

	@Test
	public void getUsers() {
		given() // arrange
			.baseUri("https://anvnacare.alwaysdata.net")
		.when() // act
			.get("/api/users.php")
		.then()
			.log().all(); // assert

	}

	@Test
	public void createUser() {
		given() // arrange
			.baseUri("https://anvnacare.alwaysdata.net")
			.body("{\"name\":\"Karan Johari\","
				    + "\"email\":\"karan.johari@avnacare.com\","
				    + "\"phone\":\"1234567890\","
				    + "\"password\":\"karan@1234\","
				    + "\"role\":\"user\"}")
		.when() // act
			.post("/api/users.php")
		.then()
			.statusCode(201)
			.body("success", equalTo(true))
			.body("message", containsString("User created successfully"))
			.body("user.name", equalTo("Karan Johari"))
			.log().all(); // assert
	}

	@Test
	public void updateUser() {
		given() // arrange
			.baseUri("https://anvnacare.alwaysdata.net")	
			.body("{\"name\":\"Karan arjun\","
				    + "\"email\":\"karan.arjun@avnacare.com\","
				    + "\"phone\":\"0987654321\","
				    + "\"password\":\"karan@1234\","
				    + "\"role\":\"user\"}")
		.when() // act
			.put("/api/users.php?id=222")
		.then()
			.log().all();// assert
	}

	@Test
	public void deleteUser() {
		given() // arrange
			.baseUri("https://anvnacare.alwaysdata.net")
			.body("{\r\n"
					+ "  \"id\": 222,\r\n"
					+ "  \"password\": \"karan@1234\"\r\n"
					+ "}")
			
		.when() // act
			.delete("/api/users.php?id=222")
		.then()
//			.body("success", equalTo(true))
			.log().all(); // assert
	}

}

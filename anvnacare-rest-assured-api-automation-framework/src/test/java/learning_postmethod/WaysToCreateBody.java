package learning_postmethod;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;

import org.json.JSONObject;
import org.testng.annotations.Test;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

public class WaysToCreateBody {
	
	RequestSpecification reqSpec = new RequestSpecBuilder()
										.setBaseUri("https://anvnacare.alwaysdata.net")
										.setContentType(ContentType.JSON)
										.build();
	
//	1> by hashmap
	@Test
	public void byHashMap() {
		String name = "Rajas mishra";
	
		HashMap<String, Object> data = new HashMap<>();
		data.put("name", name);
		data.put("email", "Rajasmishra@gmail.com");
		data.put("phone", "0987654321");
		data.put("password", "Rajasmishra@gmail.com");
		data.put("role", "user");
		
		given() // arrange
			.spec(reqSpec) // Add request data from a pre-defined specification.
			.body(data) // java to json via Jackson
		.when() // act
			.post("/api/users.php")
		.then()
			.statusCode(201)
			.body("success", equalTo(true))
			.body("message", containsString("User created successfully"))
			.body("user.name", equalTo(name))
			.log().all(); // assert
	}

//	2> by json object
	@Test
	public void byJsonObject() {
		String name = "Ojas mishra";

		JSONObject data = new JSONObject();

		data.put("name", name);
		data.put("email", "Ojasmishra@gmail.com");
		data.put("phone", "0987654321");
		data.put("password", "Ojasmishra@gmail.com");
		data.put("role", "user");
		
		given() // arrange
			.body(data.toString()) // we can't directly pass json object to body()
		.when() // act
			.post("/api/users.php")
		.then()
			.statusCode(201)
			.body("success", equalTo(true))
			.body("message", containsString("User created successfully"))
			.body("user.name", equalTo(name))
			.log().all(); // assert
	}
	
	
//	by POJO
	@Test
	public void byPOJO() {
		String name = "Tejas mishra";

		User data = new User(name, "tejasmishra@gmail.com", "0987654321", "tejasmishra@gmail.com", "user");
		
		given() // arrange
			.body(data)
		.when() // act
			.post("/api/users.php")
		.then()
			.statusCode(201)
			.body("success", equalTo(true))
			.body("message", containsString("User created successfully"))
			.body("user.name", equalTo(name))
			.log().all(); // assert
	}
	
//	4> by json file
	
	@Test
	public void byJsonFile() throws FileNotFoundException {
		
		File data = new File("./src/test/resources/user.json");
		
//		FileReader fr = new FileReader("./src/test/resources/user.json");
//		JSONTokener jt = new JSONTokener(fr);
//		JSONObject data = new JSONObject(jt);
		
		given() // arrange
			.body(data)
		.when() // act
			.post("/api/users.php")
		.then()
			.statusCode(201)
			.body("success", equalTo(true))
			.body("message", containsString("User created successfully"))
			.body("user.name", equalTo("Kushar Singh"))
			.log().all(); // assert
	}
}

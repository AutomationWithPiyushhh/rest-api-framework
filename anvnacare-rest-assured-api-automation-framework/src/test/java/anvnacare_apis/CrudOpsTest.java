package anvnacare_apis;

import org.testng.annotations.Test;

import static io.restassured.RestAssured.*;

public class CrudOpsTest {

	@Test
	public void getUsers() {
		given() // arrange
		.when() // act
			.get("https://anvnacare.alwaysdata.net/api/users.php")
		.then()
			.log().all(); // assert

	}

	@Test
	public void createUser() {
		given() // arrange
		.when() // act
		.then(); // assert
	}

	@Test
	public void updateUser() {
		given() // arrange
		.when() // act
		.then(); // assert
	}

	@Test
	public void deleteUser() {
		given() // arrange
		.when() // act
		.then(); // assert
	}

}

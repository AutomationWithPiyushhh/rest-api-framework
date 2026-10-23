package learning_response_validation;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;

import java.util.concurrent.TimeUnit;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class DummyJsonTest {
	@Test
	public void getProductById() {		
		given()
			.pathParam("resource", 1)
			.contentType(ContentType.JSON)
		.when()
			.get("https://dummyjson.com/products/{resource}")		
		.then()
			.statusCode(200)
//	        .header("Content-Type", containsString("application/json"))
// 			.contentType(ContentType.JSON)
	        .contentType("application/json")
	        .time(lessThan(2L), TimeUnit.SECONDS)
			.header("Cache-Control", notNullValue())
			.body("id", equalTo(1))
			.body("title", equalTo("Essence Mascara Lash Princess"))
			.body("description", containsString("cruelty-free formula"))
			.body("reviews[0].rating", greaterThanOrEqualTo(3))
			.body("reviews.rating", everyItem(greaterThanOrEqualTo(3)))
			.log().all();
	}
	
	@Test
	public void getProducts() {		
		Response res = given()
			.pathParam("resource", 1)
			.contentType(ContentType.JSON)
		.when()
			.get("https://dummyjson.com/products/{resource}");
		
		String title = res.jsonPath().getString("title");
		int id = res.jsonPath().getInt("id");
		float rating = res.jsonPath().getFloat("rating");
	
		Assert.assertEquals(id, 1);
		Assert.assertTrue(rating > 0);
		Assert.assertNotNull(title);
	    Assert.assertFalse(title.isEmpty());
	}
}

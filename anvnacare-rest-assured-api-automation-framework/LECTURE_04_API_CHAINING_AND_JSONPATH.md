# 📚 Lecture 04: Query Parameters, Response Extraction (`JsonPath`), and Dynamic API Chaining

**Course:** REST Assured API Automation Framework  
**Target API:** AnvnaCare User Management API (`https://anvnacare.alwaysdata.net/api/users.php`)  
**Prerequisites Covered:**
- ✅ Lecture 01: REST Assured Setup, BDD Syntax (`given()`, `when()`, `then()`), Hamcrest Matchers
- ✅ Lecture 02: Basic CRUD Operations with raw JSON strings
- ✅ Lecture 03: 4 Ways to Create POST Request Body (HashMap, org.json, POJO, JSON file) + `RequestSpecification`

---

## 🎯 Learning Objectives for this Lecture
By the end of this lecture, students will be able to:
1. Understand the difference between **Path Parameters** and **Query Parameters** in REST Assured.
2. Use `.queryParam()` and `.pathParam()` instead of manual URL string concatenation (`?id=222`).
3. Extract response values dynamically using `.extract().response()` and `JsonPath`.
4. Solve the **"Hardcoded ID Flakiness"** problem present in previous tests.
5. Build an **End-to-End (E2E) API Chaining Test** (POST ➡️ GET ➡️ PUT ➡️ DELETE ➡️ GET [Verify 404]).
6. Manage execution sequence and state sharing using TestNG's `dependsOnMethods` and class-level variables.

---

## 🚨 The Problem in Our Current Codebase (Why this Lecture is Critical)

Look at what we wrote in [`anvnacare_apis/CrudOpsTest.java`](file:///c:/Users/User/git/rest-m10/anvnacare-rest-assured-api-automation-framework/src/test/java/anvnacare_apis/CrudOpsTest.java):

```java
// From previous CrudOpsTest.java
@Test
public void updateUser() {
    given()
        .baseUri("https://anvnacare.alwaysdata.net")    
        .body(...)
    .when()
        .put("/api/users.php?id=222") // ⚠️ Issue 1: Hardcoded URL query string
    .then()
        .log().all();
}

@Test
public void deleteUser() {
    given()
        .baseUri("https://anvnacare.alwaysdata.net")
        .body("{\"id\": 222, \"password\": \"karan@1234\"}") // ⚠️ Issue 2: Hardcoded ID 222
    .when()
        .delete("/api/users.php?id=222")
    .then()
        // .body("success", equalTo(true)) // ⚠️ Issue 3: Commented out because ID 222 was already deleted!
        .log().all();
}
```

### ❌ What happens when students run this multiple times?
1. **Flaky Tests:** If user `222` is deleted, `updateUser()` and `deleteUser()` fail on the second run with `404 Not Found` or `User not found`.
2. **Brittle String URLs:** String concatenating `?id=222` bypasses REST Assured's built-in parameter sanitization and URL encoding.
3. **No Dynamic Flow:** In production API testing, test data must be generated dynamically, validated, updated, and cleaned up cleanly.

---

## 💡 Concept 1: Query Parameters vs. Path Parameters

### What is the difference?
| Feature | Query Parameter (`.queryParam()`) | Path Parameter (`.pathParam()`) |
| :--- | :--- | :--- |
| **URL Pattern** | Appended after `?` (e.g., `/api/users.php?id=294&role=user`) | Embedded in the route (e.g., `/api/users/{id}`) |
| **Purpose** | Filtering, sorting, searching, pagination | Identifying a specific resource path |
| **REST Assured Method** | `.queryParam("key", value)` or `.queryParams(map)` | `.pathParam("key", value)` with `{key}` in path |
| **AnvnaCare API Usage** | `GET /api/users.php?id=1` uses **Query Parameters** | Not used in PHP script-based routing |

### Bad vs Good Practice in REST Assured:
```java
// ❌ BAD PRACTICE (Manual string concatenation)
.when().get("/api/users.php?id=" + userId)

// ✅ BEST PRACTICE (Using REST Assured queryParam)
given()
    .queryParam("id", userId)
.when()
    .get("/api/users.php")
```

---

## 💡 Concept 2: Response Extraction & JsonPath

To pass data from one API to another, we must **extract** values from the response payload.

### Method 1: Direct path extraction
```java
int userId = given()
    .spec(reqSpec)
    .body(payload)
.when()
    .post("/api/users.php")
.then()
    .statusCode(201)
    .extract()
    .path("user.id"); // Extracts directly into int
```

### Method 2: Extracting entire Response and using `JsonPath`
```java
Response response = given()
    .spec(reqSpec)
    .body(payload)
.when()
    .post("/api/users.php")
.then()
    .statusCode(201)
    .extract()
    .response();

// Extract via JsonPath instance:
JsonPath jsonPath = response.jsonPath();
int userId = jsonPath.getInt("user.id");
String userName = jsonPath.getString("user.name");
boolean isSuccess = jsonPath.getBoolean("success");
```

---

## 💡 Concept 3: Dynamic Data Generation (Avoiding Duplicate Email Errors)

The AnvnaCare database enforces unique emails for users. If students run `createUser` with the same static email twice, it throws a database error.

**Teach students this quick trick:**
```java
String uniqueEmail = "student_" + System.currentTimeMillis() + "@anvnacare.com";
```
*(In upcoming lectures, we will replace this with JavaFaker / Bogus data generators).*

---

## 🔄 Concept 4: The 5-Step End-to-End API Chaining Workflow

```
+-------------------------------------------------------------+
| Step 1: POST /api/users.php                                 |
| -> Create new user with dynamic email                       |
| -> Extract created `userId`                                 |
+------------------------------+------------------------------+
                               |
                               v
+-------------------------------------------------------------+
| Step 2: GET /api/users.php?id={userId}                      |
| -> Validate that user details match created data            |
+------------------------------+------------------------------+
                               |
                               v
+-------------------------------------------------------------+
| Step 3: PUT /api/users.php?id={userId}                      |
| -> Update user phone and name                               |
| -> Validate response confirms update                        |
+------------------------------+------------------------------+
                               |
                               v
+-------------------------------------------------------------+
| Step 4: DELETE /api/users.php?id={userId}                   |
| -> Send deletion payload with password & id                 |
| -> Validate success status 200                              |
+------------------------------+------------------------------+
                               |
                               v
+-------------------------------------------------------------+
| Step 5: GET /api/users.php?id={userId} (Negative Check)     |
| -> Verify status 404 Not Found & "User not found."          |
+-------------------------------------------------------------+
```

---

## 💻 Complete Class Implementation for Lecture Demo

**File location to create in project:** `src/test/java/anvnacare_apis/UserApiChainingTest.java`

```java
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
            .queryParam("id", userId) // Clean query parameter
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
```

---

## 📝 Classroom Live Exercise (For Students)

Give your students 15 minutes to code this:
> **Challenge:**
> 1. In `testCreateUser()`, extract and print the response time using `response.getTime()`.
> 2. Add an assertion that the response time is less than 3000 ms (`time(lessThan(3000L))`).
> 3. Refactor `testCreateUser()` to use the `User` POJO we created in Lecture 3 instead of `HashMap`.

---

## 🎤 Top 5 Interview Questions from this Lecture

1. **How do you extract values from a response body in REST Assured?**
   - *Answer:* Use `.extract().path("field.nestedField")` for a single field or `.extract().response().jsonPath()` to query multiple fields using Groovy GPath expressions.
2. **What is API Chaining and why is it important in automation?**
   - *Answer:* API Chaining is passing dynamic output from one request (e.g., token, userId, orderId) into the input of subsequent requests. It models real user journeys and ensures tests are independent of static mock data.
3. **How do you pass Query Parameters and Path Parameters in REST Assured?**
   - *Answer:* Query params use `.queryParam("key", "val")`, Path params use `.pathParam("key", "val")` matched against `{key}` in the URI template.
4. **How do you guarantee test execution order in TestNG for chained tests?**
   - *Answer:* Using `priority` and `dependsOnMethods`. If the prerequisite test fails (e.g., `createUser`), dependent tests (`updateUser`, `deleteUser`) are automatically marked as `SKIPPED` rather than failing misleadingly.
5. **How can you share data across tests if they are in different classes?**
   - *Answer:* Using TestNG's `ITestContext` (e.g., `context.setAttribute("userId", id)` and `context.getAttribute("userId")`) or static test context singletons/ThreadLocal variables in a multi-threaded framework.

---

## 🗺️ What's Next in the Curriculum?

| Lecture | Topic | Description |
| :--- | :--- | :--- |
| **Lecture 05** | **POJO Deserialization & Response POJOs** | Mapping JSON response directly back to Java objects (`response.as(UserResponse.class)`), using `@JsonIgnoreProperties`. |
| **Lecture 06** | **JSON Schema Validation** | Validating API response structure against schema files using `matchesJsonSchemaInClasspath`. |
| **Lecture 07** | **Data-Driven Testing with Excel** | Connecting our `ANVNA_Care_User_Module_Test_Cases.xlsx` with Apache POI and `@DataProvider`. |
| **Lecture 08** | **Full Framework Architecture** | BaseTest, Routes constants, Faker data generator, ExtentReports / Allure listener, and CI/CD ready `testng.xml`. |

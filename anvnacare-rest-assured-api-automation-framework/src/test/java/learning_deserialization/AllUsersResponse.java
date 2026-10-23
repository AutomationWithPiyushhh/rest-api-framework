package learning_deserialization;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AllUsersResponse {

	private boolean success;
	private int count;
	private List<User> users;

	public AllUsersResponse() {
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	@Override
	public String toString() {
		return "AllUsersResponse [success=" + success + ", count=" + count + ", usersCount=" + (users != null ? users.size() : 0) + "]";
	}
}

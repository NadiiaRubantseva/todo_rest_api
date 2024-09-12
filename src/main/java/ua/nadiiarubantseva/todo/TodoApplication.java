package ua.nadiiarubantseva.todo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.TimeZone;

@SpringBootApplication
public class TodoApplication {

	private static final String UTC_TIMEZONE = "UTC";

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone(UTC_TIMEZONE));
		SpringApplication.run(TodoApplication.class, args);
	}
}

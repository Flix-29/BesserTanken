package de.flix29;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import lombok.Getter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@Theme(value = "BesserTanken", variant = Lumo.DARK)
public class BesserTanken implements AppShellConfigurator {

	@Getter
    private static Environment env;
	@Getter
	private static final Map<String, String> secrets = new HashMap<>();

	public static void main(String[] args) {
		env = SpringApplication.run(BesserTanken.class, args).getEnvironment();

		secrets.put("mapKey", System.getenv().getOrDefault("MAP_KEY", getEnv().getProperty("map.apikey")));
		secrets.put("directionsKey", System.getenv().getOrDefault("DIRECTIONS_KEY", getEnv().getProperty("directions.apikey")));
		secrets.put("bessertankenKey", System.getenv().getOrDefault("API_KEY", getEnv().getProperty("bessertanken.apikey")));
	}

}

package de.flix29;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import lombok.Getter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

@SpringBootApplication
@Theme(value = "BesserTanken", variant = Lumo.DARK)
public class BesserTanken implements AppShellConfigurator {

	@Getter
    private static Environment env;

	public static void main(String[] args) {
		env = SpringApplication.run(BesserTanken.class, args).getEnvironment();
	}

}

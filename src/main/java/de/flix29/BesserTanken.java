package de.flix29;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Theme(value = "BesserTanken", variant = Lumo.DARK)
public class BesserTanken implements AppShellConfigurator {

	public static void main(String[] args) {
		SpringApplication.run(BesserTanken.class, args);
	}

}

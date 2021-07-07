package app.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

import app.core.filters.LoginFilter;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
public class Application {

	public static void main(String[] args) {

		try {
			SpringApplication.run(Application.class, args);
			System.out.println(
					"\n// ================================= Program Is Running ================================ //\n");

		} catch (Exception e) {
			System.err.println("System is going down due to the following error:\n" + e);
		}

	}

	@Bean
	public FilterRegistrationBean<LoginFilter> filterRegistration() {
		// create a registration bean
		FilterRegistrationBean<LoginFilter> filterRegistrationBean = new FilterRegistrationBean<LoginFilter>();
		// create the login filter
		LoginFilter loginFilter = new LoginFilter();
		// do the registration
		filterRegistrationBean.setFilter(loginFilter);
		// set the URL pattern for the filter
		filterRegistrationBean.addUrlPatterns("/api/*");
		return filterRegistrationBean;
	}

}

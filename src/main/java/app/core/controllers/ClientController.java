package app.core.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public abstract class ClientController {

	// fields
	@Autowired
	protected ApplicationContext ctx;

}

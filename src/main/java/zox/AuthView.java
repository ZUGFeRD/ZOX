package zox;

import io.dropwizard.views.View;

public class AuthView extends View {
	protected String key;

	public AuthView(String key) {
		super("authView.mustache");
		this.key=key;
	}
	
	public String getKey() {
		return key;
	}

}

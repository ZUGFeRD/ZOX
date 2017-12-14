package zox;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

public class CorridorsConfiguration extends Configuration {
    @NotEmpty
    private String template;

    @NotEmpty
    private String defaultName = "Stranger";
    
    @NotEmpty
    private String username = "";
    @NotEmpty
    private String domain = "";
    @NotEmpty
    private String password = "";

    @NotEmpty
    private String bank_rdhfile = "";
    @NotEmpty
    private String bank_rdhpassphrase = "";
    @NotEmpty
    private String bank_account = "";
    @NotEmpty
    private String bank_code = "";
    @NotEmpty
    private String bank_user = "";
    @NotEmpty
    private String bank_url = "";

    
    @Valid
    @NotNull
    private DataSourceFactory database = new DataSourceFactory();

    @JsonProperty("database")
    public void setDataSourceFactory(DataSourceFactory factory) {
        this.database = factory;
    }

    @JsonProperty("database")
    public DataSourceFactory getDataSourceFactory() {
        return database;
    }
    
    @JsonProperty
    public String getTemplate() {
        return template;
    }

    @JsonProperty
    public void setTemplate(String template) {
        this.template = template;
    }

    @JsonProperty
    public String getDefaultName() {
        return defaultName;
    }

    @JsonProperty
    public String getUsername() {
        return username;
    }

    @JsonProperty
    public String getDomain() {
        return domain;
    }
    
    public String getPassword() {
        return password;
    }

    @JsonProperty
    public void setDefaultName(String name) {
        this.defaultName = name;
    }

	public String getBank_rdhfile() {
		return bank_rdhfile;
	}

	public void setBank_rdhfile(String bank_rdhfile) {
		this.bank_rdhfile = bank_rdhfile;
	}

	public String getBank_rdhpassphrase() {
		return bank_rdhpassphrase;
	}

	public void setBank_rdhpassphrase(String bank_rdhpassphrase) {
		this.bank_rdhpassphrase = bank_rdhpassphrase;
	}

	public String getBank_account() {
		return bank_account;
	}

	public void setBank_account(String bank_account) {
		this.bank_account = bank_account;
	}

	public String getBank_code() {
		return bank_code;
	}

	public void setBank_code(String bank_code) {
		this.bank_code = bank_code;
	}

	public String getBank_user() {
		return bank_user;
	}

	public void setBank_user(String bank_user) {
		this.bank_user = bank_user;
	}

	public String getBank_url() {
		return bank_url;
	}

	public void setBank_url(String bank_url) {
		this.bank_url = bank_url;
	}
}
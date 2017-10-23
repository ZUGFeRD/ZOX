package zox;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "people")
public class Person {
    @Id @GeneratedValue
    private int ID; 
    
    @Column
    private String name;

    @Column
    private String email;

    @Column
    private DateTime birthday;

    @JsonProperty
    public String getName() {
        return name;
    }

    @JsonProperty
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty
    public String getEmail() {
        return email;
    }

    @JsonProperty
    public void setEmail(String email) {
        this.email = email;
    }

    @JsonProperty
    public DateTime getBirthday() {
        return birthday;
    }

    @JsonProperty
    public void setBirthday(DateTime birthday) {
        this.birthday = birthday;
    }
}

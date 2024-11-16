package TheNaeunEconomy.user.config.jwt;

import lombok.Getter;

@Getter
public class Token {
    private String value;

    public Token(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

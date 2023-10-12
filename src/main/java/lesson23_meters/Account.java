package lesson23_meters;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

@AllArgsConstructor
@Data
public class Account implements Serializable {
    private String login;
    private String pass;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(login, account.login) && Objects.equals(pass, account.pass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login, pass);
    }

}

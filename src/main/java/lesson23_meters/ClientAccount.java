package lesson23_meters;

import java.util.ArrayList;
import java.util.List;

public class ClientAccount extends Account{
    private String name;
    private List<Contract> contracts = new ArrayList<>();

    public ClientAccount(String login, String pass, String name) {
        super(login, pass);
        this.name = name;
    }
    public ClientAccount(String login, String pass) {
        super(login, pass);
    }

    public List<Contract> getContracts() {
        return contracts;
    }

    public String getName() {
        return name;
    }

}

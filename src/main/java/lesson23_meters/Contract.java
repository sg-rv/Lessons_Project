package lesson23_meters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Contract implements Serializable {
    private String contract_num;
    private List<Meter> meters = new ArrayList<>();

    public Contract(String contract_num) {
        this.contract_num = contract_num;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contract contract = (Contract) o;
        return Objects.equals(contract_num, contract.contract_num);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contract_num, meters);
    }

    public String getContract_num() {
        return contract_num;
    }

    public List<Meter> getMeters() {
        return meters;
    }

    @Override
    public String toString() {
        return "Contract{" +
                "contract_num='" + contract_num + '\'' +
                '}';
    }
}

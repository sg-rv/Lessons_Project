package lesson23_meters;

import lombok.Data;

import java.io.Serializable;
import java.util.Objects;
@Data
public class Meter implements Serializable {
    private int last_month = 0;
    private int this_month = 0;
    private int total_days = 0;
    private int total_nights = 0;
    private int meter_num;
    private String month_hour;

    public Meter(int meter_num, String month_hour) {
        this.meter_num = meter_num;
        this.month_hour = month_hour;
    }

    public int calculateByMonths() {
        int result = this_month - last_month;
        last_month = this_month;
        return result;
    }
    public int calculateByHours(int dayTariff, int nightTariff) {
        return total_days*dayTariff + total_nights*nightTariff;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Meter meter = (Meter) o;
        return meter_num == meter.meter_num && month_hour.equals(meter.getMonth_hour());
    }

    @Override
    public int hashCode() {
        return Objects.hash(meter_num);
    }

    @Override
    public String toString() {
        return "Meter{" +
                "meter_num=" + meter_num +
                '}';
    }


}

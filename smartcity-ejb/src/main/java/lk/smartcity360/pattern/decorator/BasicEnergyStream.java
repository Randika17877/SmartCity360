package lk.smartcity360.pattern.decorator;

/** Concrete component: passes the payload through untouched. */
public class BasicEnergyStream implements DataStream {

    @Override
    public String process(String data) {
        return data;
    }
}

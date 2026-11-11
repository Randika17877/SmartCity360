package lk.smartcity360.pattern.decorator;

/** Component interface: anything that turns a text payload into another text payload. */
public interface DataStream {

    String process(String data);
}

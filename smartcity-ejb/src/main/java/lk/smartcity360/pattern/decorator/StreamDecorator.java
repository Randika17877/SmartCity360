package lk.smartcity360.pattern.decorator;

import java.util.Objects;

/** Base decorator: holds the wrapped stream and lets subclasses add behaviour around it. */
public abstract class StreamDecorator implements DataStream {

    protected final DataStream wrapped;

    protected StreamDecorator(DataStream wrapped) {
        this.wrapped = Objects.requireNonNull(wrapped, "wrapped stream");
    }
}

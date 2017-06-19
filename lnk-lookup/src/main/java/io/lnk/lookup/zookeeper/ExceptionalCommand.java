package io.lnk.lookup.zookeeper;

public interface ExceptionalCommand<E extends Exception> {

    void execute() throws E;
}

package io.lnk.lookup.consul;

public interface CommandListener {

    void notifyCommand(URL refUrl, String commandString);

}

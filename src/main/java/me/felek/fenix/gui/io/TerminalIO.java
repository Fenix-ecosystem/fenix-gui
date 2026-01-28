package me.felek.fenix.gui.io;

import me.felek.fenix.api.FenixIO;
import me.felek.fenix.VM.Terminal;

public class TerminalIO implements FenixIO {

    private final Terminal terminal;

    public TerminalIO(Terminal terminal) {
        this.terminal = terminal;
    }

    @Override
    public void writeChar(char c) {
        try {
            terminal.write(c);
        } catch (Exception e) {
        }
    }

    @Override
    public void writeString(String s) {
        for (char c : s.toCharArray()) {
            try {
                terminal.write(c);
            } catch (Exception e) {
            }
        }
    }

    @Override
    public int readKey() {
        return 0;
    }
}

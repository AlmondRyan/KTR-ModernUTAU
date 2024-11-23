package org.rakyuustudio.pianoroll.command;

public interface Command {
    void execute();
    void undo();
} 
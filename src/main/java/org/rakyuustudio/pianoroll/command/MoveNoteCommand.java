package org.rakyuustudio.pianoroll.command;

import org.rakyuustudio.pianoroll.Note;

public class MoveNoteCommand implements Command {
    private final Note note;
    private final int oldPitch;
    private final int oldStartTime;
    private final int newPitch;
    private final int newStartTime;
    
    public MoveNoteCommand(Note note, int newPitch, int newStartTime) {
        this.note = note;
        this.oldPitch = note.pitch;
        this.oldStartTime = note.startTime;
        this.newPitch = newPitch;
        this.newStartTime = newStartTime;
    }
    
    @Override
    public void execute() {
        note.pitch = newPitch;
        note.startTime = newStartTime;
    }
    
    @Override
    public void undo() {
        note.pitch = oldPitch;
        note.startTime = oldStartTime;
    }
} 
package Assembler;

import Emulator.Instruction;

import static Assembler.Parser.NoOperand;

public class ProtoInst {
    public static final ProtoInst ERR = new ProtoInst();
    public static final ProtoInst Comment = new ProtoInst();

    static {
        Comment.includeInProgram = false;
    }

    public Instruction inst;

    public boolean isRename;
    boolean includeInProgram = true;
    public String[] renamings;

    public OperandInfo a = NoOperand,b = NoOperand;
    String outputName = null;
}

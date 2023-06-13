package Assembler;

import Emulator.Instruction;

import static Assembler.Parser.NoOperand;

public class ProtoInst {
    public static final ProtoInst ERR = new ProtoInst();
    public static final ProtoInst Comment = new ProtoInst();

    static {
        Comment.type=ProtoInstructionType.Comment;
    }

    public Instruction inst;

    public ProtoInstructionType type = ProtoInstructionType.Normal;
    public String[] renamings;

    public OperandInfo a = NoOperand,b = NoOperand;
    String outputName, labelName;
}

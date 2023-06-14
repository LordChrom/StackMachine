package Emulator;

public class Instruction{
    public static final Instruction NOP = new Instruction(0,false,0,false,(a,b,p)->0,false);

    public String label = "unknown";

    public int a,b;
    public boolean delA,delB, output;
    public Operation op;
    public String opcode;

    public Instruction(){}

    public Instruction(int a, boolean delA, int b, boolean delB, Operation op, boolean output) {
        this.a = a;
        this.b = b;
        this.delA = delA;
        this.delB = delB;
        this.output = output;
        this.op = op;
    }

}

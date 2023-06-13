package Emulator;

import java.util.ArrayList;

public class Processor {

    public byte PC = 0;
    public boolean running, postIncrement;
    byte readA,readB;
    ArrayList<Datum> stack = new ArrayList<>();
    public byte[] RAM = new byte[256];
    public Instruction[] ROM = new Instruction[256];
    public StringBuilder outputLog;

    public void exec(Instruction inst){
        accessStack(inst.a, inst.delA, inst.b, inst.delB);
        byte result = (byte) inst.op.exec(readA,readB,this);
        if(inst.output) stPush(result,inst);
    }

    public void exec(Instruction[] program, boolean goSlowly){exec(program, goSlowly, new byte[0]);}
    public void exec(Instruction[] program, boolean goSlowly, byte[] ramPreload){
        outputLog = new StringBuilder();

        ROM = program;
        stack.clear();
        for (int i = 0; i < RAM.length; i++)
            RAM[i] = i<ramPreload.length? ramPreload[i] : 0;

        running = true;
        while(running) {
            String stateInfo = this+"\n\n";
            System.out.println(stateInfo);
            outputLog.append(stateInfo);

            postIncrement = true;
            exec(ROM[0xFF & PC]);

            if(postIncrement)
                PC++;

            if(goSlowly)
                try {
                    Thread.sleep(50L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
        }
    }

    private void accessStack(int aAddr, boolean aRemove, int bAddr, boolean bRemove) {
        readA = stGet(aAddr);
        readB = stGet(bAddr);
        if(aRemove&&bRemove) {
            if(aAddr == bAddr)
                System.out.println("simultaneous double removal is UB");
            else
                stDel(Math.max(aAddr, bAddr));
            stDel(Math.min(aAddr, bAddr));
        } else if (aRemove)
            stDel(aAddr);
        else if (bRemove)
            stDel(bAddr);
    }
    private byte stGet(int i)    { return i<stack.size() ? stack.get(i).data : 0; }
    private void stPush(byte val, Instruction source){ stack.add(0,new Datum(val,source)); }
    private void stDel(int i)    { if(i<stack.size())  stack.remove(i); }


    public String toString(){
        return "PC: \t"+ (0xFF&PC) +"\nInst:\t" +ROM[0xFF&PC].opName+"\n=============================\n\n"+showStack();
    }
    public String showStack(){
        StringBuilder b = new StringBuilder();
        for (Datum d:stack)
            b.append(d).append("\r\n");
        if(b.length()>1)
            b.delete(b.length()-2,b.length()-1);
        return b.toString();
    }


}

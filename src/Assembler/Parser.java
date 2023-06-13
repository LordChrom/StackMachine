package Assembler;

import Emulator.Datum;
import Emulator.Instruction;
import Emulator.Operation;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;

import static Assembler.ProtoInst.*;

public class Parser {
    record OpCodeInfo(Operation op,int operands,boolean outputs){}

    //placeholders
    public static final OperandInfo NoOperand = new OperandInfo(true,false,null,0);
    public static final Operation ImmOperation = (a,b,p)->0;
    public static final Operation RenamingOp = (a, b, p)->0;


    private static final HashMap<String, OpCodeInfo> opcodes = new HashMap<>();
    static{
        opcodes.put("add",new OpCodeInfo((a,b,p)->a+b,2,true));
        opcodes.put("sbb",new OpCodeInfo((a,b,p)->a-b,2,true));
        opcodes.put("sba",new OpCodeInfo((a,b,p)->b-a,2,true));
        opcodes.put("eat",new OpCodeInfo((a,b,p)->0  ,1,false));
        opcodes.put("mov",new OpCodeInfo((a,b,p)->a  ,1,true));

        opcodes.put("nop",new OpCodeInfo((a,b,p)->0  ,0,false));
        opcodes.put("imm",new OpCodeInfo(ImmOperation,0,true));

        opcodes.put("halt",new OpCodeInfo((a,b,p)->{
            p.running=false;
            return 0;
        },0,false));

        opcodes.put("gpc",new OpCodeInfo((a,b,p)->p.PC,0,true));
        opcodes.put("jal",new OpCodeInfo((a,b,p)->{
            byte tmp = p.PC;
            p.postIncrement=false;
            p.PC=(byte)a;
            return tmp;
        },1,true));
        opcodes.put("jmp",new OpCodeInfo((a,b,p)->{
            p.postIncrement=false;
            p.PC=(byte)a;
            return 0;
        },1,false));
        opcodes.put("jz",new OpCodeInfo((a,b,p)->{
            if(b==0) {
                p.postIncrement = false;
                p.PC = (byte) a;
            }
            return 0;
        },2,false));
        opcodes.put("jnz",new OpCodeInfo((a,b,p)->{
            if(b!=0) {
                p.postIncrement = false;
                p.PC = (byte) a;
            }
            return 0;
        },2,false));


        opcodes.put("ld",new OpCodeInfo((a,b,p)->p.RAM[0xFF&a],1,true));
        opcodes.put("st",new OpCodeInfo((a,b,p)->{
            p.RAM[0xFF&a]=(byte)b;
            return 0;
        },2,false));

        opcodes.put("renamestack",new OpCodeInfo(RenamingOp,0,false));

//        opcodes.put("",new OpCodeInfo((a,b,p)->,2,true));

    }

    public static Instruction[] parseFile(String path) {
        File file = new File(path);

        ArrayList<ProtoInst> prog = new ArrayList<>();
        try {
            for (String line : Files.readAllLines(file.toPath())) {
                ProtoInst p = parseLine(line);
                if(p!=Comment)
                    prog.add(p);
            }
        }catch (Exception e){
            e.printStackTrace();
            return new Instruction[0];
        }

        HashMap<String,Integer> nameLocations = new HashMap<>();
        for (int index = 0; index < prog.size(); index++) {
            ProtoInst p = prog.get(index);

            if(p.isRename) {
                for (int i = 0; i < p.renamings.length; i++) {
                    String str = p.renamings[i];
                    if(str!=null) nameLocations.put(str,i);
                }
            }else{
                Instruction i = p.inst;
                if(!p.a.numeric){
                    p.a.numeric=true;
                    if(nameLocations.containsKey(p.a.name))
                        i.a=nameLocations.get(p.a.name);
                    else
                        System.out.println("Invalid name: " + p.a.name);
                }
                if(!p.b.numeric){
                    p.b.numeric=true;
                    if(nameLocations.containsKey(p.b.name))
                        i.b=nameLocations.get(p.b.name);
                    else
                        System.out.println("Invalid name: " + p.b.name);
                }



                //these are the bookends that mark where removals happen and how that affects other data
                //hold threshold < decrease threshold
                //hold threshold = index of lower deletion if addition

                int decreaseThreshold = Integer.MAX_VALUE; //all above this decrease position
                int holdThreshold = Integer.MAX_VALUE;     //all below this increase position

                //start by assuming an item will be added to the stack
                switch ((i.delA ? 1 : 0) + (i.delB ? 1 : 0)) { //number of deletions
                    case 2 -> {
                        holdThreshold = Math.min(i.a, i.b);
                        decreaseThreshold = Math.max(i.a, i.b);
                        if (!i.output)
                            System.out.println("Problematic code line, double consumes without producing");
                    }
                    case 1 -> holdThreshold = i.delA ? i.a : i.b;
                }

                if (!i.output) {
                    decreaseThreshold = holdThreshold;
                    holdThreshold = -1;
                }

                for (String name : nameLocations.keySet().toArray(new String[0])) {
                    int pos = nameLocations.get(name);
                    if (pos < holdThreshold)
                        nameLocations.put(name, pos + 1);
                    else if (pos == holdThreshold || pos == decreaseThreshold)
                        nameLocations.remove(name);
                    else if (pos > decreaseThreshold)
                        nameLocations.put(name, pos - 1);
                }
                //add name for this inst
                if(p.outputName!=null)
                    nameLocations.put(p.outputName,0);
            }
        }


        prog.removeIf(p -> p.isRename);
        Instruction[] r = new Instruction[prog.size()];
        for (int i = 0; i < prog.size(); i++)
            r[i]=prog.get(i).inst;
        return r;
    }

    public static ProtoInst parseLine(String line){
        line = line.split("#")[0].strip();
        if(line.length()==0) return Comment;


        ProtoInst r = new ProtoInst();


        if(line.contains("=")){
            String[] segments = line.split("=");
            if(segments.length!=2) return ERR;
            r.outputName = segments[0].strip();
            line = segments[1].strip();
        }

        String[] segments = line.split("\\s+");
        OpCodeInfo info;
        if(segments.length==0)
            return Comment;

        info = opcodes.get(segments[0].toLowerCase());
        if(info == null)
            System.out.println("unknown opcode: \""+segments[0]+"\"");
        switch (info.operands){
            case 2:
                r.b=parseOperand(segments[2]);
            case 1:
                r.a=parseOperand(segments[1]);
            default:
            case 0:
        }

        if(info.op== RenamingOp){
            r.inst=Instruction.NOP;
            r.isRename = true;
            r.renamings = new String[segments.length-1];
            for (int i = 1; i < segments.length; i++) {
                String src = segments[i];
                r.renamings[i-1]=src.equals("_") ? null : src;
            }
            return r;
        }

        Instruction i = new Instruction();

        i.a=r.a.val;
        i.b=r.b.val;
        i.delA = r.a.remove;
        i.delB = r.b.remove;
        i.output = info.outputs;
        i.op = info.op;

        if(i.op==ImmOperation){
            final int imm = Integer.parseInt(segments[1]);
            i.op = (a,b,p)-> imm;
        }

        r.inst=i;

        i.opName = "["+segments[0]+"]";
        i.label = (r.outputName==null)? i.opName :r.outputName;
//        Datum.debugLabels.put(i,label);
        return r;
    }

    private static OperandInfo parseOperand(String operand){
        boolean remove = operand.charAt(0)=='-';
        if(remove) operand = operand.substring(1);

        boolean numeric = true;
        for (int i=0; i<operand.length() & numeric; i++) {
            char c = operand.charAt(i);
            numeric = ((c>='0')&&(c<='9'));
        }

        int val = numeric? Integer.parseUnsignedInt(operand) : Integer.MIN_VALUE;

        return new OperandInfo(numeric,remove,numeric? null : operand,val);
    }
}

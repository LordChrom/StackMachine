import Assembler.Parser;
import Assembler.ProtoInst;
import Emulator.Instruction;
import Emulator.Processor;

import java.io.FileWriter;
import java.io.IOException;

import static Assembler.Parser.NoOperand;

public class Main {
    public static boolean debugMode = false;

    public static void main(String... args){
        if(args.length>0)
            debugMode =args[0].equals("debug");

        ProtoInst[] protogram = Parser.parseFileProto(
                (debugMode?"C:\\Users\\Slime\\Documents\\Projects\\Stackinator\\":"")+"program.txt"
        );

        String[] lowerLevelLines = new String[protogram.length];
        for (int i = 0; i < protogram.length; i++) {
            ProtoInst p = protogram[i];
            String str = p.inst.opcode;
            if(str.equals("limm")) str = "imm";

            if(str.equals("imm"))
                str+=" "+p.inst.op.exec(0,0,null); //gets imm value

            if(p.a!=NoOperand)
                str+=(p.inst.delA?" -":" ")+ p.inst.a;
            if(p.b!=NoOperand)
                str+=(p.inst.delB?" -":" ")+ p.inst.b;
            lowerLevelLines[i]=str;
        }
        filePrint("lessAbstracted.txt",lowerLevelLines);

        Instruction[] program = Parser.convertProgram(protogram);

        Processor p = new Processor();
        p.exec(program,debugMode);
        filePrint("output.txt",p.outputLog.toString());
    }


    private static void filePrint(String name, String... fileLines){
        try (FileWriter writer = new FileWriter(name)) {
            for (int i = 0; i < fileLines.length; i++) {
                writer.write(fileLines[i]);
                if(i< fileLines.length-1)
                    writer.write("\r\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

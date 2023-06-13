package Emulator;

import java.util.HashMap;

public class Datum {
//    public static HashMap<Instruction,String> debugLabels = new HashMap<>();
    public byte data;
    public Instruction source;

    public Datum(int data, Instruction source ){
        this.data = (byte) data;
        this.source = source;
    }

    public String toString(){
        return ((int)data)+"\t"+source.label;//debugLabels.getOrDefault(source,"");
    }
}

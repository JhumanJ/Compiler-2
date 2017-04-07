package comp207p.main.tools;

import java.util.Iterator;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import org.apache.bcel.util.InstructionFinder;
import comp207p.main.exceptions.UnableToFetchValueException;


public class Helpers {

    public static Number constVal(InstructionHandle instruction_handle, ConstantPoolGen constpoolgen) {
        Number value;

        if (instruction_handle.getInstruction() instanceof LDC) {
            value = (Number) ((LDC) instruction_handle.getInstruction()).getValue(constpoolgen);
        }
        else if (instruction_handle.getInstruction() instanceof LDC2_W) {
            value = ((LDC2_W) instruction_handle.getInstruction()).getValue(constpoolgen);
        }
        else if (instruction_handle.getInstruction() instanceof ConstantPushInstruction) {
            value = ((ConstantPushInstruction) instruction_handle.getInstruction()).getValue();
        }
        else {
            throw new RuntimeException();
        }

        return value;
    }


    public static Number loadInstructVal(InstructionHandle instruction_handle, ConstantPoolGen constpoolgen, InstructionList instuction_list, String t) throws UnableToFetchValueException {
        Instruction instruction = instruction_handle.getInstruction();
        if(!(instruction instanceof LoadInstruction)) {
            throw new RuntimeException("type should be LoadInstruction");
        }

        int local_var_index = ((LocalVariableInstruction) instruction).getIndex();

        InstructionHandle iterator = instruction_handle;
        int inc_acc = 0;
        while(!(instruction instanceof StoreInstruction) || ((StoreInstruction) instruction).getIndex() != local_var_index) {

            if(instruction instanceof IINC) {
                IINC increment = (IINC) instruction;

                if(increment.getIndex() == local_var_index) {
                    System.out.println("increment instruction is found");

                    if (ifCheck(instruction_handle, instuction_list) || loopCheck(instruction_handle, instuction_list)) {
                        throw new UnableToFetchValueException("98738: Error found in for loop.");
                    }
                    System.out.format("%s | Incrementing by %d | Index: %d\n\n", increment, increment.getIncrement(), increment.getIndex());
                    inc_acc += increment.getIncrement();
                }
            }

            iterator = iterator.getPrev();
            instruction = iterator.getInstruction();
        }

        iterator = iterator.getPrev();
        instruction = iterator.getInstruction();

        Number storeValue;
        if (instruction instanceof LDC) {
            storeValue = (Number) ((LDC) instruction).getValue(constpoolgen);
        }
        else if(instruction instanceof ConstantPushInstruction) {
            storeValue = ((ConstantPushInstruction) instruction).getValue();
        }
        else if (instruction instanceof LDC2_W) {
            storeValue = ((LDC2_W) instruction).getValue(constpoolgen);
        }
        else {
            throw new UnableToFetchValueException("Value cannot be fetched for this object");
        }

        switch (t) {
            case "F":
            case "D":
                return operationFolding(new DADD(), storeValue, inc_acc);
            default:
                return operationFolding(new LADD(), storeValue, inc_acc);
        }
    }

    public static boolean ifCheck(InstructionHandle instruction_handle, InstructionList l) {
        Instruction check_instruction = instruction_handle.getInstruction();
        Instruction curr_instruction, curr_sub_instruction;
        InstructionHandle iterator = instruction_handle;
        while(iterator != null) {
            try {
                iterator = iterator.getPrev();
                curr_instruction = iterator.getInstruction();
                if (curr_instruction instanceof StoreInstruction
                    && ((StoreInstruction)curr_instruction).getIndex() == ((LoadInstruction)check_instruction).getIndex()) {
                    InstructionHandle subIterator = iterator;
                    while (subIterator != null) {
                        subIterator = subIterator.getPrev();
                        curr_sub_instruction = subIterator.getInstruction();
                        if (curr_sub_instruction instanceof BranchInstruction) {
                            if (((BranchInstruction) curr_sub_instruction).getTarget().getPosition() > iterator.getPosition()) {
                                return true;
                            } else {
                                return false;
                            }
                        }
                    }
                }
            } catch (NullPointerException e) {
                break;
            }
        }

        return false;
    }


    public static boolean loopCheck(InstructionHandle instruction_handle, InstructionList l) {
        Instruction check_instruction = instruction_handle.getInstruction();
        Instruction curr_instruction, prev_instruction, curr_sub_instruction;
        InstructionHandle iterator = l.getStart();
        while(iterator != null) {
            try {
                iterator = iterator.getNext();
                curr_instruction = iterator.getInstruction();
                prev_instruction = iterator.getPrev().getInstruction();
                if (curr_instruction instanceof GotoInstruction
                        && (prev_instruction instanceof IINC
                        || prev_instruction instanceof StoreInstruction)
                        && (iterator.getPosition() > ((BranchInstruction) curr_instruction).getTarget().getPosition())) {
                    if (((BranchInstruction) curr_instruction).getTarget().getInstruction().equals(check_instruction)) {
                        return true;
                    }
                    InstructionHandle subIterator = iterator;
                    while (subIterator != null) {
                        subIterator = subIterator.getPrev();
                        curr_sub_instruction = subIterator.getInstruction();
                        if (curr_sub_instruction instanceof StoreInstruction) {
                            if (((StoreInstruction)curr_sub_instruction).getIndex() == ((LoadInstruction)check_instruction).getIndex()) {
                                return true;
                            }
                        } else {
                            if (subIterator.equals((InstructionHandle) ((BranchInstruction)iterator.getInstruction()).getTarget())) {
                                break;
                            }
                        }
                    }
                }
            } catch (NullPointerException e) {
                break;
            }
        }

        return false;
    }


    public static Number operationFolding(ArithmeticInstruction op, Number l, Number r) {
        if(op instanceof FDIV ||  op instanceof DDIV) {
            return l.doubleValue() / r.doubleValue();
        }
        else if(op instanceof FREM ||  op instanceof DREM) {
            return l.doubleValue() % r.doubleValue();
        }
        else if(op instanceof IREM || op instanceof LREM){
            return l.longValue() % r.longValue();
        }
        else if(op instanceof IAND || op instanceof  LAND){
            return l.longValue() & r.longValue();
        }
        else if(op instanceof IOR || op instanceof  LOR){
            return l.longValue() | r.longValue();
        }
        else if(op instanceof IXOR || op instanceof LXOR){
            return l.longValue() ^ r.longValue();
        }
        else if(op instanceof ISHL || op instanceof LSHL){
            return l.longValue() << r.longValue();
        }
        else if(op instanceof ISHR || op instanceof LSHR){
            return l.longValue() >> r.longValue();
        }
        else if(op instanceof IADD || op instanceof LADD) {
            return l.longValue() + r.longValue();
        }
        else if(op instanceof FADD ||  op instanceof DADD) {
            return l.doubleValue() + r.doubleValue();
        }
        else if(op instanceof ISUB || op instanceof LSUB) {
            return l.longValue() - r.longValue();
        }
        else if(op instanceof FSUB ||  op instanceof DSUB) {
            return l.doubleValue() - r.doubleValue();
        }
        else if(op instanceof IMUL || op instanceof LMUL){
            return l.longValue() * r.longValue();
        }
        else if(op instanceof FMUL ||  op instanceof DMUL) {
            return l.doubleValue() * r.doubleValue();
        }
        else if(op instanceof IDIV || op instanceof LDIV){
            return l.longValue() / r.longValue();
        }

        else {
            throw new RuntimeException("Not supported operation");
        }
    }


    public static int poolInsert(Number value, String tp, ConstantPoolGen constpoolgen) {
        if (tp == "I"){
            return constpoolgen.addInteger(value.intValue());
        }
        else if (tp == "D"){
            return constpoolgen.addDouble(value.doubleValue());
        }
        else if(tp == "F"){
            return constpoolgen.addFloat(value.floatValue());
        }
        else if (tp == "J"){
            return constpoolgen.addLong(value.longValue());
        }
        else if (tp == "S"){
            return constpoolgen.addInteger(value.intValue());
        }
        else if (tp == "B"){
            return constpoolgen.addInteger(value.intValue());
        }
        else{
            throw new RuntimeException("Error");
        }
    }


    public static String foldConstSign(InstructionHandle l, InstructionHandle r, ConstantPoolGen constpoolgen) {

        if(signCheck(l, r, constpoolgen, "D")) {
            return "D";
        } else if(signCheck(l, r, constpoolgen, "F")) {
            return "F";
        } else if(signCheck(l, r, constpoolgen, "J")) {
            return "J";
        } else if(signCheck(l, r, constpoolgen, "S")) {
            return "I";
        } else if(signCheck(l, r, constpoolgen, "I")) {
            return "I";
        } else if(signCheck(l, r, constpoolgen, "B")) {
            return "I";
        } else {
            throw new RuntimeException("Undefined type");
        }
    }


    public static boolean signCheck(InstructionHandle l, InstructionHandle r, ConstantPoolGen constpoolgen, String s) {
        if (l.getInstruction() instanceof LoadInstruction && r.getInstruction() instanceof LoadInstruction) {
            if (getInstructionSignature(l, constpoolgen).equals(s) || getInstructionSignature(r, constpoolgen).equals(s)) {
                return true;
            }
        } else if (l.getInstruction() instanceof LoadInstruction) {
            if (getInstructionSignature(l, constpoolgen).equals(s) || ((TypedInstruction)r.getInstruction()).getType(constpoolgen).getSignature().equals(s)) {
                return true;
            }
        } else if (r.getInstruction() instanceof LoadInstruction) {
            if (((TypedInstruction)l.getInstruction()).getType(constpoolgen).getSignature().equals(s) || getInstructionSignature(r, constpoolgen).equals(s) ) {
                return true;
            }
        } else {
            if(((TypedInstruction)l.getInstruction()).getType(constpoolgen).getSignature().equals(s) || ((TypedInstruction)r.getInstruction()).getType(constpoolgen).getSignature().equals(s)) {
                return true;
            }
        }

        return false;
    }


    public static String getInstructionSignature(InstructionHandle handle, ConstantPoolGen constpoolgen) {
        Instruction instruction = handle.getInstruction();
        if(!(instruction instanceof TypedInstruction)) {
            throw new RuntimeException("Type of InstructionHandle has to be TypedInstruction instead of: " + instruction.getClass());
        }

        if(instruction instanceof LoadInstruction) {
            int local_var_index = ((LocalVariableInstruction) instruction).getIndex();

            InstructionHandle iterator = handle;
            while (!(instruction instanceof StoreInstruction) || ((StoreInstruction) instruction).getIndex() != local_var_index) {
                iterator = iterator.getPrev();
                instruction = iterator.getInstruction();
            }

            iterator = iterator.getPrev();
            instruction = iterator.getInstruction();
        }

        return ((TypedInstruction)instruction).getType(constpoolgen).getSignature();
    }


    public static int typeDependantInsert(Number v, String tp, ConstantPoolGen constpoolgen) {
        switch (tp) {
            case "D":
                return constpoolgen.addDouble(v.doubleValue());
            case "F":
                return constpoolgen.addFloat(v.floatValue());
            case "J":
                return constpoolgen.addLong(v.longValue());
            case "I":
                return constpoolgen.addInteger(v.intValue());
            case "S":
                return constpoolgen.addInteger(v.intValue());
            case "B":
                return constpoolgen.addInteger(v.intValue());
            default:
                throw new RuntimeException("Undefined type");
        }
    }

    // ---------------------- New functions since last kristelle changes -----------------
    // Change them as much as you can without changing the function's name (already did)
    public static int intCompar(IfInstruction comparison, Number leftValue, Number rightValue) {
        if (comparison instanceof IF_ICMPEQ) { // if value 1 equals value 2
            if (leftValue.intValue() == rightValue.intValue()) return 1;
            else return 0;
        } else if (comparison instanceof IF_ICMPGE) { // if value 1 greater than or equal to to value 2
            if (leftValue.intValue() >= rightValue.intValue()) return 1;
            else return 0;
        } else if (comparison instanceof IF_ICMPGT) { // if value 1 greater than value 2
            if (leftValue.intValue() > rightValue.intValue()) return 1;
            else return 0;
        } else if (comparison instanceof IF_ICMPLE) { // if value 1 less than or equal to value 2
            if (leftValue.intValue() <= rightValue.intValue()) return 1;
            else return 0;
        } else if (comparison instanceof IF_ICMPLT) { // if value 1 less than value 2
            if (leftValue.intValue() < rightValue.intValue()) return 1;
            else return 0;
        } else if (comparison instanceof IF_ICMPNE) { // if value 1 not equal to value 2
            if (leftValue.intValue() != rightValue.intValue()) return 1;
            else return 0;
        } else {
            throw new RuntimeException("Comparison not defined");
        }
    }

    public static int firstCompar(InstructionHandle comparison, Number leftValue, Number rightValue) {
        if (comparison.getInstruction() instanceof DCMPG) { //if double 1 greater than double 2
            if (leftValue.doubleValue() > rightValue.doubleValue()) return 1;
            else return -1;
        } else if (comparison.getInstruction()  instanceof DCMPL) { //if double 1 less than double 2
            if (leftValue.doubleValue() < rightValue.doubleValue()) return -1;
            else return 1;
        } else if (comparison.getInstruction()  instanceof FCMPG) { //if float 1 greater than float 2
            if (leftValue.floatValue() > rightValue.floatValue()) return 1;
            else return -1;
        } else if (comparison.getInstruction()  instanceof FCMPL) { //if float 1 less than float 2
            if (leftValue.floatValue() < rightValue.floatValue()) return -1;
            else return 1;
        } else if (comparison.getInstruction()  instanceof LCMP) { //long comparison, 0 if equal, 1 if long 1 greater than long 2, -1 if long 1 less than long 2
            if (leftValue.longValue() == rightValue.longValue()) return 0;
            else if (leftValue.longValue() > rightValue.longValue()) return 1;
            else return -1;
        } else {
            throw new RuntimeException("Comparison not defined");
        }
    }

    public static int secondCompar(IfInstruction comparison, int value) {
        if (comparison instanceof IFEQ || comparison instanceof IF_ICMPEQ) { //if equal
            if (value == 0) return 1;
            else return 0;
        } else if (comparison instanceof IFGE || comparison instanceof IF_ICMPGE) { //if greater than or equal
            if (value >= 0) return 1;
            else return 0;
        } else if (comparison instanceof IFGT || comparison instanceof IF_ICMPGT) { //if greater than
            if (value > 0) return 1;
            else return 0;
        } else if (comparison instanceof IFLE || comparison instanceof IF_ICMPLE) { //if less than or equal
            if (value <= 0) return 1;
            else return 0;
        } else if (comparison instanceof IFLT || comparison instanceof IF_ICMPLT) { //if less than
            if (value < 0) return 1;
            else return 0;
        } else if (comparison instanceof IFNE || comparison instanceof IF_ICMPNE) { //if not equal
            if (value != 0) return 1;
            else return 0;
        } else {
            throw new RuntimeException("Comparison not defined, got: " + comparison.getClass());
        }
    }

    public static boolean checkDynamicVariable(InstructionHandle h, InstructionList list) {
        if (ifCheck(h, list) || loopCheck(h, list)) {
            return true;
        } else {
            return false;
        }
    }


}

package comp207p.main.tools;

import java.util.Iterator;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import org.apache.bcel.util.InstructionFinder;
import comp207p.main.tools.Helpers;
import comp207p.main.exceptions.UnableToFetchValueException;

public class Optimizers {

    //TODO: delete comments

    private static final String LOAD_INSTRUCTION_REGEXP = "(ConstantPushInstruction|LDC|LDC2_W|LoadInstruction)";

    public static int arithmeticOptimizations(InstructionList listOfInstructions, ConstantPoolGen cpgen) {
        int optimizationsDone = 0;

        String regex = LOAD_INSTRUCTION_REGEXP + " (ConversionInstruction)? " + LOAD_INSTRUCTION_REGEXP + " (ConversionInstruction)? " + "ArithmeticInstruction";

        InstructionFinder searcher = new InstructionFinder(listOfInstructions);

        for(Iterator iterate = searcher.search(regex); iterate.hasNext();) {
            InstructionHandle[] found = (InstructionHandle[]) iterate.next();

            Number firstVal, secundVal;
            InstructionHandle firstInstruct, secundInstruct, operationInstruc;

            firstInstruct = found[0];
            //Init secundinstruct
            if (found[1].getInstruction() instanceof ConversionInstruction) {
                secundInstruct = found[2];
            } else {
                secundInstruct = found[1];
            }
            //init operation
            if (secundInstruct == found[2] && found[3].getInstruction() instanceof ConversionInstruction) {
                operationInstruc = found[4];
            } else if (secundInstruct == found[2] || (secundInstruct == found[1] && found[2].getInstruction() instanceof ConversionInstruction)) {
                operationInstruc = found[3];
            } else {
                operationInstruc = found[2];
            }

            // TODO: can these two blocks be deleted?
            if (firstInstruct.getInstruction() instanceof LoadInstruction) {
                if (Helpers.ifCheck(firstInstruct, listOfInstructions) || Helpers.loopCheck(firstInstruct, listOfInstructions)) {
                    continue;
                }
            }
            if (secundInstruct.getInstruction() instanceof LoadInstruction) {
                if (Helpers.ifCheck(secundInstruct, listOfInstructions) || Helpers.loopCheck(secundInstruct, listOfInstructions)) {
                    continue;
                }
            }

            String actualInstructionType = Helpers.foldConstSign(firstInstruct, secundInstruct, cpgen);

            try {
                if(firstInstruct.getInstruction() instanceof LoadInstruction) {
                    firstVal = Helpers.loadInstructVal(firstInstruct, cpgen, listOfInstructions, actualInstructionType);
                } else {
                    firstVal = Helpers.constVal(firstInstruct, cpgen);
                }

                if(secundInstruct.getInstruction() instanceof LoadInstruction) {
                    secundVal = Helpers.loadInstructVal(secundInstruct, cpgen, listOfInstructions, actualInstructionType);
                } else {
                    secundVal = Helpers.constVal(secundInstruct, cpgen);
                }
            } catch (UnableToFetchValueException e) {continue;}

            ArithmeticInstruction ope = (ArithmeticInstruction) operationInstruc.getInstruction();
            Number foldedValue = Helpers.operationFolding(ope, firstVal, secundVal);
            int index = Helpers.typeDependantInsert(foldedValue, actualInstructionType, cpgen);

            if (actualInstructionType.equals("F") || actualInstructionType.equals("I") || actualInstructionType.equals("S")) { //Float, short or integer
                LDC newIns = new LDC(index);
                firstInstruct.setInstruction(newIns);
            } else {
                LDC2_W newIns = new LDC2_W(index);
                firstInstruct.setInstruction(newIns);
            }
            try {
                listOfInstructions.delete(found[1], operationInstruc);
            } catch (TargetLostException e) {}

            optimizationsDone++;
            break;
        }

        return optimizationsDone;
    }

    public static int comparisonsOptimizations(InstructionList listOfInstructions, ConstantPoolGen cpgen) {
        int optimizationsDone = 0;
        String regex =  LOAD_INSTRUCTION_REGEXP + "InvokeInstruction?" + " (ConversionInstruction)?" +
                        LOAD_INSTRUCTION_REGEXP + "?" + " (ConversionInstruction)?" +
                        "(LCMP|DCMPG|DCMPL|FCMPG|FCMPL)? IfInstruction (ICONST GOTO ICONST)?";

        InstructionFinder searcher = new InstructionFinder(listOfInstructions);

        for(Iterator it = searcher.search(regex); it.hasNext();) {
            InstructionHandle[] found = (InstructionHandle[]) it.next();
            Number firstVal = 0, secundVal = 0;
            InstructionHandle firstInstruct = null, secundInstruct = null, compare = null, comparInstruc = null;

            firstInstruct = found[0];
            if (found[1].getInstruction() instanceof ConversionInstruction
                && !(found[2].getInstruction() instanceof IfInstruction)) {
                secundInstruct = found[2];
            } else if (!(found[1].getInstruction() instanceof IfInstruction)) {
                secundInstruct = found[1];
            } else {
                secundInstruct = null;
            }

            int foundCounter = 0;
            if (secundInstruct != null) {
                if (secundInstruct == found[2]
                    && found[3].getInstruction() instanceof ConversionInstruction) {
                    foundCounter = 2;
                } else if (secundInstruct == found[2]
                    || (secundInstruct == found[1]
                    && found[2].getInstruction() instanceof ConversionInstruction)) {
                        foundCounter = 1;
                } else {
                    foundCounter = 0;
                }
            } else {
                if (!(found[1].getInstruction() instanceof ConversionInstruction)) {
                    foundCounter = -1;
                }
            }

            if (found[2+foundCounter].getInstruction() instanceof IfInstruction) { //If the following instruction after left and right is an IfInstruction (meaning integer comparison), such as IF_ICMPGE
                comparInstruc = found[2+foundCounter];
            } else {
                compare = found[2+foundCounter]; //Comparison for non-integers, such as LCMP
                comparInstruc = found[3+foundCounter]; //IfInstruction
            }

            String actualInstructionType;
            if(secundInstruct != null) {
                actualInstructionType = Helpers.foldConstSign(firstInstruct, secundInstruct, cpgen);
            } else {
                actualInstructionType = Helpers.getInstructionSignature(firstInstruct, cpgen);
            }

            try {
                if(firstInstruct.getInstruction() instanceof LoadInstruction) {
                    firstVal = Helpers.loadInstructVal(firstInstruct, cpgen, listOfInstructions, actualInstructionType);
                } else {
                    firstVal = Helpers.constVal(firstInstruct, cpgen);
                }

                if (secundInstruct != null) {
                    if(secundInstruct.getInstruction() instanceof LoadInstruction) {
                        secundVal = Helpers.loadInstructVal(secundInstruct, cpgen, listOfInstructions, actualInstructionType);
                    } else {
                        secundVal = Helpers.constVal(secundInstruct, cpgen);
                    }
                }
            } catch (UnableToFetchValueException e) {continue;}

            IfInstruction comparison = (IfInstruction) comparInstruc.getInstruction();

            int total;
            if (secundInstruct != null) {
                if (comparInstruc == found[2]) {
                    total = Helpers.intCompar(comparison, firstVal, secundVal);
                } else {
                    total = Helpers.firstCompar(compare, firstVal, secundVal);
                    total = Helpers.secondCompar(comparison, total);
                }
            } else {
                total = Helpers.secondCompar(comparison, firstVal.intValue());
            }

            if (total == 1) {
                ICONST newInstruction = new ICONST(0);
                firstInstruct.setInstruction(newInstruction);
                total = 0;
            } else if (total == 0) {
                ICONST newInstruction = new ICONST(1);
                firstInstruct.setInstruction(newInstruction);
                total = 1;
            } else {
                ICONST newInstruction = new ICONST(-1);
                firstInstruct.setInstruction(newInstruction);
            }

            try {
                if (found[found.length-1].getInstruction() instanceof IfInstruction) {
                    InstructionHandle tempHandle = (InstructionHandle) ((BranchInstruction)comparInstruc.getInstruction()).getTarget().getPrev();
                    if (total == 1) {
                        listOfInstructions.delete(found[0], comparInstruc);
                        if (tempHandle.getInstruction() instanceof GotoInstruction) {
                            InstructionHandle gotoTarget = (InstructionHandle) ((BranchInstruction)tempHandle.getInstruction()).getTarget().getPrev();
                            listOfInstructions.delete(tempHandle, gotoTarget);
                        }
                    } else {
                        listOfInstructions.delete(found[0], tempHandle);
                    }
                } else {
                    listOfInstructions.delete(found[1], found[found.length-1]);
                }
            } catch (TargetLostException e) {
                System.out.println("----------------- Error when deleting -----------------");

            }

            optimizationsDone++; //Optimisation found
            break;
        }

        return optimizationsDone;
    }

    public static int negationsOptimizations(InstructionList listOfInstructions, ConstantPoolGen cpgen) {
        int changeCounter = 0;

        String regExp = LOAD_INSTRUCTION_REGEXP + " (INEG|FNEG|LNEG|DNEG)";

        // Search for instruction list where two constants are loaded from the pool, followed by an arithmetic
        InstructionFinder finder = new InstructionFinder(listOfInstructions);

        for (Iterator it = finder.search(regExp); it.hasNext(); ) { // Iterate through instructions to look for arithmetic optimisation
            InstructionHandle[] match = (InstructionHandle[]) it.next();

            //Debug output
            System.out.println("==================================");
            System.out.println("Found optimisable negation");

            InstructionHandle loadInstruction = match[0];
            InstructionHandle negationInstruction = match[1];

            String type = comp207p.main.tools.Helpers.getInstructionSignature(negationInstruction, cpgen);

            Number value;
            Instruction instruction = loadInstruction.getInstruction();
            if(instruction instanceof LoadInstruction) {
                value = Helpers.loadInstructVal(loadInstruction, cpgen, listOfInstructions, type);
            } else {
                value = Helpers.constVal(loadInstruction, cpgen);
            }

            //Multiply by -1 to negate it, inefficient but oh well
            Number negatedValue = Helpers.operationFolding(new DMUL(), value, -1);

            System.out.format("Folding to value %s | Type: %s\n", negatedValue, type);

            int newPoolIndex = Helpers.poolInsert(negatedValue, type, cpgen);

            //Set left constant handle to point to new index
            if (type.equals("F") || type.equals("I") || type.equals("S")) { //Float, short or integer
                LDC newInstruction = new LDC(newPoolIndex);
                loadInstruction.setInstruction(newInstruction);
            } else { //Types larger than integer use LDC2_W
                LDC2_W newInstruction = new LDC2_W(newPoolIndex);
                loadInstruction.setInstruction(newInstruction);
            }

            //Delete other handles
            try {
                listOfInstructions.delete(match[1]);
            } catch (TargetLostException e) {
                e.printStackTrace();
            }

            System.out.println("==================================");
            changeCounter++;

        }

        return changeCounter;
    }
}

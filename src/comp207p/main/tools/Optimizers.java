package comp207p.main.tools;

import java.util.Iterator;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import org.apache.bcel.util.InstructionFinder;
import comp207p.main.tools.Helpers;
import comp207p.main.exceptions.UnableToFetchValueException;

public class Optimizers {

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
            if (found[1].getInstruction() instanceof ConversionInstruction) {
                secundInstruct = found[2];
            } else {
                secundInstruct = found[1];
            }
            if (secundInstruct == found[2] && found[3].getInstruction() instanceof ConversionInstruction) {
                operationInstruc = found[4];
            } else if (secundInstruct == found[2] || (secundInstruct == found[1] && found[2].getInstruction() instanceof ConversionInstruction)) {
                operationInstruc = found[3];
            } else {
                operationInstruc = found[2];
            }

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

            if (found[1].getInstruction() instanceof InvokeInstruction ) {
                continue;
            }

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

            if (firstInstruct.getInstruction() instanceof LoadInstruction) {
                if (Helpers.checkDynamicVariable(firstInstruct, listOfInstructions)) {
                    continue;
                }
            }
            if (secundInstruct != null && secundInstruct.getInstruction() instanceof LoadInstruction) {
                if (Helpers.checkDynamicVariable(secundInstruct, listOfInstructions)) {
                    continue;
                }
            }

            if (found[2+foundCounter].getInstruction() instanceof IfInstruction) {
                comparInstruc = found[2+foundCounter];
            } else {
                compare = found[2+foundCounter];
                comparInstruc = found[3+foundCounter];
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

            optimizationsDone++;
            break;
        }

        return optimizationsDone;
    }

    public static int negationsOptimizations(InstructionList listOfInstructions, ConstantPoolGen cpgen) {
        int optimizationsDone = 0;

        String regExp = LOAD_INSTRUCTION_REGEXP + " (INEG|FNEG|LNEG|DNEG)";

        InstructionFinder finder = new InstructionFinder(listOfInstructions);

        for (Iterator it = finder.search(regExp); it.hasNext(); ) {
            InstructionHandle[] match = (InstructionHandle[]) it.next();

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

            Number negatedValue = Helpers.operationFolding(new DMUL(), value, -1);

            int newPoolIndex = Helpers.poolInsert(negatedValue, type, cpgen);

            if (type.equals("F") || type.equals("I") || type.equals("S")) {
                LDC newInstruction = new LDC(newPoolIndex);
                loadInstruction.setInstruction(newInstruction);
            } else {
                LDC2_W newInstruction = new LDC2_W(newPoolIndex);
                loadInstruction.setInstruction(newInstruction);
            }

            try {
                listOfInstructions.delete(match[1]);
            } catch (TargetLostException e) {
                e.printStackTrace();
            }
            optimizationsDone++;
        }

        return optimizationsDone;
    }
}

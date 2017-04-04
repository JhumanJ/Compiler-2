package comp207p.main;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import org.apache.bcel.util.InstructionFinder;
import comp207p.main.tools.Optimizers;
import comp207p.main.exceptions.UnableToFetchValueException;


public class ConstantFolder
{
	ClassParser parser = null;
	ClassGen gen = null;

	JavaClass original = null;
	JavaClass optimized = null;

	public ConstantFolder(String classFilePath)
	{
		try{
			this.parser = new ClassParser(classFilePath);
			this.original = this.parser.parse();
			this.gen = new ClassGen(this.original);
		} catch(IOException e){
			e.printStackTrace();
		}
	}

	// TODO: check all todo functions and cheange them as much as possible
	// I already changed the names of the functions and changed the functions in optimizers but you can change them again to improve changes

	public void optimize()
	{
		ClassGen cgen = new ClassGen(original);
		cgen.setMajor(50);
		ConstantPoolGen cpgen = cgen.getConstantPool();

		// Implement your optimization here
		ConstantPool constantP = cpgen.getConstantPool();
        Constant[] constants = constantP.getConstantPool();
        Method[] methods = cgen.getMethods();

		for(int i =0;i<methods.length;i++){
			System.out.println("Method name:"+methods[i]); //TODO: delete this line
            optimiseMethod(cgen, cpgen, methods[i]);
		}

		this.optimized = cgen.getJavaClass();
	}

	private void optimiseMethod(ClassGen cgen, ConstantPoolGen cPoolGen, Method method) {

		Code methodCode = method.getCode();
        InstructionList instructionList = new InstructionList(methodCode.getCode());
        MethodGen methodGen = new MethodGen(method.getAccessFlags(),method.getReturnType(),method.getArgumentTypes(),null, method.getName(),cgen.getClassName(),instructionList,cPoolGen);

        int possibleOptimizations = 1;

        while (possibleOptimizations > 0) {
			//We reset the the number of optimizations to 0
            possibleOptimizations = 0;
            possibleOptimizations += tryAllOptimizations(instructionList, cPoolGen);
        }

        instructionList.setPositions(true);

        methodGen.setMaxStack();
        methodGen.setMaxLocals();

        Method newMethod = methodGen.getMethod();
        cgen.replaceMethod(method, newMethod);
    }

	private int tryAllOptimizations(InstructionList listOfInstructions, ConstantPoolGen cpgen){
		System.out.println("- Negated Optimization");
		int negatedOptimizations = Optimizers.negationsOptimizations(listOfInstructions, cpgen);
		System.out.println("- Arithmetic Optimization");
		int arithmeticOptimizations = Optimizers.arithmeticOptimizations(listOfInstructions, cpgen);
		//System.out.println("- Comparison Optimization");
		//int comparisonsOptimizations = Optimizers.comparisonsOptimizations(listOfInstructions, cpgen);
		//System.out.println("- Arithmetic Optimization2");
		//int arithmeticOptimizations2 = Optimizers.arithmeticOptimizations(listOfInstructions, cpgen);
		return negatedOptimizations + arithmeticOptimizations;// + comparisonsOptimizations; //+ dew + fwd
	}

	public void write(String optimisedFilePath)
	{
		this.optimize();

		try {
			FileOutputStream out = new FileOutputStream(new File(optimisedFilePath));
			this.optimized.dump(out);
		} catch (FileNotFoundException e) {
			// Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
	}
}
